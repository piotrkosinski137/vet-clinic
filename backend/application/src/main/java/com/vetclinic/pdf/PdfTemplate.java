package com.vetclinic.pdf;

import java.util.Locale;

/**
 * Generic PDF template interface for extensibility. Implementations can generate various document
 * types (visit summaries, invoices, certificates).
 *
 * @param <T> The data type this template renders
 */
public interface PdfTemplate<T> {

    /**
     * Generate PDF bytes from the provided data.
     *
     * @param data The data to render in the PDF
     * @param locale The locale for i18n (PL/EN)
     * @return PDF file as byte array
     * @throws PdfGenerationException if generation fails
     */
    byte[] generate(T data, Locale locale);

    /**
     * Get the default filename for this PDF type.
     *
     * @param data The data being rendered
     * @return Suggested filename without extension
     */
    String getFilename(T data);
}
