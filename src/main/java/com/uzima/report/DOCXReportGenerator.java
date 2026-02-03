package com.uzima.report;

import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * DOCX (Word) Report Generator using Apache POI library
 */
public class DOCXReportGenerator {
    
    /**
     * Generate DOCX report from text content
     * @param filePath Path where DOCX will be saved
     * @param reportTitle Title of the report
     * @param reportContent Main content of the report
     * @param reportType Type/category of the report
     * @throws Exception if DOCX generation fails
     */
    public static void generateDOCXReport(String filePath, String reportTitle, String reportContent, String reportType) throws Exception {
        XWPFDocument document = new XWPFDocument();
        
        // Add title
        XWPFParagraph titlePara = document.createParagraph();
        titlePara.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun titleRun = titlePara.createRun();
        titleRun.setText("UZIMA BOREHOLE DRILLING COMPANY");
        titleRun.setBold(true);
        titleRun.setFontSize(18);
        titleRun.setFontFamily("Arial");
        titleRun.setColor("1E3C72");
        
        // Add separator
        XWPFParagraph separator = document.createParagraph();
        separator.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun sepRun = separator.createRun();
        sepRun.setText("═══════════════════════════════════════════════════");
        sepRun.setFontSize(10);
        
        // Add report title
        XWPFParagraph reportTitlePara = document.createParagraph();
        reportTitlePara.setAlignment(ParagraphAlignment.CENTER);
        reportTitlePara.setSpacingAfter(200);
        XWPFRun reportTitleRun = reportTitlePara.createRun();
        reportTitleRun.setText(reportTitle);
        reportTitleRun.setBold(true);
        reportTitleRun.setFontSize(14);
        
        // Add timestamp
        XWPFParagraph timestampPara = document.createParagraph();
        timestampPara.setAlignment(ParagraphAlignment.RIGHT);
        timestampPara.setSpacingAfter(200);
        XWPFRun timestampRun = timestampPara.createRun();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        timestampRun.setText("Generated: " + sdf.format(new Date()));
        timestampRun.setFontSize(9);
        timestampRun.setItalic(true);
        
        // Add content
        String[] lines = reportContent.split("\n");
        
        for (String line : lines) {
            XWPFParagraph para = document.createParagraph();
            XWPFRun run = para.createRun();
            
            if (line.trim().isEmpty()) {
                run.addBreak();
                continue;
            }
            
            // Check for special formatting
            if (line.contains("═") || line.contains("─") || line.contains("╔") || line.contains("║")) {
                // Separator or border line
                run.setFontFamily("Courier New");
                run.setFontSize(10);
                run.setColor("404040");
                run.setText(line);
            } else if (line.toUpperCase().equals(line.trim()) && line.trim().length() > 3 && !line.contains("KSh")) {
                // Section header (all caps)
                run.setBold(true);
                run.setFontSize(12);
                run.setFontFamily("Arial");
                run.setText(line);
                para.setSpacingBefore(200);
                para.setSpacingAfter(100);
            } else {
                // Normal content
                run.setFontFamily("Courier New");
                run.setFontSize(10);
                run.setText(line);
            }
        }
        
        // Add footer
        XWPFParagraph footerPara = document.createParagraph();
        footerPara.setAlignment(ParagraphAlignment.CENTER);
        footerPara.setSpacingBefore(400);
        footerPara.setBorderTop(Borders.SINGLE);
        XWPFRun footerRun = footerPara.createRun();
        footerRun.setText("UZIMA Borehole System - Confidential Report");
        footerRun.setFontSize(8);
        footerRun.setItalic(true);
        footerRun.setColor("808080");
        
        // Write to file
        FileOutputStream out = new FileOutputStream(filePath);
        document.write(out);
        out.close();
        document.close();
    }
    
    /**
     * Generate DOCX with custom table data
     * @param filePath Path where DOCX will be saved
     * @param reportTitle Title of the report
     * @param tableHeaders Array of column headers
     * @param tableData 2D array of table data
     * @throws Exception if DOCX generation fails
     */
    public static void generateTableDOCXReport(String filePath, String reportTitle, String[] tableHeaders, String[][] tableData) throws Exception {
        XWPFDocument document = new XWPFDocument();
        
        // Add title
        XWPFParagraph titlePara = document.createParagraph();
        titlePara.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun titleRun = titlePara.createRun();
        titleRun.setText("UZIMA BOREHOLE DRILLING COMPANY");
        titleRun.setBold(true);
        titleRun.setFontSize(18);
        
        // Add report title
        XWPFParagraph reportTitlePara = document.createParagraph();
        reportTitlePara.setAlignment(ParagraphAlignment.CENTER);
        reportTitlePara.setSpacingAfter(200);
        XWPFRun reportTitleRun = reportTitlePara.createRun();
        reportTitleRun.setText(reportTitle);
        reportTitleRun.setBold(true);
        reportTitleRun.setFontSize(14);
        
        // Create table
        XWPFTable table = document.createTable(tableData.length + 1, tableHeaders.length);
        table.setWidth("100%");
        
        // Add headers
        XWPFTableRow headerRow = table.getRow(0);
        for (int i = 0; i < tableHeaders.length; i++) {
            XWPFTableCell cell = headerRow.getCell(i);
            cell.setColor("1E3C72");
            XWPFParagraph para = cell.getParagraphs().get(0);
            para.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun run = para.createRun();
            run.setText(tableHeaders[i]);
            run.setBold(true);
            run.setColor("FFFFFF");
            run.setFontSize(11);
        }
        
        // Add data rows
        for (int i = 0; i < tableData.length; i++) {
            XWPFTableRow row = table.getRow(i + 1);
            for (int j = 0; j < tableData[i].length; j++) {
                XWPFTableCell cell = row.getCell(j);
                XWPFParagraph para = cell.getParagraphs().get(0);
                XWPFRun run = para.createRun();
                run.setText(tableData[i][j]);
                run.setFontSize(10);
            }
        }
        
        // Write to file
        FileOutputStream out = new FileOutputStream(filePath);
        document.write(out);
        out.close();
        document.close();
    }
}
