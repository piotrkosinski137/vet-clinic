package com.vetclinic.billing.domain;

import static com.vetclinic.billing.domain.KamsoftFormatConstants.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * Parses KAMSOFT format supplier invoice files. Extracts invoice header, supplier info, and line
 * items from the structured text format.
 */
@Service
@Slf4j
public class InvoiceParserService {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final Pattern ITEM_COUNT_PATTERN = Pattern.compile("Ilosc pozycji\\s+(\\d+)");

    public record ParsedInvoice(
            String invoiceNumber,
            String supplierName,
            String supplierNip,
            LocalDate invoiceDate,
            LocalDate saleDate,
            LocalDate paymentDueDate,
            String paymentMethod,
            List<ParsedInvoiceItem> items) {}

    public record ParsedInvoiceItem(
            String productCode,
            String productName,
            int quantity,
            String unit,
            BigDecimal netPrice,
            BigDecimal grossPrice,
            int discountPercent,
            int vatRate,
            BigDecimal vatAmount,
            String batchNumber,
            LocalDate expirationDate,
            String barcode,
            String pkwiu) {}

    public ParsedInvoice parseKamsoftInvoice(String content) {
        var lines = content.split("\\r?\\n");

        validateMinimumLineCount(lines);

        var headerLine = lines[HEADER_LINE_INDEX];
        var headerParts = splitByMultipleSpaces(headerLine);

        var invoiceNumber = safeGet(headerParts, HEADER_INVOICE_NUMBER);
        var invoiceDate = parseDate(safeGet(headerParts, HEADER_INVOICE_DATE));
        var saleDate = parseDate(safeGet(headerParts, HEADER_SALE_DATE));
        var paymentDueDate = parseDate(safeGet(headerParts, HEADER_PAYMENT_DUE_DATE));
        var paymentMethod = safeGet(headerParts, HEADER_PAYMENT_METHOD);
        var supplierNip = safeGet(headerParts, HEADER_SUPPLIER_NIP);

        var supplierLine = lines[SUPPLIER_LINE_INDEX];
        var supplierParts = splitByMultipleSpaces(supplierLine);
        var supplierName = safeGet(supplierParts, SUPPLIER_NAME_INDEX);

        var items = parseInvoiceItems(lines);

        return new ParsedInvoice(
                invoiceNumber,
                supplierName,
                supplierNip,
                invoiceDate,
                saleDate,
                paymentDueDate,
                paymentMethod,
                items);
    }

    private void validateMinimumLineCount(String[] lines) {
        if (lines.length < MINIMUM_LINE_COUNT) {
            throw new InvoiceParseException(
                    "Invalid KAMSOFT format: file has "
                            + lines.length
                            + " lines, expected at least "
                            + MINIMUM_LINE_COUNT);
        }
    }

    private List<ParsedInvoiceItem> parseInvoiceItems(String[] lines) {
        var items = new ArrayList<ParsedInvoiceItem>();

        for (var i = ITEMS_START_LINE_INDEX; i < lines.length; i++) {
            var line = lines[i].trim();

            if (line.isEmpty() || line.startsWith(".")) {
                continue;
            }

            if (isItemCountValidationLine(line, items.size())) {
                break;
            }

            parseItemLineSafe(line, i).ifPresent(items::add);
        }

        return items;
    }

    private boolean isItemCountValidationLine(String line, int currentItemCount) {
        var matcher = ITEM_COUNT_PATTERN.matcher(line);
        if (matcher.find()) {
            var expectedCount = Integer.parseInt(matcher.group(1));
            if (currentItemCount != expectedCount) {
                log.warn(
                        "Item count mismatch: expected {}, found {}",
                        expectedCount,
                        currentItemCount);
            }
            return true;
        }
        return false;
    }

    private Optional<ParsedInvoiceItem> parseItemLineSafe(String line, int lineNumber) {
        try {
            return Optional.of(parseItemLine(line));
        } catch (NumberFormatException e) {
            log.error("Invalid number format at line {}: {}", lineNumber, line, e);
            return Optional.empty();
        } catch (DateTimeParseException e) {
            log.error("Invalid date format at line {}: {}", lineNumber, line, e);
            return Optional.empty();
        } catch (IndexOutOfBoundsException e) {
            log.error("Insufficient fields at line {}: {}", lineNumber, line, e);
            return Optional.empty();
        }
    }

    private ParsedInvoiceItem parseItemLine(String line) {
        var parts = splitByMultipleSpaces(line);

        var productCode = safeGet(parts, ITEM_PRODUCT_CODE);
        var productName = safeGet(parts, ITEM_PRODUCT_NAME);
        var quantity = parseInteger(safeGet(parts, ITEM_QUANTITY), 0);
        var unit = safeGet(parts, ITEM_UNIT);

        var netPrice = parseBigDecimal(safeGet(parts, ITEM_NET_PRICE));
        var grossPrice = parseBigDecimal(safeGet(parts, ITEM_GROSS_PRICE));
        var discountPercent = parseInteger(safeGet(parts, ITEM_DISCOUNT_PERCENT), 0);

        var vatRate = parseInteger(safeGet(parts, ITEM_VAT_RATE), 0);
        var vatAmount = parseBigDecimal(safeGet(parts, ITEM_VAT_AMOUNT));

        var expirationDate = parseDate(safeGet(parts, ITEM_EXPIRATION_DATE));
        var batchNumber = safeGet(parts, ITEM_BATCH_NUMBER);
        var pkwiu = safeGet(parts, ITEM_PKWIU);
        var barcode = safeGet(parts, ITEM_BARCODE);

        return new ParsedInvoiceItem(
                productCode,
                productName,
                quantity,
                unit,
                netPrice,
                grossPrice,
                discountPercent,
                vatRate,
                vatAmount,
                batchNumber,
                expirationDate,
                barcode,
                pkwiu);
    }

    private String[] splitByMultipleSpaces(String line) {
        // Split by 2 or more spaces to handle variable spacing
        return line.trim().split("\\s{2,}");
    }

    private String safeGet(String[] array, int index) {
        if (array == null || index < 0 || index >= array.length) {
            return "";
        }
        return array[index].trim();
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }

        try {
            return LocalDate.parse(dateStr.trim(), DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            log.warn("Failed to parse date: {}", dateStr);
            return null;
        }
    }

    private BigDecimal parseBigDecimal(String value) {
        if (value == null || value.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }

        try {
            // Remove any non-numeric characters except decimal separator
            String cleaned = value.trim().replace(",", ".");
            return new BigDecimal(cleaned);
        } catch (NumberFormatException e) {
            log.warn("Failed to parse BigDecimal: {}", value);
            return BigDecimal.ZERO;
        }
    }

    private int parseInteger(String value, int defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }

        try {
            // Remove any decimal part
            String cleaned = value.trim().split("[,.]")[0];
            return Integer.parseInt(cleaned);
        } catch (NumberFormatException e) {
            log.warn("Failed to parse integer: {}", value);
            return defaultValue;
        }
    }
}
