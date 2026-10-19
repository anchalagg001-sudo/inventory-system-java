package com.inventory.service;

import com.inventory.model.NotificationType;
import com.inventory.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LowStockScheduler {

    private static final Logger log = LoggerFactory.getLogger(LowStockScheduler.class);

    @Autowired private ProductService productService;
    @Autowired private NotificationService notificationService;

    /**
     * Runs every hour to check all products for low stock.
     * Creates a notification for each product below threshold.
     */
    @Scheduled(fixedRate = 3_600_000) // every 1 hour
    public void checkLowStock() {
        List<Product> lowStockProducts = productService.getLowStockProducts();
        log.info("Low-stock check: {} product(s) below threshold", lowStockProducts.size());

        for (Product p : lowStockProducts) {
            notificationService.createNotification(
                "⚠️ Scheduled Alert: \"" + p.getName() + "\" has only "
                    + p.getStockQuantity() + " units left (threshold: " + p.getMinStockThreshold() + ")",
                NotificationType.LOW_STOCK,
                p.getId()
            );
        }
    }
}
