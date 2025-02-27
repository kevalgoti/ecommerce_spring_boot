package com.ecommerce.ecommerce.controller.order;

import com.ecommerce.ecommerce.dto.order.OrderDTO;
import com.ecommerce.ecommerce.model.order.Order;
import com.ecommerce.ecommerce.service.Order.OrderService;
import com.ecommerce.ecommerce.service.paymentService.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "http://localhost:5500")
public class OrderController {

    private final OrderService orderService;
    private final PaymentService paymentService;

    public OrderController(OrderService orderService , PaymentService paymentService) {
        this.orderService = orderService;
        this.paymentService = paymentService;
    }

    @PostMapping("/place")
    public ResponseEntity<Long> createOrder(@RequestBody OrderDTO orderDTO, @RequestParam String paymentId) {
        boolean isPaymentValid = orderService.verifyPayment(paymentId);
        if (!isPaymentValid) {
            paymentService.refundPayment(paymentId);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        Order order = orderService.createOrder(orderDTO , paymentId);
        return ResponseEntity.ok(order.getId());
    }

    @PutMapping("/acceptOrder/{id}")
    public ResponseEntity<Order> acceptOrder(@PathVariable Long id, @RequestParam String action) {
        Order order = orderService.acceptOrder(id, action);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/userOrders")
    public List<Order> getUserOrders(@RequestParam String username) {
        return orderService.getOrdersByBuyerUsername(username);
    }

    @GetMapping("orderStatus/{id}")
    public ResponseEntity<Order> getOrderStatus(@PathVariable Long id) {
        Order order = orderService.getOrderStatus(id);
        return ResponseEntity.ok(order);
    }
}
