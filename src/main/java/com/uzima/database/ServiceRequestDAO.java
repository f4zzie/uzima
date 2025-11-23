package com.uzima.database;

import com.uzima.models.ServiceRequestData;
import com.uzima.models.ServiceRequestSummary;
import com.uzima.models.DrillingService;
import com.uzima.models.PumpService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for storing service request records for invoices
 */
public class ServiceRequestDAO {

    /**
     * Persist the service request data and return the generated request ID
     */
    public static int createServiceRequest(ServiceRequestData data, int clientId) {
        String sql = "INSERT INTO service_requests (client_id, drilling_id, pump_id, depth_meters, height_meters, status) VALUES (?, ?, ?, ?, ?, ?)";
        DrillingService drilling = null;
        PumpService pump = null;

        if (data.drillingType != null) {
            drilling = ServiceDAO.getDrillingServiceByType(data.drillingType);
        }
        if (data.pumpType != null) {
            pump = ServiceDAO.getPumpServiceByType(data.pumpType);
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, clientId);

            if (drilling != null) {
                pstmt.setInt(2, drilling.getDrillingId());
            } else {
                pstmt.setNull(2, Types.INTEGER);
            }

            if (pump != null) {
                pstmt.setInt(3, pump.getPumpId());
            } else {
                pstmt.setNull(3, Types.INTEGER);
            }

            pstmt.setInt(4, Math.max(0, data.depthMeters));
            pstmt.setInt(5, Math.max(0, data.heightMeters));
            pstmt.setString(6, "Pending");

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                return -1;
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int requestId = generatedKeys.getInt(1);
                    data.requestId = requestId;
                    return requestId;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error creating service request: " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    public static List<ServiceRequestSummary> getAllServiceRequests() {
        String sql = "SELECT sr.request_id, sr.client_id, c.full_name, sr.status, sr.request_date, sr.scheduled_date " +
                "FROM service_requests sr " +
                "JOIN clients c ON sr.client_id = c.client_id " +
                "ORDER BY sr.request_date DESC";
        List<ServiceRequestSummary> requests = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                int requestId = rs.getInt("request_id");
                int clientId = rs.getInt("client_id");
                String clientName = rs.getString("full_name");
                String status = rs.getString("status");
                Timestamp requestTs = rs.getTimestamp("request_date");
                Timestamp scheduledTs = rs.getTimestamp("scheduled_date");

                LocalDateTime requestDate = requestTs != null ? requestTs.toLocalDateTime() : null;
                LocalDateTime scheduledDate = scheduledTs != null ? scheduledTs.toLocalDateTime() : null;

                requests.add(new ServiceRequestSummary(requestId, clientId, clientName, status, requestDate, scheduledDate));
            }
        } catch (SQLException e) {
            System.err.println("Error loading service requests: " + e.getMessage());
            e.printStackTrace();
        }

        return requests;
    }

    public static boolean approveServiceRequest(int requestId, String status, LocalDateTime scheduledDate) {
        String sql = "UPDATE service_requests SET status = ?, scheduled_date = ? WHERE request_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);
            if (scheduledDate != null) {
                pstmt.setTimestamp(2, Timestamp.valueOf(scheduledDate));
            } else {
                pstmt.setNull(2, Types.TIMESTAMP);
            }
            pstmt.setInt(3, requestId);

            return pstmt.executeUpdate() == 1;
        } catch (SQLException e) {
            System.err.println("Error updating service request status: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
}
