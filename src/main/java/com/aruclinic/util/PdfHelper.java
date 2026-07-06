package com.aruclinic.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

public class PdfHelper {

    public static ByteArrayInputStream generateInvoicePdf(String invoiceId, String patientName, String doctorName, String date, String amount) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(out);

        writer.println("%PDF-1.4");
        writer.println("1 0 obj");
        writer.println("<< /Type /Catalog /Pages 2 0 R >>");
        writer.println("endobj");
        writer.println("2 0 obj");
        writer.println("<< /Type /Pages /Kids [3 0 R] /Count 1 >>");
        writer.println("endobj");
        writer.println("3 0 obj");
        writer.println("<< /Type /Page /Parent 2 0 R /Resources << /Font << /F1 4 0 R >> >> /MediaBox [0 0 595 842] /Contents 5 0 R >>");
        writer.println("endobj");
        writer.println("4 0 obj");
        writer.println("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>");
        writer.println("endobj");

        StringBuilder content = new StringBuilder();
        // Logo Badge Graphic
        content.append("0.11 0.39 0.95 rg\n"); // Blue color
        content.append("50 780 36 36 re f\n");  // Draw solid square
        content.append("1 1 1 rg\n");          // White text color
        content.append("BT\n");
        content.append("/F1 14 Tf\n");
        content.append("60 792 Td\n");
        content.append("(AC) Tj\n");
        content.append("ET\n");

        // Main Invoice Text
        content.append("0 0 0 rg\n");          // Black text color
        content.append("BT\n");
        content.append("/F1 18 Tf\n");
        content.append("110 792 Td\n");
        content.append("(ARUCLINIC HEALTHCARE INVOICE) Tj\n");
        content.append("-60 -40 Td\n");        // Shift X back to 50
        content.append("/F1 12 Tf\n");
        content.append("(Invoice Number: ").append(invoiceId).append(") Tj\n");
        content.append("0 -20 Td\n");
        content.append("(Date: ").append(date).append(") Tj\n");
        content.append("0 -30 Td\n");
        content.append("(Patient Name: ").append(patientName).append(") Tj\n");
        content.append("0 -20 Td\n");
        content.append("(Doctor Name: ").append(doctorName).append(") Tj\n");
        content.append("0 -30 Td\n");
        content.append("/F1 14 Tf\n");
        content.append("(Total Amount Due: ").append(amount).append(") Tj\n");
        content.append("0 -40 Td\n");
        content.append("/F1 10 Tf\n");
        content.append("(Thank you for choosing AruClinic!) Tj\n");
        content.append("ET\n");

        byte[] contentBytes = content.toString().getBytes(StandardCharsets.US_ASCII);

        writer.println("5 0 obj");
        writer.println("<< /Length " + contentBytes.length + " >>");
        writer.println("stream");
        writer.flush();
        try {
            out.write(contentBytes);
        } catch (Exception e) {}
        writer.println();
        writer.println("endstream");
        writer.println("endobj");
        writer.println("xref");
        writer.println("0 6");
        writer.println("0000000000 65535 f");
        writer.println("trailer");
        writer.println("<< /Size 6 /Root 1 0 R >>");
        writer.println("startxref");
        writer.println("380");
        writer.println("%%EOF");
        writer.flush();

        return new ByteArrayInputStream(out.toByteArray());
    }

    public static ByteArrayInputStream generatePrescriptionPdf(String rxId, String patientName, String doctorName, String date, String diagnosis, String items) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(out);

        writer.println("%PDF-1.4");
        writer.println("1 0 obj");
        writer.println("<< /Type /Catalog /Pages 2 0 R >>");
        writer.println("endobj");
        writer.println("2 0 obj");
        writer.println("<< /Type /Pages /Kids [3 0 R] /Count 1 >>");
        writer.println("endobj");
        writer.println("3 0 obj");
        writer.println("<< /Type /Page /Parent 2 0 R /Resources << /Font << /F1 4 0 R >> >> /MediaBox [0 0 595 842] /Contents 5 0 R >>");
        writer.println("endobj");
        writer.println("4 0 obj");
        writer.println("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>");
        writer.println("endobj");

        StringBuilder content = new StringBuilder();
        // Logo Badge Graphic
        content.append("0.11 0.39 0.95 rg\n"); // Blue color
        content.append("50 780 36 36 re f\n");  // Draw solid square
        content.append("1 1 1 rg\n");          // White text color
        content.append("BT\n");
        content.append("/F1 14 Tf\n");
        content.append("60 792 Td\n");
        content.append("(AC) Tj\n");
        content.append("ET\n");

        // Main Prescription Text
        content.append("0 0 0 rg\n");          // Black text color
        content.append("BT\n");
        content.append("/F1 18 Tf\n");
        content.append("110 792 Td\n");
        content.append("(ARUCLINIC MEDICAL PRESCRIPTION) Tj\n");
        content.append("-60 -40 Td\n");        // Shift X back to 50
        content.append("/F1 12 Tf\n");
        content.append("(Prescription Number: ").append(rxId).append(") Tj\n");
        content.append("0 -20 Td\n");
        content.append("(Date: ").append(date).append(") Tj\n");
        content.append("0 -30 Td\n");
        content.append("(Patient Name: ").append(patientName).append(") Tj\n");
        content.append("0 -20 Td\n");
        content.append("(Doctor Name: ").append(doctorName).append(") Tj\n");
        content.append("0 -30 Td\n");
        content.append("(Diagnosis: ").append(diagnosis).append(") Tj\n");
        content.append("0 -30 Td\n");
        content.append("/F1 14 Tf\n");
        content.append("(Medications: ").append(items).append(") Tj\n");
        content.append("0 -40 Td\n");
        content.append("/F1 10 Tf\n");
        content.append("(Take as directed by your physician.) Tj\n");
        content.append("ET\n");

        byte[] contentBytes = content.toString().getBytes(StandardCharsets.US_ASCII);

        writer.println("5 0 obj");
        writer.println("<< /Length " + contentBytes.length + " >>");
        writer.println("stream");
        writer.flush();
        try {
            out.write(contentBytes);
        } catch (Exception e) {}
        writer.println();
        writer.println("endstream");
        writer.println("endobj");
        writer.println("xref");
        writer.println("0 6");
        writer.println("0000000000 65535 f");
        writer.println("trailer");
        writer.println("<< /Size 6 /Root 1 0 R >>");
        writer.println("startxref");
        writer.println("380");
        writer.println("%%EOF");
        writer.flush();

        return new ByteArrayInputStream(out.toByteArray());
    }

    public static ByteArrayInputStream generateReportPdf(String reportName, String description, String summaryStats) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(out);

        writer.println("%PDF-1.4");
        writer.println("1 0 obj");
        writer.println("<< /Type /Catalog /Pages 2 0 R >>");
        writer.println("endobj");
        writer.println("2 0 obj");
        writer.println("<< /Type /Pages /Kids [3 0 R] /Count 1 >>");
        writer.println("endobj");
        writer.println("3 0 obj");
        writer.println("<< /Type /Page /Parent 2 0 R /Resources << /Font << /F1 4 0 R >> >> /MediaBox [0 0 595 842] /Contents 5 0 R >>");
        writer.println("endobj");
        writer.println("4 0 obj");
        writer.println("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>");
        writer.println("endobj");

        StringBuilder content = new StringBuilder();
        // Logo Badge Graphic
        content.append("0.11 0.39 0.95 rg\n"); // Blue color
        content.append("50 780 36 36 re f\n");  // Draw solid square
        content.append("1 1 1 rg\n");          // White text color
        content.append("BT\n");
        content.append("/F1 14 Tf\n");
        content.append("60 792 Td\n");
        content.append("(AC) Tj\n");
        content.append("ET\n");

        // Main Report Text
        content.append("0 0 0 rg\n");          // Black text color
        content.append("BT\n");
        content.append("/F1 18 Tf\n");
        content.append("110 792 Td\n");
        content.append("(ARUCLINIC ANALYTICS REPORT) Tj\n");
        content.append("-60 -40 Td\n");        // Shift X back to 50
        content.append("/F1 14 Tf\n");
        content.append("(Report: ").append(reportName).append(") Tj\n");
        content.append("0 -30 Td\n");
        content.append("/F1 11 Tf\n");
        content.append("(Description: ").append(description).append(") Tj\n");
        content.append("0 -30 Td\n");
        content.append("/F1 13 Tf\n");
        content.append("(Summary Metrics: ").append(summaryStats).append(") Tj\n");
        content.append("0 -40 Td\n");
        content.append("/F1 10 Tf\n");
        content.append("(Generated dynamically from clinic database.) Tj\n");
        content.append("ET\n");

        byte[] contentBytes = content.toString().getBytes(StandardCharsets.US_ASCII);

        writer.println("5 0 obj");
        writer.println("<< /Length " + contentBytes.length + " >>");
        writer.println("stream");
        writer.flush();
        try {
            out.write(contentBytes);
        } catch (Exception e) {}
        writer.println();
        writer.println("endstream");
        writer.println("endobj");
        writer.println("xref");
        writer.println("0 6");
        writer.println("0000000000 65535 f");
        writer.println("trailer");
        writer.println("<< /Size 6 /Root 1 0 R >>");
        writer.println("startxref");
        writer.println("380");
        writer.println("%%EOF");
        writer.flush();

        return new ByteArrayInputStream(out.toByteArray());
    }
}
