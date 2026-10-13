package com.example.carservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.carservice.domain.Car;
import com.example.carservice.domain.Customer;
import com.example.carservice.domain.Mechanic;
import com.example.carservice.domain.MechanicSpecialization;
import com.example.carservice.domain.OrderStatus;
import com.example.carservice.domain.RepairOrder;
import com.example.carservice.domain.ServiceWork;
import com.example.carservice.exception.DomainException;
import com.example.carservice.exception.EntityNotFoundException;
import com.example.carservice.exception.InvalidStateTransitionException;
import com.example.carservice.pricing.PriceCalculator;
import com.example.carservice.repository.CarRepository;
import com.example.carservice.repository.MechanicRepository;
import com.example.carservice.repository.RepairOrderRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RepairOrderServiceTest {
    private String orderId = "order-test";
    private String vin = "VIN-TEST";
    private String mechanicId = "mechanic-test";
    private String workDescription = "Test repair";
    private BigDecimal workParts = new BigDecimal("10.00");
    private BigDecimal workLabor = new BigDecimal("5.00");

    @Mock
    private RepairOrderRepository repairOrderRepository;

    @Mock
    private CarRepository carRepository;

    @Mock
    private MechanicRepository mechanicRepository;

    @Mock
    private PriceCalculator priceCalculator;

    private RepairOrder order;
    private RepairOrderService service;
    private ServiceWork work;
    private Mechanic mechanic;

    @BeforeEach
    void setUp() {
        Customer customer = new Customer("customer-test", "Test Customer", "555-0100");
        Car car = new Car(vin, "Test", "Model", 2020, customer);
        order = new RepairOrder(orderId, car);
        work = createWork();
        mechanic = new Mechanic(mechanicId, "Test Mechanic", MechanicSpecialization.GENERAL);
        service = new RepairOrderService(
                repairOrderRepository,
                carRepository,
                mechanicRepository,
                priceCalculator,
                new GiftPolicy());
        when(repairOrderRepository.findById(orderId)).thenReturn(Optional.of(order));
    }

    @ParameterizedTest(name = "allows {0} from {1} to {2}")
    @CsvSource({
            "diagnose,CREATED,DIAGNOSED",
            "approve,DIAGNOSED,APPROVED",
            "startProgress,APPROVED,IN_PROGRESS",
            "complete,IN_PROGRESS,COMPLETED",
            "cancel,CREATED,CANCELLED"
    })
    @DisplayName("Allows valid repair order status transitions")
    void allowsValidTransition(String operation, OrderStatus initial, OrderStatus expected) {
        // Given
        prepareFor(initial, operation);

        // When
        perform(operation);

        // Then
        assertEquals(expected, order.getStatus());
        verify(repairOrderRepository).save(order);
    }

    @ParameterizedTest(name = "rejects {0} while status is {1}")
    @CsvSource({
            "approve,CREATED",
            "startProgress,CREATED",
            "complete,CREATED",
            "diagnose,DIAGNOSED",
            "startProgress,DIAGNOSED",
            "complete,DIAGNOSED",
            "diagnose,APPROVED",
            "approve,APPROVED",
            "complete,APPROVED",
            "diagnose,IN_PROGRESS",
            "approve,IN_PROGRESS",
            "startProgress,IN_PROGRESS",
            "cancel,IN_PROGRESS",
            "diagnose,COMPLETED",
            "approve,COMPLETED",
            "startProgress,COMPLETED",
            "complete,COMPLETED",
            "cancel,COMPLETED",
            "diagnose,CANCELLED",
            "approve,CANCELLED",
            "startProgress,CANCELLED",
            "complete,CANCELLED",
            "cancel,CANCELLED"
    })
    @DisplayName("Rejects forbidden repair order status transitions")
    void rejectsForbiddenTransition(String operation, OrderStatus initial) {
        // Given
        prepareFor(initial, operation);

        // When
        Runnable forbiddenOperation = () -> perform(operation);

        // Then
        assertThrows(DomainException.class, forbiddenOperation::run);
    }

    @Test
    @DisplayName("Does not complete work while any work item is unfinished")
    void rejectsCompletionWithUnfinishedWork() {
        // Given
        order.addWork(work);
        order.diagnose();
        order.approve();
        order.assignMechanic(mechanic);
        order.startProgress();

        // When
        Runnable completeOrder = () -> service.complete(orderId);

        // Then
        assertThrows(InvalidStateTransitionException.class, completeOrder::run);
    }

    @Test
    @DisplayName("Requires a mechanic before starting progress")
    void requiresMechanicBeforeProgress() {
        // Given
        order.addWork(work);
        order.diagnose();
        order.approve();

        // When
        Runnable startOrder = () -> service.startProgress(orderId);

        // Then
        assertThrows(InvalidStateTransitionException.class, startOrder::run);
    }

    @Test
    @DisplayName("Does not save when order is missing")
    void missingOrderDoesNotSave() {
        // Given
        when(repairOrderRepository.findById(orderId)).thenReturn(Optional.empty());

        // When
        Runnable diagnoseMissing = () -> service.diagnose(orderId);
        assertThrows(EntityNotFoundException.class, diagnoseMissing::run);

        // Then
        verify(repairOrderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Saves order after a successful operation")
    void successfulOperationSavesOrder() {
        // Given
        when(repairOrderRepository.save(order)).thenReturn(order);

        // When
        RepairOrder result = service.addWork(orderId, work);

        // Then
        assertSame(order, result);
        verify(repairOrderRepository).save(order);
    }

    @Test
    @DisplayName("Delegates base price calculation to calculator")
    void delegatesBasePriceCalculation() {
        // Given
        BigDecimal expectedPrice = new BigDecimal("15.00");
        when(priceCalculator.calculateBasePrice(order)).thenReturn(expectedPrice);

        // When
        BigDecimal actualPrice = service.calculateBasePrice(orderId);

        // Then
        assertEquals(expectedPrice, actualPrice);
        verify(priceCalculator).calculateBasePrice(order);
    }

    @Test
    @DisplayName("Delegates calculated price to calculator")
    void delegatesFinalPriceCalculation() {
        // Given
        BigDecimal expectedPrice = new BigDecimal("18.00");
        when(priceCalculator.calculateFinalPrice(order)).thenReturn(expectedPrice);

        // When
        BigDecimal actualPrice = service.calculateFinalPrice(orderId);

        // Then
        assertEquals(expectedPrice, actualPrice);
        verify(priceCalculator).calculateFinalPrice(order);
    }

    private void prepareFor(OrderStatus status, String operation) {
        if (status == OrderStatus.DIAGNOSED) {
            order.addWork(createWork());
            order.diagnose();
        } else if (status == OrderStatus.APPROVED) {
            order.addWork(createWork());
            order.diagnose();
            order.approve();
            order.assignMechanic(mechanic);
        } else if (status == OrderStatus.IN_PROGRESS || status == OrderStatus.COMPLETED) {
            order.addWork(createWork());
            order.diagnose();
            order.approve();
            order.assignMechanic(mechanic);
            order.startProgress();
            if (status == OrderStatus.IN_PROGRESS && "complete".equals(operation)) {
                order.getWorks().get(0).complete();
            }
            if (status == OrderStatus.COMPLETED) {
                order.getWorks().get(0).complete();
                order.complete();
            }
        } else if (status == OrderStatus.CANCELLED) {
            order.cancel();
        }
    }

    private void perform(String operation) {
        if ("diagnose".equals(operation)) {
            service.diagnose(orderId);
        } else if ("approve".equals(operation)) {
            service.approve(orderId);
        } else if ("startProgress".equals(operation)) {
            service.startProgress(orderId);
        } else if ("complete".equals(operation)) {
            service.complete(orderId);
        } else if ("cancel".equals(operation)) {
            service.cancel(orderId);
        }
    }

    private ServiceWork createWork() {
        return new ServiceWork(workDescription, workParts, workLabor, MechanicSpecialization.GENERAL);
    }
}
