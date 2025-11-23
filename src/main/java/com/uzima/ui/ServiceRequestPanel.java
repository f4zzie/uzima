package com.uzima.ui;

import com.uzima.database.ServiceRequestDAO;
import com.uzima.models.ServiceRequestSummary;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Panel that allows admins to review and approve service requests.
 */
public class ServiceRequestPanel extends JPanel {
    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private JTable requestTable;
    private DefaultTableModel tableModel;

    public ServiceRequestPanel() {
        initUI();
        loadRequests();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 246, 250));

        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        header.setBackground(new Color(12, 45, 97));

        JLabel title = new JLabel("Service Request Tasks");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Approve pending requests and schedule delivery dates");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(new Color(200, 200, 200));

        JPanel titleColumn = new JPanel();
        titleColumn.setLayout(new BoxLayout(titleColumn, BoxLayout.Y_AXIS));
        titleColumn.setBackground(new Color(12, 45, 97));
        titleColumn.add(title);
        titleColumn.add(Box.createVerticalStrut(6));
        titleColumn.add(subtitle);

        header.add(titleColumn, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        String[] columns = {"Request ID", "Client", "Status", "Requested", "Scheduled"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        requestTable = new JTable(tableModel);
        requestTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        requestTable.setRowHeight(30);
        requestTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        requestTable.getTableHeader().setBackground(new Color(33, 150, 243));
        requestTable.getTableHeader().setForeground(Color.WHITE);
        requestTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        requestTable.setSelectionBackground(new Color(33, 150, 243, 60));

        requestTable.getColumnModel().getColumn(2).setCellRenderer(new StatusCellRenderer());

        JScrollPane scrollPane = new JScrollPane(requestTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonBar.setBackground(new Color(245, 246, 250));

        JButton approveButton = new JButton("Approve & Schedule");
        approveButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        approveButton.setBackground(new Color(76, 175, 80));
        approveButton.setForeground(Color.WHITE);
        approveButton.setFocusPainted(false);
        approveButton.addActionListener(e -> approveRequest());

        JButton refreshButton = new JButton("Refresh");
        refreshButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        refreshButton.setBackground(new Color(33, 150, 243));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFocusPainted(false);
        refreshButton.addActionListener(e -> loadRequests());

        buttonBar.add(approveButton);
        buttonBar.add(refreshButton);
        add(buttonBar, BorderLayout.SOUTH);
    }

    private void loadRequests() {
        tableModel.setRowCount(0);
        List<ServiceRequestSummary> requests = ServiceRequestDAO.getAllServiceRequests();

        for (ServiceRequestSummary request : requests) {
            tableModel.addRow(new Object[]{
                    request.getRequestId(),
                    request.getClientName(),
                    request.getStatus(),
                    request.getRequestDate() != null ? request.getRequestDate().format(DISPLAY_FORMAT) : "-",
                    request.getScheduledDate() != null ? request.getScheduledDate().format(DISPLAY_FORMAT) : "Not Scheduled"
            });
        }
    }

    private void approveRequest() {
        int selectedRow = requestTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a service request to approve.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int requestId = (int) tableModel.getValueAt(selectedRow, 0);
        String currentStatus = (String) tableModel.getValueAt(selectedRow, 2);
        if ("Approved".equalsIgnoreCase(currentStatus)) {
            JOptionPane.showMessageDialog(this, "This request is already approved.", "Already Approved", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        LocalDateTime suggested = LocalDateTime.now().plusDays(2);
        String input = JOptionPane.showInputDialog(this,
                "Schedule date/time (YYYY-MM-DD HH:mm)",
                suggested.format(DISPLAY_FORMAT));
        if (input == null) {
            return;
        }

        LocalDateTime scheduled;
        try {
            scheduled = LocalDateTime.parse(input.trim(), DISPLAY_FORMAT);
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this,
                    "Please enter the date in the format YYYY-MM-DD HH:mm.",
                    "Invalid Date",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean updated = ServiceRequestDAO.approveServiceRequest(requestId, "Approved", scheduled);
        if (updated) {
            JOptionPane.showMessageDialog(this,
                    "Request approved and scheduled for " + scheduled.format(DISPLAY_FORMAT) + ".",
                    "Approved",
                    JOptionPane.INFORMATION_MESSAGE);
            loadRequests();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Failed to update request status. Please try again.",
                    "Update Failed",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private static class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setHorizontalAlignment(CENTER);
            label.setOpaque(true);

            if (value != null && "Pending".equalsIgnoreCase(value.toString())) {
                label.setBackground(new Color(255, 235, 59));
                label.setForeground(Color.BLACK);
            } else if (value != null && "Approved".equalsIgnoreCase(value.toString())) {
                label.setBackground(new Color(76, 175, 80));
                label.setForeground(Color.WHITE);
            } else {
                label.setBackground(new Color(200, 200, 200));
                label.setForeground(Color.BLACK);
            }

            return label;
        }
    }
}
