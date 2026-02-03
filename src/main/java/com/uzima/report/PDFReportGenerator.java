package com.uzima.report;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * PDF Report Generator using iText library
 */
public class PDFReportGenerator {
    
    private static final Font TITLE_FONT = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, BaseColor.DARK_GRAY);
    private static final Font HEADER_FONT = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, BaseColor.BLACK);
    private static final Font NORMAL_FONT = new Font(Font.FontFamily.COURIER, 10, Font.NORMAL, BaseColor.BLACK);
    private static final Font SMALL_FONT = new Font(Font.FontFamily.HELVETICA, 8, Font.ITALIC, BaseColor.GRAY);
    
    /**
     * Generate PDF report from text content
     * @param filePath Path where PDF will be saved
     * @param reportTitle Title of the report
     * @param reportContent Main content of the report
     * @param reportType Type/category of the report
     * @throws Exception if PDF generation fails
     */
    public static void generatePDFReport(String filePath, String reportTitle, String reportContent, String reportType) throws Exception {
        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filePath));
        
        // Add metadata
        document.addTitle(reportTitle);
        document.addAuthor("UZIMA Borehole System");
        document.addCreator("UZIMA Report Generator");
        document.addSubject(reportType);
        document.addCreationDate();
        
        document.open();
        
        // Add header
        addHeader(document, reportTitle);
        
        // Add timestamp
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Paragraph timestamp = new Paragraph("Generated: " + sdf.format(new Date()), SMALL_FONT);
        timestamp.setAlignment(Element.ALIGN_RIGHT);
        timestamp.setSpacingAfter(20);
        document.add(timestamp);
        
        // Add report content
        addReportContent(document, reportContent);
        
        // Add footer with page numbers
        addFooter(writer, document);
        
        document.close();
    }
    
    /**
     * Add header section to PDF
     */
    private static void addHeader(Document document, String title) throws DocumentException {
        // Company name
        Paragraph companyName = new Paragraph("UZIMA BOREHOLE DRILLING COMPANY", TITLE_FONT);
        companyName.setAlignment(Element.ALIGN_CENTER);
        companyName.setSpacingAfter(5);
        document.add(companyName);
        
        // Separator line
        LineSeparator line = new LineSeparator();
        line.setLineColor(new BaseColor(50, 50, 150));
        document.add(new Chunk(line));
        document.add(Chunk.NEWLINE);
        
        // Report title
        Paragraph reportTitle = new Paragraph(title, HEADER_FONT);
        reportTitle.setAlignment(Element.ALIGN_CENTER);
        reportTitle.setSpacingAfter(20);
        document.add(reportTitle);
    }
    
    /**
     * Add formatted report content to PDF
     */
    private static void addReportContent(Document document, String content) throws DocumentException {
        // Split content into lines and process
        String[] lines = content.split("\n");
        
        for (String line : lines) {
            if (line.trim().isEmpty()) {
                document.add(Chunk.NEWLINE);
                continue;
            }
            
            // Check for special formatting
            if (line.contains("═") || line.contains("─") || line.contains("╔") || line.contains("║")) {
                // Separator or border line
                Paragraph p = new Paragraph(line, new Font(Font.FontFamily.COURIER, 10, Font.NORMAL, BaseColor.DARK_GRAY));
                document.add(p);
            } else if (line.toUpperCase().equals(line.trim()) && line.trim().length() > 3 && !line.contains("KSh")) {
                // Section header (all caps)
                Paragraph p = new Paragraph(line, new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.BLACK));
                p.setSpacingBefore(10);
                p.setSpacingAfter(5);
                document.add(p);
            } else {
                // Normal content
                Paragraph p = new Paragraph(line, NORMAL_FONT);
                document.add(p);
            }
        }
    }
    
    /**
     * Add footer with page numbers
     */
    private static void addFooter(PdfWriter writer, Document document) throws DocumentException {
        PdfContentByte cb = writer.getDirectContent();
        
        // Add footer text
        Phrase footer = new Phrase("UZIMA Borehole System - Confidential Report", SMALL_FONT);
        ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
                footer,
                (document.right() - document.left()) / 2 + document.leftMargin(),
                document.bottom() - 10, 0);
        
        // Add page number
        Phrase pageNum = new Phrase("Page " + writer.getPageNumber(), SMALL_FONT);
        ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT,
                pageNum,
                document.right(),
                document.bottom() - 10, 0);
    }
    
    /**
     * Generate PDF with custom table data
     * @param filePath Path where PDF will be saved
     * @param reportTitle Title of the report
     * @param tableHeaders Array of column headers
     * @param tableData 2D array of table data
     * @throws Exception if PDF generation fails
     */
    public static void generateTablePDFReport(String filePath, String reportTitle, String[] tableHeaders, String[][] tableData) throws Exception {
        Document document = new Document(PageSize.A4.rotate(), 50, 50, 50, 50); // Landscape for tables
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filePath));
        
        document.addTitle(reportTitle);
        document.addAuthor("UZIMA Borehole System");
        
        document.open();
        
        addHeader(document, reportTitle);
        
        // Create table
        PdfPTable table = new PdfPTable(tableHeaders.length);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        table.setSpacingAfter(10f);
        
        // Add headers
        Font headerFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.WHITE);
        for (String header : tableHeaders) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setBackgroundColor(new BaseColor(50, 50, 150));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(8);
            table.addCell(cell);
        }
        
        // Add data rows
        Font dataFont = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, BaseColor.BLACK);
        for (String[] row : tableData) {
            for (String cell : row) {
                PdfPCell dataCell = new PdfPCell(new Phrase(cell, dataFont));
                dataCell.setPadding(5);
                table.addCell(dataCell);
            }
        }
        
        document.add(table);
        document.close();
    }
}
