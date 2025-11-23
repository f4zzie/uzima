package com.uzima.services;

import com.uzima.database.ServiceDAO;
import com.uzima.models.DrillingService;
import com.uzima.models.PumpService;
import java.util.List;

/**
 * Business logic for Service management
 */
public class ServiceManagementService {

    /**
     * Get all available drilling services
     * @return List of drilling services
     */
    public static List<DrillingService> getAllDrillingServices() {
        return ServiceDAO.getAllDrillingServices();
    }

    /**
     * Get drilling service details by type
     * @param serviceType The drilling service type
     * @return DrillingService object or null
     */
    public static DrillingService getDrillingServiceByType(String serviceType) {
        return ServiceDAO.getDrillingServiceByType(serviceType);
    }

    /**
     * Get drilling down payment fee
     * @param serviceType The drilling service type
     * @return Down payment fee or 0 if not found
     */
    public static double getDrillingDownPaymentFee(String serviceType) {
        DrillingService service = ServiceDAO.getDrillingServiceByType(serviceType);
        return service != null ? service.getDownPaymentFee() : 0;
    }

    /**
     * Get all available pump services
     * @return List of pump services
     */
    public static List<PumpService> getAllPumpServices() {
        return ServiceDAO.getAllPumpServices();
    }

    /**
     * Get pump service details by type
     * @param pumpType The pump type
     * @return PumpService object or null
     */
    public static PumpService getPumpServiceByType(String pumpType) {
        return ServiceDAO.getPumpServiceByType(pumpType);
    }

    /**
     * Get pump installation cost
     * @param pumpType The pump type
     * @return Installation cost or 0 if not found
     */
    public static double getPumpCost(String pumpType) {
        PumpService service = ServiceDAO.getPumpServiceByType(pumpType);
        return service != null ? service.getCost() : 0;
    }

    /**
     * Get depth/height charge for a specific depth
     * @param depth The depth in meters
     * @return Cost per meter for that depth
     */
    public static double getDepthHeightCharge(int depth) {
        return ServiceDAO.getDepthHeightCharge(depth);
    }

    /**
     * Get fees for a client category
     * @param category The client category
     * @return Array with [surveyFee, localAuthorityFee]
     */
    public static double[] getFeesByCategory(String category) {
        return ServiceDAO.getFeesByCategory(category);
    }

    /**
     * Get survey fee for a client category
     * @param category The client category
     * @return Survey fee amount
     */
    public static double getSurveyFee(String category) {
        double[] fees = ServiceDAO.getFeesByCategory(category);
        return fees.length > 0 ? fees[0] : 0;
    }

    /**
     * Get local authority fee for a client category
     * @param category The client category
     * @return Local authority fee amount
     */
    public static double getLocalAuthorityFee(String category) {
        double[] fees = ServiceDAO.getFeesByCategory(category);
        return fees.length > 1 ? fees[1] : 0;
    }
}
