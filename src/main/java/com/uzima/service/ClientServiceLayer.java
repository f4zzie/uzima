package com.uzima.service;

import com.uzima.dao.ClientDAO;
import com.uzima.model.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Business logic layer for Client operations
 * Provides service methods with validation and business rules
 */
public class ClientServiceLayer {
    private static final Logger logger = LoggerFactory.getLogger(ClientServiceLayer.class);
    private ClientDAO clientDAO;
    
    public ClientServiceLayer() {
        this.clientDAO = new ClientDAO();
    }

    /**
     * Register a new client with validation
     */
    public boolean registerClient(Client client) {
        if (client.getFullName() == null || client.getFullName().trim().isEmpty()) {
            logger.error("Client name cannot be empty");
            return false;
        }
        
        if (client.getPhoneNumber() == null || client.getPhoneNumber().trim().isEmpty()) {
            logger.error("Phone number cannot be empty");
            return false;
        }

        if (client.getClientCategory() == null || client.getClientCategory().trim().isEmpty()) {
            logger.error("Client category cannot be empty");
            return false;
        }

        if (!isValidCategory(client.getClientCategory())) {
            logger.error("Invalid client category: " + client.getClientCategory());
            return false;
        }

        return clientDAO.addClient(client);
    }

    /**
     * Update client information
     */
    public boolean updateClient(Client client) {
        if (client.getClientId() == null || client.getClientId().trim().isEmpty()) {
            logger.error("Client ID cannot be empty for update");
            return false;
        }
        return clientDAO.updateClient(client);
    }

    /**
     * Get client by ID
     */
    public Client getClientById(String clientId) {
        return clientDAO.getClientById(clientId);
    }

    /**
     * Get all clients
     */
    public List<Client> getAllClients() {
        return clientDAO.getAllClients();
    }

    /**
     * Get clients by category
     */
    public List<Client> getClientsByCategory(String category) {
        return clientDAO.getClientsByCategory(category);
    }

    /**
     * Search clients by name or phone
     */
    public List<Client> searchClients(String searchTerm) {
        return clientDAO.searchClients(searchTerm);
    }

    /**
     * Delete a client
     */
    public boolean deleteClient(String clientId) {
        return clientDAO.deleteClient(clientId);
    }

    /**
     * Get total client count
     */
    public int getTotalClientCount() {
        return clientDAO.getTotalClientCount();
    }

    /**
     * Get client count by category
     */
    public int getClientCountByCategory(String category) {
        List<Client> clients = clientDAO.getClientsByCategory(category);
        return clients != null ? clients.size() : 0;
    }

    /**
     * Generate unique client ID
     */
    public String generateClientId(String category) {
        return clientDAO.generateClientId(category);
    }

    private static boolean isValidCategory(String category) {
        return "INDUSTRIAL".equalsIgnoreCase(category) ||
               "COMMERCIAL".equalsIgnoreCase(category) ||
               "DOMESTIC".equalsIgnoreCase(category);
    }
}
