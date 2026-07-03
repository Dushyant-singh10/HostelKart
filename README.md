TTN E-Commerce Backend
A scalable and secure E-Commerce Backend application built using Spring Boot following layered architecture and RESTful API design principles.

The system supports multiple user roles including Admin, Seller, and Buyer, enabling complete e-commerce operations such as product management, category management, cart handling, order processing, reviews, authentication, and role-based authorization.

Tech Stack
Technology	Purpose
Spring Boot	Application Framework
Spring Security	Authentication & Authorization
Hibernate / JPA	ORM & Database Mapping
Maven	Dependency Management
REST API	Client-Server Communication
MySQL / SQL	Relational Database
JWT	Secure Authentication
Lombok	Boilerplate Code Reduction
Project Architecture
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
User Roles
The application follows Role-Based Access Control (RBAC).

Admin
Manage sellers
Manage buyers
Manage categories
Activate/Deactivate users
View platform activities
Manage products and orders
Assign roles and permissions
Seller
Register as seller
Create products
Manage product inventory
Update product variations
Process customer orders
View order history
Manage product catalog
Buyer
Register/Login
Browse products
Add products to cart
Place orders
Track order status
Review purchased products
Manage addresses
High-Level System Flow
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
Database Relationship Overview
User Module
User
 │
 ├── Seller
 │
 └── Customer
One User can be a Seller.
One User can be a Customer.
User can have multiple Roles.
User can have multiple Addresses.
Product Module
Seller
   │
   ▼
 Product
   │
   ▼
Product Variation
Seller owns multiple Products.
Product contains multiple Variations.
Variations maintain inventory and pricing.
Category Module
Category
   │
   └── Parent Category
Supports hierarchical categories.
Parent-child category structure.
Order Module
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
Customer places orders.
Order contains multiple products.
Product variations are linked with orders.
Cart Module
Customer
   │
   ▼
 Cart
   │
   ▼
Product Variation
Customers can maintain cart items before checkout.
Review Module
Customer
   │
   ▼
Product Review
   │
   ▼
Product
Customers can review purchased products.
Rating and feedback support.
Core Features
Authentication & Security
JWT Based Authentication
Spring Security Integration
Role-Based Authorization
Password Encryption
Secure REST Endpoints
Product Management
Create Product
Update Product
Delete Product
Product Variations
Inventory Tracking
Product Availability Status
Category Management
Parent Categories
Child Categories
Dynamic Category Metadata
Cart Management
Add Item to Cart
Update Quantity
Remove Item
Wishlist Support
Order Management
Place Order
Order Status Tracking
Order History
Order Product Mapping
Review Management
Product Ratings
Product Feedback
Customer Reviews
Security Architecture
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
API Design Principles
RESTful Architecture
Layered Architecture
DTO-Based Communication
Global Exception Handling
Validation Support
Clean Separation of Concerns
Future Enhancements
Payment Gateway Integration
Email Notifications
Product Search & Filtering
Redis Caching
ElasticSearch Integration
Docker Deployment
Microservice Migration
Recommendation System
Admin Dashboard
Seller Analytics
Order Tracking Notifications
Getting Started
Clone Repository
git clone <repository-url>
Navigate To Project
cd TTN_E-Commerce
Build Project
mvn clean install
Run Application
mvn spring-boot:run
Database Configuration
Update application.properties

spring.datasource.url=jdbc:mysql://localhost:3306/ecommerce
spring.datasource.username=root
spring.datasource.password=password

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
Author
Dushyant

Backend Developer

Focused on building scalable, secure, and production-ready backend systems using Spring Boot, Hibernate/JPA, Spring Security, REST APIs, and SQL databases.
