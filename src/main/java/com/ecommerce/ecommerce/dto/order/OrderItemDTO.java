package com.ecommerce.ecommerce.dto.order;

import com.ecommerce.ecommerce.dto.product.ProductDTO;
import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDTO {
    private Long id;
    private ProductDTO product;
    private int quantity;
    private double price;
}
