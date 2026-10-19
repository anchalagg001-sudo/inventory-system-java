package com.inventory.repository;

import com.inventory.model.Seller;
import com.inventory.model.SellerProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SellerProductRepository extends JpaRepository<SellerProduct, Long> {
    List<SellerProduct> findBySellerAndActiveTrue(Seller seller);
    List<SellerProduct> findByActiveTrueAndStockQuantityGreaterThanOrderByNameAsc(Integer stockQuantity);
}
