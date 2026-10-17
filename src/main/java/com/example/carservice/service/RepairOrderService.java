package com.example.carservice.service;

import com.example.carservice.domain.Car;
import com.example.carservice.domain.Mechanic;
import com.example.carservice.domain.OrderStatus;
import com.example.carservice.domain.OrderPriority;
import com.example.carservice.domain.Present;
import com.example.carservice.domain.RepairOrder;
import com.example.carservice.domain.ServiceWork;
import com.example.carservice.exception.EntityAlreadyExistsException;
import com.example.carservice.exception.EntityNotFoundException;
import com.example.carservice.exception.InvalidArgumentException;
import com.example.carservice.exception.ServiceMessages;
import com.example.carservice.pricing.PricingPolicy;
import com.example.carservice.repository.CarRepository;
import com.example.carservice.repository.MechanicRepository;
import com.example.carservice.repository.RepairOrderRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class RepairOrderService {
    private RepairOrderRepository repairOrderRepository;
    private CarRepository carRepository;
    private MechanicRepository mechanicRepository;
    private PricingPolicy pricingPolicy;
    private GiftPolicy giftPolicy;

    public RepairOrderService(
            RepairOrderRepository repairOrderRepository,
            CarRepository carRepository,
            MechanicRepository mechanicRepository,
            PricingPolicy pricingPolicy,
            GiftPolicy giftPolicy) {
        if (repairOrderRepository == null
                || carRepository == null
                || mechanicRepository == null
                || pricingPolicy == null
                || giftPolicy == null) {
            throw new InvalidArgumentException(ServiceMessages.serviceDependenciesRequired());
        }
        this.repairOrderRepository = repairOrderRepository;
        this.carRepository = carRepository;
        this.mechanicRepository = mechanicRepository;
        this.pricingPolicy = pricingPolicy;
        this.giftPolicy = giftPolicy;
    }

    public RepairOrder createOrder(String orderId, String vin) {
        if (repairOrderRepository.findById(orderId).isPresent()) {
            throw new EntityAlreadyExistsException(
                    String.format(ServiceMessages.duplicateRepairOrder(), orderId));
        }
        Car car = carRepository.findByVin(vin)
                .orElseThrow(() -> new EntityNotFoundException(String.format(ServiceMessages.carNotFound(), vin)));
        return repairOrderRepository.save(new RepairOrder(orderId, car));
    }

    public RepairOrder getOrder(String orderId) {
        return getOrThrow(orderId);
    }

    public RepairOrder addWork(String orderId, ServiceWork work) {
        RepairOrder order = getOrThrow(orderId);
        order.addWork(work);
        return repairOrderRepository.save(order);
    }

    public RepairOrder changePriority(String orderId, OrderPriority priority) {
        RepairOrder order = getOrThrow(orderId);
        order.changePriority(priority);
        return repairOrderRepository.save(order);
    }

    public RepairOrder diagnose(String orderId) {
        RepairOrder order = getOrThrow(orderId);
        order.diagnose();
        return repairOrderRepository.save(order);
    }

    public RepairOrder approve(String orderId) {
        RepairOrder order = getOrThrow(orderId);
        order.approve();
        return repairOrderRepository.save(order);
    }

    public RepairOrder assignMechanic(String orderId, String mechanicId) {
        RepairOrder order = getOrThrow(orderId);
        Mechanic mechanic = mechanicRepository.findById(mechanicId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format(ServiceMessages.mechanicNotFound(), mechanicId)));
        order.assignMechanic(mechanic);
        return repairOrderRepository.save(order);
    }

    public RepairOrder startProgress(String orderId) {
        RepairOrder order = getOrThrow(orderId);
        order.startProgress();
        return repairOrderRepository.save(order);
    }

    public RepairOrder completeWork(String orderId, int workIndex) {
        RepairOrder order = getOrThrow(orderId);
        order.completeWork(workIndex);
        return repairOrderRepository.save(order);
    }

    public RepairOrder complete(String orderId) {
        RepairOrder order = getOrThrow(orderId);
        order.complete();
        return repairOrderRepository.save(order);
    }

    public RepairOrder cancel(String orderId) {
        RepairOrder order = getOrThrow(orderId);
        order.cancel();
        return repairOrderRepository.save(order);
    }

    public List<RepairOrder> findByStatus(OrderStatus status) {
        return repairOrderRepository.findByStatus(status);
    }

    public BigDecimal calculateBasePrice(String orderId) {
        return pricingPolicy.calculateBasePrice(getOrThrow(orderId));
    }

    public BigDecimal calculateFinalPrice(String orderId) {
        return pricingPolicy.calculateFinalPrice(getOrThrow(orderId));
    }

    public Present createPresent(String orderId) {
        return giftPolicy.createPresent(getOrThrow(orderId));
    }

    private RepairOrder getOrThrow(String orderId) {
        Optional<RepairOrder> order = repairOrderRepository.findById(orderId);
        return order.orElseThrow(() -> new EntityNotFoundException(
                String.format(ServiceMessages.repairOrderNotFound(), orderId)));
    }
}
