package com.example.carservice.pricing.rule;

import com.example.carservice.exception.DomainMessages;
import com.example.carservice.exception.InvalidArgumentException;
import com.example.carservice.pricing.PricingContext;
import com.example.carservice.pricing.PricingRule;
import java.math.BigDecimal;

public class FixedDiscountRule implements PricingRule {
    private static BigDecimal zeroPrice = BigDecimal.ZERO;

    private BigDecimal amount;

    public FixedDiscountRule(BigDecimal amount) {
        if (amount == null) {
            throw new InvalidArgumentException(DomainMessages.fixedDiscountRequired());
        }
        if (amount.compareTo(zeroPrice) < 0) {
            throw new InvalidArgumentException(DomainMessages.negativeFixedDiscount());
        }
        this.amount = amount;
    }

    @Override
    public int getPriority() {
        return DISCOUNT_PRIORITY;
    }

    @Override
    public BigDecimal apply(PricingContext context, BigDecimal currentTotal) {
        if (context == null || currentTotal == null) {
            throw new InvalidArgumentException(DomainMessages.pricingOrderRequired());
        }
        return currentTotal.subtract(amount).max(zeroPrice);
    }
}
