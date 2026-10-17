package com.example.carservice.pricing.rule;

import com.example.carservice.exception.DomainMessages;
import com.example.carservice.exception.InvalidArgumentException;
import com.example.carservice.pricing.PricingContext;
import com.example.carservice.pricing.PricingRule;
import java.math.BigDecimal;

public class PercentageDiscountRule implements PricingRule {
    private static BigDecimal zeroPercent = BigDecimal.ZERO;
    private static BigDecimal maximumPercent = new BigDecimal("100");
    private static BigDecimal percentageDivisor = new BigDecimal("100");

    private BigDecimal percent;

    public PercentageDiscountRule(BigDecimal percent) {
        if (percent == null) {
            throw new InvalidArgumentException(DomainMessages.percentageRequired());
        }
        if (percent.compareTo(zeroPercent) < 0 || percent.compareTo(maximumPercent) > 0) {
            throw new InvalidArgumentException(DomainMessages.percentageOutOfRange());
        }
        this.percent = percent;
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
        return currentTotal.multiply(percentageDivisor.subtract(percent)).divide(percentageDivisor);
    }
}
