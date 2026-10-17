package com.example.carservice.pricing;

import java.math.BigDecimal;

public interface PricingRule {
    int SURCHARGE_PRIORITY = 100;
    int DISCOUNT_PRIORITY = 200;

    int getPriority();

    BigDecimal apply(PricingContext context, BigDecimal currentTotal);
}
