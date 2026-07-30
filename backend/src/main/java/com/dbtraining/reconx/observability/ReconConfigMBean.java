package com.dbtraining.reconx.observability;

import org.springframework.cache.CacheManager;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

/**
 * TICKET-ADV096 — Runtime tuning via JMX.
 *
 * priceTolerance / cachingEnabled are demonstrative only — they are NOT
 * wired into ReconciliationEngine, whose ReconciliationRule is a fixed-value
 * enum (EXACT, PRICE_TOLERANCE_1PCT, ...), not a single mutable tolerance
 * the engine reads per run. Wiring them for real would mean changing that
 * enum-based design, which day2 (ADV026) deliberately chose. clearCache()
 * is fully real — it clears the actual Caffeine caches via CacheManager.
 */
@Component
@ManagedResource(
        objectName = "reconx:type=ReconConfig",
        description = "Runtime tuning for the reconciliation engine (demo attributes) and cache control"
)
public class ReconConfigMBean {

    private volatile double priceTolerance = 0.01;
    private volatile boolean cachingEnabled = true;
    private final CacheManager cacheManager;

    public ReconConfigMBean(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @ManagedAttribute(description = "Demo only — not read by ReconciliationEngine yet")
    public double getPriceTolerance() {
        return priceTolerance;
    }

    @ManagedAttribute
    public void setPriceTolerance(double v) {
        if (v < 0 || v > 1) throw new IllegalArgumentException("tolerance must be 0..1");
        this.priceTolerance = v;
    }

    @ManagedAttribute(description = "Demo only — not read anywhere yet")
    public boolean isCachingEnabled() {
        return cachingEnabled;
    }

    @ManagedAttribute
    public void setCachingEnabled(boolean enabled) {
        this.cachingEnabled = enabled;
    }

    @ManagedOperation(description = "Evict all entries from every configured cache")
    public void clearCache() {
        cacheManager.getCacheNames().forEach(name -> {
            var cache = cacheManager.getCache(name);
            if (cache != null) cache.clear();
        });
    }
}