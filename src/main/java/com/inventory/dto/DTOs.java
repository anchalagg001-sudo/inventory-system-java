package com.inventory.dto;

import lombok.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class DTOs {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequest {
        @NotBlank @Email
        private String email;

        @NotBlank @Size(min = 6)
        private String password;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegisterRequest {
        @NotBlank private String name;
        @NotBlank @Email private String email;
        @NotBlank @Size(min = 6) private String password;

        private String phone;
        private String role;
    }

    // ✅ FIXED JwtResponse
    @Data
    public static class JwtResponse {
        private String token;
        private String type = "Bearer";
        private Long id;
        private String name;
        private String email;
        private String role;

        public JwtResponse(String token, Long id, String name, String email, String role) {
            this.token = token;
            this.id = id;
            this.name = name;
            this.email = email;
            this.role = role;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApiResponse {
        private boolean success;
        private String message;
        private Object data;

        public static ApiResponse ok(String message) {
            return new ApiResponse(true, message, null);
        }

        public static ApiResponse ok(String message, Object data) {
            return new ApiResponse(true, message, data);
        }

        public static ApiResponse error(String message) {
            return new ApiResponse(false, message, null);
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductRequest {
        @NotBlank private String name;
        private String description;

        @NotNull @DecimalMin("0.0")
        private BigDecimal price;

        @NotNull @Min(0)
        private Integer stockQuantity;

        @Min(0)
        private Integer minStockThreshold = 10;

        private Long categoryId;
        private String brand;
        private String modelNumber;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CartRequest {
        @NotNull private Long productId;
        @NotNull @Min(1) private Integer quantity;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderRequest {
        @NotBlank
        private String shippingAddress;
        private String notes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderUserDto {
        private Long id;
        private String name;
        private String email;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderProductDto {
        private Long id;
        private String name;
        private String brand;
        private String modelNumber;
        private CategoryDto category;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryDto {
        private Long id;
        private String name;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemDto {
        private Long id;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
        private OrderProductDto product;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderDto {
        private Long id;
        private OrderUserDto user;
        private BigDecimal totalAmount;
        private BigDecimal discount;
        private BigDecimal finalAmount;
        private String status;
        private String shippingAddress;
        private String notes;
        private List<OrderItemDto> orderItems;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SupplyRequestDto {
        private Long sellerId;
        private Long sellerProductId;

        @NotBlank private String productName;

        @NotNull @Min(1)
        private Integer quantity;

        @NotNull @DecimalMin("0.01")
        private BigDecimal pricePerUnit;

        private String notes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SupplyProductOptionDto {
        private Long sellerProductId;
        private String productName;
        private String brand;
        private String categoryName;
        private Integer availableStock;
        private BigDecimal pricePerUnit;
        private Long sellerId;
        private String sellerName;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddSellerRequest {
        @NotBlank private String name;
        @NotBlank @Email private String email;
        @NotBlank @Size(min = 6) private String password;

        private String phone;
        private String companyName;
        private String address;
        private String gstNumber;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusUpdateRequest {
        @NotBlank private String status;
        private String remarks;
    }
}
