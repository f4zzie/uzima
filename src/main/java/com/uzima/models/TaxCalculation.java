package com.uzima.models;

/**
 * Represents tax calculations for invoices
 */
public class TaxCalculation {
    private static final double TAX_RATE = 0.16; // 16% tax

    /**
     * Calculate tax amount on a subtotal
     * @param subtotal The subtotal before tax
     * @return The tax amount (16% of subtotal)
     */
    public static double calculateTax(double subtotal) {
        return subtotal * TAX_RATE;
    }

    /**
     * Calculate total amount including tax
     * @param subtotal The subtotal before tax
     * @return The total amount including 16% tax
     */
    public static double calculateTotal(double subtotal) {
        return subtotal + calculateTax(subtotal);
    }

    /**
     * Get the tax rate
     * @return Tax rate as a decimal (0.16)
     */
    public static double getTaxRate() {
        return TAX_RATE;
    }

    /**
     * Get the tax rate as percentage
     * @return Tax rate as percentage (16)
     */
    public static int getTaxRatePercentage() {
        return (int) (TAX_RATE * 100);
    }
}
