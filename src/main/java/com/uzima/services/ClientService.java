package com.uzima.services;

import com.uzima.models.Client;
import com.uzima.database.ClientDAO;
import java.util.List;

/**
 * Business logic for Client operations
 * Handles authentication, registration, and profile management
 */
public class ClientService {

    /**
     * Verify a password against stored password (plain text)
     * @param password The plaintext password
     * @param storedPassword The stored password
     * @return true if password matches, false otherwise
     */
    private static boolean verifyPassword(String password, String storedPassword) {
        return password.equals(storedPassword);
    }

    /**
     * Register a new client with password
     * @param fullName Client's full name
     * @param email Client's email
     * @param phone Client's phone
     * @param address Client's address
     * @param category Client's category (Industrial, Commercial, Domestic)
     * @param boreholeLocation Location of the intended borehole
     * @param password Client's password (will be hashed)
     * @return true if registration successful, false otherwise
     */
    public static boolean registerClient(String fullName, String email, String phone, String address, String category, String boreholeLocation, String password) {
        // Validate inputs
        if (fullName == null || fullName.trim().isEmpty() ||
            email == null || email.trim().isEmpty() ||
            phone == null || phone.trim().isEmpty() ||
            password == null || password.trim().isEmpty() ||
            category == null || category.trim().isEmpty()) {
            System.err.println("Invalid input: required fields cannot be empty");
            return false;
        }

        // Check if email already exists
        if (ClientDAO.emailExists(email)) {
            System.err.println("Email already registered");
            return false;
        }

        // Validate category
        if (!isValidCategory(category)) {
            System.err.println("Invalid client category");
            return false;
        }

        Client client = new Client(fullName, email, phone, address, category, boreholeLocation);
        client.setPassword(password);  // Store password in plain text
        return ClientDAO.createClient(client);
    }

    /**
     * Authenticate a client with email and password
     * @param email Client's email
     * @param password Client's password (plaintext)
     * @return Client object if authentication successful, null otherwise
     */
    public static Client authenticateClient(String email, String password) {
        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            System.err.println("Email and password cannot be empty");
            return null;
        }

        Client client = ClientDAO.getClientByEmail(email);
        if (client == null) {
            System.err.println("Client not found");
            return null;
        }

        if (verifyPassword(password, client.getPassword())) {
            return client;
        }

        System.err.println("Invalid password");
        return null;
    }

    /**
     * Get a client by ID
     * @param clientId The client ID
     * @return Client object or null if not found
     */
    public static Client getClientById(int clientId) {
        return ClientDAO.getClientById(clientId);
    }

    /**
     * Get a client by email
     * @param email The client email
     * @return Client object or null if not found
     */
    public static Client getClientByEmail(String email) {
        return ClientDAO.getClientByEmail(email);
    }

    /**
     * Get all clients
     * @return List of all clients
     */
    public static List<Client> getAllClients() {
        return ClientDAO.getAllClients();
    }

    /**
     * Update client information
     * @param clientId The client ID
     * @param fullName Updated full name
     * @param phone Updated phone
     * @param address Updated address
     * @param boreholeLocation Updated borehole location
     * @return true if update successful, false otherwise
     */
    public static boolean updateClient(int clientId, String fullName, String phone, String address, String boreholeLocation) {
        Client client = ClientDAO.getClientById(clientId);
        if (client == null) {
            System.err.println("Client not found");
            return false;
        }

        if (fullName != null && !fullName.trim().isEmpty()) {
            client.setFullName(fullName);
        }
        if (phone != null && !phone.trim().isEmpty()) {
            client.setPhone(phone);
        }
        if (address != null && !address.trim().isEmpty()) {
            client.setAddress(address);
        }
        if (boreholeLocation != null && !boreholeLocation.trim().isEmpty()) {
            client.setBoreholeLocation(boreholeLocation);
        }

        return ClientDAO.updateClient(client);
    }

    /**
     * Delete a client
     * @param clientId The client ID
     * @return true if deletion successful, false otherwise
     */
    public static boolean deleteClient(int clientId) {
        return ClientDAO.deleteClient(clientId);
    }

    /**
     * Get clients by category
     * @param category The client category
     * @return List of clients in that category
     */
    public static List<Client> getClientsByCategory(String category) {
        if (!isValidCategory(category)) {
            System.err.println("Invalid client category");
            return null;
        }
        return ClientDAO.getClientsByCategory(category);
    }

    /**
     * Validate client category
     */
    private static boolean isValidCategory(String category) {
        return category.equals("Industrial") || category.equals("Commercial") || category.equals("Domestic");
    }

    /**
     * Get total number of clients
     * @return Number of clients
     */
    public static int getTotalClientCount() {
        return ClientDAO.getAllClients().size();
    }

    /**
     * Get client count by category
     * @param category The client category
     * @return Number of clients in that category
     */
    public static int getClientCountByCategory(String category) {
        return ClientDAO.getClientsByCategory(category).size();
    }
}
