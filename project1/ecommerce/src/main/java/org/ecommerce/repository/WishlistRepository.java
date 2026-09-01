package org.ecommerce.repository;

import org.ecommerce.model.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, UUID>
{


    List<Wishlist> findByUserIdAndIsDeletedFalse(UUID userId);

    Optional<Wishlist> findByUserIdAndProductIdAndIsDeletedFalse(UUID userId, UUID productId);


}
