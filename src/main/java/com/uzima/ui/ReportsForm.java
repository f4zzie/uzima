package com.uzima.ui;

import com.uzima.report.ClientReportGenerator;
import com.uzima.report.CompanyRevenueReport;
import com.uzima.report.PlumbingRevenueReport;
import com.uzima.report.ServiceRevenueReport;
import com.uzima.report.TaxReport;
import com.uzima.report.PDFReportGenerator;
import com.uzima.report.DOCXReportGenerator;
import com.uzima.util.UIHelper;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.print.PrinterException;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Reports Form - Generate and display various reports
 */
public class ReportsForm extends JFrame {
    private JTextArea txtReportDisplay;
    private JComboBox<String> cmbReportType;
    private JButton btnGenerate, btnPrint, btnExportPDF, btnExportWord, btnClose;
    private ClientReportGenerator clientReportGen;
    private CompanyRevenueReport companyReportGen;
    private ServiceRevenueReport serviceReportGen;
    private TaxReport taxReportGen;
    private PlumbingRevenueReport plumbingReportGen;
    private String currentReportContent = "";
    private String currentReportTitle = "";
    
    public ReportsForm() {
        setTitle("Reports - UZIMA Borehole System");
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        clientReportGen = new ClientReportGenerator();
        companyReportGen = new CompanyRevenueReport();
        serviceReportGen = new ServiceRevenueReport();
        taxReportGen = new TaxReport();
        plumbingReportGen = new PlumbingRevenueReport();
        
        initComponents();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        
        // Header Panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Center Panel - Report Display
        JPanel centerPanel = createCenterPanel();
        add(centerPanel, BorderLayout.CENTER);
        
        // Bottom Panel - Actions
        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setBackground(UIHelper.PRIMARY_COLOR);
        
        JLabel lblTitle = new JLabel("REPORT GENERATOR");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(lblTitle, BorderLayout.NORTH);
        
        // Report Selection Panel
        JPanel selectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        selectionPanel.setBackground(UIHelper.PRIMARY_COLOR);
        
        JLabel lblReportType = new JLabel("Select Report:");
        lblReportType.setForeground(Color.WHITE);
        lblReportType.setFont(new Font("Segoe UI", Font.BOLD, 14));
        selectionPanel.add(lblReportType);
        
        cmbReportType = new JComboBox<>(new String[]{
            "All Clients Report",
            "Clients by Category",
            "Financial Report",
            "Fee Collection Report",
            "Payment Status Summary",
            "Service Revenue Report",
            "Plumbing Revenue Report",
            "Average Transaction Report",
            "Tax Report"
        });
        cmbReportType.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cmbReportType.setPreferredSize(new Dimension(250, 30));
        selectionPanel.add(cmbReportType);
        
        btnGenerate = new JButton("Generate Report");
        UIHelper.styleSuccessButton(btnGenerate);
        btnGenerate.addActionListener(e -> generateReport());
        selectionPanel.add(btnGenerate);
        
        panel.add(selectionPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel lblInstructions = new JLabel("Select a report type and click 'Generate Report'");
        lblInstructions.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblInstructions.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(lblInstructions, BorderLayout.NORTH);
        
        txtReportDisplay = new JTextArea();
        txtReportDisplay.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtReportDisplay.setEditable(false);
        txtReportDisplay.setLineWrap(false);
        txtReportDisplay.setText("No report generated yet.\n\nAvailable Reports:\n" +
                "• All Clients Report - Complete list of all registered clients\n" +
                "• Clients by Category - Breakdown by Industrial/Commercial/Domestic\n" +
                "• Financial Report - Comprehensive company financial overview\n" +
                "• Fee Collection Report - Survey and authority fees summary\n" +
                "• Payment Status Summary - Payment status breakdown\n" +
                "• Service Revenue Report - Revenue by service type\n" +
                "• Average Transaction Report - Average transaction values\n" +
                "• Tax Report - Tax collection summary (16% VAT)");
        
        JScrollPane scrollPane = new JScrollPane(txtReportDisplay);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Report Output"));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        
        btnPrint = new JButton("Print Report");
        UIHelper.stylePrimaryButton(btnPrint);
        btnPrint.addActionListener(e -> printReport());
        btnPrint.setEnabled(false);
        panel.add(btnPrint);
        
        btnExportPDF = new JButton("Export PDF");
        UIHelper.styleSecondaryButton(btnExportPDF);
        btnExportPDF.addActionListener(e -> exportPDF());
        btnExportPDF.setEnabled(false);
        panel.add(btnExportPDF);
        
        btnExportWord = new JButton("Export Word");
        UIHelper.styleSecondaryButton(btnExportWord);
        btnExportWord.addActionListener(e -> exportWord());
        btnExportWord.setEnabled(false);
        panel.add(btnExportWord);
        
        btnClose = new JButton("Close");
        UIHelper.styleDangerButton(btnClose);
        btnClose.addActionListener(e -> dispose());
        panel.add(btnClose);
        
        return panel;
    }
    
    private void generateReport() {
        String reportType = (String) cmbReportType.getSelectedItem();
        String reportContent = "";
        
        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            
            switch (reportType) {
                case "All Clients Report":
                    reportContent = clientReportGen.generateAllClientsReport();
                    break;
                    
                case "Clients by Category":
                    reportContent = clientReportGen.generateClientsByCategoryReport();
                    break;
                    
                case "Financial Report":
                    reportContent = companyReportGen.generateComprehensiveFinancialReport();
                    break;
                    
                case "Fee Collection Report":
                    reportContent = companyReportGen.generateFeeCollectionReport();
                    break;
                    
                case "Payment Status Summary":
                    reportContent = companyReportGen.generatePaymentStatusReport();
                    break;
                    
                case "Service Revenue Report":
                    reportContent = serviceReportGen.generateServiceRevenueReport();
                    break;
                    
                case "Plumbing Revenue Report":
                    reportContent = plumbingReportGen.generatePlumbingRevenueReport();
                    break;
                    
                case "Average Transaction Report":
                    reportContent = serviceReportGen.generateAverageTransactionReport();
                    break;
                    
                case "Tax Report":
                    reportContent = taxReportGen.generateTaxReport();
                    break;
                    
                default:
                    reportContent = "Report type not implemented yet.";
            }
            
            // Store current report data
            currentReportContent = reportContent;
            currentReportTitle = reportType;
            
            // Add timestamp header
            String timestamp = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());
            String finalReport = "Generated: " + timestamp + "\n\n" + reportContent;
            
            txtReportDisplay.setText(finalReport);
            txtReportDisplay.setCaretPosition(0);
            btnPrint.setEnabled(true);
            btnExportPDF.setEnabled(true);
            btnExportWord.setEnabled(true);
            
            UIHelper.showSuccess(this, "Report generated successfully!");
            
        } catch (Exception e) {
            UIHelper.showError(this, "Error generating report: " + e.getMessage());
            e.printStackTrace();
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }
    
    private void printReport() {
        try {
            boolean complete = txtReportDisplay.print();
            if (complete) {
                UIHelper.showSuccess(this, "Report sent to printer");
            } else {
                UIHelper.showInfo(this, "Printing cancelled");
            }
        } catch (PrinterException e) {
            UIHelper.showError(this, "Error printing report: " + e.getMessage());
        }
    }
    
    private void exportPDF() {
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save Report as PDF");
            fileChooser.setFileFilter(new FileNameExtensionFilter("PDF files (*.pdf)", "pdf"));
            
            // Suggest filename
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String suggestedName = currentReportTitle.replaceAll("[^a-zA-Z0-9]", "_") + "_" + timestamp + ".pdf";
            fileChooser.setSelectedFile(new File(suggestedName));
            
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                
                // Ensure .pdf extension
                if (!file.getName().toLowerCase().endsWith(".pdf")) {
                    file = new File(file.getAbsolutePath() + ".pdf");
                }
                
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                
                // Generate PDF
                PDFReportGenerator.generatePDFReport(
                    file.getAbsolutePath(),
                    currentReportTitle,
                    currentReportContent,
                    currentReportTitle
                );
                
                setCursor(Cursor.getDefaultCursor());
                
                int result = JOptionPane.showConfirmDialog(
                    this,
                    "PDF report saved successfully!\n\nFile: " + file.getName() + "\n\nDo you want to open it?",
                    "Export Successful",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.INFORMATION_MESSAGE
                );
                
                if (result == JOptionPane.YES_OPTION) {
                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().open(file);
                    }
                }
            }
        } catch (Exception e) {
            setCursor(Cursor.getDefaultCursor());
            UIHelper.showError(this, "Error exporting PDF: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void exportWord() {
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save Report as Word Document");
            fileChooser.setFileFilter(new FileNameExtensionFilter("Word files (*.docx)", "docx"));
            
            // Suggest filename
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String suggestedName = currentReportTitle.replaceAll("[^a-zA-Z0-9]", "_") + "_" + timestamp + ".docx";
            fileChooser.setSelectedFile(new File(suggestedName));
            
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                
                // Ensure .docx extension
                if (!file.getName().toLowerCase().endsWith(".docx")) {
                    file = new File(file.getAbsolutePath() + ".docx");
                }
                
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                
                // Generate Word document
                DOCXReportGenerator.generateDOCXReport(
                    file.getAbsolutePath(),
                    currentReportTitle,
                    currentReportContent,
                    currentReportTitle
                );
                
                setCursor(Cursor.getDefaultCursor());
                
                int result = JOptionPane.showConfirmDialog(
                    this,
                    "Word report saved successfully!\n\nFile: " + file.getName() + "\n\nDo you want to open it?",
                    "Export Successful",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.INFORMATION_MESSAGE
                );
                
                if (result == JOptionPane.YES_OPTION) {
                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().open(file);
                    }
                }
            }
        } catch (Exception e) {
            setCursor(Cursor.getDefaultCursor());
            UIHelper.showError(this, "Error exporting Word document: " + e.getMessage());
            e.printStackTrace();
        }
    }
}


