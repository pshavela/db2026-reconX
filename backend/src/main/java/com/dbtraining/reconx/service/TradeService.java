package com.dbtraining.reconx.service;

import com.dbtraining.reconx.dto.TradeRequest;
import com.dbtraining.reconx.exception.CounterpartyNotFoundException;
import com.dbtraining.reconx.exception.DuplicateTradeRefException;
import com.dbtraining.reconx.exception.InstrumentNotFoundException;
import com.dbtraining.reconx.exception.TradeNotFoundException;
import com.dbtraining.reconx.kafka.TradeEventProducer;
import com.dbtraining.reconx.observability.TradeMetrics;
import com.dbtraining.reconx.repository.CounterpartyRepository;
import com.dbtraining.reconx.repository.InstrumentRepository;
import com.dbtraining.reconx.repository.TradeRepository;
import com.dbtraining.reconx.repository.TradeSpecifications;
import com.dbtraining.reconx.repository.entity.Counterparty;
import com.dbtraining.reconx.repository.entity.Instrument;
import com.dbtraining.reconx.repository.entity.Trade;
import com.dbtraining.reconx.dto.TradeEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static com.dbtraining.reconx.repository.TradeSpecifications.*;

/**
 * ============================================================================
 * TICKET-ADV064 — TradeService.create (POST endpoint backing)
 * TICKET-ADV065 — update
 * TICKET-ADV066 — updateStatus (PATCH)
 * TICKET-ADV067 — softDelete
 * TICKET-ADV083 — increments trade_created_total Counter on create
 * TICKET-ADV129 — publishes TradeEvent on every state change
 * TICKET-ADV055/ADV056 — list() uses Specifications + filter query
 * ============================================================================
 */
@Service
@Transactional
public class TradeService {

    private final TradeRepository tradeRepo;
    private final CounterpartyRepository cpRepo;
    private final InstrumentRepository instRepo;
    private final TradeEventProducer events;
    private final TradeMetrics metrics;

    public TradeService(TradeRepository tradeRepo,
                        CounterpartyRepository cpRepo,
                        InstrumentRepository instRepo,
                        TradeEventProducer events,
                        TradeMetrics metrics) {
        this.tradeRepo = tradeRepo;
        this.cpRepo = cpRepo;
        this.instRepo = instRepo;
        this.events = events;
        this.metrics = metrics;
    }

    public Trade create(TradeRequest req, String actor) {
        // TODO(TICKET-ADV064): reject duplicate tradeRef via DuplicateTradeRefException,
        //   build a new Trade with instrument + counterparty looked up from
        //   their repos (throw TradeNotFoundException on miss), status = "PENDING",
        //   save, then:
        //     - metrics.incrementTradeCreated() + metrics.recordTradeValue(qty*price) — TICKET-ADV083
        //     - events.publish(new TradeEvent(... TRADE_CREATED ... actor ...)) — TICKET-ADV129

        if (tradeRepo.findByTradeRef(req.tradeRef()).isPresent()) {
            throw new DuplicateTradeRefException(req.tradeRef());
        }

        Instrument instrument = instRepo.findById(req.instrumentId()).orElse(null);
        Counterparty counterparty = cpRepo.findById(req.counterpartyId()).orElse(null);

        if (instrument == null) {
            throw new InstrumentNotFoundException(req.instrumentId());
        }

        if (counterparty == null) {
            throw new CounterpartyNotFoundException(req.counterpartyId());
        }

        Trade trade = new Trade.Builder()
                .tradeRef(req.tradeRef())
                .instrument(instrument)
                .counterparty(counterparty)
                .assetClass(req.assetClass())
                .side(req.side())
                .quantity(req.quantity())
                .price(req.price())
                .tradeDate(req.tradeDate())
                .build();

        return tradeRepo.save(trade);
    }

    public Trade update(Long id, TradeRequest req, String actor) {
        if (id == null) {
            throw new TradeNotFoundException("Id not specified");
        }

        Trade trade = tradeRepo.findById(id).orElse(null);
        Instrument instrument = instRepo.findById(req.instrumentId()).orElse(null);
        Counterparty counterparty = cpRepo.findById(req.counterpartyId()).orElse(null);

        if (trade == null) {
            throw new TradeNotFoundException(req.tradeRef());
        }

        if (instrument == null) {
            throw new InstrumentNotFoundException(req.instrumentId());
        }

        if (counterparty == null) {
            throw new CounterpartyNotFoundException(req.counterpartyId());
        }

        trade.setInstrument(instrument);
        trade.setCounterparty(counterparty);
        trade.setPrice(req.price());
        trade.setQuantity(req.quantity());
        trade.setSide(req.side());
        trade.setAssetClass(req.assetClass());
        trade.setTradeDate(req.tradeDate());

        // TODO publish a TRADE_UPDATED event.

        return tradeRepo.save(trade);
    }

    public Trade updateStatus(Long id, String status, String actor) {
        // TODO(TICKET-ADV066): load, setStatus(status), save, publish TRADE_UPDATED
        //   with the new status in the "after" slot of the event.
        throw new UnsupportedOperationException("TICKET-ADV066");
    }

    public void softDelete(Long id, String actor) {
        // TODO(TICKET-ADV067): load, call t.softDelete() (sets deleted_at), save,
        //   publish a TRADE_CANCELLED event.
        throw new UnsupportedOperationException("TICKET-ADV067");
    }

    @Transactional(readOnly = true)
    public Page<Trade> list(LocalDate from, LocalDate to, String status, Long counterpartyId, Pageable pageable) {
        return tradeRepo.findAll(
                TradeSpecifications.hasCounterparty(counterpartyId)
                        .and(TradeSpecifications.tradeDateBetween(from, to))
                        .and(TradeSpecifications.hasStatus(status)),
                pageable);
    }
}
