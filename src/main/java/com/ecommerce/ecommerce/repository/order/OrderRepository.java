package com.ecommerce.ecommerce.repository.order;

import com.ecommerce.ecommerce.model.order.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByBuyerUsername(String username);
}
