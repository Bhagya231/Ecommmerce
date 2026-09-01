package org.ecommerce.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ecommerce.dto.ProductRequest;
import org.ecommerce.dto.ProductResponse;
import org.ecommerce.facade.api.ProductFacade;
import org.ecommerce.utility.AppLogger;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@CrossOrigin
public class ProductController
{

    private static final Logger log = AppLogger.getLogger(ProductController.class);

    private final ProductFacade productFacade;

    @PostMapping
    public ResponseEntity<ProductResponse> addProduct(@RequestParam String email, @Valid @RequestBody ProductRequest request)
    {
        log.info("Add product request received. Admin email: {}, Product: {}", email, request.getName());

        ProductResponse response = productFacade.addProduct(email, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {

        log.info("Request received to fetch all products");

        List<ProductResponse> responses = productFacade.getAllProducts();

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable UUID id) {

        log.info("Request received to fetch product. Product ID: {}", id);
        ProductResponse response = productFacade.getProductById(id);


        return ResponseEntity.ok(response);
    }


    @GetMapping("/name")
    public ResponseEntity<List<ProductResponse>> getProductsByName(@RequestParam String name)
    {
        log.info("Request received to search products by name: {}", name);
        List<ProductResponse> responses = productFacade.getProductsByName(name);


        return ResponseEntity.ok(responses);
    }

    @GetMapping("/category")
    public ResponseEntity<List<ProductResponse>> getProductsByCategory(@RequestParam String category) {

        log.info("Request received to fetch products by category: {}", category);

        List<ProductResponse> responses = productFacade.getProductsByCategory(category);

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/price")
    public ResponseEntity<List<ProductResponse>> getProductsByPrice(@RequestParam BigDecimal price) {

        log.info("Request received to fetch products with price <= {}", price);

        List<ProductResponse> responses = productFacade.getProductsByPrice(price);

        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(@RequestParam String email, @PathVariable UUID id, @Valid @RequestBody ProductRequest request) {

        log.info("Update product request received. Admin email: {}, Product ID: {}", email, id);

        ProductResponse response = productFacade.updateProduct(email, id, request);


        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@RequestParam String email, @PathVariable UUID id) {

        log.info("Delete product request received. Admin email: {}, Product ID: {}", email, id);

        productFacade.deleteProduct(email, id);

        return ResponseEntity.noContent().build();
    }
}
