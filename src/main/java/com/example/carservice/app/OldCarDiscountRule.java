package com.example.carservice.app;

import com.example.carservice.domain.RepairOrder;
import com.example.carservice.exception.DomainMessages;
import com.example.carservice.exception.InvalidArgumentException;
import com.example.carservice.pricing.PricingRule;
import com.example.carservice.pricing.PricingStage;
import java.math.BigDecimal;

public class OldCarDiscountRule implements PricingRule {
    private static int currentYear = 2026;
    private static int minimumAge = 10;
    private static BigDecimal discountPercent = new BigDecimal("5");
    private static BigDecimal percentageBase = new BigDecimal("100");

    @Override
    public PricingStage getStage() {
        return PricingStage.DISCOUNT;
    }

    @Override
    public BigDecimal apply(RepairOrder order, BigDecimal currentTotal) {
        if (order == null || currentTotal == null) {
            throw new InvalidArgumentException(DomainMessages.pricingOrderRequired);
        }
        if (currentYear - order.getCar().getYear() > minimumAge) {
            return currentTotal.multiply(percentageBase.subtract(discountPercent))
                    .divide(percentageBase);
        }
        return currentTotal;
    }
}
