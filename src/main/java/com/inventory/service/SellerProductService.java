package com.inventory.service;

import com.inventory.dto.DTOs;
import com.inventory.model.*;
import com.inventory.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SellerProductService {

    @Autowired private SellerProductRepository sellerProductRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private NotificationService notificationService;

    public List<SellerProduct> getSellerProducts(Seller seller) {
        return sellerProductRepository.findBySellerAndActiveTrue(seller);
    }

    public List<DTOs.SupplyProductOptionDto> getAvailableSupplyOptions() {
        return sellerProductRepository.findByActiveTrueAndStockQuantityGreaterThanOrderByNameAsc(0).stream()
            .map(this::toSupplyOptionDto)
            .collect(Collectors.toList());
    }

    @Transactional
    public SellerProduct createSellerProduct(Seller seller, DTOs.ProductRequest req) {
        SellerProduct p = new SellerProduct();
        fillSellerProductFields(p, req);
        p.setSeller(seller);
        SellerProduct saved = sellerProductRepository.save(p);
        checkLowStock(saved);
        return saved;
    }

    @Transactional
    public SellerProduct updateSellerProduct(Seller seller, Long productId, DTOs.ProductRequest req) {
        SellerProduct p = sellerProductRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Product not found"));
        if (p.getSeller() == null || !p.getSeller().getId().equals(seller.getId()))
            throw new RuntimeException("Product not found");

        fillSellerProductFields(p, req);
        p.setSeller(seller);
        SellerProduct saved = sellerProductRepository.save(p);
        checkLowStock(saved);
        return saved;
    }

    @Transactional
    public void deleteSellerProduct(Seller seller, Long productId) {
        SellerProduct p = sellerProductRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Product not found"));
        if (p.getSeller() == null || !p.getSeller().getId().equals(seller.getId()))
            throw new RuntimeException("Product not found");

        p.setActive(false);
        sellerProductRepository.save(p);
    }

    public SellerProduct getSellerProductForSeller(Long id, Seller seller) {
        SellerProduct p = sellerProductRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Product not found"));
        if (p.getSeller() == null || !p.getSeller().getId().equals(seller.getId()))
            throw new RuntimeException("Product not found");
        return p;
    }

    public SellerProduct getById(Long id) {
        return sellerProductRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    @Transactional
    public SellerProduct save(SellerProduct product) {
        SellerProduct saved = sellerProductRepository.save(product);
        checkLowStock(saved);
        return saved;
    }

    private void checkLowStock(SellerProduct p) {
        if (p.isLowStock()) {
            notificationService.createNotification(
                "Low stock alert: \"" + p.getName() + "\" has only " + p.getStockQuantity() + " units left!",
                NotificationType.LOW_STOCK, p.getId());
        }
    }

    private void fillSellerProductFields(SellerProduct p, DTOs.ProductRequest req) {
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

    private DTOs.SupplyProductOptionDto toSupplyOptionDto(SellerProduct product) {
        String sellerName = product.getSeller() == null ? null :
            (product.getSeller().getCompanyName() != null ? product.getSeller().getCompanyName() :
                product.getSeller().getUser() != null ? product.getSeller().getUser().getName() : null);

        return DTOs.SupplyProductOptionDto.builder()
            .sellerProductId(product.getId())
            .productName(product.getName())
            .brand(product.getBrand())
            .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
            .availableStock(product.getStockQuantity())
            .pricePerUnit(product.getPrice())
            .sellerId(product.getSeller() != null ? product.getSeller().getId() : null)
            .sellerName(sellerName)
            .build();
    }
}
