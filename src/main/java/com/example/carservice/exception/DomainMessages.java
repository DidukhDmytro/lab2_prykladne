package com.example.carservice.exception;

public class DomainMessages {
    public static String invalidVin = "VIN must not be null or blank.";
    public static String invalidPhoneNumber = "Phone number must not be null or blank.";
    public static String invalidCustomerId = "Customer identifier must not be null or blank.";
    public static String invalidCustomerName = "Customer name must not be null or blank.";
    public static String ownerRequired = "Car owner is required.";
    public static String invalidServiceWorkDescription =
            "Service work description must not be null or blank.";
    public static String costsMustBeNonNegative = "Parts and labor costs must not be negative.";
    public static String specializationRequired = "Mechanic specialization is required.";
    public static String invalidRepairOrderId = "Repair order identifier must not be null or blank.";
    public static String carRequired = "A car is required for a repair order.";
    public static String invalidMechanicId = "Mechanic identifier must not be null or blank.";
    public static String invalidMechanicName = "Mechanic name must not be null or blank.";
    public static String priorityRequired = "Order priority is required.";
    public static String costsRequired = "Parts and labor costs are required.";
    public static String workAlreadyCompleted = "Service work is already completed.";
    public static String invalidDiagnoseStatus = "Only created orders can be diagnosed.";
    public static String invalidApproveStatus = "Only diagnosed orders can be approved.";
    public static String emptyOrder = "An order cannot be approved without service work.";
    public static String invalidProgressStatus = "Only approved orders can start progress.";
    public static String mechanicRequired = "A mechanic must be assigned before work starts.";
    public static String invalidCompleteStatus = "Only in-progress orders can be completed.";
    public static String unfinishedWork = "An order cannot be completed with unfinished work.";
    public static String closedOrder = "A completed or cancelled order cannot be modified.";
    public static String invalidCancelStatus = "Cannot cancel order in status: ";
    public static String pricingRulesRequired = "Pricing rules are required.";
    public static String pricingRuleRequired = "Pricing rules must not contain null.";
    public static String minimumPriceRequired = "Minimum price is required.";
    public static String pricingOrderRequired = "A repair order is required for price calculation.";
    public static String invalidUrgentSurchargeFactor = "Urgent surcharge factor must not be negative.";
    public static String percentageRequired = "Discount percentage is required.";
    public static String percentageOutOfRange = "Discount percentage must be between zero and one hundred.";
    public static String fixedDiscountRequired = "Fixed discount amount is required.";
    public static String negativeFixedDiscount = "Fixed discount amount must not be negative.";
    public static String giftOrderRequired = "A repair order is required to create a present.";

    private DomainMessages() {
    }
}
