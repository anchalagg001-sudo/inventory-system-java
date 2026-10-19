package com.inventory.repository;

import com.inventory.model.Product;
import com.inventory.model.Seller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByActiveTrue();
    List<Product> findBySeller(Seller seller);
    List<Product> findBySellerAndActiveTrue(Seller seller);
    List<Product> findByCategoryId(Long categoryId);
    List<Product> findByActiveTrueAndSellerIsNotNullAndStockQuantityGreaterThanOrderByNameAsc(Integer stockQuantity);

    @Query("SELECT p FROM Product p WHERE p.stockQuantity <= p.minStockThreshold AND p.active = true")
    List<Product> findLowStockProducts();

    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) AND p.active = true")
    List<Product> searchByName(String keyword);
}
