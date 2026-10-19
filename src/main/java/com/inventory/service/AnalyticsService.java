package com.inventory.service;

import com.inventory.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class AnalyticsService {

    @Autowired private OrderRepository orderRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private OrderItemRepository orderItemRepository;

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

        stats.put("totalRevenue", orderRepository.getTotalRevenue());
        stats.put("revenueThisMonth", orderRepository.getRevenueSince(thirtyDaysAgo));
        stats.put("totalOrders", orderRepository.count());
        stats.put("ordersThisMonth", orderRepository.countOrdersSince(thirtyDaysAgo));
        stats.put("totalProducts", productRepository.count());
        stats.put("lowStockCount", productRepository.findLowStockProducts().size());
        stats.put("totalUsers", userRepository.count());

        return stats;
    }

    public Map<String, Object> getMonthlySalesData(int year) {
        List<Object[]> rawData = orderRepository.getMonthlyRevenue(year);
        String[] months = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
        BigDecimal[] revenue = new BigDecimal[12];
        Arrays.fill(revenue, BigDecimal.ZERO);

        for (Object[] row : rawData) {
            int month = ((Number) row[0]).intValue() - 1;
            revenue[month] = (BigDecimal) row[1];
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("labels", months);
        result.put("revenue", revenue);
        return result;
    }

    public List<Map<String, Object>> getTopSellingProducts() {
        List<Object[]> rawData = orderItemRepository.getTopSellingProducts();
        List<Map<String, Object>> result = new ArrayList<>();
        int limit = Math.min(rawData.size(), 10);
        for (int i = 0; i < limit; i++) {
            Object[] row = rawData.get(i);
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("productId", row[0]);
            m.put("productName", row[1]);
            m.put("totalQuantitySold", row[2]);
            result.add(m);
        }
        return result;
    }
}
