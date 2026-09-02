# Security Hardening Guide

## Overview

This document outlines all security measures implemented in the E-Commerce Platform.

## 1. Input Validation & Sanitization

### Validation Utilities

**File:** `ValidationUtil.java`

Provides validation for:
- Email format
- Phone number
- Strong password requirements
- SQL injection detection
- Input length
- Name format
- Price format
- URL format

### Input Sanitizer

**File:** `InputSanitizer.java`

Provides sanitization for:
- String inputs (max length)
- Email addresses
- Passwords
- Names
- Phone numbers
- URLs

### Usage Example

```java
// Validate email
try {
    String email = InputSanitizer.validateAndSanitizeEmail(userEmail);
} catch (SecurityValidationException e) {
    // Handle validation error
}

// Validate password
try {
    String password = InputSanitizer.validateAndSanitizePassword(userPassword);
} catch (SecurityValidationException e) {
    // Handle validation error
}
```

## 2. Password Requirements

Passwords must meet ALL of these criteria:
- ✅ Minimum 8 characters
- ✅ At least one uppercase letter (A-Z)
- ✅ At least one lowercase letter (a-z)
- ✅ At least one number (0-9)
- ✅ At least one special character (@$!%*?&)

**Example Strong Password:** `MyPassword@123`

## 3. SQL Injection Prevention

### Detection

The system automatically detects and blocks common SQL injection patterns:
- `--` (SQL comment)
- `;` (Statement terminator)
- `'` and `"` (String delimiters)
- `or`, `and`, `drop`, `delete`, `insert`, `update`, `select`, `union`

### Implementation

```java
// Automatic validation in input sanitizer
if (ValidationUtil.containsSQLInjection(input)) {
    throw new SecurityValidationException("Potential SQL injection detected");
}
```

## 4. CORS Security

### Configuration

**File:** `CorsConfig.java`

Allowed Origins:
- `http://localhost:3000` (Development)
- `http://localhost:8080` (Development)
- `https://yourdomain.com` (Production - update this)

Allowed Methods:
- GET, POST, PUT, DELETE, OPTIONS, PATCH

Allowed Headers:
- All headers are allowed (configure stricter in production)

**Note:** Update allowed origins in `application.properties` for production.

## 5. HTTP Security Headers

**File:** `SecurityHeadersFilter.java`

Implements the following security headers:

```
X-Content-Type-Options: nosniff
  → Prevents MIME type sniffing

X-Frame-Options: DENY
  → Prevents clickjacking attacks

X-XSS-Protection: 1; mode=block
  → Enables XSS protection in browsers

Strict-Transport-Security: max-age=31536000; includeSubDomains
  → Enforces HTTPS connections

Content-Security-Policy: default-src 'self'
  → Restricts content sources

Referrer-Policy: strict-origin-when-cross-origin
  → Controls referrer information
```

## 6. Authentication & Authorization

### JWT Security

- Tokens expire after 24 hours (configurable)
- Tokens are signed with HS256 algorithm
- Bearer token format required

### Role-Based Access Control (RBAC)

```java
@PreAuthorize("hasRole('ADMIN')")
public void adminOnlyEndpoint() { }

@PreAuthorize("hasRole('USER')")
public void userOnlyEndpoint() { }
```

### Password Hashing

- Uses BCrypt algorithm
- Automatic salt generation
- 10+ rounds of hashing

## 7. Data Encryption

**File:** `EncryptionUtil.java`

Provides:
- Secure token generation (256-bit)
- SHA-256 hashing
- Base64 encoding/decoding
- Email masking (e.g., j***@example.com)
- Phone masking (e.g., 123***45)

### Usage Example

```java
// Generate secure token
String token = EncryptionUtil.generateSecureToken(32);

// Hash sensitive data
String hashed = EncryptionUtil.hashSHA256(sensitiveData);

// Mask email for logs
String maskedEmail = EncryptionUtil.maskEmail(email);
```

## 8. Global Exception Handling

**File:** `GlobalExceptionHandler.java`

Handles all exceptions and returns proper HTTP status codes:

- `400 Bad Request` - Validation failures, illegal arguments
- `401 Unauthorized` - Authentication failures
- `403 Forbidden` - Authorization failures
- `404 Not Found` - Resource not found
- `500 Internal Server Error` - Unexpected errors

## 9. API Security Best Practices

### Rate Limiting (Recommended Implementation)

```bash
# Add dependency to pom.xml
<dependency>
    <groupId>io.github.bucket4j</groupId>
    <artifactId>bucket4j-core</artifactId>
    <version>7.6.0</version>
</dependency>
```

### HTTPS Enforcement

```properties
# In application.properties
server.ssl.enabled=true
server.ssl.key-store=classpath:keystore.p12
server.ssl.key-store-password=your_password
server.ssl.key-store-type=PKCS12
```

## 10. Sensitive Data Logging

### DO NOT Log

```java
// ❌ NEVER log passwords
logger.info("Password: " + password);

// ❌ NEVER log full email addresses in logs
logger.info("User email: " + email);

// ❌ NEVER log credit card information
logger.info("Card: " + cardNumber);
```

### DO Log (with masking)

```java
// ✅ Use masked values
logger.info("User login: " + EncryptionUtil.maskEmail(email));
logger.info("User action: purchase from " + EncryptionUtil.maskPhone(phone));
```

## 11. Production Security Checklist

- [ ] Update CORS allowed origins
- [ ] Enable HTTPS/SSL certificates
- [ ] Configure rate limiting
- [ ] Update database credentials
- [ ] Update JWT secret key (min 256 bits)
- [ ] Configure firewall rules
- [ ] Enable logging and monitoring
- [ ] Set up regular backups
- [ ] Configure intrusion detection
- [ ] Review and update security headers
- [ ] Enable Web Application Firewall (WAF)
- [ ] Configure DDoS protection
- [ ] Regular security audits
- [ ] Dependency vulnerability scanning

## 12. Testing Security

### Run Security Tests

```bash
# Run all tests including security tests
mvn test

# Run specific security test class
mvn test -Dtest=ValidationUtilTest
```

### Test Coverage

- Input validation tests
- SQL injection prevention tests
- Authentication tests
- Authorization tests
- Exception handling tests

## 13. Security Headers Configuration

### Update in Production

```properties
# application.properties
cors.allowed-origins=https://yourdomain.com
cors.allowed-methods=GET,POST,PUT,DELETE
jwt.secret=your_long_secret_key_minimum_256_bits_long
jwt.expiration=86400000
```

## 14. Common Vulnerabilities & Prevention

### OWASP Top 10

1. **Injection (SQL, NoSQL)** → Parameterized queries, input validation
2. **Authentication** → JWT tokens, strong passwords, 2FA ready
3. **Sensitive Data Exposure** → Encryption, HTTPS, data masking
4. **XML External Entities (XXE)** → Validate XML input
5. **Broken Access Control** → RBAC, @PreAuthorize
6. **Security Misconfiguration** → Security headers, CORS config
7. **XSS (Cross-Site Scripting)** → Input sanitization, CSP headers
8. **Insecure Deserialization** → Validate serialized objects
9. **Using Components with Known Vulnerabilities** → Dependency scanning
10. **Insufficient Logging & Monitoring** → Request logging enabled

## 15. Regular Maintenance

### Weekly
- Review security logs
- Check for failed authentication attempts
- Monitor for unusual patterns

### Monthly
- Update dependencies
- Run security tests
- Review access logs

### Quarterly
- Security audit
- Penetration testing
- Vulnerability assessment

## Support & Updates

For security concerns or to report vulnerabilities, contact: `security@yourdomain.com`
