# Admin Dashboard Guide

## Overview

The Admin Dashboard provides comprehensive analytics, reporting, and management tools for e-commerce administrators.

## Features

### 1. Dashboard Statistics
**Endpoint:** `GET /api/admin/dashboard/stats`

Returns:
- Total users
- Total orders
- Total products
- Total categories
- Total revenue
- Average order value
- Order status breakdown (Pending, Processing, Shipped, Delivered, Cancelled)

### 2. Sales Report
**Endpoint:** `GET /api/admin/reports/sales`

Includes:
- Order ID
- Customer name and email
- Order total amount
- Order status
- Payment status
- Order creation date

### 3. Inventory Report
**Endpoint:** `GET /api/admin/reports/inventory`

Shows:
- Product name and ID
- Category
- Current stock level
- Low stock indicator
- Product rating

### 4. Revenue Analysis
**Endpoint:** `GET /api/admin/reports/revenue?days=30`

Displays:
- Daily revenue for specified period (default: 30 days)
- Order count per day
- Revenue trend

### 5. Top Selling Products
**Endpoint:** `GET /api/admin/reports/top-products?limit=10`

Shows:
- Best performing products
- Sales volume
- Product details

### 6. Low Stock Alerts
**Endpoint:** `GET /api/admin/reports/low-stock?threshold=10`

Identifies:
- Products below stock threshold
- Current inventory level
- Product details for quick reordering

### 7. Customer Statistics
**Endpoint:** `GET /api/admin/reports/customers`

Provides:
- Total customers
- Active customers
- Customers with orders
- Email verified customers

## Export Features

### Export Sales Report
**Endpoint:** `GET /api/admin/export/sales`

Generates CSV file with all sales data.

### Export Inventory Report
**Endpoint:** `GET /api/admin/export/inventory`

Generates CSV file with inventory data.

## API Examples

### Get Dashboard Statistics
```bash
curl -X GET "http://localhost:8080/api/admin/dashboard/stats" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN"
```

Response:
```json
{
  "totalUsers": 50,
  "totalOrders": 150,
  "totalProducts": 200,
  "totalCategories": 10,
  "totalRevenue": 15000.00,
  "averageOrderValue": 100.00,
  "pendingOrders": 5,
  "processingOrders": 10,
  "shippedOrders": 20,
  "deliveredOrders": 110,
  "cancelledOrders": 5
}
```

### Get Sales Report
```bash
curl -X GET "http://localhost:8080/api/admin/reports/sales" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN"
```

Response:
```json
[
  {
    "orderId": 1,
    "customerName": "John Doe",
    "customerEmail": "john@example.com",
    "totalAmount": 299.99,
    "orderStatus": "DELIVERED",
    "paymentStatus": "COMPLETED",
    "createdAt": "2026-09-02T10:30:00"
  },
  ...
]
```

### Get Revenue by Date
```bash
curl -X GET "http://localhost:8080/api/admin/reports/revenue?days=7" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN"
```

Response:
```json
[
  {
    "date": "2026-08-27",
    "revenue": 1500.00,
    "orderCount": 5
  },
  {
    "date": "2026-08-28",
    "revenue": 2000.00,
    "orderCount": 8
  }
]
```

### Get Top Selling Products
```bash
curl -X GET "http://localhost:8080/api/admin/reports/top-products?limit=5" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN"
```

### Get Low Stock Products
```bash
curl -X GET "http://localhost:8080/api/admin/reports/low-stock?threshold=10" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN"
```

Response:
```json
[
  {
    "productId": 1,
    "productName": "Laptop",
    "categoryName": "Electronics",
    "currentStock": 3,
    "lowStockThreshold": 10,
    "lowStock": true,
    "rating": 4.5
  }
]
```

## Frontend Integration (React Example)

```javascript
import React, { useState, useEffect } from 'react';
import axios from 'axios';

function AdminDashboard() {
  const [stats, setStats] = useState(null);
  const [salesReport, setSalesReport] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const token = localStorage.getItem('adminToken');
    
    // Fetch dashboard stats
    axios.get('http://localhost:8080/api/admin/dashboard/stats', {
      headers: { 'Authorization': `Bearer ${token}` }
    }).then(res => {
      setStats(res.data);
      setLoading(false);
    }).catch(err => console.error(err));

    // Fetch sales report
    axios.get('http://localhost:8080/api/admin/reports/sales', {
      headers: { 'Authorization': `Bearer ${token}` }
    }).then(res => {
      setSalesReport(res.data);
    }).catch(err => console.error(err));
  }, []);

  if (loading) return <div>Loading...</div>;

  return (
    <div className="admin-dashboard">
      <h1>Admin Dashboard</h1>
      
      {/* Stats Cards */}
      <div className="stats-container">
        <div className="stat-card">
          <h3>Total Revenue</h3>
          <p>${stats.totalRevenue.toFixed(2)}</p>
        </div>
        <div className="stat-card">
          <h3>Total Orders</h3>
          <p>{stats.totalOrders}</p>
        </div>
        <div className="stat-card">
          <h3>Total Products</h3>
          <p>{stats.totalProducts}</p>
        </div>
        <div className="stat-card">
          <h3>Total Users</h3>
          <p>{stats.totalUsers}</p>
        </div>
      </div>

      {/* Order Status Breakdown */}
      <div className="chart-section">
        <h2>Order Status Breakdown</h2>
        <div className="status-grid">
          <div>Pending: {stats.pendingOrders}</div>
          <div>Processing: {stats.processingOrders}</div>
          <div>Shipped: {stats.shippedOrders}</div>
          <div>Delivered: {stats.deliveredOrders}</div>
          <div>Cancelled: {stats.cancelledOrders}</div>
        </div>
      </div>

      {/* Sales Report Table */}
      <div className="report-section">
        <h2>Recent Sales</h2>
        <table>
          <thead>
            <tr>
              <th>Order ID</th>
              <th>Customer</th>
              <th>Amount</th>
              <th>Status</th>
              <th>Payment</th>
            </tr>
          </thead>
          <tbody>
            {salesReport.map(sale => (
              <tr key={sale.orderId}>
                <td>{sale.orderId}</td>
                <td>{sale.customerName}</td>
                <td>${sale.totalAmount.toFixed(2)}</td>
                <td>{sale.orderStatus}</td>
                <td>{sale.paymentStatus}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

export default AdminDashboard;
```

## Dashboard Features Checklist

- [x] Dashboard Statistics
- [x] Sales Reports
- [x] Inventory Management
- [x] Revenue Analysis
- [x] Top Products
- [x] Low Stock Alerts
- [x] Customer Analytics
- [x] Export Reports (CSV ready)
- [ ] Charts & Graphs (Chart.js/Chart Library)
- [ ] Custom Date Range Reports
- [ ] Email Report Scheduling
- [ ] User Activity Logs

## Security Notes

- All admin endpoints require `@PreAuthorize("hasRole('ADMIN')")`
- Token-based authentication required
- Rate limiting recommended for production
- Audit logging should be implemented

## Future Enhancements

1. Real-time dashboard with WebSockets
2. Advanced analytics with machine learning
3. Scheduled report generation and email delivery
4. Custom dashboard widget configuration
5. User activity audit logs
6. Performance optimization alerts
7. Competitor price monitoring
8. Customer segmentation analytics
