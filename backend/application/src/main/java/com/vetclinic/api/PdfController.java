package com.vetclinic.api;

import static com.vetclinic.common.security.Roles.HAS_ANY_ROLE;

import java.util.Locale;
import java.util.UUID;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vetclinic.pdf.PdfService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/** REST controller for PDF document generation endpoints. */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "PDF", description = "PDF document generation endpoints")
public class PdfController {

    private final PdfService pdfService;

    /**
     * Generate PDF summary for a completed visit.
     *
     * @param id Visit ID
     * @param lang Language code (pl or en, defaults to pl)
     * @return PDF file as byte stream
     */
    @GetMapping("/visits/{id}/pdf")
    @PreAuthorize(HAS_ANY_ROLE)
    @Operation(
            summary = "Download visit summary PDF",
            description =
                    "Generate and download a PDF summary for a visit. Supports Polish (pl) and English (en) locales.")
    @ApiResponse(responseCode = "200", description = "PDF generated successfully")
    @ApiResponse(responseCode = "404", description = "Visit not found")
    public ResponseEntity<byte[]> getVisitPdf(
            @Parameter(description = "Visit UUID") @PathVariable UUID id,
            @Parameter(description = "Language code (pl or en)") @RequestParam(defaultValue = "pl")
                    String lang) {

        var locale = "en".equalsIgnoreCase(lang) ? Locale.ENGLISH : Locale.forLanguageTag("pl");
        var result = pdfService.generateVisitSummary(id, locale);

        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
                ContentDisposition.attachment().filename(result.filename()).build());
        headers.setContentLength(result.data().length);

        return ResponseEntity.ok().headers(headers).body(result.data());
    }

    /**
     * Preview PDF in browser (inline disposition).
     *
     * @param id Visit ID
     * @param lang Language code (pl or en, defaults to pl)
     * @return PDF file for inline display
     */
    @GetMapping("/visits/{id}/pdf/preview")
    @PreAuthorize(HAS_ANY_ROLE)
    @Operation(
            summary = "Preview visit summary PDF",
            description = "Generate and preview a PDF summary for a visit in the browser.")
    public ResponseEntity<byte[]> previewVisitPdf(
            @Parameter(description = "Visit UUID") @PathVariable UUID id,
            @Parameter(description = "Language code (pl or en)") @RequestParam(defaultValue = "pl")
                    String lang) {

        var locale = "en".equalsIgnoreCase(lang) ? Locale.ENGLISH : Locale.forLanguageTag("pl");
        var result = pdfService.generateVisitSummary(id, locale);

        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
                ContentDisposition.inline().filename(result.filename()).build());
        headers.setContentLength(result.data().length);

        return ResponseEntity.ok().headers(headers).body(result.data());
    }
}
