# 🛒 E-Commerce Backend System API

A scalable, production-ready E-Commerce Backend RESTful API built with **Java (Spring Boot)** and **MongoDB**, featuring JWT authentication, role-based authorization, inventory concurrency control, cart persistence, order lifecycle management, and simulated idempotent payments.

---

## 🌐 Live Deployment & API Docs

| Resource | Link |
| :--- | :--- |
| **Live API Base URL** | `https://ecommerce-api-production.up.railway.app` *(Replace with your deployed URL)* |
| **Interactive Swagger UI** | `http://localhost:8080/swagger-ui/index.html` (Local) / `/swagger-ui/index.html` (Live) |
| **OpenAPI 3 JSON Specification** | `http://localhost:8080/v3/api-docs` |

---

## 📑 Table of Contents
1. [Key Features](#-key-features)
2. [Tech Stack](#-tech-stack)
3. [System Architecture & Entity Relationships](#-system-architecture--entity-relationships)
4. [Concurrency, Consistency & Design Decisions](#-concurrency-consistency--design-decisions)
5. [Environment Variables](#-environment-variables)
6. [Local Setup & Running Step-by-Step](#-local-setup--running-step-by-step)
7. [API Documentation & Sample Requests](#-api-documentation--sample-requests)
   - [Authentication Endpoints](#1-authentication-endpoints)
   - [Product Catalog Endpoints](#2-product-catalog-endpoints)
   - [Cart Endpoints](#3-cart-endpoints)
   - [Order Management Endpoints](#4-order-management-endpoints)
   - [Payment Simulation Endpoints](#5-payment-simulation-endpoints)
8. [Deployment Guide (Render / Railway / AWS EC2)](#-deployment-guide)

---

## 🚀 Key Features

* **User Management & Security:**
  * JWT-based stateless authentication (`jjwt 0.13.0`).
  * Role-Based Access Control (RBAC): `USER`, `ADMIN`, `MANAGER`.
  * BCrypt password encryption (`BCryptPasswordEncoder`).
* **Product Catalog:**
  * Admin-only CRUD operations (Create, Update, Delete).
  * Public catalog browsing with pagination and dynamic sorting (`Pageable`, `Sort`).
  * Search by category (`/category/{category}`) and case-insensitive search by product name (`/search?name=...`).
* **Persistent Shopping Cart:**
  * One-to-one cart isolation per user using unique indexed MongoDB field (`@Indexed(unique = true) userId`).
  * Stock validation to prevent adding more quantity than available inventory.
  * Embedded sub-document architecture for ultra-fast single-document updates.
* **Order Management & Lifecycle:**
  * Complete 4-step transactional flow: Stock validation → Atomic inventory deduction → Order creation → Cart clearing.
  * Historical price preservation (`priceAtPurchase`) on line items to ensure financial immutability.
  * Order cancellation with automatic inventory stock replenishment.
* **Payment Simulation & Idempotency:**
  * Simulated payment gateway endpoint (`/api/payments/pay`) with configurable success/failure rate.
  * Safe retries on failed payments (`PAYMENT_FAILED` status).
  * Strict idempotency: Calling payment on an already `CONFIRMED` order returns success without double-charging or corrupting order state.
* **Enterprise Non-Functional Capabilities:**
  * **Zero-Lock Concurrency Control:** MongoDB conditional atomic updates (`findAndModify`) prevent race conditions and stock overselling.
  * **Standardized Error Handling:** `@RestControllerAdvice` global exception handler with RFC-compliant error schemas.
  * **Structured Logging:** SLF4J / Logback instrumentation logging key events (orders placed, cancellations, payments, and errors).

---

## 🛠 Tech Stack

| Layer | Technology |
| :--- | :--- |
| **Language** | Java 17+ (or Java 21 / 25) |
| **Framework** | Spring Boot (Web, Security, Data MongoDB, Validation) |
| **Database** | MongoDB (Atlas or Self-Hosted) |
| **Security & Auth** | Spring Security 6 + JJWT (JSON Web Token) |
| **API Documentation** | SpringDoc OpenAPI 3 / Swagger UI |
| **Boilerplate Reduction** | Project Lombok |
| **Build Tool** | Apache Maven |

---

## 🏛 System Architecture & Entity Relationships

The application uses MongoDB document-oriented modeling, balancing **Referencing** for root aggregates and **Embedding** for dependent line items:

```mermaid
erDiagram
    USER ||--|| CART : "1 : 1 (by unique userId)"
    USER ||--o{ ORDER : "1 : N (by userId)"
    
    CART ||--|{ CART_ITEMS : "embeds (1 : N Composition)"
    CART_ITEMS }o--|| PRODUCT : "references (N : 1 by productId)"

    ORDER ||--|{ ORDER_ITEM : "embeds (1 : N Composition)"
    ORDER_ITEM }o--|| PRODUCT : "references (N : 1 by productId)"

    USER {
        string id PK
        string username UK
        string email
        string password
        Role role
    }

    PRODUCT {
        string id PK
        string productId UK
        string name
        string description
        BigDecimal price
        int stockQuantity
        string category
    }

    CART {
        string id PK
        string userId UK
        List items "Embedded CartItems"
    }

    CART_ITEMS {
        string productId FK
        string name
        int quantity
        BigDecimal price
    }

    ORDER {
        string orderId PK
        string userId FK
        BigDecimal totalAmount
        OrderStatus status
        List orderItems "Embedded OrderItem"
    }

    ORDER_ITEM {
        string productId FK
        int quantity
        BigDecimal priceAtPurchase
    }
```

---

## 💡 Concurrency, Consistency & Design Decisions

### 1. Preventing Overselling (Atomic Updates vs Locking)
* **Challenge:** High-concurrency flash sales where multiple users attempt to purchase the last available stock simultaneously.
* **Solution:** Instead of heavy distributed locks or slow pessimistic table locks, the system utilizes MongoDB's atomic `findAndModify` with conditional criteria:
  ```java
  Query query = new Query(
      Criteria.where("productId").is(items.getProductId())
              .and("stockQuantity").gte(items.getQuantity())
  );
  Update update = new Update().inc("stockQuantity", -items.getQuantity());
  Product updated = mongoTemplate.findAndModify(query, update, Product.class);

  if (updated == null) {
      throw new InsufficientStockException("Insufficient stock for product: " + items.getProductId());
  }
  ```
* **Why this works:** The decrement operation only succeeds at the database engine level if `stockQuantity >= requestedQuantity`. If another thread depletes the inventory milliseconds earlier, `findAndModify` returns `null`, throwing `InsufficientStockException` and preventing overselling.

### 2. Historical Price Snapshotting
* In `OrderItem`, the unit price is stored as `priceAtPurchase`. If an administrator changes the product price from \$50 to \$70 in the product catalog next week, previously placed orders and receipts remain unaffected.

### 3. Payment Idempotency & Safe Retries
* If network latency causes a client to submit duplicate payment requests for an already `CONFIRMED` order, the service returns the confirmation response without re-executing business logic or corrupting state.
* If a payment simulation returns `false`, the status is marked `PAYMENT_FAILED`, allowing the customer to safely retry the payment.

---

## ⚙ Environment Variables

Never commit secrets or database credentials to version control. The application reads its configuration from environment variables:

| Variable Name | Description | Default Value | Example |
| :--- | :--- | :--- | :--- |
| `MONGODB_URI` | MongoDB Connection URI string | *(Required)* | `mongodb+srv://admin:pass@cluster.mongodb.net` |
| `MONGODB_DATABASE` | Target Database name | `ecommerceDb` | `ecommerceDb` |
| `JWT_SECRET` | 256-bit Hex/Base64 secret key | *(Required)* | `404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970` |
| `JWT_EXPIRATION` | JWT expiration in milliseconds | `86400000` (24h) | `86400000` |
| `PORT` | Application server port | `8080` | `8080` |

---

## 💻 Local Setup & Running Step-by-Step

### Prerequisites
* **Java Development Kit (JDK 17 or higher)**
* **Apache Maven 3.8+**
* **MongoDB** (Local instance or free [MongoDB Atlas](https://www.mongodb.com/atlas) cluster)
* **Git**

### Step 1: Clone the Repository
```bash
git clone https://github.com/<YOUR_USERNAME>/ecommerceApi.git
cd ecommerceApi
```

### Step 2: Set Environment Variables

**Windows (PowerShell):**
```powershell
$env:MONGODB_URI="mongodb://localhost:27017/ecommerceDb"
$env:JWT_SECRET="404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970"
```

**Linux / macOS:**
```bash
export MONGODB_URI="mongodb://localhost:27017/ecommerceDb"
export JWT_SECRET="404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970"
```

### Step 3: Build the Application
```bash
mvn clean package -DskipTests
```

### Step 4: Run the Application
```bash
mvn spring-boot:run
```
The application will start on `http://localhost:8080`.

---

## 📡 API Documentation & Sample Requests

> All requests that require authentication must include the HTTP header:  
> `Authorization: Bearer <YOUR_JWT_TOKEN>`

### 1. Authentication Endpoints

#### Register User
* **Method & URL:** `POST /api/auth/register`
* **Access:** Public
* **Request Body:**
```json
{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "Password123!",
  "role": "USER"
}
```
* **Response (200 OK):**
```text
User registered successfully!
```

#### Login
* **Method & URL:** `POST /api/auth/login`
* **Access:** Public
* **Request Body:**
```json
{
  "username": "john_doe",
  "password": "Password123!"
}
```
* **Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huX2RvZS...",
  "type": "Bearer",
  "username": "john_doe"
}
```

---

### 2. Product Catalog Endpoints

#### Browse Products (Paginated & Sorted)
* **Method & URL:** `GET /api/products?page=0&size=10&sortBy=price&sortDir=asc`
* **Access:** Public
* **Response (200 OK):**
```json
{
  "content": [
    {
      "id": "664fa1...",
      "productId": "PROD-001",
      "name": "Wireless Mouse",
      "description": "Ergonomic Bluetooth mouse",
      "price": 29.99,
      "stockQuantity": 50,
      "category": "Electronics"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 10,
  "number": 0
}
```

#### Search Products by Name
* **Method & URL:** `GET /api/products/search?name=mouse&page=0&size=10&sortBy=price&sortDir=asc`
* **Access:** Public

#### Filter Products by Category
* **Method & URL:** `GET /api/products/category/Electronics?page=0&size=5`
* **Access:** Public

#### Add New Product (Admin Only)
* **Method & URL:** `POST /api/products/add`
* **Access:** `ROLE_ADMIN`
* **Request Body:**
```json
{
  "productId": "PROD-002",
  "name": "Mechanical Keyboard",
  "description": "RGB Backlit Cherry MX Red",
  "price": 89.99,
  "stockQuantity": 30,
  "category": "Electronics"
}
```

#### Update Product (Admin Only)
* **Method & URL:** `PUT /api/products/update`
* **Access:** `ROLE_ADMIN`

#### Delete Product (Admin Only)
* **Method & URL:** `DELETE /api/products/delete/{id}`
* **Access:** `ROLE_ADMIN`

---

### 3. Cart Endpoints

#### View Cart
* **Method & URL:** `GET /cart`
* **Access:** Authenticated User
* **Response (200 OK):**
```json
{
  "userId": "john_doe",
  "items": [
    {
      "productId": "PROD-001",
      "name": "Wireless Mouse",
      "quantity": 2,
      "price": 29.99
    }
  ]
}
```

#### Add Item to Cart
* **Method & URL:** `POST /cart/items`
* **Access:** Authenticated User
* **Request Body:**
```json
{
  "productId": "PROD-001",
  "quantity": 2
}
```

#### Update Item Quantity in Cart
* **Method & URL:** `PUT /cart/items/{productId}`
* **Access:** Authenticated User
* **Request Body:**
```json
{
  "quantity": 3
}
```

#### Remove Item from Cart
* **Method & URL:** `DELETE /cart/items/{productId}`
* **Access:** Authenticated User

---

### 4. Order Management Endpoints

#### Place Order from Cart
* **Method & URL:** `POST /api/orders/place`
* **Access:** Authenticated User
* **Response (200 OK):**
```json
{
  "orderId": "664fa8...",
  "userId": "john_doe",
  "totalAmount": 59.98,
  "status": "PLACED",
  "items": [
    {
      "productId": "PROD-001",
      "quantity": 2,
      "priceAtPurchase": 29.99
    }
  ]
}
```

#### View User's Orders
* **Method & URL:** `GET /api/orders/my-orders`
* **Access:** Authenticated User

#### View Specific Order
* **Method & URL:** `GET /api/orders/{orderId}`
* **Access:** Authenticated User

#### Cancel Order
* **Method & URL:** `PUT /api/orders/cancel/{orderId}`
* **Access:** Authenticated User

---

### 5. Payment Simulation Endpoints

#### Process Payment
* **Method & URL:** `POST /api/payments/pay`
* **Access:** Authenticated User
* **Request Body:**
```json
{
  "orderId": "664fa8..."
}
```
* **Success Response (200 OK):**
```json
{
  "orderId": "664fa8...",
  "success": true,
  "message": "Payment successful, order confirmed"
}
```
* **Simulated Failure Response (200 OK):**
```json
{
  "orderId": "664fa8...",
  "success": false,
  "message": "Payment failed, please retry"
}
```
* **Idempotent Retry Response (Order already confirmed):**
```json
{
  "orderId": "664fa8...",
  "success": true,
  "message": "Order is already paid and confirmed"
}
```

---

## 🚢 Deployment Guide

### Deploying to Render
1. Create a free account on [Render.com](https://render.com).
2. Connect your GitHub repository.
3. Select **Web Service** with runtime **Docker** or **Java**:
   * **Build Command:** `mvn clean package -DskipTests`
   * **Start Command:** `java -jar target/ecommerceApi-0.0.1-SNAPSHOT.jar`
4. In the **Environment** tab, add your environment variables:
   * `MONGODB_URI`
   * `JWT_SECRET`
   * `PORT` = `8080`
5. Click **Deploy Web Service** and copy your live URL.

### Deploying to Railway
1. Go to [Railway.app](https://railway.app) and create a New Project from your GitHub Repo.
2. Under **Variables**, add `MONGODB_URI` and `JWT_SECRET`.
3. Railway automatically detects Maven and starts the Spring Boot service.

---

## 📋 Submission Checklist

- [x] Push all code to a public GitHub repository.
- [x] README includes complete setup, endpoints, architecture, and concurrency explanation.
- [x] Application successfully connects to remote MongoDB Atlas.
- [x] Live working URL deployed and accessible over the internet.
- [x] All endpoints testable via Swagger UI / Postman.
- [x] No secrets or credentials committed in the repository.
