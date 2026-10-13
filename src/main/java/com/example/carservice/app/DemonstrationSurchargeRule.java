package com.example.carservice.app;

import com.example.carservice.domain.RepairOrder;
import com.example.carservice.exception.DomainMessages;
import com.example.carservice.exception.InvalidArgumentException;
import com.example.carservice.pricing.PricingRule;
import com.example.carservice.pricing.PricingStage;
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
    public PricingStage getStage() {
        return PricingStage.SURCHARGE;
    }

    @Override
    public BigDecimal apply(RepairOrder order, BigDecimal currentTotal) {
        if (order == null || currentTotal == null) {
            throw new InvalidArgumentException(DomainMessages.pricingOrderRequired);
        }
        return currentTotal.add(amount);
    }
}
