package com.dbtraining.reconx.service;

import com.dbtraining.reconx.dto.ReconResult;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class ReconSummaryCollector implements Collector<ReconResult, ReconSummary.Builder, ReconSummary> {

    @Override
    public Supplier<ReconSummary.Builder> supplier() {
        return ReconSummary.Builder::new;
    }

    @Override
    public BiConsumer<ReconSummary.Builder, ReconResult> accumulator() {
        return (builder, reconResult) -> {
            switch(reconResult.status()) {
                case BREAK -> ++builder.broken;
                case MATCHED -> ++builder.matched;
            }

            ++builder.total;
        };
    }

    @Override
    public BinaryOperator<ReconSummary.Builder> combiner() {
        return (a, b) -> {
            a.total += b.total;
            a.matched += b.matched;
            a.broken += b.broken;
            return a;
        };
    }

    @Override
    public Function<ReconSummary.Builder, ReconSummary> finisher() {
        return ReconSummary.Builder::build;
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Set.of(Characteristics.UNORDERED);
    }
}
