package com.inventory.controller;

import com.inventory.dto.DTOs.*;
import com.inventory.model.User;
import com.inventory.repository.UserRepository;
import com.inventory.security.UserDetailsImpl;
import com.inventory.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

// ─────────────────────────────────────────────────────────────
// Public Product endpoints
// ─────────────────────────────────────────────────────────────
@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
class ProductController {

    @Autowired private ProductService productService;

    @GetMapping
    public ResponseEntity<?> all() {
        return ResponseEntity.ok(ApiResponse.ok("ok", productService.getAllActiveProducts()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> one(@PathVariable Long id) {
        return productService.getById(id)
            .map(p -> ResponseEntity.ok(ApiResponse.ok("ok", p)))
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam String q) {
        return ResponseEntity.ok(ApiResponse.ok("ok", productService.searchProducts(q)));
    }

    @GetMapping("/category/{id}")
    public ResponseEntity<?> byCategory(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("ok", productService.getByCategory(id)));
    }
}

// ─────────────────────────────────────────────────────────────
// Cart endpoints
// ─────────────────────────────────────────────────────────────
@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "*")
class CartController {

    @Autowired private CartService cartService;
    @Autowired private UserRepository userRepository;

    private User getUser(Authentication auth) {
        UserDetailsImpl ud = (UserDetailsImpl) auth.getPrincipal();
        return userRepository.findById(ud.getId()).orElseThrow();
    }

    @GetMapping
    public ResponseEntity<?> getCart(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok("ok", cartService.getCartItems(getUser(auth))));
    }

    @GetMapping("/count")
    public ResponseEntity<?> count(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok("ok", cartService.getCartCount(getUser(auth))));
    }

    @PostMapping
    public ResponseEntity<?> add(@Valid @RequestBody CartRequest req, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok("Added to cart",
            cartService.addToCart(getUser(auth), req.getProductId(), req.getQuantity())));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody CartRequest req, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok("Updated",
            cartService.updateQuantity(getUser(auth), id, req.getQuantity())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> remove(@PathVariable Long id, Authentication auth) {
        cartService.removeFromCart(getUser(auth), id);
        return ResponseEntity.ok(ApiResponse.ok("Removed from cart"));
    }

    @DeleteMapping
    public ResponseEntity<?> clear(Authentication auth) {
        cartService.clearCart(getUser(auth));
        return ResponseEntity.ok(ApiResponse.ok("Cart cleared"));
    }
}

// ─────────────────────────────────────────────────────────────
// Order endpoints
// ─────────────────────────────────────────────────────────────
@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
class OrderController {

    @Autowired private OrderService orderService;
    @Autowired private UserRepository userRepository;

    private User getUser(Authentication auth) {
        UserDetailsImpl ud = (UserDetailsImpl) auth.getPrincipal();
        return userRepository.findById(ud.getId()).orElseThrow();
    }

    @GetMapping
    public ResponseEntity<?> myOrders(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok("ok", orderService.getUserOrders(getUser(auth))));
    }

    @PostMapping
    public ResponseEntity<?> placeOrder(@Valid @RequestBody OrderRequest req, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok("Order placed!",
            orderService.placeOrder(getUser(auth), req.getShippingAddress(), req.getNotes())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrder(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok("ok", orderService.getByIdForUser(getUser(auth), id)));
    }
}
