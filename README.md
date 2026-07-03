# 🛒 TTN E-Commerce Backend

A scalable and secure **E-Commerce Backend** built using **Spring Boot**, following **Layered Architecture** and **RESTful API** design principles.

The application supports multiple user roles including **Admin**, **Seller**, and **Buyer**, enabling complete e-commerce operations such as authentication, product management, category management, cart handling, order processing, reviews, and role-based authorization.

---



| Technology | Purpose |
|------------|---------|
| Spring Boot | Application Framework |
| Spring Security | Authentication & Authorization |
| Hibernate / JPA | ORM & Database Mapping |
| Maven | Dependency Management |
| REST API | Client-Server Communication |
| MySQL | Relational Database |
| JWT | Secure Authentication |
| Lombok | Boilerplate Code Reduction |

---

# 📁 Project Architecture

```text
TTN_E-Commerce
│
├── Config
│   ├── Security Configuration
│   ├── JWT Configuration
│   └── Application Configuration
│
├── Controller
│   ├── Authentication APIs
│   ├── User APIs
│   ├── Seller APIs
│   ├── Product APIs
│   ├── Category APIs
│   ├── Cart APIs
│   ├── Order APIs
│   └── Review APIs
│
├── DTO
│   ├── Request DTOs
│   └── Response DTOs
│
├── Entity
│   ├── User
│   ├── Role
│   ├── Customer
│   ├── Seller
│   ├── Address
│   ├── Category
│   ├── Product
│   ├── ProductVariation
│   ├── Cart
│   ├── Order
│   ├── OrderProduct
│   ├── ProductReview
│   └── OrderStatus
│
├── Repository
│   └── JPA Repositories
│
├── Service
│   ├── Business Logic
│   └── Transaction Handling
│
├── Mapper
│   └── Entity ↔ DTO Conversion
│
├── Exception
│   └── Global Exception Handling
│
└── Enum
    └── Application Constants
```

---

# 👥 User Roles

The application follows **Role-Based Access Control (RBAC)**.

## 👨‍💼 Admin

- Manage Sellers
- Manage Buyers
- Manage Categories
- Activate / Deactivate Users
- View Platform Activities
- Manage Products & Orders
- Assign Roles & Permissions

---

## 🏪 Seller

- Register as Seller
- Create Products
- Manage Product Inventory
- Update Product Variations
- Process Customer Orders
- View Order History
- Manage Product Catalog

---

## 🛍️ Buyer

- Register / Login
- Browse Products
- Add Products to Cart
- Place Orders
- Track Order Status
- Review Purchased Products
- Manage Addresses

---

# 🔄 High-Level System Flow

```text
Buyer
   │
   ▼
Browse Products
   │
   ▼
Add To Cart
   │
   ▼
Place Order
   │
   ▼
Order Created
   │
   ▼
Seller Processes Order
   │
   ▼
Order Status Updated
   │
   ▼
Delivered
   │
   ▼
Buyer Review
```

---

# 🗄️ Database Relationship Overview

## 👤 User Module

```text
User
 │
 ├── Seller
 │
 └── Customer
```

- One User can be a Seller.
- One User can be a Customer.
- A User can have multiple Roles.
- A User can have multiple Addresses.

---

## 📦 Product Module

```text
Seller
   │
   ▼
 Product
   │
   ▼
Product Variation
```

- One Seller owns multiple Products.
- Each Product contains multiple Variations.
- Variations maintain inventory and pricing.

---

## 📂 Category Module

```text
Category
   │
   └── Parent Category
```

- Supports hierarchical categories.
- Parent-child category structure.

---

## 📋 Order Module

```text
Customer
   │
   ▼
Order
   │
   ▼
Order Product
   │
   ▼
Product Variation
```

- Customers place Orders.
- Each Order contains multiple Products.
- Product Variations are linked with Orders.

---

## 🛒 Cart Module

```text
Customer
   │
   ▼
Cart
   │
   ▼
Product Variation
```

- Customers can maintain cart items before checkout.

---

## ⭐ Review Module

```text
Customer
   │
   ▼
Product Review
   │
   ▼
Product
```

- Customers can review purchased products.
- Supports ratings and feedback.

---

# ✨ Core Features

## 🔐 Authentication & Security

- JWT-Based Authentication
- Spring Security Integration
- Role-Based Authorization (RBAC)
- Password Encryption
- Secure REST Endpoints

### 📦 Product Management

- Create Product
- Update Product
- Delete Product
- Product Variations
- Inventory Tracking
- Product Availability Status

### 📂 Category Management

- Parent Categories
- Child Categories
- Dynamic Category Metadata

### 🛒 Cart Management

- Add Item to Cart
- Update Quantity
- Remove Item
- Wishlist Support

### 📋 Order Management

- Place Orders
- Order Status Tracking
- Order History
- Order Product Mapping

### ⭐ Review Management

- Product Ratings
- Product Feedback
- Customer Reviews

---

# 🔒 Security Architecture

```text
Client
   │
   ▼
JWT Authentication
   │
   ▼
Spring Security Filter
   │
   ▼
Role Verification
   │
   ▼
Protected REST APIs
   │
   ▼
Service Layer
   │
   ▼
Database
```

---

# 📐 API Design Principles

- RESTful Architecture
- Layered Architecture
- DTO-Based Communication
- Global Exception Handling
- Validation Support
- Clean Separation of Concerns

---

# 🚀 Future Enhancements

- Payment Gateway Integration
- Email Notifications
- Product Search & Filtering
- Redis Caching
- Elasticsearch Integration
- Docker Deployment
- Microservice Migration
- Recommendation System
- Admin Dashboard
- Seller Analytics
- Order Tracking Notifications

---

# ⚙️ Getting Started

## Clone Repository

```bash
git clone <repository-url>
```

## Navigate to Project

```bash
cd TTN_E-Commerce
```

## Build Project

```bash
mvn clean install
```

## Run Application

```bash
mvn spring-boot:run
```

## Database Configuration

Update `application.properties`

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/ecommerce
spring.datasource.username=root
spring.datasource.password=password

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

---

# 👨‍💻 Author

## Dushyant

**Backend Developer**

Focused on building scalable, secure, and production-ready backend systems using **Spring Boot**, **Hibernate/JPA**, **Spring Security**, **REST APIs**, and **SQL databases**.
