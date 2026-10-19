package com.inventory.repository;

import com.inventory.model.Order;
import com.inventory.model.OrderStatus;
import com.inventory.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserOrderByCreatedAtDesc(User user);
    List<Order> findByStatus(OrderStatus status);
    List<Order> findAllByOrderByCreatedAtDesc();

    @Query("SELECT COALESCE(SUM(o.finalAmount), 0) FROM Order o WHERE o.status != 'CANCELLED'")
    BigDecimal getTotalRevenue();

    @Query("SELECT COALESCE(SUM(o.finalAmount), 0) FROM Order o WHERE o.createdAt >= :from AND o.status != 'CANCELLED'")
    BigDecimal getRevenueSince(@Param("from") LocalDateTime from);

    @Query("SELECT MONTH(o.createdAt) as month, COALESCE(SUM(o.finalAmount), 0) as revenue " +
           "FROM Order o WHERE YEAR(o.createdAt) = :year AND o.status != 'CANCELLED' " +
           "GROUP BY MONTH(o.createdAt) ORDER BY month")
    List<Object[]> getMonthlyRevenue(@Param("year") int year);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt >= :from")
    long countOrdersSince(@Param("from") LocalDateTime from);
}
