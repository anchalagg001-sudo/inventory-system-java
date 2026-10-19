package com.inventory.controller;

import com.inventory.dto.DTOs.*;
import com.inventory.model.Seller;
import com.inventory.model.User;
import com.inventory.repository.UserRepository;
import com.inventory.repository.SellerRepository;
import com.inventory.security.UserDetailsImpl;
import com.inventory.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/seller")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('SELLER')")
public class SellerController {

    @Autowired private SupplyRequestService supplyRequestService;
    @Autowired private ProductService productService;
    @Autowired private SellerProductService sellerProductService;
    @Autowired private UserRepository userRepository;
    @Autowired private SellerRepository sellerRepository;

    private User getUser(Authentication auth) {
        UserDetailsImpl ud = (UserDetailsImpl) auth.getPrincipal();
        return userRepository.findById(ud.getId()).orElseThrow();
    }

    @GetMapping("/supply-requests")
    public ResponseEntity<?> getMyRequests(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok("ok",
            supplyRequestService.getSellerRequests(getUser(auth))));
    }

    @PutMapping("/supply-requests/{id}")
    public ResponseEntity<?> respondToSupplyRequest(@PathVariable Long id,
                                                    @RequestBody StatusUpdateRequest req,
                                                    Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok("Supply request updated",
            supplyRequestService.updateStatusBySeller(getUser(auth), id, req.getStatus(), req.getRemarks())));
    }

    @GetMapping("/products")
    public ResponseEntity<?> getProducts(Authentication auth) {
        Seller seller = sellerRepository.findByUser(getUser(auth))
            .orElseThrow(() -> new RuntimeException("Seller profile not found"));
        return ResponseEntity.ok(ApiResponse.ok("ok", sellerProductService.getSellerProducts(seller)));
    }

    @PostMapping("/products")
    public ResponseEntity<?> createProduct(@Valid @RequestBody ProductRequest req, Authentication auth) {
        Seller seller = sellerRepository.findByUser(getUser(auth))
            .orElseThrow(() -> new RuntimeException("Seller profile not found"));
        return ResponseEntity.ok(ApiResponse.ok("Product added",
            sellerProductService.createSellerProduct(seller, req)));
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id,
                                           @Valid @RequestBody ProductRequest req,
                                           Authentication auth) {
        Seller seller = sellerRepository.findByUser(getUser(auth))
            .orElseThrow(() -> new RuntimeException("Seller profile not found"));
        return ResponseEntity.ok(ApiResponse.ok("Product updated",
            sellerProductService.updateSellerProduct(seller, id, req)));
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id, Authentication auth) {
        Seller seller = sellerRepository.findByUser(getUser(auth))
            .orElseThrow(() -> new RuntimeException("Seller profile not found"));
        sellerProductService.deleteSellerProduct(seller, id);
        return ResponseEntity.ok(ApiResponse.ok("Product deleted"));
    }
}
