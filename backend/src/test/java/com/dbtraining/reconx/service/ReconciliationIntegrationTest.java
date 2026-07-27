package com.dbtraining.reconx.service;

import com.dbtraining.reconx.dto.ReconResult;
import com.dbtraining.reconx.model.EquityTrade;
import com.dbtraining.reconx.model.ReconciliationRule;
import com.dbtraining.reconx.model.Side;
import com.dbtraining.reconx.model.TradeRef;
import com.dbtraining.reconx.model.TradeType;
import com.dbtraining.reconx.repository.CounterpartyRepository;
import com.dbtraining.reconx.repository.InstrumentRepository;
import com.dbtraining.reconx.repository.TradeRepository;
import com.dbtraining.reconx.repository.entity.Counterparty;
import com.dbtraining.reconx.repository.entity.Instrument;
import com.dbtraining.reconx.repository.entity.Trade;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TICKET-ADV044 — Testcontainers-managed PostgreSQL for integration tests.
 * TICKET-ADV045 — insert -> recon -> verify.
 *
 * NOTE on adaptation: the generic day-3 reference solution assumes a
 * persisted ReconResult row and separate internal/external trade repos.
 * This schema has neither: ReconciliationEngine.reconcile() returns an
 * in-memory List<ReconResult> (no repository backs it, see ReconResult's
 * javadoc), there is a single TradeRepository (not internal/external), and
 * the counterparty's "external" feed is never persisted here -- it arrives
 * over Kafka/CSV in later days. So this test proves the two halves that DO
 * exist in this codebase: (1) a real Postgres round-trip through
 * TradeRepository/CounterpartyRepository/InstrumentRepository, and (2) the
 * engine matching an internal trade against a same-shaped external feed.
 */
@SpringBootTest
@Testcontainers
class ReconciliationIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("reconx")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
        // application-dev.yml pins the H2 dialect explicitly; override it so
        // Hibernate validates against the real Postgres schema instead.
        r.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.PostgreSQLDialect");
    }

    @Autowired private TradeRepository tradeRepository;
    @Autowired private CounterpartyRepository counterpartyRepository;
    @Autowired private InstrumentRepository instrumentRepository;
    @Autowired private ReconciliationEngine reconciliationEngine;

    @Test
    void containerIsRunning() {
        // sanity: if this passes, the container started and Spring wired up
        // spring.datasource.* against it. The real assertions live below.
    }

    @Test
    @Transactional
    void insertedTradeIsReconciledAgainstMatchingExternalFeed() {
        // given -- an internal trade booked against the seeded counterparty/instrument
        // rows (id 1 = Goldman Sachs / SAP.DE, loaded by Liquibase's 008-seed.xml)
        Counterparty counterparty = counterpartyRepository.findById(1L).orElseThrow();
        Instrument instrument = instrumentRepository.findById(1L).orElseThrow();

        Trade internal = new Trade();
        internal.setTradeRef("EQU-20260603-0001");
        internal.setCounterparty(counterparty);
        internal.setInstrument(instrument);
        internal.setAssetClass("EQUITY");
        internal.setSide("BUY");
        internal.setQuantity(new BigDecimal("1000"));
        internal.setPrice(new BigDecimal("245.50"));
        internal.setTradeDate(LocalDate.of(2026, 6, 3));
        tradeRepository.save(internal);

        // the two TradeType inputs the engine actually compares -- built from the
        // same values as the persisted row, plus a same-shaped "external" feed
        // (no external_trades table exists yet; the feed is in-memory only)
        TradeType internalTrade = EquityTrade.builder()
                .tradeRef(TradeRef.of(internal.getTradeRef()))
                .instrumentSymbol(instrument.getSymbol())
                .quantity(internal.getQuantity())
                .price(internal.getPrice())
                .currency(instrument.getCurrency())
                .side(Side.BUY)
                .tradeDate(internal.getTradeDate())
                .counterpartyId(counterparty.getId())
                .build();

        TradeType externalTrade = EquityTrade.builder()
                .tradeRef(TradeRef.of("EQU-20260603-0001"))
                .instrumentSymbol(instrument.getSymbol())
                .quantity(new BigDecimal("1000"))
                .price(new BigDecimal("245.50"))
                .currency(instrument.getCurrency())
                .side(Side.BUY)
                .tradeDate(LocalDate.of(2026, 6, 3))
                .counterpartyId(counterparty.getId())
                .build();

        // when
        List<ReconResult> results = reconciliationEngine.reconcile(
                List.of(internalTrade), List.of(externalTrade), ReconciliationRule.EXACT);

        // then -- exactly one MATCHED result, keyed by the seeded trade ref
        assertThat(results).hasSize(1);
        assertThat(results.get(0).status()).isEqualTo(ReconResult.Status.MATCHED);
        assertThat(results.get(0).tradeRef()).isEqualTo("EQU-20260603-0001");

        // and the internal trade really did round-trip through Postgres
        assertThat(tradeRepository.findByTradeRef("EQU-20260603-0001")).isPresent();
    }
}
