package com.inventory.service;

import com.inventory.dto.DTOs;
import com.inventory.model.*;
import com.inventory.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderItemRepository orderItemRepository;
    @Autowired private CartItemRepository cartItemRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private NotificationService notificationService;

    @Transactional
    public DTOs.OrderDto placeOrder(User user, String shippingAddress, String notes) {
        List<CartItem> cartItems = cartItemRepository.findByUser(user);
        if (cartItems.isEmpty()) throw new RuntimeException("Cart is empty");

        BigDecimal total = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        Order order = Order.builder()
            .user(user)
            .status(OrderStatus.PENDING)
            .shippingAddress(shippingAddress)
            .notes(notes)
            .discount(BigDecimal.ZERO)
            .totalAmount(BigDecimal.ZERO)
            .finalAmount(BigDecimal.ZERO)
            .build();
        Order savedOrder = orderRepository.save(order);

        for (CartItem ci : cartItems) {
            Product p = ci.getProduct();
            if (p.getStockQuantity() < ci.getQuantity())
                throw new RuntimeException("Insufficient stock for: " + p.getName());

            BigDecimal itemTotal = p.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity()));
            total = total.add(itemTotal);

            OrderItem oi = OrderItem.builder()
                .order(savedOrder).product(p)
                .quantity(ci.getQuantity())
                .unitPrice(p.getPrice())
                .totalPrice(itemTotal)
                .build();
            orderItems.add(oi);

            // Deduct stock
            p.setStockQuantity(p.getStockQuantity() - ci.getQuantity());
            productRepository.save(p);

            // Check low stock after deduction
            if (p.isLowStock()) {
                notificationService.createNotification(
                    "⚠ Low stock: \"" + p.getName() + "\" has only " + p.getStockQuantity() + " units remaining!",
                    NotificationType.LOW_STOCK, p.getId());
            }
        }

        orderItemRepository.saveAll(orderItems);
        savedOrder.setTotalAmount(total);
        savedOrder.setFinalAmount(total);
        savedOrder.setOrderItems(orderItems);
        orderRepository.save(savedOrder);

        cartItemRepository.deleteByUser(user);

        notificationService.createNotification(
            "📦 New order #" + savedOrder.getId() + " placed by " + user.getName() + " for ₹" + total,
            NotificationType.ORDER, savedOrder.getId());

        return toOrderDto(savedOrder);
    }

    public List<DTOs.OrderDto> getUserOrders(User user) {
        return orderRepository.findByUserOrderByCreatedAtDesc(user).stream()
            .map(this::toOrderDto)
            .collect(Collectors.toList());
    }

    public List<DTOs.OrderDto> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc().stream()
            .map(this::toOrderDto)
            .collect(Collectors.toList());
    }

    public DTOs.OrderDto getById(Long id) {
        return toOrderDto(findOrderById(id));
    }

    public DTOs.OrderDto getByIdForUser(User user, Long id) {
        Order order = findOrderById(id);
        if (order.getUser() == null || !order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Order not found");
        }
        return toOrderDto(order);
    }

    @Transactional
    public DTOs.OrderDto updateStatus(Long orderId, String status) {
        Order order = findOrderById(orderId);
        order.setStatus(OrderStatus.valueOf(status.toUpperCase()));
        return toOrderDto(orderRepository.save(order));
    }

    private Order findOrderById(Long id) {
        return orderRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    private DTOs.OrderDto toOrderDto(Order order) {
        return DTOs.OrderDto.builder()
            .id(order.getId())
            .user(toUserDto(order.getUser()))
            .totalAmount(order.getTotalAmount())
            .discount(order.getDiscount())
            .finalAmount(order.getFinalAmount())
            .status(order.getStatus() != null ? order.getStatus().name() : null)
            .shippingAddress(order.getShippingAddress())
            .notes(order.getNotes())
            .orderItems(order.getOrderItems() == null ? List.of() : order.getOrderItems().stream()
                .map(this::toOrderItemDto)
                .collect(Collectors.toList()))
            .createdAt(order.getCreatedAt())
            .updatedAt(order.getUpdatedAt())
            .build();
    }

    private DTOs.OrderUserDto toUserDto(User user) {
        if (user == null) return null;
        return DTOs.OrderUserDto.builder()
            .id(user.getId())
            .name(user.getName())
            .email(user.getEmail())
            .build();
    }

    private DTOs.OrderItemDto toOrderItemDto(OrderItem item) {
        return DTOs.OrderItemDto.builder()
            .id(item.getId())
            .quantity(item.getQuantity())
            .unitPrice(item.getUnitPrice())
            .totalPrice(item.getTotalPrice())
            .product(toOrderProductDto(item.getProduct()))
            .build();
    }

    private DTOs.OrderProductDto toOrderProductDto(Product product) {
        if (product == null) return null;
        return DTOs.OrderProductDto.builder()
            .id(product.getId())
            .name(product.getName())
            .brand(product.getBrand())
            .modelNumber(product.getModelNumber())
            .category(product.getCategory() == null ? null : DTOs.CategoryDto.builder()
                .id(product.getCategory().getId())
                .name(product.getCategory().getName())
                .build())
            .build();
    }
}
