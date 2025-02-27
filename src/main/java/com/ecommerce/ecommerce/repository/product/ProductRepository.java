package com.ecommerce.ecommerce.repository.product;

import com.ecommerce.ecommerce.model.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    Product findById(Long id);
}
