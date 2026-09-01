package org.ecommerce.service.impl;

import lombok.RequiredArgsConstructor;
import org.ecommerce.dto.ProductRequest;
import org.ecommerce.dto.ProductResponse;
import org.ecommerce.exception.ResourceNotFoundException;
import org.ecommerce.model.Product;
import org.ecommerce.repository.ProductRepository;
import org.ecommerce.service.api.ProductService;
import org.ecommerce.utility.AppLogger;
import org.slf4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService
{
    private static final Logger log = AppLogger.getLogger(ProductServiceImpl.class);

    private final ProductRepository productRepository;

    @Override
    public ProductResponse addProduct(ProductRequest request) {

        log.info("Adding new product: {}", request.getName());

        try {
            Product product = new Product();
            BeanUtils.copyProperties(request, product);
            Product savedProduct = productRepository.save(product);

            log.info("Product added successfully. Product ID: {}", savedProduct.getId());

            ProductResponse response = new ProductResponse();
            BeanUtils.copyProperties(savedProduct, response);
            return response;
        } catch (Exception e) {
            log.error("Error while adding product: {}", request.getName(), e);

            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public List<ProductResponse> getAllProducts() {
        log.info("Fetching all active products");
        try {
            List<Product> products = productRepository.findByIsDeletedFalse();
            List<ProductResponse> responses = new ArrayList<>();
            for (Product product : products) {
                ProductResponse response = new ProductResponse();
                BeanUtils.copyProperties(product, response);
                responses.add(response);
            }
            log.info("Successfully fetched {} active products", responses.size());
            return responses;
        } catch (Exception e) {
            log.error("Error while fetching all products", e);
            throw new RuntimeException(e.getMessage());
        }
    }


    @Override
    public ProductResponse getProductById(UUID id) {

        log.info("Fetching product with ID: {}", id);

        try {

            Product product = productRepository.findByIdAndIsDeletedFalse(id)
                            .orElseThrow(() ->
                                    new ResourceNotFoundException("Product not found with ID: " + id));

            ProductResponse response = new ProductResponse();
            BeanUtils.copyProperties(product, response);
            log.info("Product fetched successfully. Product ID: {}", id);
            return response;
        } catch (ResourceNotFoundException e) {
            log.warn("Product not found. Product ID: {}", id);
            throw e;
        } catch (Exception e) {
            log.error("Error while fetching product with ID: {}", id, e);
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public List<ProductResponse> getProductsByName(String name) {

        log.info("Searching products by name: {}", name);

        try {
            List<Product> products = productRepository.findByNameContainingIgnoreCaseAndIsDeletedFalse(name);
            List<ProductResponse> responses = new ArrayList<>();
            for (Product product : products) {
                ProductResponse response = new ProductResponse();
                BeanUtils.copyProperties(product, response);
                responses.add(response);
            }
            log.info("Found {} products for name: {}", responses.size(), name);
            return responses;
        } catch (Exception e) {
            log.error("Error while searching products by name: {}", name, e);

            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public List<ProductResponse> getProductsByCategory(String category) {
        log.info("Fetching products by category: {}", category);
        try {
            List<Product> products = productRepository.findByCategoryAndIsDeletedFalse(category);
            List<ProductResponse> responses = new ArrayList<>();
            for (Product product : products) {

                ProductResponse response = new ProductResponse();
                BeanUtils.copyProperties(product, response);
                responses.add(response);
            }

            log.info("Found {} products for category: {}", responses.size(), category);
            return responses;
        } catch (Exception e) {
            log.error("Error while fetching products by category: {}", category, e);
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public List<ProductResponse> getProductsByPrice(BigDecimal price) {
        log.info("Fetching products with price less than or equal to: {}", price);
        try {
            List<Product> products = productRepository.findByPriceLessThanEqualAndIsDeletedFalse(price);
            List<ProductResponse> responses = new ArrayList<>();
            for (Product product : products) {
                ProductResponse response = new ProductResponse();
                BeanUtils.copyProperties(product, response);
                responses.add(response);
            }
            log.info("Found {} products for price filter: {}", responses.size(), price);
            return responses;
        } catch (Exception e) {
            log.error("Error while fetching products by price: {}", price, e);
            throw new RuntimeException(e.getMessage());
        }
    }


    @Override
    public ProductResponse updateProduct(UUID id, ProductRequest request) {
        log.info("Updating product with ID: {}", id);
        try {
            Product product = productRepository.findByIdAndIsDeletedFalse(id)
                            .orElseThrow(() -> new ResourceNotFoundException(
                                            "Product not found with ID: " + id));
            BeanUtils.copyProperties(request, product);
            Product updatedProduct = productRepository.save(product);
            ProductResponse response = new ProductResponse();
            BeanUtils.copyProperties(updatedProduct, response);
            log.info("Product updated successfully. Product ID: {}", id);
            return response;
        } catch (ResourceNotFoundException e) {
            log.warn("Product not found for update. Product ID: {}", id);
            throw e;
        } catch (Exception e) {
            log.error("Error while updating product with ID: {}", id, e);
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public void deleteProduct(UUID id) {

        log.info("Deleting product with ID: {}", id);

        try {
            Product product = productRepository.findByIdAndIsDeletedFalse(id)
                            .orElseThrow(() ->
                                    new ResourceNotFoundException("Product not found with ID: " + id));

            product.setIsDeleted(true);
            productRepository.save(product);
            log.info("Product deleted successfully. Product ID: {}", id);

        } catch (ResourceNotFoundException e) {
            log.warn("Product not found for deletion. Product ID: {}", id);
            throw e;
        } catch (Exception e) {
            log.error("Error while deleting product with ID: {}", id, e);
            throw new RuntimeException(e.getMessage());
        }
    }

}
