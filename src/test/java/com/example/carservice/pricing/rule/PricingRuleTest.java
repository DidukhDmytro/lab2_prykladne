package com.example.carservice.pricing.rule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.carservice.config.AppConstants;
import com.example.carservice.domain.Car;
import com.example.carservice.domain.Customer;
import com.example.carservice.domain.MechanicSpecialization;
import com.example.carservice.domain.OrderPriority;
import com.example.carservice.domain.RepairOrder;
import com.example.carservice.domain.ServiceWork;
import com.example.carservice.exception.InvalidArgumentException;
import com.example.carservice.pricing.PriceCalculator;
import com.example.carservice.pricing.PricingRule;
import com.example.carservice.pricing.PricingStage;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PricingRuleTest {
    private String description = "Rule test work";
    private RepairOrder order;

    @BeforeEach
    void setUp() {
        Customer customer = new Customer("customer-rule", "Rule Customer", "555-0100");
        Car car = new Car("VIN-RULE", "Test", "Model", 2020, customer);
        order = new RepairOrder("order-rule", car);
        order.addWork(new ServiceWork(
                description,
                new BigDecimal("100.00"),
                new BigDecimal("50.00"),
                MechanicSpecialization.GENERAL));
    }

    @ParameterizedTest(name = "{0}% discount leaves {1}")
    @CsvSource({"0,150.00", "100,0.00"})
    @DisplayName("Applies percentage discount at valid boundaries")
    void appliesPercentageBoundary(String percent, String expected) {
        // Given
        PriceCalculator calculator = new PriceCalculator(
                List.of(new PercentageDiscountRule(new BigDecimal(percent))),
                BigDecimal.ZERO);

        // When
        BigDecimal price = calculator.calculateFinalPrice(order);

        // Then
        assertEquals(new BigDecimal(expected), price);
    }

    @ParameterizedTest(name = "rejects percentage {0}")
    @CsvSource({"-0.01", "101"})
    @DisplayName("Rejects percentage discounts outside valid range")
    void rejectsInvalidPercentage(String percent) {
        // Given
        BigDecimal value = new BigDecimal(percent);

        // When
        Runnable constructRule = () -> new PercentageDiscountRule(value);

        // Then
        assertThrows(InvalidArgumentException.class, constructRule::run);
    }

    @ParameterizedTest(name = "fixed discount {0} leaves {1}")
    @CsvSource({"0.00,150.00", "200.00,0.00"})
    @DisplayName("Applies zero and oversized fixed discounts")
    void appliesFixedDiscountBoundary(String amount, String expected) {
        // Given
        PriceCalculator calculator = new PriceCalculator(
                List.of(new FixedDiscountRule(new BigDecimal(amount))),
                BigDecimal.ZERO);

        // When
        BigDecimal price = calculator.calculateFinalPrice(order);

        // Then
        assertEquals(new BigDecimal(expected), price);
    }

    @Test
    @DisplayName("Rejects a negative fixed discount")
    void rejectsNegativeFixedDiscount() {
        // Given
        BigDecimal amount = new BigDecimal("-0.01");

        // When
        Runnable constructRule = () -> new FixedDiscountRule(amount);

        // Then
        assertThrows(InvalidArgumentException.class, constructRule::run);
    }

    @Test
    @DisplayName("Combines urgent surcharge and discounts in stage order")
    void combinesRulesInStageOrder() {
        // Given
        order.changePriority(OrderPriority.URGENT);
        PriceCalculator calculator = new PriceCalculator(
                List.of(
                        new PercentageDiscountRule(new BigDecimal("10")),
                        new UrgentSurchargeRule(),
                        new FixedDiscountRule(new BigDecimal("5"))),
                BigDecimal.ZERO);

        // When
        BigDecimal price = calculator.calculateFinalPrice(order);

        // Then
        assertEquals(new BigDecimal("139.00"), price);
    }

    @Test
    @DisplayName("Shows that percentage discount after surcharge differs from reverse order")
    void stageOrderChangesDiscountedPrice() {
        // Given
        BigDecimal base = order.calculateBaseTotal();
        BigDecimal surcharge = order.calculateLaborTotal()
                .multiply(AppConstants.urgentLaborSurchargeFactor);
        BigDecimal percent = new BigDecimal("10");
        order.changePriority(OrderPriority.URGENT);

        // When
        BigDecimal surchargeThenDiscount = base.add(surcharge)
                .multiply(new BigDecimal("100").subtract(percent))
                .divide(new BigDecimal("100"));
        BigDecimal discountThenSurcharge = base
                .multiply(new BigDecimal("100").subtract(percent))
                .divide(new BigDecimal("100"))
                .add(surcharge);

        // Then
        assertEquals(0, new BigDecimal("144.00").compareTo(surchargeThenDiscount));
        assertEquals(0, new BigDecimal("145.00").compareTo(discountThenSurcharge));
    }

    @Test
    @DisplayName("Accepts a custom pricing rule without calculator changes")
    void acceptsCustomPricingRule() {
        // Given
        List<PricingRule> rules = new ArrayList<>();
        rules.add(new TestDiscountRule());
        PriceCalculator calculator = new PriceCalculator(rules, BigDecimal.ZERO);
        rules.clear();

        // When
        BigDecimal price = calculator.calculateFinalPrice(order);

        // Then
        assertEquals(new BigDecimal("145.00"), price);
    }

    private class TestDiscountRule implements PricingRule {
        private BigDecimal discount = new BigDecimal("5.00");

        @Override
        public PricingStage getStage() {
            return PricingStage.DISCOUNT;
        }

        @Override
        public BigDecimal apply(RepairOrder targetOrder, BigDecimal currentTotal) {
            return currentTotal.subtract(discount);
        }
    }
}
