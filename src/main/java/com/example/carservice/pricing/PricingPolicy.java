package com.example.carservice.pricing;

import com.example.carservice.domain.RepairOrder;
import java.math.BigDecimal;

public interface PricingPolicy {
    BigDecimal calculateBasePrice(RepairOrder order);

    BigDecimal calculateFinalPrice(RepairOrder order);
}
