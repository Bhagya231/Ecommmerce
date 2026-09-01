package org.ecommerce.facade.impl;

import lombok.RequiredArgsConstructor;
import org.ecommerce.dto.ProductRequest;
import org.ecommerce.dto.ProductResponse;
import org.ecommerce.exception.AccessDeniedException;
import org.ecommerce.exception.ResourceNotFoundException;
import org.ecommerce.facade.api.ProductFacade;
import org.ecommerce.model.User;
import org.ecommerce.repository.UserRepository;
import org.ecommerce.service.api.ProductService;
import org.ecommerce.utility.AppLogger;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProductFacadeImpl implements ProductFacade
{

    private static final Logger log = AppLogger.getLogger(ProductFacadeImpl.class);

    private final ProductService productService;
    private final UserRepository userRepository;


    @Override
    public List<ProductResponse> getAllProducts() {

        log.info("Fetching all products");

        return productService.getAllProducts();
    }

    @Override
    public ProductResponse getProductById(UUID id) {

        log.info("Fetching product with ID: {}",id);

        return productService.getProductById(id);
    }

    @Override
    public List<ProductResponse> getProductsByName(String name) {

        log.info("Searching products by name: {}", name);

        return productService.getProductsByName(name);
    }

    @Override
    public List<ProductResponse> getProductsByCategory(String category) {

        log.info("Fetching products by category: {}", category);
        return productService.getProductsByCategory(category);
    }

    @Override
    public List<ProductResponse> getProductsByPrice(BigDecimal price) {

        log.info("Fetching products by price: {}", price);

        return productService.getProductsByPrice(price);
    }


    private User checkAdmin(String email) {
        log.info("Checking admin access for email: {}", email);
        User user = userRepository.findByEmail(email).orElseThrow(() -> {
            log.warn("Admin check failed. User not found: {}", email);
            return new ResourceNotFoundException("User not found");
        });
        if (!"ADMIN".equals(user.getRole())) {
            log.warn("Access denied. User is not an admin. Email: {}", email);
            throw new AccessDeniedException("Access denied. Only admin can perform this operation");
        }
        log.info("Admin access verified successfully. Email: {}", email);
        return user;
    }

    @Override
        public ProductResponse addProduct(String email, ProductRequest request) {
            log.info("Admin attempting to add product. Email: {}", email);

            checkAdmin(email);
            ProductResponse response = productService.addProduct(request);
            log.info("Product added successfully by admin. Email: {}", email);
            return response;
        }

        @Override
        public ProductResponse updateProduct(String email, UUID id, ProductRequest request) {
            log.info("Admin attempting to update product. Email: {}, Product ID: {}", email, id);

            checkAdmin(email);
            ProductResponse response = productService.updateProduct(id, request);
            log.info("Product updated successfully by admin. Email: {}, Product ID: {}", email, id);
            return response;
        }

        @Override
        public void deleteProduct(String email, UUID id) {
            log.info("Admin attempting to delete product. Email: {}, Product ID: {}", email, id);
            checkAdmin(email);
            productService.deleteProduct(id);
            log.info("Product deleted successfully by admin. Email: {}, Product ID: {}", email, id);
        }
}
