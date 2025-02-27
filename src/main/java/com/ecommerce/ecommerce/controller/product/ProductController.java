package com.ecommerce.ecommerce.controller.product;

import com.ecommerce.ecommerce.dto.product.ProductDTO;
import com.ecommerce.ecommerce.model.product.Product;
import com.ecommerce.ecommerce.service.product.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping("/add")
     public ResponseEntity<Product> addProduct(@RequestBody ProductDTO productDTO) {
        return ResponseEntity.ok(productService.addProduct(productDTO));
    }

    @GetMapping("/all")
    public ResponseEntity<List<Product>> getAllProducts(
            @RequestParam(defaultValue = "0") int startIndex,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "true") boolean ascending
    ) {
        Sort sort = ascending ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        int page = startIndex / size;
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Product> products = productService.findAll(pageable);

        List<Product> productList = products.getContent();

        return ResponseEntity.ok(productList);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody ProductDTO productDTO) {
        return ResponseEntity.ok(productService.updateProduct(id, productDTO));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {
        return productService.deleteProduct(id);
    }   

}