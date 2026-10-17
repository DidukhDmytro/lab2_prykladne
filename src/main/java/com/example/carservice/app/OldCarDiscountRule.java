package com.example.carservice.app;

import com.example.carservice.exception.DomainMessages;
import com.example.carservice.exception.InvalidArgumentException;
import com.example.carservice.pricing.PricingContext;
import com.example.carservice.pricing.PricingRule;
import com.example.carservice.pricing.rule.PercentageDiscountRule;
import java.math.BigDecimal;

public class OldCarDiscountRule implements PricingRule {
    private int currentYear;
    private int minimumAge;
    private PercentageDiscountRule percentageDiscountRule;

    public OldCarDiscountRule(int currentYear, int minimumAge, BigDecimal discountPercent) {
        if (currentYear <= 0) {
            throw new InvalidArgumentException(DomainMessages.oldCarCurrentYearRequired());
        }
        if (minimumAge < 0) {
            throw new InvalidArgumentException(DomainMessages.oldCarAgeLimitInvalid());
        }
        this.currentYear = currentYear;
        this.minimumAge = minimumAge;
        this.percentageDiscountRule = new PercentageDiscountRule(discountPercent);
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
        if (currentYear - context.getCarYear() > minimumAge) {
            return percentageDiscountRule.apply(context, currentTotal);
        }
        return currentTotal;
    }
}
