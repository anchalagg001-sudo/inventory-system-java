# Inventory System

Inventory System is a full-stack electric warehouse management project with three roles:
- `Admin` manages store inventory, categories, users, sellers, orders, and supplier requests.
- `Seller` maintains a dedicated supplier inventory and responds to admin supply requests.
- `User` browses products, manages cart, and places orders.

## Description

This project manages an electric warehouse where the admin acts as the warehouse owner, sellers act as suppliers, and users act as small dealers. Admin stock and supplier stock are handled separately so the warehouse can request inventory from suppliers and also serve dealer orders.

Key business flow:
- Sellers maintain their own inventory in a dedicated `seller_products` table.
- Admin can open the `Supply Requests` page, select an item, see which sellers currently have it available, and send a request.
- Seller receives the admin request and can `accept` or `reject` it.
- If seller accepts, stock is deducted from seller inventory and reflected into admin inventory.

This makes the project useful for:
- multi-role inventory management
- supplier order handling
- admin stock replenishment from seller stock
- simple e-commerce checkout for end users

## Tech Stack

- Backend: Java 17, Spring Boot 3.2, Spring Security, Spring Data JPA, JWT
- Database: MySQL 8.x
- Frontend: HTML, CSS, Vanilla JavaScript

## Main Features

### Admin
- Dashboard with analytics and notifications
- Product CRUD for admin inventory
- Categories management
- Seller management
- User management
- Order management
- Supply request creation for sellers
- Auto stock sync into admin inventory when seller accepts a request

### Seller
- Dedicated `My Inventory` section
- Add, edit, and delete seller products
- Receive admin supply requests
- Accept or reject requests
- Seller stock reduces when a request is accepted

### User
- Browse products
- Add to cart
- Place orders
- Track order history

## Database Notes

Important inventory tables:
- `products`: admin/store inventory
- `seller_products`: seller-managed inventory
- `supply_requests`: admin-to-seller requests

If you are using an existing database, make sure the newer schema changes are present, especially:
- `seller_products` table
- `seller_product_id` column inside `supply_requests`

Use [database/schema.sql](C:\Users\98har\Desktop\electrical-inventory\database\schema.sql) as the reference schema.

## Setup

### 1. Create database

```bash
mysql -u root -p < database/schema.sql
```

### 2. Configure backend

Update [src/main/resources/application.properties](C:\Users\98har\Desktop\electrical-inventory\src\main\resources\application.properties) with your MySQL password.

### 3. Run backend

```bash
mvn spring-boot:run
```

Backend runs at:
- [http://localhost:8080](http://localhost:8080)

### 4. Run frontend

From the `frontend` folder:

```bash
python -m http.server 3000
```

Open:
- [http://localhost:3000/login.html](http://localhost:3000/login.html)

## Default Credentials

| Role | Email | Password |
|---|---|---|
| Admin | `admin@inventory.com` | `admin123` |
| Seller | `seller@inventory.com` | `seller123` |
| User | `user@inventory.com` | `user123` |

## Project Structure

```text
database/
  schema.sql

src/main/java/com/inventory/
  config/
  controller/
  dto/
  model/
  repository/
  security/
  service/

frontend/
  admin/
  seller/
  user/
  css/
  js/
  login.html
```

## Current Supply Workflow

1. Seller adds products in `My Inventory`.
2. Seller inventory is stored in `seller_products`.
3. Admin opens `Supply Requests`.
4. Admin selects an item and then selects a seller who has availability.
5. Admin sends the request.
6. Seller sees the request in `Admin Requests`.
7. Seller accepts or rejects.
8. On accept:
   admin inventory increases
   seller inventory decreases

## Important Note

This project now uses one dedicated `seller_products` table instead of creating separate physical tables like `sellername_products` for each seller. That design is simpler, safer, and easier to query while still keeping seller inventory logically separate.
