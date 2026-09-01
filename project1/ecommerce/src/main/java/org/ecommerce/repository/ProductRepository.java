package org.ecommerce.repository;

import org.ecommerce.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID>
{
    List<Product> findByIsDeletedFalse();

    Optional<Product> findByIdAndIsDeletedFalse(UUID id);

    List<Product> findByCategoryAndIsDeletedFalse(String category);

    List<Product> findByNameContainingIgnoreCaseAndIsDeletedFalse(String name);

    List<Product> findByPriceLessThanEqualAndIsDeletedFalse(BigDecimal price);




}
