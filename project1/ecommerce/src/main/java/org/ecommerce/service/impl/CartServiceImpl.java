package org.ecommerce.service.impl;

import lombok.RequiredArgsConstructor;
import org.ecommerce.dto.CartItemsResponse;
import org.ecommerce.dto.CartRequest;
import org.ecommerce.dto.CartResponse;
import org.ecommerce.exception.BadRequestException;
import org.ecommerce.exception.ResourceNotFoundException;
import org.ecommerce.model.Cart;
import org.ecommerce.model.CartItems;
import org.ecommerce.model.Product;
import org.ecommerce.model.User;
import org.ecommerce.repository.CartItemsRepository;
import org.ecommerce.repository.CartRepository;
import org.ecommerce.repository.ProductRepository;
import org.ecommerce.repository.UserRepository;
import org.ecommerce.service.api.CartService;
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
public class CartServiceImpl implements CartService
{
    private static final Logger log = AppLogger.getLogger(CartServiceImpl.class);

    private final CartRepository cartRepository;
    private final CartItemsRepository cartItemsRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;


    @Override
    public CartResponse addToCart(UUID userId, CartRequest request)
    {
        log.info("Adding product to cart. User ID: {}", userId);

        try {
            User user = userRepository.findById(userId).orElseThrow(() ->
                            new ResourceNotFoundException("User not found with ID: " + userId));

            Product product = productRepository.findByIdAndIsDeletedFalse(request.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product not found with ID: " + request.getProductId()));

            if (request.getQuantity() <= 0) {
                throw new BadRequestException("Quantity must be greater than zero");
            }
            if (product.getStock() < request.getQuantity())
            {
                throw new BadRequestException("Insufficient stock");
            }

            Cart cart = cartRepository.findByUserIdAndUserIsDeletedFalse(userId)
                    .orElse(null);

            if (cart == null)
            {
                cart = new Cart();
                cart.setUser(user);
                cart = cartRepository.save(cart);
            }

            CartItems cartItem = cartItemsRepository
                    .findByCartIdAndProductIdAndIsDeletedFalseAndProductIsDeletedFalse(cart.getId(), product.getId())
                    .orElse(null);

            if (cartItem == null) {

                cartItem = new CartItems();
                cartItem.setCart(cart);
                cartItem.setProduct(product);
                cartItem.setQuantity(request.getQuantity());
                cart.getCartItems().add(cartItem);

            } else {

                int quantity = cartItem.getQuantity()
                        + request.getQuantity();

                if (quantity > product.getStock()) {
                    throw new BadRequestException("Insufficient stock");
                }

                cartItem.setQuantity(quantity);
            }

            cartItemsRepository.save(cartItem);

            log.info("Product added to cart successfully");

            return getCartResponse(cart);

        } catch (ResourceNotFoundException | BadRequestException e)
        {
            log.warn("Unable to add product to cart: {}", e.getMessage());
            throw e;

        } catch (Exception e) {
            log.error("Error while adding product to cart", e);
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public CartResponse getCart(UUID userId)
    {
        log.info("Fetching cart for user ID: {}", userId);

        try {
            Cart cart = cartRepository.findByUserIdAndUserIsDeletedFalse(userId).orElseThrow(() ->
                            new ResourceNotFoundException("Cart not found for user ID: " + userId));

            return getCartResponse(cart);

        } catch (ResourceNotFoundException e)
        {
            log.warn("Unable to get products in the cart: {}", e.getMessage());
            throw e;

        } catch (Exception e) {
            log.error("Error while fetching cart for user ID: {}", userId, e);
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public CartResponse increaseQuantity(UUID userId, UUID productId)
    {
        log.info("Increasing product quantity. User ID: {}", userId);

        try {
            CartItems cartItem = getCartItem(userId, productId);

            Product product = cartItem.getProduct();

            if (cartItem.getQuantity() >= product.getStock())
            {
                throw new BadRequestException("Insufficient stock");
            }

            cartItem.setQuantity(cartItem.getQuantity() + 1);

            cartItemsRepository.save(cartItem);

            return getCartResponse(cartItem.getCart());

        } catch (ResourceNotFoundException | BadRequestException e)
        {
            log.warn("Unable to increase quantity of product to cart: {}", e.getMessage());
            throw e;

        } catch (Exception e) {
            log.error("Error while increasing quantity", e);
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public CartResponse decreaseQuantity(UUID userId, UUID productId) {

        log.info("Decreasing product quantity. User ID: {}", userId);

        try {
            CartItems cartItem = getCartItem(userId, productId);

            Cart cart = cartItem.getCart();

            if (cartItem.getQuantity() > 1)
            {
                cartItem.setQuantity(cartItem.getQuantity() - 1);
                cartItemsRepository.save(cartItem);

            } else {
                cartItem.setIsDeleted(true);
                cartItemsRepository.save(cartItem);
            }

            return getCartResponse(cart);

        } catch (ResourceNotFoundException e)
        {
            log.warn("Unable to decrease quantity of product to cart: {}", e.getMessage());
            throw e;

        } catch (Exception e)
        {
            log.error("Error while decreasing quantity", e);
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public void removeFromCart(UUID userId, UUID productId)
    {
        log.info("Removing product from cart. User ID: {}", userId);

        try {
            CartItems cartItem = getCartItem(userId, productId);

            cartItem.setIsDeleted(true);
            cartItemsRepository.save(cartItem);

            log.info("Product removed from cart successfully");

        } catch (ResourceNotFoundException e)
        {
            log.warn("Unable to remove the product from cart: {}", e.getMessage());
            throw e;

        } catch (Exception e) {
            log.error("Error while removing product from cart", e);
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public void clearCart(UUID userId)
    {
        log.info("Clearing cart for user ID: {}", userId);

        try {
            Cart cart = cartRepository.findByUserIdAndUserIsDeletedFalse(userId)
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Cart not found for user ID: " + userId));

            for (CartItems cartItem : cart.getCartItems())
            {
                cartItem.setIsDeleted(true);
            }

            cartItemsRepository.saveAll(cart.getCartItems());

            log.info("Cart cleared successfully for user ID: {}", userId);

        } catch (ResourceNotFoundException e)
        {
            log.warn("Unable to clear cart: {}", e.getMessage());
            throw e;

        } catch (Exception e)
        {
            log.error("Error while clearing cart for user ID: {}", userId, e);
            throw new RuntimeException(e.getMessage());
        }
    }

    private CartItems getCartItem(UUID userId, UUID productId)
    {
        Cart cart = cartRepository.findByUserIdAndUserIsDeletedFalse(userId).orElseThrow(() ->
                        new ResourceNotFoundException("Cart not found"));

        return cartItemsRepository.findByCartIdAndProductIdAndIsDeletedFalseAndProductIsDeletedFalse(cart.getId(), productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found in cart"));
    }

    private CartResponse getCartResponse(Cart cart)
    {
        CartResponse response = new CartResponse();

        response.setCartId(cart.getId());
        response.setUserId(cart.getUser().getId());

        List<CartItemsResponse> items = new ArrayList<>();

        BigDecimal subtotal = BigDecimal.ZERO;

        for (CartItems cartItem : cart.getCartItems())
        {
            if (cartItem.getIsDeleted())
            {
                continue;
            }

            CartItemsResponse item = new CartItemsResponse();

            BeanUtils.copyProperties(cartItem, item);

            item.setId(cartItem.getId());
            item.setProductId(cartItem.getProduct().getId());
            item.setProductName(cartItem.getProduct().getName());
            item.setCategory(cartItem.getProduct().getCategory());
            item.setPrice(cartItem.getProduct().getPrice());
            item.setStock(cartItem.getProduct().getStock());

            BigDecimal itemTotal = cartItem.getProduct().getPrice()
                    .multiply(BigDecimal.valueOf(cartItem.getQuantity()));

            item.setItemTotal(itemTotal);

            subtotal = subtotal.add(itemTotal);

            items.add(item);
        }

        BigDecimal discount = BigDecimal.ZERO;

        if (subtotal.compareTo(BigDecimal.valueOf(5000)) >= 0)
        {
            discount = subtotal.multiply(BigDecimal.valueOf(0.10));
        }
        else if (subtotal.compareTo(BigDecimal.valueOf(3000)) >= 0)
        {
            discount = subtotal.multiply(BigDecimal.valueOf(0.05));
        }

        BigDecimal total = subtotal.subtract(discount);

        response.setCartItems(items);
        response.setSubtotal(subtotal);
        response.setDiscount(discount);
        response.setTotal(total);

        return response;
    }


}
