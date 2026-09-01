package org.ecommerce.service.api;

import org.ecommerce.dto.ProductRequest;
import org.ecommerce.dto.ProductResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface ProductService
{
    ProductResponse addProduct(ProductRequest request);

    ProductResponse getProductById(UUID id);

    List<ProductResponse> getAllProducts();

    List<ProductResponse> getProductsByName(String name);

    List<ProductResponse> getProductsByCategory(String category);

    List<ProductResponse> getProductsByPrice(BigDecimal price);

    ProductResponse updateProduct(UUID id, ProductRequest request);

    void deleteProduct(UUID id);
}
