package com.example.carservice.pricing;

import com.example.carservice.domain.OrderPriority;
import com.example.carservice.domain.RepairOrder;
import com.example.carservice.exception.DomainMessages;
import com.example.carservice.exception.InvalidArgumentException;
import java.math.BigDecimal;

public class PricingContext {
    private BigDecimal laborTotal;
    private BigDecimal partsTotal;
    private boolean urgent;
    private int carYear;

    private PricingContext(
            BigDecimal laborTotal,
            BigDecimal partsTotal,
            boolean urgent,
            int carYear) {
        this.laborTotal = laborTotal;
        this.partsTotal = partsTotal;
        this.urgent = urgent;
        this.carYear = carYear;
    }

    public static PricingContext from(RepairOrder order) {
        if (order == null) {
            throw new InvalidArgumentException(DomainMessages.pricingOrderRequired());
        }
        return new PricingContext(
                order.calculateLaborTotal(),
                order.calculatePartsTotal(),
                order.getPriority() == OrderPriority.URGENT,
                order.getCar().getYear());
    }

    public BigDecimal getLaborTotal() {
        return laborTotal;
    }

    public BigDecimal getPartsTotal() {
        return partsTotal;
    }

    public boolean isUrgent() {
        return urgent;
    }

    public int getCarYear() {
        return carYear;
    }
}
