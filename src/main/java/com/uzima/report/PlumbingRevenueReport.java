package com.uzima.report;

import com.uzima.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

/**
 * Report generator for plumbing services revenue
 * As per requirement (c)(ii): Total amount charged on plumbing
 */
public class PlumbingRevenueReport {
    private static final Logger logger = LoggerFactory.getLogger(PlumbingRevenueReport.class);
    
    /**
     * Generate comprehensive plumbing revenue report
     * Shows total plumbing charges from all clients
     */
    public String generatePlumbingRevenueReport() {
        StringBuilder report = new StringBuilder();
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            report.append("╔═══════════════════════════════════════════════════════════════════╗\n");
            report.append("║           UZIMA BOREHOLE - PLUMBING REVENUE REPORT                ║\n");
            report.append("╚═══════════════════════════════════════════════════════════════════╝\n\n");
            
            // Overall plumbing statistics
            String overallSql = "SELECT " +
                    "COUNT(*) as total_transactions, " +
                    "SUM(plumbing_cost) as total_plumbing_revenue, " +
                    "AVG(plumbing_cost) as avg_plumbing_cost, " +
                    "MIN(plumbing_cost) as min_plumbing_cost, " +
                    "MAX(plumbing_cost) as max_plumbing_cost " +
                    "FROM transactions WHERE has_plumbing = TRUE";
            
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(overallSql);
            
            if (rs.next()) {
                report.append("OVERALL PLUMBING STATISTICS:\n");
                report.append("─────────────────────────────────────────────────────────────────────\n");
                report.append(String.format("  Total Transactions with Plumbing:   %d\n", rs.getInt("total_transactions")));
                report.append(String.format("  Total Plumbing Revenue:             KSh %,.2f\n", rs.getDouble("total_plumbing_revenue")));
                report.append(String.format("  Average Plumbing Cost:              KSh %,.2f\n", rs.getDouble("avg_plumbing_cost")));
                report.append(String.format("  Minimum Plumbing Cost:              KSh %,.2f\n", rs.getDouble("min_plumbing_cost")));
                report.append(String.format("  Maximum Plumbing Cost:              KSh %,.2f\n", rs.getDouble("max_plumbing_cost")));
                report.append("\n");
            }
            
            // Plumbing revenue by client category
            String categorySql = "SELECT " +
                    "c.client_category, " +
                    "COUNT(*) as transaction_count, " +
                    "SUM(t.plumbing_cost) as category_plumbing_revenue " +
                    "FROM transactions t " +
                    "JOIN clients c ON t.client_id = c.client_id " +
                    "WHERE t.has_plumbing = TRUE " +
                    "GROUP BY c.client_category " +
                    "ORDER BY category_plumbing_revenue DESC";
            
            rs = stmt.executeQuery(categorySql);
            
            report.append("PLUMBING REVENUE BY CLIENT CATEGORY:\n");
            report.append("─────────────────────────────────────────────────────────────────────\n");
            report.append(String.format("  %-15s %-20s %s\n", "Category", "Transactions", "Total Revenue"));
            report.append("  ────────────────────────────────────────────────────────────────────\n");
            
            while (rs.next()) {
                String category = rs.getString("client_category");
                int count = rs.getInt("transaction_count");
                double revenue = rs.getDouble("category_plumbing_revenue");
                report.append(String.format("  %-15s %-20d KSh %,.2f\n", category, count, revenue));
            }
            report.append("\n");
            
            // Detailed plumbing details (pipe types, etc.)
            String detailSql = "SELECT " +
                    "pt.pipe_name, " +
                    "COUNT(*) as usage_count, " +
                    "SUM(pd.pipe_length) as total_length, " +
                    "AVG(pd.pipe_diameter) as avg_diameter, " +
                    "SUM(pd.number_of_outlets) as total_outlets, " +
                    "SUM(pd.cost) as total_cost " +
                    "FROM plumbing_details pd " +
                    "JOIN pipe_types pt ON pd.pipe_type_id = pt.pipe_type_id " +
                    "GROUP BY pt.pipe_name " +
                    "ORDER BY total_cost DESC";
            
            rs = stmt.executeQuery(detailSql);
            
            report.append("PLUMBING DETAILS BY PIPE TYPE:\n");
            report.append("─────────────────────────────────────────────────────────────────────\n");
            report.append(String.format("  %-15s %-10s %-15s %-15s %-15s %s\n", 
                    "Pipe Type", "Jobs", "Total Length(m)", "Avg Diameter", "Total Outlets", "Revenue"));
            report.append("  ────────────────────────────────────────────────────────────────────\n");
            
            while (rs.next()) {
                String pipeType = rs.getString("pipe_name");
                int count = rs.getInt("usage_count");
                double totalLength = rs.getDouble("total_length");
                double avgDiameter = rs.getDouble("avg_diameter");
                int totalOutlets = rs.getInt("total_outlets");
                double cost = rs.getDouble("total_cost");
                
                report.append(String.format("  %-15s %-10d %-15.2f %-15.2f %-15d KSh %,.2f\n", 
                        pipeType, count, totalLength, avgDiameter, totalOutlets, cost));
            }
            report.append("\n");
            
            // Recent plumbing jobs
            String recentSql = "SELECT " +
                    "t.transaction_no, " +
                    "t.transaction_date, " +
                    "c.full_name, " +
                    "c.client_category, " +
                    "t.plumbing_cost " +
                    "FROM transactions t " +
                    "JOIN clients c ON t.client_id = c.client_id " +
                    "WHERE t.has_plumbing = TRUE " +
                    "ORDER BY t.transaction_date DESC " +
                    "LIMIT 10";
            
            rs = stmt.executeQuery(recentSql);
            
            report.append("RECENT PLUMBING JOBS (Last 10):\n");
            report.append("─────────────────────────────────────────────────────────────────────\n");
            report.append(String.format("  %-18s %-12s %-25s %-12s %s\n", 
                    "Transaction No", "Date", "Client", "Category", "Cost"));
            report.append("  ────────────────────────────────────────────────────────────────────\n");
            
            while (rs.next()) {
                String txnNo = rs.getString("transaction_no");
                Date date = rs.getDate("transaction_date");
                String clientName = rs.getString("full_name");
                String category = rs.getString("client_category");
                double cost = rs.getDouble("plumbing_cost");
                
                report.append(String.format("  %-18s %-12tF %-25s %-12s KSh %,.2f\n", 
                        txnNo, date, clientName, category, cost));
            }
            report.append("\n");
            
            report.append("═══════════════════════════════════════════════════════════════════\n");
            report.append("Note: Plumbing costs calculated as:\n");
            report.append("      (Pipe cost per meter × length) + (Diameter × 500) + (Outlets × 2,000)\n");
            report.append("═══════════════════════════════════════════════════════════════════\n");
            
        } catch (SQLException e) {
            logger.error("Error generating plumbing revenue report", e);
            report.append("\nError generating report: ").append(e.getMessage());
        }
        
        return report.toString();
    }
    
    /**
     * Get total plumbing revenue (quick summary)
     */
    public double getTotalPlumbingRevenue() {
        String sql = "SELECT SUM(plumbing_cost) as total FROM transactions WHERE has_plumbing = TRUE";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (SQLException e) {
            logger.error("Error fetching total plumbing revenue", e);
        }
        
        return 0.0;
    }
}
