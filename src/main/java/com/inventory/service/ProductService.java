package com.inventory.service;

import com.inventory.dto.DTOs;
import com.inventory.model.*;
import com.inventory.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired private ProductRepository productRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private NotificationService notificationService;

    public List<Product> getAllActiveProducts() {
        return productRepository.findByActiveTrue();
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Optional<Product> getById(Long id) {
        return productRepository.findById(id);
    }

    public List<Product> searchProducts(String keyword) {
        return productRepository.searchByName(keyword);
    }

    public List<Product> getLowStockProducts() {
        return productRepository.findLowStockProducts();
    }

    public List<Product> getByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }

    @Transactional
    public Product createProduct(DTOs.ProductRequest req) {
        Product p = new Product();
        fillProductFields(p, req);
        Product saved = productRepository.save(p);
        checkLowStock(saved);
        return saved;
    }

    @Transactional
    public Product updateProduct(Long id, DTOs.ProductRequest req) {
        Product p = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Product not found"));
        fillProductFields(p, req);
        Product saved = productRepository.save(p);
        checkLowStock(saved);
        return saved;
    }

    @Transactional
    public void addStock(Long productId, int qty, Seller seller) {
        Product p = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Product not found"));
        p.setStockQuantity(p.getStockQuantity() + qty);
        p.setSeller(seller);
        productRepository.save(p);
    }

    public void deleteProduct(Long id) {
        Product p = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Product not found"));
        p.setActive(false);
        productRepository.save(p);
    }

    private void checkLowStock(Product p) {
        if (p.isLowStock()) {
            notificationService.createNotification(
                "Low stock alert: \"" + p.getName() + "\" has only " + p.getStockQuantity() + " units left!",
                NotificationType.LOW_STOCK, p.getId());
        }
    }

    private void fillProductFields(Product p, DTOs.ProductRequest req) {
        p.setName(req.getName());
        p.setDescription(req.getDescription());
        p.setPrice(req.getPrice());
        p.setStockQuantity(req.getStockQuantity());
        p.setMinStockThreshold(req.getMinStockThreshold() != null ? req.getMinStockThreshold() : 10);
        p.setBrand(req.getBrand());
        p.setModelNumber(req.getModelNumber());
        p.setCategory(null);
        if (req.getCategoryId() != null)
            categoryRepository.findById(req.getCategoryId()).ifPresent(p::setCategory);
    }
}
