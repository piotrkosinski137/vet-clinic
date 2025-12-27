package com.vetclinic.billing.domain;

/**
 * Constants for parsing KAMSOFT invoice files. These define the file structure and field positions
 * in the KAMSOFT format.
 */
public final class KamsoftFormatConstants {

    private KamsoftFormatConstants() {
        // Utility class - prevent instantiation
    }

    // ============================================
    // File Structure Constants
    // ============================================

    /** Minimum number of lines required for a valid KAMSOFT invoice file. */
    public static final int MINIMUM_LINE_COUNT = 13;

    /** Zero-based line index containing the invoice header information. */
    public static final int HEADER_LINE_INDEX = 8;

    /** Zero-based line index containing supplier information. */
    public static final int SUPPLIER_LINE_INDEX = 10;

    /** Zero-based line index where invoice items begin. */
    public static final int ITEMS_START_LINE_INDEX = 12;

    // ============================================
    // Header Field Indices (after splitting by multiple spaces)
    // ============================================

    /** Index of invoice number in the header line. */
    public static final int HEADER_INVOICE_NUMBER = 0;

    /** Index of invoice date in the header line. */
    public static final int HEADER_INVOICE_DATE = 1;

    /** Index of sale date in the header line. */
    public static final int HEADER_SALE_DATE = 2;

    /** Index of payment due date in the header line. */
    public static final int HEADER_PAYMENT_DUE_DATE = 3;

    /** Index of payment method in the header line. */
    public static final int HEADER_PAYMENT_METHOD = 4;

    /** Index of supplier NIP in the header line. */
    public static final int HEADER_SUPPLIER_NIP = 5;

    // ============================================
    // Supplier Line Field Indices
    // ============================================

    /** Index of supplier name in the supplier info line. */
    public static final int SUPPLIER_NAME_INDEX = 2;

    // ============================================
    // Item Line Field Indices (after splitting by multiple spaces)
    // ============================================

    /** Index of product code in item line. */
    public static final int ITEM_PRODUCT_CODE = 0;

    /** Index of product name in item line. */
    public static final int ITEM_PRODUCT_NAME = 1;

    /** Index of quantity in item line. */
    public static final int ITEM_QUANTITY = 2;

    /** Index of unit in item line. */
    public static final int ITEM_UNIT = 3;

    /** Index of discount percent in item line. */
    public static final int ITEM_DISCOUNT_PERCENT = 5;

    /** Index of net price in item line. */
    public static final int ITEM_NET_PRICE = 6;

    /** Index of gross price in item line. */
    public static final int ITEM_GROSS_PRICE = 8;

    /** Index of VAT rate in item line. */
    public static final int ITEM_VAT_RATE = 10;

    /** Index of VAT amount in item line. */
    public static final int ITEM_VAT_AMOUNT = 11;

    /** Index of expiration date in item line. */
    public static final int ITEM_EXPIRATION_DATE = 12;

    /** Index of batch number in item line. */
    public static final int ITEM_BATCH_NUMBER = 13;

    /** Index of PKWIU classification code in item line. */
    public static final int ITEM_PKWIU = 14;

    /** Index of barcode in item line. */
    public static final int ITEM_BARCODE = 15;
}
