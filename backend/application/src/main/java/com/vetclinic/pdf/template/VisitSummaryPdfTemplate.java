package com.vetclinic.pdf.template;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Currency;
import java.util.Locale;
import java.util.ResourceBundle;

import org.springframework.stereotype.Component;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import com.vetclinic.pdf.PdfGenerationException;
import com.vetclinic.pdf.PdfTemplate;
import com.vetclinic.pdf.config.PdfProperties;
import com.vetclinic.visit.domain.model.Medication;
import com.vetclinic.visit.domain.model.UsedMaterial;
import com.vetclinic.visit.domain.model.Visit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * PDF template for generating visit summary documents. Supports Polish and English locales with
 * proper Unicode handling.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VisitSummaryPdfTemplate implements PdfTemplate<Visit> {

    private static final String BUNDLE_NAME = "pdf-messages";
    private static final float MARGIN = 40f;

    // Colors matching the app theme
    private static final Color PRIMARY_COLOR = new Color(8, 145, 178); // #0891b2
    private static final Color SECONDARY_COLOR = new Color(30, 41, 59); // #1e293b
    private static final Color SECTION_BG = new Color(248, 250, 252); // Light gray
    private static final Color BORDER_COLOR = new Color(226, 232, 240); // Border

    private final PdfProperties pdfProperties;

    @Override
    public byte[] generate(Visit visit, Locale locale) {
        var bundle = ResourceBundle.getBundle(BUNDLE_NAME, locale);

        try (var baos = new ByteArrayOutputStream()) {
            var document = new Document(PageSize.A4, MARGIN, MARGIN, MARGIN, MARGIN);
            PdfWriter.getInstance(document, baos);
            document.open();

            // Create fonts with Unicode support
            var fonts = createFonts();

            // Header with clinic info
            addClinicHeader(document, bundle, fonts);

            // Title
            addTitle(document, bundle.getString("pdf.title.visit_summary"), fonts);

            // Visit info table
            addVisitInfoSection(document, visit, bundle, fonts, locale);

            // Patient & Client info
            addPatientClientSection(document, visit, bundle, fonts);

            // Clinical sections (only if data exists)
            addTextSection(document, visit.getInterview(), "pdf.interview.header", bundle, fonts);
            addExaminationSection(document, visit, bundle, fonts);
            addTextSection(document, visit.getDiagnosis(), "pdf.diagnosis.header", bundle, fonts);
            addTextSection(document, visit.getTreatment(), "pdf.treatment.header", bundle, fonts);
            addTextSection(
                    document,
                    visit.getRecommendations(),
                    "pdf.recommendations.header",
                    bundle,
                    fonts);

            // Medications table
            if (visit.getMedications() != null && !visit.getMedications().isEmpty()) {
                addMedicationsSection(document, visit, bundle, fonts);
            }

            // Materials table
            if (visit.getUsedMaterials() != null && !visit.getUsedMaterials().isEmpty()) {
                addMaterialsSection(document, visit, bundle, fonts, locale);
            }

            // Next visit
            if (visit.getNextVisitDate() != null) {
                addNextVisitSection(document, visit, bundle, fonts, locale);
            }

            // Notes
            addTextSection(document, visit.getNotes(), "pdf.notes.header", bundle, fonts);

            // Footer
            addFooter(document, visit, bundle, fonts, locale);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Failed to generate visit summary PDF for visit {}", visit.getId(), e);
            throw new PdfGenerationException("Failed to generate visit summary PDF", e);
        }
    }

    @Override
    public String getFilename(Visit visit) {
        var formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        var dateStr =
                visit.getVisitDate() != null ? visit.getVisitDate().format(formatter) : "unknown";
        var patientName =
                visit.getPatientName() != null
                        ? visit.getPatientName().replaceAll("[^a-zA-Z0-9]", "_")
                        : "patient";
        return String.format("visit_%s_%s", patientName, dateStr);
    }

    private FontSet createFonts() {
        // Register fonts with CP1250 encoding for Polish support
        var baseFont =
                FontFactory.getFont(FontFactory.HELVETICA, BaseFont.CP1250, BaseFont.EMBEDDED)
                        .getBaseFont();
        var baseFontBold =
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, BaseFont.CP1250, BaseFont.EMBEDDED)
                        .getBaseFont();

        return new FontSet(
                new Font(baseFontBold, 16, Font.BOLD, Color.WHITE), // titleWhite
                new Font(baseFontBold, 14, Font.BOLD, SECONDARY_COLOR), // sectionHeader
                new Font(baseFontBold, 10, Font.BOLD, SECONDARY_COLOR), // labelBold
                new Font(baseFont, 10, Font.NORMAL, SECONDARY_COLOR), // normal
                new Font(baseFont, 9, Font.NORMAL, Color.GRAY), // small
                new Font(baseFontBold, 10, Font.BOLD, Color.WHITE) // tableHeader
                );
    }

    private void addClinicHeader(Document doc, ResourceBundle bundle, FontSet fonts)
            throws DocumentException {
        var table = new PdfPTable(1);
        table.setWidthPercentage(100);

        var clinicName =
                pdfProperties.getClinicName().isEmpty()
                        ? bundle.getString("pdf.clinic.header")
                        : pdfProperties.getClinicName();

        var cell = new PdfPCell(new Phrase(clinicName, fonts.titleWhite()));
        cell.setBackgroundColor(PRIMARY_COLOR);
        cell.setPadding(15);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBorderWidth(0);
        table.addCell(cell);

        // Add address/contact if configured
        if (!pdfProperties.getAddress().isEmpty() || !pdfProperties.getPhone().isEmpty()) {
            var contactInfo = new StringBuilder();
            if (!pdfProperties.getAddress().isEmpty()) {
                contactInfo.append(pdfProperties.getAddress());
            }
            if (!pdfProperties.getPhone().isEmpty()) {
                if (contactInfo.length() > 0) contactInfo.append(" | ");
                contactInfo.append("Tel: ").append(pdfProperties.getPhone());
            }
            if (!pdfProperties.getEmail().isEmpty()) {
                if (contactInfo.length() > 0) contactInfo.append(" | ");
                contactInfo.append(pdfProperties.getEmail());
            }

            var contactCell = new PdfPCell(new Phrase(contactInfo.toString(), fonts.small()));
            contactCell.setBackgroundColor(PRIMARY_COLOR);
            contactCell.setPadding(5);
            contactCell.setPaddingBottom(10);
            contactCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            contactCell.setBorderWidth(0);
            table.addCell(contactCell);
        }

        doc.add(table);
        doc.add(new Paragraph(" "));
    }

    private void addTitle(Document doc, String title, FontSet fonts) throws DocumentException {
        var paragraph = new Paragraph(title, fonts.sectionHeader());
        paragraph.setAlignment(Element.ALIGN_CENTER);
        paragraph.setSpacingAfter(15);
        doc.add(paragraph);
    }

    private void addVisitInfoSection(
            Document doc, Visit visit, ResourceBundle bundle, FontSet fonts, Locale locale)
            throws DocumentException {
        var table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[] {1, 1.5f, 1, 1.5f});

        // Date & Time
        addLabelValueCell(
                table,
                bundle.getString("pdf.visit.date"),
                formatDate(visit.getVisitDate(), locale),
                fonts);
        addLabelValueCell(
                table, bundle.getString("pdf.visit.time"), formatTime(visit.getVisitDate()), fonts);

        // Type & Duration
        addLabelValueCell(
                table,
                bundle.getString("pdf.visit.type"),
                visit.getVisitType() != null ? visit.getVisitType().name() : "-",
                fonts);
        addLabelValueCell(
                table,
                bundle.getString("pdf.visit.duration"),
                visit.getDurationMinutes() + " " + bundle.getString("pdf.visit.minutes"),
                fonts);

        // Veterinarian
        addLabelValueCell(
                table,
                bundle.getString("pdf.veterinarian.name"),
                orDefault(visit.getVeterinarianName(), bundle),
                fonts);
        addLabelValueCell(
                table,
                bundle.getString("pdf.visit.status"),
                visit.getStatus() != null ? visit.getStatus().name() : "-",
                fonts);

        table.setSpacingAfter(15);
        doc.add(table);
    }

    private void addPatientClientSection(
            Document doc, Visit visit, ResourceBundle bundle, FontSet fonts)
            throws DocumentException {
        var table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[] {1, 1});

        // Patient info
        var patientCell = createSectionCell(bundle.getString("pdf.patient.header"), fonts);
        var patientContent = new Paragraph();
        patientContent.add(
                new Phrase(bundle.getString("pdf.patient.name") + ": ", fonts.labelBold()));
        patientContent.add(
                new Phrase(orDefault(visit.getPatientName(), bundle) + "\n", fonts.normal()));
        patientCell.addElement(patientContent);
        table.addCell(patientCell);

        // Client info
        var clientCell = createSectionCell(bundle.getString("pdf.client.header"), fonts);
        var clientContent = new Paragraph();
        clientContent.add(
                new Phrase(bundle.getString("pdf.client.name") + ": ", fonts.labelBold()));
        clientContent.add(
                new Phrase(orDefault(visit.getClientName(), bundle) + "\n", fonts.normal()));
        clientCell.addElement(clientContent);
        table.addCell(clientCell);

        table.setSpacingAfter(15);
        doc.add(table);
    }

    private void addExaminationSection(
            Document doc, Visit visit, ResourceBundle bundle, FontSet fonts)
            throws DocumentException {
        if (visit.getWeight() == null
                && visit.getTemperature() == null
                && (visit.getExamination() == null || visit.getExamination().isBlank())) {
            return;
        }

        addSectionHeader(doc, bundle.getString("pdf.examination.header"), fonts);

        var table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[] {1, 1.5f, 1, 1.5f});

        if (visit.getWeight() != null) {
            addLabelValueCell(
                    table,
                    bundle.getString("pdf.examination.weight"),
                    String.format(
                            "%.1f %s",
                            visit.getWeight(), bundle.getString("pdf.examination.weight_unit")),
                    fonts);
        } else {
            addEmptyCell(table, 2);
        }

        if (visit.getTemperature() != null) {
            addLabelValueCell(
                    table,
                    bundle.getString("pdf.examination.temperature"),
                    String.format(
                            "%.1f %s",
                            visit.getTemperature(),
                            bundle.getString("pdf.examination.temperature_unit")),
                    fonts);
        } else {
            addEmptyCell(table, 2);
        }

        doc.add(table);

        if (visit.getExamination() != null && !visit.getExamination().isBlank()) {
            var examPara = new Paragraph(visit.getExamination(), fonts.normal());
            examPara.setSpacingBefore(5);
            doc.add(examPara);
        }

        doc.add(new Paragraph(" "));
    }

    private void addTextSection(
            Document doc, String text, String headerKey, ResourceBundle bundle, FontSet fonts)
            throws DocumentException {
        if (text == null || text.isBlank()) {
            return;
        }

        addSectionHeader(doc, bundle.getString(headerKey), fonts);
        var paragraph = new Paragraph(text, fonts.normal());
        paragraph.setSpacingAfter(15);
        doc.add(paragraph);
    }

    private void addMedicationsSection(
            Document doc, Visit visit, ResourceBundle bundle, FontSet fonts)
            throws DocumentException {
        addSectionHeader(doc, bundle.getString("pdf.medications.header"), fonts);

        var table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.setWidths(new float[] {2, 1.5f, 1.5f});

        // Header
        addTableHeaderCell(table, bundle.getString("pdf.medications.name"), fonts);
        addTableHeaderCell(table, bundle.getString("pdf.medications.dosage"), fonts);
        addTableHeaderCell(table, "Frequency / Duration", fonts);

        // Rows
        for (Medication med : visit.getMedications()) {
            addTableCell(table, orDefault(med.getName(), bundle), fonts);
            addTableCell(table, orDefault(med.getDosage(), bundle), fonts);
            var freqDur = new StringBuilder();
            if (med.getFrequency() != null) freqDur.append(med.getFrequency());
            if (med.getDuration() != null) {
                if (freqDur.length() > 0) freqDur.append(" / ");
                freqDur.append(med.getDuration());
            }
            addTableCell(table, freqDur.length() > 0 ? freqDur.toString() : "-", fonts);
        }

        table.setSpacingAfter(15);
        doc.add(table);
    }

    private void addMaterialsSection(
            Document doc, Visit visit, ResourceBundle bundle, FontSet fonts, Locale locale)
            throws DocumentException {
        addSectionHeader(doc, bundle.getString("pdf.materials.header"), fonts);

        var table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[] {3, 1, 1.5f, 1.5f});

        // Header
        addTableHeaderCell(table, bundle.getString("pdf.materials.name"), fonts);
        addTableHeaderCell(table, bundle.getString("pdf.materials.quantity"), fonts);
        addTableHeaderCell(table, bundle.getString("pdf.materials.unit_price"), fonts);
        addTableHeaderCell(table, bundle.getString("pdf.materials.total"), fonts);

        // Rows
        for (UsedMaterial mat : visit.getUsedMaterials()) {
            addTableCell(table, orDefault(mat.getName(), bundle), fonts);
            addTableCell(table, String.valueOf(mat.getQuantity()), fonts);
            addTableCell(table, formatCurrency(mat.getSellPrice(), locale), fonts);
            addTableCell(table, formatCurrency(mat.getTotalSell(), locale), fonts);
        }

        // Total row
        var totalLabel =
                new PdfPCell(
                        new Phrase(
                                bundle.getString("pdf.materials.grand_total"), fonts.labelBold()));
        totalLabel.setColspan(3);
        totalLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalLabel.setPadding(8);
        totalLabel.setBackgroundColor(SECTION_BG);
        table.addCell(totalLabel);

        var totalValue =
                new PdfPCell(
                        new Phrase(
                                formatCurrency(visit.getTotalMaterialsSell(), locale),
                                fonts.labelBold()));
        totalValue.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalValue.setPadding(8);
        totalValue.setBackgroundColor(SECTION_BG);
        table.addCell(totalValue);

        table.setSpacingAfter(15);
        doc.add(table);
    }

    private void addNextVisitSection(
            Document doc, Visit visit, ResourceBundle bundle, FontSet fonts, Locale locale)
            throws DocumentException {
        addSectionHeader(doc, bundle.getString("pdf.next_visit.header"), fonts);

        var paragraph = new Paragraph();
        paragraph.add(
                new Phrase(bundle.getString("pdf.next_visit.date") + ": ", fonts.labelBold()));
        paragraph.add(new Phrase(formatDateTime(visit.getNextVisitDate(), locale), fonts.normal()));
        paragraph.setSpacingAfter(15);
        doc.add(paragraph);
    }

    private void addFooter(
            Document doc, Visit visit, ResourceBundle bundle, FontSet fonts, Locale locale)
            throws DocumentException {
        doc.add(new Paragraph(" "));

        var table = new PdfPTable(2);
        table.setWidthPercentage(100);

        var leftCell =
                new PdfPCell(
                        new Phrase(
                                bundle.getString("pdf.footer.visit_id")
                                        + ": "
                                        + visit.getId().toString().substring(0, 8),
                                fonts.small()));
        leftCell.setBorderWidth(0);
        leftCell.setBorderWidthTop(1);
        leftCell.setBorderColor(BORDER_COLOR);
        leftCell.setPaddingTop(10);
        table.addCell(leftCell);

        var rightCell =
                new PdfPCell(
                        new Phrase(
                                bundle.getString("pdf.footer.generated")
                                        + ": "
                                        + formatDateTime(LocalDateTime.now(), locale),
                                fonts.small()));
        rightCell.setBorderWidth(0);
        rightCell.setBorderWidthTop(1);
        rightCell.setBorderColor(BORDER_COLOR);
        rightCell.setPaddingTop(10);
        rightCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(rightCell);

        doc.add(table);
    }

    // Helper methods

    private void addSectionHeader(Document doc, String text, FontSet fonts)
            throws DocumentException {
        var table = new PdfPTable(1);
        table.setWidthPercentage(100);

        var cell = new PdfPCell(new Phrase(text, fonts.sectionHeader()));
        cell.setBackgroundColor(SECTION_BG);
        cell.setPadding(8);
        cell.setBorderWidth(0);
        cell.setBorderWidthBottom(2);
        cell.setBorderColor(PRIMARY_COLOR);
        table.addCell(cell);

        table.setSpacingAfter(10);
        doc.add(table);
    }

    private PdfPCell createSectionCell(String header, FontSet fonts) {
        var cell = new PdfPCell();
        cell.setBackgroundColor(SECTION_BG);
        cell.setPadding(10);
        cell.setBorderWidth(1);
        cell.setBorderColor(BORDER_COLOR);

        var headerPara = new Paragraph(header, fonts.labelBold());
        headerPara.setSpacingAfter(5);
        cell.addElement(headerPara);

        return cell;
    }

    private void addLabelValueCell(PdfPTable table, String label, String value, FontSet fonts) {
        var labelCell = new PdfPCell(new Phrase(label + ":", fonts.labelBold()));
        labelCell.setBorderWidth(0);
        labelCell.setPadding(5);
        table.addCell(labelCell);

        var valueCell = new PdfPCell(new Phrase(value, fonts.normal()));
        valueCell.setBorderWidth(0);
        valueCell.setPadding(5);
        table.addCell(valueCell);
    }

    private void addTableHeaderCell(PdfPTable table, String text, FontSet fonts) {
        var cell = new PdfPCell(new Phrase(text, fonts.tableHeader()));
        cell.setBackgroundColor(PRIMARY_COLOR);
        cell.setPadding(8);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    private void addTableCell(PdfPTable table, String text, FontSet fonts) {
        var cell = new PdfPCell(new Phrase(text, fonts.normal()));
        cell.setPadding(6);
        cell.setBorderColor(BORDER_COLOR);
        table.addCell(cell);
    }

    private void addEmptyCell(PdfPTable table, int colspan) {
        var cell = new PdfPCell(new Phrase(""));
        cell.setColspan(colspan);
        cell.setBorderWidth(0);
        table.addCell(cell);
    }

    private String orDefault(String value, ResourceBundle bundle) {
        return (value != null && !value.isBlank()) ? value : bundle.getString("pdf.not_applicable");
    }

    private String formatDate(LocalDateTime dateTime, Locale locale) {
        if (dateTime == null) return "-";
        var formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy", locale);
        return dateTime.format(formatter);
    }

    private String formatTime(LocalDateTime dateTime) {
        if (dateTime == null) return "-";
        var formatter = DateTimeFormatter.ofPattern("HH:mm");
        return dateTime.format(formatter);
    }

    private String formatDateTime(LocalDateTime dateTime, Locale locale) {
        if (dateTime == null) return "-";
        var formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm", locale);
        return dateTime.format(formatter);
    }

    private String formatCurrency(BigDecimal amount, Locale locale) {
        if (amount == null) return "-";
        var format = NumberFormat.getCurrencyInstance(new Locale("pl", "PL"));
        format.setCurrency(Currency.getInstance("PLN"));
        return format.format(amount);
    }

    /** Font set record to hold all required fonts. */
    private record FontSet(
            Font titleWhite,
            Font sectionHeader,
            Font labelBold,
            Font normal,
            Font small,
            Font tableHeader) {}
}
