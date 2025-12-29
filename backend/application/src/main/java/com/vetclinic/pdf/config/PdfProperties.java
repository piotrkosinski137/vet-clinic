package com.vetclinic.pdf.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/** Configuration properties for PDF generation. */
@Data
@Component
@ConfigurationProperties(prefix = "vetclinic.pdf")
public class PdfProperties {

    /** Clinic name displayed in PDF header */
    private String clinicName = "VetClinic";

    /** Clinic address line */
    private String address = "";

    /** Contact phone number */
    private String phone = "";

    /** Contact email */
    private String email = "";

    /** Path to clinic logo image (optional) */
    private String logoPath = "";
}
