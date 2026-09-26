# 🛒 E-Commerce Backend System API

A scalable, production-ready E-Commerce Backend RESTful API built with **Java (Spring Boot)** and **MongoDB**, featuring JWT authentication, role-based authorization, inventory concurrency control, cart persistence, order lifecycle management, and simulated idempotent payments.

---

## 🌐 Live Deployment & API Docs

| Resource | Link |
| :--- | :--- |
| **Live API Base URL** | `https://ecommerce-api-production.up.railway.app` *(Replace with your deployed EC2/Railway/Render URL)* |
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
8. [Deployment Guide (AWS EC2 / Render / Railway)](#-deployment-guide)

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
| **Language** | Java 17+ (or Java 21) |
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