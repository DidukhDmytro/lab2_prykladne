package com.example.carservice.pricing.rule;

import com.example.carservice.config.AppConstants;
import com.example.carservice.domain.OrderPriority;
import com.example.carservice.domain.RepairOrder;
import com.example.carservice.exception.DomainMessages;
import com.example.carservice.exception.InvalidArgumentException;
import com.example.carservice.pricing.PricingRule;
import com.example.carservice.pricing.PricingStage;
import java.math.BigDecimal;

public class UrgentSurchargeRule implements PricingRule {
    private static BigDecimal zeroPrice = BigDecimal.ZERO;

    public UrgentSurchargeRule() {
        if (AppConstants.urgentLaborSurchargeFactor.compareTo(zeroPrice) < 0) {
            throw new InvalidArgumentException(DomainMessages.invalidUrgentSurchargeFactor);
        }
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
        if (order.getPriority() == OrderPriority.URGENT) {
            return currentTotal.add(order.calculateLaborTotal()
                    .multiply(AppConstants.urgentLaborSurchargeFactor));
        }
        return currentTotal;
    }
}
