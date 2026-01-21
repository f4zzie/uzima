package com.uzima.report;

import com.uzima.model.Client;
import com.uzima.service.ClientServiceLayer;

import java.util.List;

/**
 * Generates reports for client data
 */
public class ClientReportGenerator {
    private ClientServiceLayer clientService;
    
    public ClientReportGenerator() {
        this.clientService = new ClientServiceLayer();
    }

    /**
     * Generate a report of all clients
     * @return Formatted report string
     */
    public String generateAllClientsReport() {
        List<Client> clients = clientService.getAllClients();
        
        StringBuilder report = new StringBuilder();
        report.append("═══════════════════════════════════════════════════════════════════\n");
        report.append("                     UZIMA CLIENT REPORT\n");
        report.append("═══════════════════════════════════════════════════════════════════\n\n");
        report.append(String.format("Total Clients: %d\n\n", clients.size()));
        report.append(String.format("%-15s %-25s %-25s %-15s %-12s\n", 
            "ID", "Name", "Email", "Phone", "Category"));
        report.append("─────────────────────────────────────────────────────────────────────\n");
        
        for (Client client : clients) {
            report.append(String.format("%-15s %-25s %-25s %-15s %-12s\n",
                client.getClientId(),
                truncate(client.getFullName(), 25),
                truncate(client.getEmail() != null ? client.getEmail() : "N/A", 25),
                client.getPhoneNumber(),
                client.getClientCategory()));
        }
        
        report.append("═══════════════════════════════════════════════════════════════════\n");
        return report.toString();
    }

    /**
     * Generate a report for clients by category
     * @return Formatted report string
     */
    public String generateClientsByCategoryReport() {
        StringBuilder report = new StringBuilder();
        report.append("═══════════════════════════════════════════════════════════════════\n");
        report.append("               UZIMA CLIENTS BY CATEGORY REPORT\n");
        report.append("═══════════════════════════════════════════════════════════════════\n\n");

        String[] categories = {"INDUSTRIAL", "COMMERCIAL", "DOMESTIC"};
        
        for (String category : categories) {
            List<Client> clients = clientService.getClientsByCategory(category);
            report.append(category).append(" (").append(clients.size()).append(" clients):\n");
            report.append("─────────────────────────────────────────────────────────────────────\n");
            
            for (Client client : clients) {
                report.append(String.format("  %-25s | %-25s | %s\n",
                    truncate(client.getFullName(), 25),
                    truncate(client.getEmail() != null ? client.getEmail() : "N/A", 25),
                    client.getPhoneNumber()));
            }
            report.append("\n");
        }
        
        report.append("═══════════════════════════════════════════════════════════════════\n");
        report.append("SUMMARY:\n");
        report.append(String.format("  Industrial:  %d clients\n", clientService.getClientCountByCategory("INDUSTRIAL")));
        report.append(String.format("  Commercial:  %d clients\n", clientService.getClientCountByCategory("COMMERCIAL")));
        report.append(String.format("  Domestic:    %d clients\n", clientService.getClientCountByCategory("DOMESTIC")));
        report.append(String.format("  TOTAL:       %d clients\n", clientService.getTotalClientCount()));
        report.append("═══════════════════════════════════════════════════════════════════\n");
        
        return report.toString();
    }

    /**
     * Generate a detailed client profile report
     * @param clientId The client ID
     * @return Formatted report string or error message
     */
    public String generateClientProfileReport(String clientId) {
        Client client = clientService.getClientById(clientId);
        
        if (client == null) {
            return "Client not found!";
        }
        
        StringBuilder report = new StringBuilder();
        report.append("═══════════════════════════════════════════════════════════════════\n");
        report.append("                   CLIENT PROFILE REPORT\n");
        report.append("═══════════════════════════════════════════════════════════════════\n\n");
        report.append(String.format("Client ID:              %s\n", client.getClientId()));
        report.append(String.format("Full Name:              %s\n", client.getFullName()));
        report.append(String.format("Phone Number:           %s\n", client.getPhoneNumber()));
        report.append(String.format("Phone Number 2:         %s\n", client.getPhoneNumber2() != null ? client.getPhoneNumber2() : "N/A"));
        report.append(String.format("Email:                  %s\n", client.getEmail() != null ? client.getEmail() : "N/A"));
        report.append(String.format("Physical Address:       %s\n", client.getPhysicalAddress() != null ? client.getPhysicalAddress() : "N/A"));
        report.append(String.format("Category:               %s\n", client.getClientCategory()));
        report.append(String.format("Borehole Location:      %s\n", client.getBoreholeLocation() != null ? client.getBoreholeLocation() : "N/A"));
        report.append(String.format("County:                 %s\n", client.getCounty() != null ? client.getCounty() : "N/A"));
        report.append(String.format("GPS Coordinates:        %s\n", client.getGpsCoordinates() != null ? client.getGpsCoordinates() : "N/A"));
        report.append(String.format("ID Number:              %s\n", client.getIdNumber() != null ? client.getIdNumber() : "N/A"));
        report.append(String.format("Registration Date:      %s\n", client.getRegistrationDate() != null ? client.getRegistrationDate() : "N/A"));
        report.append(String.format("Status:                 %s\n", client.isActive() ? "Active" : "Inactive"));
        report.append("═══════════════════════════════════════════════════════════════════\n");
        
        return report.toString();
    }

    /**
     * Helper method to truncate strings
     */
    private static String truncate(String str, int length) {
        if (str == null) return "";
        return str.length() > length ? str.substring(0, length - 3) + "..." : str;
    }
}
