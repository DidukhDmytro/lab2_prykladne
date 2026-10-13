package com.example.carservice.pricing;

import com.example.carservice.domain.RepairOrder;
import java.math.BigDecimal;

public interface PricingRule {
    PricingStage getStage();

    BigDecimal apply(RepairOrder order, BigDecimal currentTotal);
}
