package com.ecommerce.controller;

import com.ecommerce.dto.*;
import com.ecommerce.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "*", maxAge = 3600)
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private AdminService adminService;

    /**
     * Get dashboard statistics
     * GET /api/admin/dashboard/stats
     */
    @GetMapping("/dashboard/stats")
    public ResponseEntity<?> getDashboardStats() {
        try {
            DashboardStatsDTO stats = adminService.getDashboardStats();
            return new ResponseEntity<>(stats, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get sales report
     * GET /api/admin/reports/sales
     */
    @GetMapping("/reports/sales")
    public ResponseEntity<?> getSalesReport() {
        try {
            List<SalesReportDTO> report = adminService.getSalesReport();
            return new ResponseEntity<>(report, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get inventory report
     * GET /api/admin/reports/inventory
     */
    @GetMapping("/reports/inventory")
    public ResponseEntity<?> getInventoryReport() {
        try {
            List<InventoryReportDTO> report = adminService.getInventoryReport();
            return new ResponseEntity<>(report, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get revenue by date
     * GET /api/admin/reports/revenue?days=30
     */
    @GetMapping("/reports/revenue")
    public ResponseEntity<?> getRevenueByDate(@RequestParam(defaultValue = "30") int days) {
        try {
            List<RevenueByDateDTO> report = adminService.getRevenueByDate(days);
            return new ResponseEntity<>(report, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get top selling products
     * GET /api/admin/reports/top-products?limit=10
     */
    @GetMapping("/reports/top-products")
    public ResponseEntity<?> getTopSellingProducts(@RequestParam(defaultValue = "10") int limit) {
        try {
            List<ProductDTO> products = adminService.getTopSellingProducts(limit);
            return new ResponseEntity<>(products, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get low stock products
     * GET /api/admin/reports/low-stock?threshold=10
     */
    @GetMapping("/reports/low-stock")
    public ResponseEntity<?> getLowStockProducts(@RequestParam(defaultValue = "10") int threshold) {
        try {
            List<InventoryReportDTO> products = adminService.getLowStockProducts(threshold);
            return new ResponseEntity<>(products, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get customer statistics
     * GET /api/admin/reports/customers
     */
    @GetMapping("/reports/customers")
    public ResponseEntity<?> getCustomerStats() {
        try {
            Map<String, Object> stats = adminService.getCustomerStats();
            return new ResponseEntity<>(stats, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Export sales report as CSV (placeholder)
     * GET /api/admin/export/sales
     */
    @GetMapping("/export/sales")
    public ResponseEntity<?> exportSalesReport() {
        try {
            List<SalesReportDTO> report = adminService.getSalesReport();
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Export functionality would generate CSV file");
            response.put("recordCount", report.size());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Export inventory report as CSV (placeholder)
     * GET /api/admin/export/inventory
     */
    @GetMapping("/export/inventory")
    public ResponseEntity<?> exportInventoryReport() {
        try {
            List<InventoryReportDTO> report = adminService.getInventoryReport();
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Export functionality would generate CSV file");
            response.put("recordCount", report.size());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
