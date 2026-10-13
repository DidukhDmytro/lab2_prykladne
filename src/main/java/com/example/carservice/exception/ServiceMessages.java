package com.example.carservice.exception;

public class ServiceMessages {
    public static String entityRequired = "Registered entities must not be null.";
    public static String duplicateEntity = "An entity with identifier '%s' is already registered.";
    public static String duplicateVin = "A car with VIN '%s' is already registered.";
    public static String repositoryOrderRequired = "A repair order is required.";
    public static String repositoryCarRequired = "A car is required.";
    public static String repositoryMechanicRequired = "A mechanic is required.";
    public static String repositoryIdentifierRequired = "An identifier is required.";
    public static String repositoryStatusRequired = "An order status is required.";
    public static String repairOrderNotFound = "Repair order '%s' was not found.";
    public static String carNotFound = "Car with VIN '%s' was not found.";
    public static String mechanicNotFound = "Mechanic '%s' was not found.";
    public static String workIndexOutOfRange = "Work index is outside the order work list.";
    public static String serviceDependenciesRequired = "Service dependencies are required.";

    private ServiceMessages() {
    }
}
