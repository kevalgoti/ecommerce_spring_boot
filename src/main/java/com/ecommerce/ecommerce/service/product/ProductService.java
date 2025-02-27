package com.ecommerce.ecommerce.service.product;

import com.ecommerce.ecommerce.dto.product.ProductDTO;
import com.ecommerce.ecommerce.model.product.Product;
import com.ecommerce.ecommerce.repository.product.ProductRepository;
import com.ecommerce.ecommerce.service.jwt.JwtService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final JwtService jwtService;

    public ProductService(ProductRepository productRepository, JwtService jwtService) {
        this.productRepository = productRepository;
        this.jwtService = jwtService;
    }

    public Product addProduct(ProductDTO productDTO) {
        validateSellerRole();

        Product product = new Product(
                productDTO.getName(),
                productDTO.getPrice(),
                productDTO.getDescription(),
                getCurrentUsername() // Assign authenticated seller's username
        );

        return productRepository.save(product);
    }

    public List<Product> getAllProducts() {


        return productRepository.findAll();
    }

    public Product updateProduct(Long id, ProductDTO productDTO) {
        validateSellerRole();

        Product existingProduct = productRepository.findById(id);
        if (existingProduct == null) {
            throw new RuntimeException("Product not found.");
        }

        existingProduct.setName(productDTO.getName());
        existingProduct.setPrice(productDTO.getPrice());
        existingProduct.setDescription(productDTO.getDescription());
        existingProduct.setSellerUsername(getCurrentUsername());

        return productRepository.save(existingProduct);
    }

    public ResponseEntity<String> deleteProduct(Long id) {
        validateSellerRole();

        if (productRepository.existsById(Math.toIntExact(id))) {
            productRepository.deleteById(Math.toIntExact(id));
            return ResponseEntity.ok("Product deleted successfully.");
        } else {
            throw new RuntimeException("Product not found.");
        }
    }

    private void validateSellerRole() {
        String role = getCurrentUserRole();
        if (!"SELLER".equals(role)) {
            throw new RuntimeException("Unauthorized: Only SELLER can perform this action.");
        }
    }

    private String getCurrentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private String getCurrentUserRole() {
        return jwtService.extractRoleFromContext(); // Extract role from JWT token
    }

    public Page<Product> findAll(Pageable pageable) {
        return productRepository.findAll(pageable);
    }
}
