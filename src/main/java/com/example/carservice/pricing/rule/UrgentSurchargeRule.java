package com.example.carservice.pricing.rule;

import com.example.carservice.exception.DomainMessages;
import com.example.carservice.exception.InvalidArgumentException;
import com.example.carservice.pricing.PricingContext;
import com.example.carservice.pricing.PricingRule;
import java.math.BigDecimal;

public class UrgentSurchargeRule implements PricingRule {
    private static BigDecimal zeroPrice = BigDecimal.ZERO;
    private BigDecimal laborSurchargeFactor;

    public UrgentSurchargeRule(BigDecimal laborSurchargeFactor) {
        if (laborSurchargeFactor == null) {
            throw new InvalidArgumentException(DomainMessages.urgentSurchargeFactorRequired());
        }
        if (laborSurchargeFactor.compareTo(zeroPrice) < 0) {
            throw new InvalidArgumentException(DomainMessages.invalidUrgentSurchargeFactor());
        }
        this.laborSurchargeFactor = laborSurchargeFactor;
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
        if (context.isUrgent()) {
            return currentTotal.add(context.getLaborTotal().multiply(laborSurchargeFactor));
        }
        return currentTotal;
    }
}
