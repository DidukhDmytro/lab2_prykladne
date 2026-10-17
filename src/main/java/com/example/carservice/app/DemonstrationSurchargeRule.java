package com.example.carservice.app;

import com.example.carservice.exception.DomainMessages;
import com.example.carservice.exception.InvalidArgumentException;
import com.example.carservice.pricing.PricingContext;
import com.example.carservice.pricing.PricingRule;
import java.math.BigDecimal;

public class DemonstrationSurchargeRule implements PricingRule {
    private static String amountRequiredMessage = "Demonstration surcharge amount is required.";

    private BigDecimal amount;

    public DemonstrationSurchargeRule(BigDecimal amount) {
        if (amount == null) {
            throw new InvalidArgumentException(amountRequiredMessage);
        }
        this.amount = amount;
    }

    @Override
    public int getPriority() {
        return SURCHARGE_PRIORITY;
    }

    @Override
    public BigDecimal apply(PricingContext context, BigDecimal currentTotal) {
        if (context == null || currentTotal == null) {
            throw new InvalidArgumentException(DomainMessages.pricingOrderRequired());
        }
        return currentTotal.add(amount);
    }
}
