package com.ecommerce.ecommerce.dto.order;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    private Long id;
    private String status;
    private String paymentMethod;
    private String shippingAddress;
    private double totalPrice;
    private String buyerUsername;
    private String sellerUsername;
    private List<OrderItemDTO> orderItems;
}
