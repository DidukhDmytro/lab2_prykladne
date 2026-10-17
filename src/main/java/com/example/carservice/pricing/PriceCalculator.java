package com.example.carservice.pricing;

import com.example.carservice.domain.RepairOrder;
import java.math.BigDecimal;
import java.util.List;

/**
 * Adapts repair orders to the portable pricing rule engine.
 */
public class PriceCalculator implements PricingPolicy {
    private PricingRuleEngine pricingRuleEngine;

    public PriceCalculator(List<PricingRule> rules, BigDecimal minimumPrice) {
        pricingRuleEngine = new PricingRuleEngine(rules, minimumPrice);
    }

    @Override
    public BigDecimal calculateBasePrice(RepairOrder order) {
        return pricingRuleEngine.calculateBasePrice(PricingContext.from(order));
    }

    @Override
    public BigDecimal calculateFinalPrice(RepairOrder order) {
        return pricingRuleEngine.calculateFinalPrice(PricingContext.from(order));
    }
}
