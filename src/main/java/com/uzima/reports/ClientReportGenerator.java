package com.uzima.reports;

import com.uzima.models.Client;
import com.uzima.services.ClientService;
import java.util.List;

/**
 * Generates reports for client data
 */
public class ClientReportGenerator {

    /**
     * Generate a report of all clients
     * @return Formatted report string
     */
    public static String generateAllClientsReport() {
        List<Client> clients = ClientService.getAllClients();
        
        StringBuilder report = new StringBuilder();
        report.append("═══════════════════════════════════════════════════════════\n");
        report.append("                 UZIMA CLIENT REPORT\n");
        report.append("═══════════════════════════════════════════════════════════\n\n");
        report.append(String.format("Total Clients: %d\n\n", clients.size()));
        report.append(String.format("%-5s %-20s %-25s %-15s %-12s\n", 
            "ID", "Name", "Email", "Phone", "Category"));
        report.append("─────────────────────────────────────────────────────────────\n");
        
        for (Client client : clients) {
            report.append(String.format("%-5d %-20s %-25s %-15s %-12s\n",
                client.getClientId(),
                client.getFullName(),
                client.getEmail(),
                client.getPhone(),
                client.getCategory()));
        }
        
        report.append("═══════════════════════════════════════════════════════════\n");
        return report.toString();
    }

    /**
     * Generate a report for clients by category
     * @return Formatted report string
     */
    public static String generateClientsByCategoryReport() {
        StringBuilder report = new StringBuilder();
        report.append("═══════════════════════════════════════════════════════════\n");
        report.append("          UZIMA CLIENTS BY CATEGORY REPORT\n");
        report.append("═══════════════════════════════════════════════════════════\n\n");

        String[] categories = {"Industrial", "Commercial", "Domestic"};
        
        for (String category : categories) {
            List<Client> clients = ClientService.getClientsByCategory(category);
            report.append(category).append(" (").append(clients.size()).append(" clients):\n");
            report.append("─────────────────────────────────────────────────────────────\n");
            
            for (Client client : clients) {
                report.append(String.format("  %-20s | %s | %s\n",
                    client.getFullName(),
                    client.getEmail(),
                    client.getPhone()));
            }
            report.append("\n");
        }
        
        report.append("═══════════════════════════════════════════════════════════\n");
        report.append("SUMMARY:\n");
        report.append(String.format("  Industrial:  %d clients\n", ClientService.getClientCountByCategory("Industrial")));
        report.append(String.format("  Commercial:  %d clients\n", ClientService.getClientCountByCategory("Commercial")));
        report.append(String.format("  Domestic:    %d clients\n", ClientService.getClientCountByCategory("Domestic")));
        report.append(String.format("  TOTAL:       %d clients\n", ClientService.getTotalClientCount()));
        report.append("═══════════════════════════════════════════════════════════\n");
        
        return report.toString();
    }

    /**
     * Generate a detailed client profile report
     * @param clientId The client ID
     * @return Formatted report string or error message
     */
    public static String generateClientProfileReport(int clientId) {
        Client client = ClientService.getClientById(clientId);
        
        if (client == null) {
            return "Client not found!";
        }
        
        StringBuilder report = new StringBuilder();
        report.append("═══════════════════════════════════════════════════════════\n");
        report.append("                 CLIENT PROFILE REPORT\n");
        report.append("═══════════════════════════════════════════════════════════\n\n");
        report.append(String.format("Client ID:              %d\n", client.getClientId()));
        report.append(String.format("Full Name:              %s\n", client.getFullName()));
        report.append(String.format("Email:                  %s\n", client.getEmail()));
        report.append(String.format("Phone:                  %s\n", client.getPhone()));
        report.append(String.format("Address:                %s\n", client.getAddress()));
        report.append(String.format("Category:               %s\n", client.getCategory()));
        report.append(String.format("Borehole Location:      %s\n", client.getBoreholeLocation()));
        report.append(String.format("Registered Date:        %s\n", client.getCreatedAt()));
        report.append(String.format("Last Updated:           %s\n", client.getUpdatedAt()));
        report.append("═══════════════════════════════════════════════════════════\n");
        
        return report.toString();
    }
}
