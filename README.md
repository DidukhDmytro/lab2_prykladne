# Car Service

A Java application for modeling car-service customers, cars, mechanics, repair orders, work,
pricing, and customer gifts. The project uses Maven's standard source layout and includes a
console demonstration and JUnit 5 tests.

## Requirements

- JDK 17 or newer
- Apache Maven 3.9 or compatible

## Commands

Run the test suite:

```sh
mvn test
```

Compile the application and run the console demo:

```sh
mvn compile exec:java
```

## Project structure

```text
src/
├── main/java/com/example/carservice/
│   ├── app/          # Demo and demonstration pricing rules
│   ├── config/       # Shared application constants
│   ├── domain/       # Repair-order and related business objects
│   ├── exception/    # Domain and application exceptions/messages
│   ├── pricing/      # Pricing pipeline and reusable rules
│   ├── repository/   # Repository interfaces and in-memory adapters
│   └── service/      # Application services and gift policy
└── test/java/com/example/carservice/
    ├── domain/
    ├── pricing/
    ├── repository/
    └── service/
```

## Architecture

- **Domain:** `RepairOrder`, `ServiceWork`, `Mechanic`, `Car`, and `Customer` model the business
  state and enforce their own invariants and allowed state transitions.
- **Repository:** interfaces define storage operations; `repository.memory` provides
  `LinkedHashMap` implementations. `RepairOrderService` depends on the repository interface so
  application behavior is independent of storage details and repositories can be replaced or
  mocked.
- **Service:** `RepairOrderService` coordinates repository access and domain operations.
  `RegistrationService` handles registration, and `GiftPolicy` selects gifts by parts total.
- **Pricing:** `PriceCalculator` applies rules in stage order: surcharges first, then discounts,
  preserving insertion order within each stage. It applies the minimum price after the rules and
  rounds once at the end to two decimal places using `HALF_UP`.
- **Missing orders:** repository lookups return `Optional.empty()` when no order is found.
  `RepairOrderService` converts that result to `EntityNotFoundException` when an order is required.

## Lab 1 business rules

| Rule | Enforced by |
|---|---|
| Car VIN and owner are required | `Car` constructor |
| Customer id, name, and phone number are required | `Customer` constructor |
| Mechanic id, name, and specialization are required; mechanics start free | `Mechanic` constructor |
| Work description, costs, and specialization are valid; zero costs are allowed and negative costs are rejected | `ServiceWork` constructor |
| Repair-order id and car are required | `RepairOrder` constructor |
| Only a mechanic compatible with every work specialization can be assigned; busy mechanics cannot be assigned | `RepairOrder.assignMechanic` and `Mechanic.assignToOrder` |
| Approval requires at least one work item | `RepairOrder.approve` |
| Progress requires an approved order and an assigned mechanic | `RepairOrder.startProgress` |
| Completion requires an in-progress order and completed work; completing or cancelling releases its mechanic | `RepairOrder.complete` and `RepairOrder.cancel` |
| Completed or cancelled orders cannot be modified | `RepairOrder` modification operations |
| A registered car VIN cannot be registered twice | `InMemoryCarRepository` |
| A customer id cannot be registered twice | `RegistrationService` |
| Urgent priority adds a surcharge to labor only | `UrgentSurchargeRule` |
| Parts-total gift tiers select washer fluid, interior cleaning, or a discount voucher | `GiftPolicy` |
