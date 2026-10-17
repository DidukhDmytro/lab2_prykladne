package com.example.carservice.service;

import com.example.carservice.domain.Car;
import com.example.carservice.domain.Customer;
import com.example.carservice.domain.Mechanic;
import com.example.carservice.exception.EntityAlreadyExistsException;
import com.example.carservice.exception.EntityNotFoundException;
import com.example.carservice.exception.InvalidArgumentException;
import com.example.carservice.exception.ServiceMessages;
import com.example.carservice.repository.CarRepository;
import com.example.carservice.repository.CustomerRepository;
import com.example.carservice.repository.MechanicRepository;

public class RegistrationService {
    private CarRepository carRepository;
    private MechanicRepository mechanicRepository;
    private CustomerRepository customerRepository;

    public RegistrationService(
            CustomerRepository customerRepository,
            CarRepository carRepository,
            MechanicRepository mechanicRepository) {
        if (customerRepository == null || carRepository == null || mechanicRepository == null) {
            throw new InvalidArgumentException(ServiceMessages.serviceDependenciesRequired());
        }
        this.customerRepository = customerRepository;
        this.carRepository = carRepository;
        this.mechanicRepository = mechanicRepository;
    }

    public Customer registerCustomer(Customer customer) {
        if (customer == null) {
            throw new InvalidArgumentException(ServiceMessages.entityRequired());
        }
        if (customerRepository.findById(customer.getId()).isPresent()) {
            throw new EntityAlreadyExistsException(
                    String.format(ServiceMessages.duplicateEntity(), customer.getId()));
        }
        return customerRepository.save(customer);
    }

    public Car registerCar(Car car) {
        if (car == null) {
            throw new InvalidArgumentException(ServiceMessages.repositoryCarRequired());
        }
        String customerId = car.getOwner().getId();
        customerRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format(ServiceMessages.customerNotFound(), customerId)));
        return carRepository.save(car);
    }

    public Mechanic registerMechanic(Mechanic mechanic) {
        return mechanicRepository.save(mechanic);
    }
}
