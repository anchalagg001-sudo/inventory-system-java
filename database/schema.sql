-- ============================================================
-- Electrical Appliances Inventory System - Database Schema
-- ============================================================

CREATE DATABASE IF NOT EXISTS electrical_inventory;
USE electrical_inventory;

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    role ENUM('ADMIN', 'SELLER', 'USER') NOT NULL DEFAULT 'USER',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Categories table
CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255)
);

-- Sellers table
CREATE TABLE IF NOT EXISTS sellers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    company_name VARCHAR(200),
    phone VARCHAR(20),
    address TEXT,
    gst_number VARCHAR(50),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Products table
CREATE TABLE IF NOT EXISTS products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    price DECIMAL(12, 2) NOT NULL,
    stock_quantity INT NOT NULL DEFAULT 0,
    min_stock_threshold INT NOT NULL DEFAULT 10,
    category_id BIGINT,
    seller_id BIGINT,
    brand VARCHAR(100),
    model_number VARCHAR(100),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id),
    FOREIGN KEY (seller_id) REFERENCES sellers(id)
);

-- Seller inventory table
CREATE TABLE IF NOT EXISTS seller_products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    seller_id BIGINT NOT NULL,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    price DECIMAL(12, 2) NOT NULL,
    stock_quantity INT NOT NULL DEFAULT 0,
    min_stock_threshold INT NOT NULL DEFAULT 10,
    category_id BIGINT,
    brand VARCHAR(100),
    model_number VARCHAR(100),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (seller_id) REFERENCES sellers(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id)
);

-- Orders table
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    total_amount DECIMAL(12, 2) NOT NULL,
    discount DECIMAL(12, 2) DEFAULT 0,
    final_amount DECIMAL(12, 2) NOT NULL,
    status ENUM('PENDING','CONFIRMED','SHIPPED','DELIVERED','CANCELLED') DEFAULT 'PENDING',
    shipping_address TEXT,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Order items table
CREATE TABLE IF NOT EXISTS order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(12, 2) NOT NULL,
    total_price DECIMAL(12, 2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id)
);

-- Cart items table
CREATE TABLE IF NOT EXISTS cart_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_cart (user_id, product_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- Supply requests table
CREATE TABLE IF NOT EXISTS supply_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    seller_id BIGINT NOT NULL,
    product_id BIGINT,
    seller_product_id BIGINT,
    product_name VARCHAR(200),
    quantity INT NOT NULL,
    price_per_unit DECIMAL(12, 2) NOT NULL,
    total_value DECIMAL(12, 2) GENERATED ALWAYS AS (quantity * price_per_unit) STORED,
    status ENUM('PENDING','APPROVED','REJECTED') DEFAULT 'PENDING',
    notes TEXT,
    admin_remarks TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (seller_id) REFERENCES sellers(id),
    FOREIGN KEY (product_id) REFERENCES products(id),
    FOREIGN KEY (seller_product_id) REFERENCES seller_products(id)
);

-- Notifications table
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    message TEXT NOT NULL,
    type ENUM('LOW_STOCK','ORDER','SUPPLY','SYSTEM') NOT NULL DEFAULT 'SYSTEM',
    related_id BIGINT,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- SEED DATA
-- ============================================================

-- Default Admin (password: admin123)
INSERT IGNORE INTO users (name, email, password, role, phone) VALUES
('Admin', 'admin@inventory.com', 'admin123', 'ADMIN', '9999999999');

-- Sample Categories
INSERT IGNORE INTO categories (name, description) VALUES
('Air Conditioner', 'Cooling and heating appliances'),
('Refrigerator', 'Refrigerators and freezers'),
('Washing Machine', 'Washing and drying machines'),
('Television', 'LED, OLED and Smart TVs'),
('Microwave Oven', 'Microwave and OTG ovens'),
('Ceiling Fan', 'Fans and air circulators'),
('Water Heater', 'Geysers and water heaters'),
('Vacuum Cleaner', 'Floor and surface cleaners'),
('Dishwasher', 'Dishwashing machines'),
('Air Purifier', 'Air purifiers and humidifiers');

-- Sample Seller User (password: seller123)
INSERT IGNORE INTO users (name, email, password, role, phone) VALUES
('Raj Electronics', 'seller@inventory.com', '$2a$10$8K1p/a0dL1LXMIgoEDFrwOfMQkLPdDG1A2PGlvIDG1A2PGlvIDG1A', 'SELLER', '8888888888');

-- Sample Customer (password: user123)
INSERT IGNORE INTO users (name, email, password, role, phone) VALUES
('John Doe', 'user@inventory.com', '$2a$10$8K1p/a0dL1LXMIgoEDFrwOfMQkLPdDG1A2PGlvIDG1A2PGlvIDG1A', 'USER', '7777777777');
