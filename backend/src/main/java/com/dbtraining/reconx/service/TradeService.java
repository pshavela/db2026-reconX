package com.dbtraining.reconx.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dbtraining.reconx.dto.TradeMapper;
import com.dbtraining.reconx.dto.TradeRequest;
import com.dbtraining.reconx.dto.TradeResponse;
import com.dbtraining.reconx.dto.VolumeResponse;
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
    private final TradeMapper mapper;
    // private final TradeEventProducer events;
    // private final TradeMetrics metrics;

    public TradeService(TradeRepository tradeRepo,
                        CounterpartyRepository cpRepo,
                        InstrumentRepository instRepo, TradeMapper mapper
                        // TradeEventProducer events,
                        // TradeMetrics metrics
                    ) {
        this.tradeRepo = tradeRepo;
        this.cpRepo = cpRepo;
        this.instRepo = instRepo;
        this.mapper = mapper;
        // this.events = events;
        // this.metrics = metrics;
    }

    public Trade create(TradeRequest req, String actor) {
        // TODO
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

        // Save the trade to the database first
        Trade saved = tradeRepo.save(trade);

        // --- TICKET-ADV083 ---
        // Increment the metric ONLY after a successful save
        metrics.incrementTradeCreated();
        metrics.recordTradeValue(saved.getQuantity().multiply(saved.getPrice()).doubleValue());


        return saved;
    }

    public Trade update(Long id, TradeRequest req, String actor) {
        if (id == null) {
            throw new TradeNotFoundException(id);
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
        Trade trade = null;

        if (id == null || (trade = tradeRepo.findById(id).orElse(null)) == null) {
            throw new TradeNotFoundException(id);
        }

        trade.setStatus(status);

        // TODO publish TRADE_UPDATED with the new status in the "after" slot of the event.

        return tradeRepo.save(trade);
    }

    public void softDelete(Long id, String actor) {
        Trade trade = null;

        if (id == null || (trade = tradeRepo.findById(id).orElse(null)) == null) {
            throw new TradeNotFoundException(id);
        }

        trade.softDelete();

        // TODO publish a TRADE_CANCELLED event.
    }

    @Transactional(readOnly = true)
    public Page<TradeResponse> list(LocalDate from, LocalDate to, String status, Long counterpartyId, Pageable pageable) {
        return tradeRepo.findAll(
                TradeSpecifications.hasCounterparty(counterpartyId)
                        .and(TradeSpecifications.tradeDateBetween(from, to))
                        .and(TradeSpecifications.hasStatus(status)),
                pageable).map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<Trade> listPending(LocalDate from, LocalDate to, String assetClass) {
        return tradeRepo.findAll(
                TradeSpecifications.tradeDateBetween(from, to)
                        .and(TradeSpecifications.hasStatus("PENDING"))
                        .and(TradeSpecifications.hasAssetClass(assetClass))
        );
    }

    @Transactional(readOnly = true)
    public TradeResponse getTrade(Long id) {
        return tradeRepo.findById(id)
            .map(mapper::toResponse)
            .orElseThrow(() -> new TradeNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public Long countByStatus(String status) {
        if (status == null) {
            return tradeRepo.count();
        }
        return tradeRepo.countByStatus(status);
    }

    @Transactional(readOnly = true)
    public List<VolumeResponse> volumeBetween(LocalDate from, LocalDate to) {
        return tradeRepo.countVolumeBetween(from, to)
            .stream()
            .map(arr -> new VolumeResponse((LocalDate) arr[0], (Long) arr[1]))
            .toList();
    }
}
