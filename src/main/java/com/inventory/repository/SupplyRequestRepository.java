package com.inventory.repository;

import com.inventory.model.Seller;
import com.inventory.model.SupplyRequest;
import com.inventory.model.SupplyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SupplyRequestRepository extends JpaRepository<SupplyRequest, Long> {
    List<SupplyRequest> findBySeller(Seller seller);
    List<SupplyRequest> findByStatus(SupplyStatus status);
    List<SupplyRequest> findAllByOrderByCreatedAtDesc();
    List<SupplyRequest> findBySellerOrderByCreatedAtDesc(Seller seller);
}
