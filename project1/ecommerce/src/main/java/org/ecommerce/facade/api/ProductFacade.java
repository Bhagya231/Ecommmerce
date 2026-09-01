package org.ecommerce.facade.api;

import org.ecommerce.dto.ProductRequest;
import org.ecommerce.dto.ProductResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface ProductFacade
{
    ProductResponse addProduct(String email, ProductRequest request);

    List<ProductResponse> getAllProducts();

    ProductResponse getProductById(UUID id);

    List<ProductResponse> getProductsByName(String name);

    List<ProductResponse> getProductsByCategory(String category);

    List<ProductResponse> getProductsByPrice(BigDecimal price);

    ProductResponse updateProduct(String email, UUID id, ProductRequest request);

    void deleteProduct(String email, UUID id);
}
