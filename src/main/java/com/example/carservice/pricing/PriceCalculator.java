package com.example.carservice.pricing;

import com.example.carservice.domain.RepairOrder;
import com.example.carservice.exception.DomainMessages;
import com.example.carservice.exception.InvalidArgumentException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Calculates base and final repair order prices.
 *
 * <p>Rules run in stage order: surcharges, then discounts. A percentage discount follows
 * surcharges so that it also discounts the surcharge. Fixed and percentage discounts are not
 * commutative, so their insertion order within the discount stage is preserved. The minimum price
 * is applied after all rules so a discount cannot push the result below the minimum. Rounding is
 * applied once, at the end of the final calculation.</p>
 */
public class PriceCalculator {
    private static int priceScale = 2;

    private List<PricingRule> rules;
    private BigDecimal minimumPrice;

    public PriceCalculator(List<PricingRule> rules, BigDecimal minimumPrice) {
        if (rules == null) {
            throw new InvalidArgumentException(DomainMessages.pricingRulesRequired);
        }
        if (minimumPrice == null) {
            throw new InvalidArgumentException(DomainMessages.minimumPriceRequired);
        }
        this.rules = new ArrayList<>(rules);
        if (this.rules.contains(null)) {
            throw new InvalidArgumentException(DomainMessages.pricingRuleRequired);
        }
        this.minimumPrice = minimumPrice;
    }

    public BigDecimal calculateBasePrice(RepairOrder order) {
        validateOrder(order);
        return order.calculateBaseTotal().setScale(priceScale, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateFinalPrice(RepairOrder order) {
        validateOrder(order);
        List<PricingRule> orderedRules = new ArrayList<>(rules);
        orderedRules.sort(Comparator.comparing(PricingRule::getStage));
        BigDecimal total = order.calculateBaseTotal();
        for (PricingRule rule : orderedRules) {
            total = rule.apply(order, total);
        }
        return total.max(minimumPrice).setScale(priceScale, RoundingMode.HALF_UP);
    }

    private void validateOrder(RepairOrder order) {
        if (order == null) {
            throw new InvalidArgumentException(DomainMessages.pricingOrderRequired);
        }
    }
}
