package com.inventory.service;

import com.inventory.model.*;
import com.inventory.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    @Autowired private CartItemRepository cartItemRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private UserRepository userRepository;

    public List<CartItem> getCartItems(User user) {
        return cartItemRepository.findByUser(user);
    }

    @Transactional
    public CartItem addToCart(User user, Long productId, int quantity) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Product not found"));
        if (product.getStockQuantity() < quantity)
            throw new RuntimeException("Insufficient stock");

        Optional<CartItem> existing = cartItemRepository.findByUserAndProductId(user, productId);
        if (existing.isPresent()) {
            CartItem ci = existing.get();
            ci.setQuantity(ci.getQuantity() + quantity);
            return cartItemRepository.save(ci);
        } else {
            CartItem ci = CartItem.builder().user(user).product(product).quantity(quantity).build();
            return cartItemRepository.save(ci);
        }
    }

    @Transactional
    public CartItem updateQuantity(User user, Long cartItemId, int quantity) {
        CartItem ci = cartItemRepository.findById(cartItemId)
            .orElseThrow(() -> new RuntimeException("Cart item not found"));
        if (!ci.getUser().getId().equals(user.getId()))
            throw new RuntimeException("Unauthorized");
        if (quantity <= 0) {
            cartItemRepository.delete(ci);
            return null;
        }
        ci.setQuantity(quantity);
        return cartItemRepository.save(ci);
    }

    @Transactional
    public void removeFromCart(User user, Long cartItemId) {
        CartItem ci = cartItemRepository.findById(cartItemId)
            .orElseThrow(() -> new RuntimeException("Cart item not found"));
        if (!ci.getUser().getId().equals(user.getId()))
            throw new RuntimeException("Unauthorized");
        cartItemRepository.delete(ci);
    }

    @Transactional
    public void clearCart(User user) {
        cartItemRepository.deleteByUser(user);
    }

    public long getCartCount(User user) {
        return cartItemRepository.countByUser(user);
    }
}
