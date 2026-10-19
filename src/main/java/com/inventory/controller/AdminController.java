package com.inventory.controller;

import com.inventory.dto.DTOs.*;
import com.inventory.model.*;
import com.inventory.repository.*;
import com.inventory.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.*;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired private UserRepository userRepository;
    @Autowired private SellerRepository sellerRepository;
    @Autowired private ProductService productService;
    @Autowired private SellerProductService sellerProductService;
    @Autowired private OrderService orderService;
    @Autowired private SupplyRequestService supplyRequestService;
    @Autowired private NotificationService notificationService;
    @Autowired private AnalyticsService analyticsService;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private CategoryRepository categoryRepository;

    // ── Dashboard ──────────────────────────────────────────────
    @GetMapping("/dashboard")
    public ResponseEntity<?> dashboard() {
        return ResponseEntity.ok(ApiResponse.ok("ok", analyticsService.getDashboardStats()));
    }

    // ── Analytics ─────────────────────────────────────────────
    @GetMapping("/analytics/monthly/{year}")
    public ResponseEntity<?> monthlySales(@PathVariable int year) {
        return ResponseEntity.ok(ApiResponse.ok("ok", analyticsService.getMonthlySalesData(year)));
    }

    @GetMapping("/analytics/top-products")
    public ResponseEntity<?> topProducts() {
        return ResponseEntity.ok(ApiResponse.ok("ok", analyticsService.getTopSellingProducts()));
    }

    // ── Sellers ───────────────────────────────────────────────
    @GetMapping("/sellers")
    public ResponseEntity<?> getSellers() {
        return ResponseEntity.ok(ApiResponse.ok("ok", sellerRepository.findAll()));
    }

    @PostMapping("/sellers")
    public ResponseEntity<?> addSeller(@Valid @RequestBody AddSellerRequest req) {
        if (userRepository.existsByEmail(req.getEmail()))
            return ResponseEntity.badRequest().body(ApiResponse.error("Email already in use"));

        User user = User.builder()
            .name(req.getName()).email(req.getEmail())
            .password(passwordEncoder.encode(req.getPassword()))
            .phone(req.getPhone()).role(Role.SELLER).build();
        userRepository.save(user);

        Seller seller = Seller.builder()
            .user(user).companyName(req.getCompanyName())
            .phone(req.getPhone()).address(req.getAddress())
            .gstNumber(req.getGstNumber()).build();
        sellerRepository.save(seller);

        return ResponseEntity.ok(ApiResponse.ok("Seller created successfully"));
    }

    @DeleteMapping("/sellers/{id}")
    public ResponseEntity<?> deleteSeller(@PathVariable Long id) {
        Seller s = sellerRepository.findById(id).orElseThrow(() -> new RuntimeException("Not found"));
        s.setActive(false);
        sellerRepository.save(s);
        return ResponseEntity.ok(ApiResponse.ok("Seller deactivated"));
    }

    // ── Products ──────────────────────────────────────────────
    @GetMapping("/products")
    public ResponseEntity<?> getProducts() {
        return ResponseEntity.ok(ApiResponse.ok("ok", productService.getAllProducts()));
    }

    @GetMapping("/products/supply-options")
    public ResponseEntity<?> getSupplyOptions() {
        return ResponseEntity.ok(ApiResponse.ok("ok", sellerProductService.getAvailableSupplyOptions()));
    }

    @PostMapping("/products")
    public ResponseEntity<?> createProduct(@Valid @RequestBody ProductRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Product created", productService.createProduct(req)));
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Product updated", productService.updateProduct(id, req)));
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.ok("Product deleted"));
    }

    @GetMapping("/products/low-stock")
    public ResponseEntity<?> lowStock() {
        return ResponseEntity.ok(ApiResponse.ok("ok", productService.getLowStockProducts()));
    }

    // ── Categories ────────────────────────────────────────────
    @GetMapping("/categories")
    public ResponseEntity<?> getCategories() {
        return ResponseEntity.ok(ApiResponse.ok("ok", categoryRepository.findAll()));
    }

    @PostMapping("/categories")
    public ResponseEntity<?> addCategory(@RequestBody Map<String, String> body) {
        Category c = Category.builder()
            .name(body.get("name")).description(body.get("description")).build();
        return ResponseEntity.ok(ApiResponse.ok("Category added", categoryRepository.save(c)));
    }

    // ── Orders ────────────────────────────────────────────────
    @GetMapping("/orders")
    public ResponseEntity<?> getAllOrders() {
        return ResponseEntity.ok(ApiResponse.ok("ok", orderService.getAllOrders()));
    }

    @PutMapping("/orders/{id}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long id, @RequestBody StatusUpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Status updated", orderService.updateStatus(id, req.getStatus())));
    }

    // ── Supply Requests ───────────────────────────────────────
    @GetMapping("/supply-requests")
    public ResponseEntity<?> getSupplyRequests() {
        return ResponseEntity.ok(ApiResponse.ok("ok", supplyRequestService.getAllRequests()));
    }

    @PostMapping("/supply-requests")
    public ResponseEntity<?> createSupplyRequest(@Valid @RequestBody SupplyRequestDto dto) {
        return ResponseEntity.ok(ApiResponse.ok("Supply request sent to seller",
            supplyRequestService.createRequestForSeller(dto)));
    }

    // ── Notifications ─────────────────────────────────────────
    @GetMapping("/notifications")
    public ResponseEntity<?> getNotifications() {
        Map<String, Object> data = new HashMap<>();
        data.put("notifications", notificationService.getAllNotifications());
        data.put("unreadCount", notificationService.getUnreadCount());
        return ResponseEntity.ok(ApiResponse.ok("ok", data));
    }

    @PutMapping("/notifications/mark-read")
    public ResponseEntity<?> markAllRead() {
        notificationService.markAllRead();
        return ResponseEntity.ok(ApiResponse.ok("All marked as read"));
    }

    // ── Users ─────────────────────────────────────────────────
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.ok("ok", userRepository.findAll()));
    }
}
