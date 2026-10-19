package com.inventory.service;

import com.inventory.dto.DTOs;
import com.inventory.model.*;
import com.inventory.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class SupplyRequestService {

    @Autowired private SupplyRequestRepository supplyRequestRepository;
    @Autowired private SellerRepository sellerRepository;
    @Autowired private SellerProductService sellerProductService;
    @Autowired private ProductRepository productRepository;
    @Autowired private NotificationService notificationService;

    @Transactional
    public SupplyRequest createRequestForSeller(DTOs.SupplyRequestDto dto) {
        if (dto.getSellerId() == null)
            throw new RuntimeException("Seller is required");

        Seller seller = sellerRepository.findById(dto.getSellerId())
            .orElseThrow(() -> new RuntimeException("Seller not found"));
        SellerProduct linkedProduct = null;
        if (dto.getSellerProductId() != null) {
            linkedProduct = sellerProductService.getById(dto.getSellerProductId());
            if (linkedProduct.getSeller() == null || !linkedProduct.getSeller().getId().equals(seller.getId()))
                throw new RuntimeException("Selected seller does not have this item");
            if (linkedProduct.getStockQuantity() == null || linkedProduct.getStockQuantity() <= 0)
                throw new RuntimeException("Selected item is not available with this seller");
            if (dto.getQuantity() != null && linkedProduct.getStockQuantity() < dto.getQuantity())
                throw new RuntimeException("Requested quantity exceeds seller availability");
        }

        SupplyRequest sr = SupplyRequest.builder()
            .seller(seller)
            .productName(linkedProduct != null ? linkedProduct.getName() : dto.getProductName())
            .quantity(dto.getQuantity())
            .pricePerUnit(linkedProduct != null ? linkedProduct.getPrice() : dto.getPricePerUnit())
            .notes(dto.getNotes())
            .status(SupplyStatus.PENDING)
            .build();

        if (linkedProduct != null)
            sr.setSellerProduct(linkedProduct);

        SupplyRequest saved = supplyRequestRepository.save(sr);
        String sellerName = seller.getCompanyName() != null ? seller.getCompanyName() : seller.getUser().getName();
        notificationService.createNotification(
            "Admin requested " + sellerName + " to supply " + dto.getQuantity() +
                " units of \"" + sr.getProductName() + "\"",
            NotificationType.SUPPLY, saved.getId());

        return saved;
    }

    public List<SupplyRequest> getAllRequests() {
        return supplyRequestRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<SupplyRequest> getSellerRequests(User user) {
        Seller seller = sellerRepository.findByUser(user)
            .orElseThrow(() -> new RuntimeException("Seller profile not found"));
        return supplyRequestRepository.findBySellerOrderByCreatedAtDesc(seller);
    }

    @Transactional
    public SupplyRequest updateStatusBySeller(User user, Long id, String status, String remarks) {
        Seller seller = sellerRepository.findByUser(user)
            .orElseThrow(() -> new RuntimeException("Seller profile not found"));

        SupplyRequest sr = supplyRequestRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Supply request not found"));
        if (sr.getSeller() == null || !sr.getSeller().getId().equals(seller.getId()))
            throw new RuntimeException("Supply request not found");
        if (sr.getStatus() != SupplyStatus.PENDING)
            throw new RuntimeException("This request has already been processed");

        sr.setStatus(SupplyStatus.valueOf(status.toUpperCase()));
        sr.setAdminRemarks(remarks);

        if ("APPROVED".equalsIgnoreCase(status) && sr.getSellerProduct() != null) {
            SellerProduct p = sr.getSellerProduct();
            if (p.getStockQuantity() < sr.getQuantity())
                throw new RuntimeException("Seller does not have enough stock for this request");
            p.setStockQuantity(p.getStockQuantity() - sr.getQuantity());
            sellerProductService.save(p);
            Product adminProduct = findOrCreateAdminProduct(p);
            adminProduct.setStockQuantity((adminProduct.getStockQuantity() == null ? 0 : adminProduct.getStockQuantity()) + sr.getQuantity());
            sr.setProduct(productRepository.save(adminProduct));
        }

        SupplyRequest saved = supplyRequestRepository.save(sr);
        String sellerName = seller.getCompanyName() != null ? seller.getCompanyName() : seller.getUser().getName();
        notificationService.createNotification(
            "APPROVED".equalsIgnoreCase(status)
                ? sellerName + " accepted admin's supply request for \"" + sr.getProductName() + "\""
                : sellerName + " rejected admin's supply request for \"" + sr.getProductName() + "\"",
            NotificationType.SUPPLY, saved.getId());
        return saved;
    }

    private Product findOrCreateAdminProduct(SellerProduct sellerProduct) {
        return productRepository.findAll().stream()
            .filter(p -> p.getSeller() == null)
            .filter(p -> sameValue(p.getName(), sellerProduct.getName()))
            .filter(p -> sameValue(p.getBrand(), sellerProduct.getBrand()))
            .filter(p -> sameValue(p.getModelNumber(), sellerProduct.getModelNumber()))
            .filter(p -> sameCategory(p.getCategory(), sellerProduct.getCategory()))
            .findFirst()
            .orElseGet(() -> Product.builder()
                .name(sellerProduct.getName())
                .description(sellerProduct.getDescription())
                .price(sellerProduct.getPrice())
                .stockQuantity(0)
                .minStockThreshold(sellerProduct.getMinStockThreshold() != null ? sellerProduct.getMinStockThreshold() : 10)
                .category(sellerProduct.getCategory())
                .brand(sellerProduct.getBrand())
                .modelNumber(sellerProduct.getModelNumber())
                .active(true)
                .build());
    }

    private boolean sameValue(String left, String right) {
        if (left == null && right == null) return true;
        if (left == null || right == null) return false;
        return left.trim().equalsIgnoreCase(right.trim());
    }

    private boolean sameCategory(Category left, Category right) {
        if (left == null && right == null) return true;
        if (left == null || right == null) return false;
        return left.getId() != null && left.getId().equals(right.getId());
    }
}
