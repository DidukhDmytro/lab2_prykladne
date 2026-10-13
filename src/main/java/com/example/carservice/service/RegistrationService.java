package com.example.carservice.service;

import com.example.carservice.domain.Car;
import com.example.carservice.domain.Customer;
import com.example.carservice.domain.Mechanic;
import com.example.carservice.exception.EntityAlreadyExistsException;
import com.example.carservice.exception.InvalidArgumentException;
import com.example.carservice.exception.ServiceMessages;
import com.example.carservice.repository.CarRepository;
import com.example.carservice.repository.MechanicRepository;
import java.util.LinkedHashMap;
import java.util.Map;

public class RegistrationService {
    private CarRepository carRepository;
    private MechanicRepository mechanicRepository;
    private Map<String, Customer> customers = new LinkedHashMap<>();

    public RegistrationService(CarRepository carRepository, MechanicRepository mechanicRepository) {
        if (carRepository == null || mechanicRepository == null) {
            throw new InvalidArgumentException(ServiceMessages.serviceDependenciesRequired);
        }
        this.carRepository = carRepository;
        this.mechanicRepository = mechanicRepository;
    }

    public Customer registerCustomer(Customer customer) {
        if (customer == null) {
            throw new InvalidArgumentException(ServiceMessages.entityRequired);
        }
        if (customers.containsKey(customer.getId())) {
            throw new EntityAlreadyExistsException(
                    String.format(ServiceMessages.duplicateEntity, customer.getId()));
        }
        customers.put(customer.getId(), customer);
        return customer;
    }

    public Car registerCar(Car car) {
        return carRepository.save(car);
    }

    public Mechanic registerMechanic(Mechanic mechanic) {
        return mechanicRepository.save(mechanic);
    }
}
