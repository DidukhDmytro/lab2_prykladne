package com.example.carservice.pricing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.carservice.domain.Car;
import com.example.carservice.domain.Customer;
import com.example.carservice.domain.MechanicSpecialization;
import com.example.carservice.domain.RepairOrder;
import com.example.carservice.domain.ServiceWork;
import com.example.carservice.exception.DomainMessages;
import com.example.carservice.exception.InvalidArgumentException;
import com.example.carservice.pricing.rule.FixedDiscountRule;
import com.example.carservice.pricing.rule.PercentageDiscountRule;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PricingRuleEngineTest {
    private PricingContext context;

    @BeforeEach
    void setUp() {
        Customer customer = new Customer("customer-engine", "Engine Customer", "555-0100");
        Car car = new Car("VIN-ENGINE", "Test", "Model", 2020, customer);
        RepairOrder order = new RepairOrder("order-engine", car);
        order.addWork(new ServiceWork(
                "Engine test work",
                new BigDecimal("100.00"),
                new BigDecimal("50.00"),
                MechanicSpecialization.GENERAL));
        context = PricingContext.from(order);
    }

    @Test
    @DisplayName("Rejects a null pricing context")
    void rejectsNullContext() {
        // Given
        PricingRuleEngine engine = new PricingRuleEngine(List.of(), BigDecimal.ZERO);

        // When
        Runnable calculate = () -> engine.calculateFinalPrice(null);

        // Then
        InvalidArgumentException exception =
                assertThrows(InvalidArgumentException.class, calculate::run);
        assertEquals(DomainMessages.pricingContextRequired(), exception.getMessage());
    }

    @Test
    @DisplayName("Rejects a null rule list")
    void rejectsNullRuleList() {
        // Given
        List<PricingRule> rules = null;

        // When
        Runnable createEngine = () -> new PricingRuleEngine(rules, BigDecimal.ZERO);

        // Then
        InvalidArgumentException exception =
                assertThrows(InvalidArgumentException.class, createEngine::run);
        assertEquals(DomainMessages.pricingRulesRequired(), exception.getMessage());
    }

    @Test
    @DisplayName("Rejects null elements in the rule list")
    void rejectsNullRuleElement() {
        // Given
        List<PricingRule> rules = new ArrayList<>();
        rules.add(null);

        // When
        Runnable createEngine = () -> new PricingRuleEngine(rules, BigDecimal.ZERO);

        // Then
        InvalidArgumentException exception =
                assertThrows(InvalidArgumentException.class, createEngine::run);
        assertEquals(DomainMessages.pricingRuleRequired(), exception.getMessage());
    }

    @Test
    @DisplayName("Rejects a negative minimum price")
    void rejectsNegativeMinimumPrice() {
        // Given
        BigDecimal minimumPrice = new BigDecimal("-0.01");

        // When
        Runnable createEngine = () -> new PricingRuleEngine(List.of(), minimumPrice);

        // Then
        InvalidArgumentException exception =
                assertThrows(InvalidArgumentException.class, createEngine::run);
        assertEquals(DomainMessages.negativeMinimumPrice(), exception.getMessage());
    }

    @Test
    @DisplayName("Rejects a null minimum price")
    void rejectsNullMinimumPrice() {
        // Given
        BigDecimal minimumPrice = null;

        // When
        Runnable createEngine = () -> new PricingRuleEngine(List.of(), minimumPrice);

        // Then
        InvalidArgumentException exception =
                assertThrows(InvalidArgumentException.class, createEngine::run);
        assertEquals(DomainMessages.minimumPriceRequired(), exception.getMessage());
    }

    @Test
    @DisplayName("Rejects a rule that returns null")
    void rejectsNullRuleResult() {
        // Given
        PricingRuleEngine engine =
                new PricingRuleEngine(List.of(new NullResultRule()), BigDecimal.ZERO);

        // When
        Runnable calculate = () -> engine.calculateFinalPrice(context);

        // Then
        InvalidArgumentException exception =
                assertThrows(InvalidArgumentException.class, calculate::run);
        assertEquals(DomainMessages.pricingRuleResultRequired(), exception.getMessage());
    }

    @Test
    @DisplayName("Returns equal results for repeated calls with the same context")
    void repeatedCallsReturnEqualResults() {
        // Given
        PricingRuleEngine engine = new PricingRuleEngine(
                List.of(new PercentageDiscountRule(new BigDecimal("10"))),
                BigDecimal.ZERO);

        // When
        BigDecimal firstResult = engine.calculateFinalPrice(context);
        BigDecimal secondResult = engine.calculateFinalPrice(context);

        // Then
        assertEquals(firstResult, secondResult);
    }

    @Test
    @DisplayName("Does not mutate context or the original rule list")
    void doesNotMutateInputs() {
        // Given
        BigDecimal laborTotal = context.getLaborTotal();
        BigDecimal partsTotal = context.getPartsTotal();
        boolean urgent = context.isUrgent();
        int carYear = context.getCarYear();
        List<PricingRule> rules = new ArrayList<>(List.of(new AddOneRule()));
        PricingRuleEngine engine = new PricingRuleEngine(rules, BigDecimal.ZERO);
        rules.clear();

        // When
        BigDecimal result = engine.calculateFinalPrice(context);

        // Then
        assertEquals(new BigDecimal("151.00"), result);
        assertTrue(rules.isEmpty());
        assertEquals(laborTotal, context.getLaborTotal());
        assertEquals(partsTotal, context.getPartsTotal());
        assertEquals(urgent, context.isUrgent());
        assertEquals(carYear, context.getCarYear());
    }

    @Test
    @DisplayName("Rounds only once at the end using HALF_UP")
    void roundsOnlyOnceAtHalfUpBoundary() {
        // Given
        PricingRuleEngine engine = new PricingRuleEngine(
                List.of(
                        new ExactAmountRule(new BigDecimal("1.005")),
                        new MultiplyAmountRule(new BigDecimal("2"))),
                BigDecimal.ZERO);

        // When
        BigDecimal result = engine.calculateFinalPrice(context);

        // Then
        assertEquals(new BigDecimal("2.01"), result);
    }

    @Test
    @DisplayName("Clamps negative rule output before applying the minimum")
    void neverReturnsBelowMinimum() {
        // Given
        BigDecimal minimumPrice = new BigDecimal("10.00");
        PricingRuleEngine engine =
                new PricingRuleEngine(List.of(new ExactAmountRule(new BigDecimal("-5.00"))), minimumPrice);

        // When
        BigDecimal result = engine.calculateFinalPrice(context);

        // Then
        assertEquals(minimumPrice, result);
    }

    @Test
    @DisplayName("Fixed and percentage discount order changes the result")
    void fixedAndPercentageDiscountsAreOrderSensitive() {
        // Given
        PricingRuleEngine fixedThenPercent = new PricingRuleEngine(
                List.of(
                        new FixedDiscountRule(new BigDecimal("20")),
                        new PercentageDiscountRule(new BigDecimal("10"))),
                BigDecimal.ZERO);
        PricingRuleEngine percentThenFixed = new PricingRuleEngine(
                List.of(
                        new PercentageDiscountRule(new BigDecimal("10")),
                        new FixedDiscountRule(new BigDecimal("20"))),
                BigDecimal.ZERO);

        // When
        BigDecimal firstResult = fixedThenPercent.calculateFinalPrice(context);
        BigDecimal secondResult = percentThenFixed.calculateFinalPrice(context);

        // Then
        assertEquals(new BigDecimal("117.00"), firstResult);
        assertEquals(new BigDecimal("115.00"), secondResult);
    }

    @Test
    @DisplayName("Two percentage discounts commute")
    void percentageDiscountsAreCommutative() {
        // Given
        PricingRuleEngine firstOrder = new PricingRuleEngine(
                List.of(
                        new PercentageDiscountRule(new BigDecimal("10")),
                        new PercentageDiscountRule(new BigDecimal("20"))),
                BigDecimal.ZERO);
        PricingRuleEngine reverseOrder = new PricingRuleEngine(
                List.of(
                        new PercentageDiscountRule(new BigDecimal("20")),
                        new PercentageDiscountRule(new BigDecimal("10"))),
                BigDecimal.ZERO);

        // When
        BigDecimal firstResult = firstOrder.calculateFinalPrice(context);
        BigDecimal secondResult = reverseOrder.calculateFinalPrice(context);

        // Then
        assertEquals(new BigDecimal("108.00"), firstResult);
        assertEquals(firstResult, secondResult);
    }

    private class NullResultRule implements PricingRule {
        @Override
        public int getPriority() {
            return PricingRule.SURCHARGE_PRIORITY;
        }

        @Override
        public BigDecimal apply(PricingContext ruleContext, BigDecimal currentTotal) {
            return null;
        }
    }

    private class AddOneRule implements PricingRule {
        @Override
        public int getPriority() {
            return PricingRule.SURCHARGE_PRIORITY;
        }

        @Override
        public BigDecimal apply(PricingContext ruleContext, BigDecimal currentTotal) {
            return currentTotal.add(BigDecimal.ONE);
        }
    }

    private class ExactAmountRule implements PricingRule {
        private BigDecimal amount;

        private ExactAmountRule(BigDecimal amount) {
            this.amount = amount;
        }

        @Override
        public int getPriority() {
            return PricingRule.SURCHARGE_PRIORITY;
        }

        @Override
        public BigDecimal apply(PricingContext ruleContext, BigDecimal currentTotal) {
            return amount;
        }
    }

    private class MultiplyAmountRule implements PricingRule {
        private BigDecimal multiplier;

        private MultiplyAmountRule(BigDecimal multiplier) {
            this.multiplier = multiplier;
        }

        @Override
        public int getPriority() {
            return PricingRule.DISCOUNT_PRIORITY;
        }

        @Override
        public BigDecimal apply(PricingContext ruleContext, BigDecimal currentTotal) {
            return currentTotal.multiply(multiplier);
        }
    }
}
