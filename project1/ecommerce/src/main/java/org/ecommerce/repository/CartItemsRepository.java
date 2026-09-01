package org.ecommerce.repository;

import org.ecommerce.model.CartItems;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartItemsRepository extends JpaRepository<CartItems, UUID>
{
    Optional<CartItems> findByCartIdAndProductIdAndIsDeletedFalseAndProductIsDeletedFalse(UUID cartId, UUID productId);
}
