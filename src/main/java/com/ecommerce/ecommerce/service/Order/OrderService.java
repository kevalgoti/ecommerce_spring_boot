package com.ecommerce.ecommerce.service.Order;

import com.ecommerce.ecommerce.dto.order.OrderDTO;
import com.ecommerce.ecommerce.model.order.Order;
import com.ecommerce.ecommerce.model.order.OrderItem;
import com.ecommerce.ecommerce.model.order.OrderStatus;
import com.ecommerce.ecommerce.model.product.Product;
import com.ecommerce.ecommerce.service.jwt.JwtService;
import com.ecommerce.ecommerce.service.paymentService.PaymentService;
import com.ecommerce.ecommerce.repository.order.OrderRepository;
import com.ecommerce.ecommerce.repository.product.ProductRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final JwtService jwtService;
    private final PaymentService paymentService;

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository,
                        JwtService jwtService, PaymentService paymentService) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.jwtService = jwtService;
        this.paymentService = paymentService;
    }

    public Order createOrder(OrderDTO orderDTO, String paymentId) {
        String role = jwtService.extractRoleFromContext();
        if (!"BUYER".equals(role)) {
            throw new RuntimeException("Unauthorized: Only BUYER can place an order.");
        }

        Order order = new Order();
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentMethod(orderDTO.getPaymentMethod());
        order.setShippingAddress(orderDTO.getShippingAddress());
        order.setBuyerUsername(orderDTO.getBuyerUsername());
        order.setSellerUsername(orderDTO.getSellerUsername());
        order.setPaymentId(paymentId);

        List<OrderItem> orderItems = orderDTO.getOrderItems().stream().map(itemDTO -> {
            Product product = productRepository.findById(itemDTO.getProduct().getId());
            if (product == null) {
                throw new RuntimeException("Product not found.");
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(itemDTO.getQuantity());
            orderItem.setPrice(product.getPrice().doubleValue() * itemDTO.getQuantity());
            orderItem.setOrder(order);
            return orderItem;
        }).collect(Collectors.toList());

        order.setOrderItems(orderItems);
        order.setTotalPrice(orderItems.stream().mapToDouble(OrderItem::getPrice).sum());

        return orderRepository.save(order);
    }

    public Order acceptOrder(Long id, String action) {
        String role = jwtService.extractRoleFromContext();
        if (!"SELLER".equals(role)) {
            throw new RuntimeException("Unauthorized: Only SELLERS can accept orders.");
        }

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if ("FULFILLED".equalsIgnoreCase(action)) {
            order.setStatus(OrderStatus.FULFILLED);
        } else if ("REJECTED".equalsIgnoreCase(action)) {
            order.setStatus(OrderStatus.REJECTED);
        } else {
            throw new IllegalArgumentException("Invalid action. Use 'FULFILLED' or 'REJECTED'.");
        }

        return orderRepository.save(order);
    }

    public Order getOrderStatus(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    public boolean verifyPayment(String paymentId) {
        return paymentService.verifyPayment(paymentId);
    }


    private String getCurrentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    public List<Order> getOrdersByBuyerUsername(String username) {
        String currentUsername = getCurrentUsername();
        if (currentUsername.equals(username) == false) {
            throw new RuntimeException("Unauthorized: User not logged in.");
        }
        return orderRepository.findByBuyerUsername(username);
    }
}
