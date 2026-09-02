# E-Commerce Platform

A comprehensive E-Commerce Platform built with Spring Boot, featuring user authentication, product management, shopping cart, order processing, payment integration with Stripe, and admin dashboard.

## 🚀 Features

### Phase 1: User Management
- User registration and authentication
- JWT-based authentication
- User profile management
- Password reset functionality
- Email verification

### Phase 2: Product Management
- Product catalog with search and filtering
- Product categories
- Product ratings and reviews
- Inventory management
- Product images upload

### Phase 3: Shopping Cart
- Add/remove items from cart
- Update quantities
- Cart calculation with tax and shipping
- Apply coupon/discount codes
- View cart summary

### Phase 4: Order Management
- Create orders from cart
- Order history and tracking
- Order status management
- Cancel orders
- Generate invoices

### Phase 5: Payment Integration
- Stripe payment gateway integration
- Secure payment processing
- Payment status tracking
- Refund management

### Phase 6: Admin Dashboard
- Product management
- Order management
- User management
- Sales analytics and reports
- Inventory monitoring

## 💻 Tech Stack

- **Backend:** Spring Boot 3.2.0
- **Database:** MySQL 8.0
- **Authentication:** Spring Security + JWT
- **Payment Gateway:** Stripe API
- **ORM:** Hibernate/JPA
- **Build Tool:** Maven
- **API Documentation:** Swagger/OpenAPI
- **Testing:** JUnit 5 + Mockito

## 📋 Prerequisites

- Java 17 or higher
- MySQL 8.0 or higher
- Maven 3.6+
- Git
- Stripe account (for payment integration)

## 🔧 Installation

### 1. Clone the repository
```bash
git clone https://github.com/VikashKumar49/ecommerce-platform.git
cd ecommerce-platform
```

### 2. Configure Database

Create a MySQL database:
```sql
CREATE DATABASE ecommerce_db;
```

Update `application.properties` with your database credentials:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/ecommerce_db
spring.datasource.username=root
spring.datasource.password=your_password
```

### 3. Configure JWT Secret

Update JWT secret in `application.properties`:
```properties
jwt.secret=your_very_long_secret_key_here_minimum_256_bits
jwt.expiration=86400000
```

### 4. Configure Stripe API Key

Add your Stripe API key in `application.properties`:
```properties
stripe.api.key=sk_test_your_stripe_key
```

### 5. Build and Run

```bash
mvn clean install
mvn spring-boot:run
```

The application will start at `http://localhost:8080/api`

## 📚 API Documentation

Swagger UI is available at: `http://localhost:8080/api/swagger-ui.html`

## 🗂️ Project Structure

```
src/main/java/com/ecommerce/
├── controller/          # REST API endpoints
├── service/             # Business logic
├── repository/          # Data access layer
├── entity/              # JPA entities
├── dto/                 # Data transfer objects
├── exception/           # Custom exceptions
├── config/              # Application configuration
└── util/                # Utility classes
```

## 🔐 Security

- Passwords are hashed using BCrypt
- JWT tokens for stateless authentication
- CORS configuration for cross-origin requests
- Role-based access control (RBAC)
- Spring Security integration

## 📝 API Endpoints

### Authentication
```
POST   /api/auth/register          - Register new user
POST   /api/auth/login             - Login user
POST   /api/auth/refresh-token     - Refresh JWT token
POST   /api/auth/logout            - Logout user
```

### Products
```
GET    /api/products               - Get all products
GET    /api/products/{id}          - Get product details
GET    /api/products/search        - Search products
POST   /api/products               - Create product (Admin)
PUT    /api/products/{id}          - Update product (Admin)
DELETE /api/products/{id}          - Delete product (Admin)
```

### Cart
```
GET    /api/cart                   - Get cart
POST   /api/cart/add               - Add to cart
PUT    /api/cart/update/{id}       - Update cart item
DELETE /api/cart/remove/{id}       - Remove from cart
```

### Orders
```
GET    /api/orders                 - Get user orders
GET    /api/orders/{id}            - Get order details
POST   /api/orders                 - Create order
PUT    /api/orders/{id}/cancel     - Cancel order
```

### Payment
```
POST   /api/payment/process        - Process payment
GET    /api/payment/status/{id}    - Get payment status
POST   /api/payment/refund         - Process refund
```

## 🧪 Testing

Run unit tests:
```bash
mvn test
```

Run integration tests:
```bash
mvn verify
```

## 📦 Deployment

### Docker

Build Docker image:
```bash
docker build -t ecommerce-platform .
```

Run Docker container:
```bash
docker run -p 8080:8080 ecommerce-platform
```

### AWS/Heroku

See deployment guide in `docs/deployment.md`

## 🤝 Contributing

Contributions are welcome! Please:
1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## 📄 License

This project is licensed under the MIT License - see `LICENSE` file for details.

## 👨‍💻 Author

Vikash Kumar - [@VikashKumar49](https://github.com/VikashKumar49)

## 📞 Support

For support, email: vikash@example.com or open an issue on GitHub.

## 🔄 Project Roadmap

- [x] Phase 1: User Management
- [x] Phase 2: Product Management
- [x] Phase 3: Shopping Cart
- [x] Phase 4: Order Management
- [ ] Phase 5: Payment Integration (In Progress)
- [ ] Phase 6: Admin Dashboard (Planned)
- [ ] Phase 7: Advanced Features (Planned)

---

**Happy Coding! 🎉**
