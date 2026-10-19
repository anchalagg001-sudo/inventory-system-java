package com.inventory.repository;

import com.inventory.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("SELECT oi.product.id, oi.product.name, SUM(oi.quantity) as totalQty " +
           "FROM OrderItem oi GROUP BY oi.product.id, oi.product.name ORDER BY totalQty DESC")
    List<Object[]> getTopSellingProducts();
}
