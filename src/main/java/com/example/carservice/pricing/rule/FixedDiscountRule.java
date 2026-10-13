package com.example.carservice.pricing.rule;

import com.example.carservice.domain.RepairOrder;
import com.example.carservice.exception.DomainMessages;
import com.example.carservice.exception.InvalidArgumentException;
import com.example.carservice.pricing.PricingRule;
import com.example.carservice.pricing.PricingStage;
import java.math.BigDecimal;

public class FixedDiscountRule implements PricingRule {
    private static BigDecimal zeroPrice = BigDecimal.ZERO;

    private BigDecimal amount;

    public FixedDiscountRule(BigDecimal amount) {
        if (amount == null) {
            throw new InvalidArgumentException(DomainMessages.fixedDiscountRequired);
        }
        if (amount.compareTo(zeroPrice) < 0) {
            throw new InvalidArgumentException(DomainMessages.negativeFixedDiscount);
        }
        this.amount = amount;
    }

    @Override
    public PricingStage getStage() {
        return PricingStage.DISCOUNT;
    }

    @Override
    public BigDecimal apply(RepairOrder order, BigDecimal currentTotal) {
        if (order == null || currentTotal == null) {
            throw new InvalidArgumentException(DomainMessages.pricingOrderRequired);
        }
        return currentTotal.subtract(amount).max(zeroPrice);
    }
}
