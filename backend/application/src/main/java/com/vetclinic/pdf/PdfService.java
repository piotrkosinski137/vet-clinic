package com.vetclinic.pdf;

import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.vetclinic.pdf.template.VisitSummaryPdfTemplate;
import com.vetclinic.visit.domain.VisitService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Service for PDF document generation. Orchestrates template selection and data fetching. */
@Slf4j
@Service
@RequiredArgsConstructor
public class PdfService {

    private final VisitService visitService;
    private final VisitSummaryPdfTemplate visitSummaryTemplate;

    /**
     * Generate a visit summary PDF.
     *
     * @param visitId The visit ID
     * @param locale The locale for i18n
     * @return PDF generation result with bytes and filename
     */
    public PdfResult generateVisitSummary(UUID visitId, Locale locale) {
        log.info("Generating visit summary PDF for visit {} in locale {}", visitId, locale);

        var visit = visitService.getVisit(visitId);
        var pdfBytes = visitSummaryTemplate.generate(visit, locale);
        var filename = visitSummaryTemplate.getFilename(visit) + ".pdf";

        log.info("Generated PDF {} ({} bytes) for visit {}", filename, pdfBytes.length, visitId);

        return new PdfResult(pdfBytes, filename);
    }

    /** Result record containing PDF data and suggested filename. */
    public record PdfResult(byte[] data, String filename) {}
}
