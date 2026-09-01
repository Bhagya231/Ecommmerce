package org.ecommerce.service.impl;

import lombok.RequiredArgsConstructor;

import org.ecommerce.dto.OrderItemRequest;
import org.ecommerce.dto.OrderItemsResponse;
import org.ecommerce.dto.OrderRequest;
import org.ecommerce.dto.OrderResponse;
import org.ecommerce.exception.BadRequestException;
import org.ecommerce.exception.ResourceNotFoundException;

import org.ecommerce.model.*;
import org.ecommerce.repository.*;
import org.ecommerce.service.api.CartService;
import org.ecommerce.service.api.OrderService;
import org.ecommerce.utility.AppLogger;
import org.slf4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService
{
    private static final Logger log = AppLogger.getLogger(OrderServiceImpl.class);

    private final OrderRepository orderRepository;
    private final OrderItemsRepository orderItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final CartService cartService;
    private final CartItemsRepository cartItemsRepository;


    @Transactional
    @Override
    public OrderResponse placeOrder(UUID userId, OrderRequest request)
    {
        log.info("Placing order for user ID: {}", userId);

        try
        {
            User user = userRepository.findById(userId).orElseThrow(() ->
                            new ResourceNotFoundException("User not found with ID: " + userId));

            Cart cart = cartRepository.findByUserIdAndUserIsDeletedFalse(userId).orElseThrow(() ->
                            new ResourceNotFoundException("Cart not found for user ID: " + userId));

            Order order = new Order();

            order.setUser(user);
            order.setDeliveryDate(LocalDate.now().plusDays(5));
            order.setAddress(request.getAddress());
            order.setPhone(user.getPhone());
            order.setOrderDate(LocalDate.now());
            order.setOrderNumber(generateOrderNumber());

            BigDecimal cost = BigDecimal.ZERO;

            List<OrderItems> orderItems = new ArrayList<>();

            for (OrderItemRequest itemRequest : request.getOrderItems())
            {
                UUID productId = itemRequest.getProductId();

                CartItems cartItem = cartItemsRepository
                        .findByCartIdAndProductIdAndIsDeletedFalseAndProductIsDeletedFalse(cart.getId(),
                                productId).orElseThrow(() ->
                                new ResourceNotFoundException("Product not found in cart"));

                Product product = cartItem.getProduct();

                int quantity = cartItem.getQuantity();

                if (quantity <= 0)
                {
                    throw new BadRequestException("Quantity must be greater than zero");
                }

                if (product.getStock() < quantity)
                {
                    throw new BadRequestException("Insufficient stock for product: " + product.getName());
                }

                OrderItems orderItem = new OrderItems();

                orderItem.setOrder(order);
                orderItem.setProduct(product);
                orderItem.setQuantity(quantity);
                orderItem.setUnitPrice(product.getPrice());

                orderItems.add(orderItem);

                BigDecimal itemCost = product.getPrice().multiply(BigDecimal.valueOf(quantity));

                cost = cost.add(itemCost);

                product.setStock(product.getStock() - quantity);

                productRepository.save(product);
            }

            BigDecimal discount = calculateDiscount(cost);

            order.setCost(cost);
            order.setDiscount(discount);
            order.setOrderItems(orderItems);

            Order savedOrder = orderRepository.save(order);

            orderItemRepository.saveAll(orderItems);

            for (OrderItemRequest itemRequest : request.getOrderItems())
            {
                cartService.removeFromCart(userId, itemRequest.getProductId());
            }

            log.info("Order placed successfully. Order ID: {}", savedOrder.getId());

            return convertToResponse(savedOrder, orderItems);
        }
        catch (ResourceNotFoundException | BadRequestException e)
        {
            log.warn("Order placement failed: {}", e.getMessage());
            throw e;
        }
        catch (Exception e)
        {
            log.error("Error while placing order for user ID: {}", userId, e);

            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public List<OrderResponse> getCustomerOrders(UUID userId) {

        log.info("Fetching orders for customer ID: {}", userId);

        try {
            List<Order> orders = orderRepository.findCustomerOrders(userId);

            List<OrderResponse> responses = new ArrayList<>();

            for (Order order : orders) {
                responses.add(convertToResponse(order, order.getOrderItems()));
            }

            log.info("Found {} orders for customer ID: {}", responses.size(), userId);

            return responses;

        } catch (Exception e) {
            log.error("Error while fetching orders for user ID: {}", userId, e);
            throw new RuntimeException(e.getMessage());
        }
    }


    @Override
    public OrderResponse getCustomerOrder(UUID orderId, UUID userId) {

        log.info("Fetching order ID: {} for customer ID: {}", orderId, userId);

        try {
            Order order = orderRepository.findCustomerOrder(orderId, userId)
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Order not found with ID: " + orderId));

            return convertToResponse(order, order.getOrderItems());

        } catch (ResourceNotFoundException e) {
            log.warn("Order not found with ID: {}", orderId);
            throw e;

        } catch (Exception e) {
            log.error("Error while fetching order ID: {}", orderId, e);
            throw new RuntimeException(e.getMessage());
        }
    }


    @Override
    public List<OrderResponse> getAllOrders() {

        log.info("Fetching all active orders");

        try {
            List<Order> orders = orderRepository.findAllActiveOrders();

            List<OrderResponse> responses = new ArrayList<>();

            for (Order order : orders) {
                responses.add(convertToResponse(order, order.getOrderItems()));
            }

            log.info("Found {} active orders", responses.size());

            return responses;

        } catch (Exception e) {
            log.error("Error while fetching all orders", e);
            throw new RuntimeException(e.getMessage());
        }
    }


    @Override
    public OrderResponse getOrderByOrderNumber(String orderNumber) {

        log.info("Fetching order with number: {}", orderNumber);

        try {
            Order order = orderRepository.getOrderByOrderNumber(orderNumber)
                    .filter(existingOrder -> !existingOrder.getIsDeleted())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Order not found with number: " + orderNumber));

            System.out.println("Order number received: " + orderNumber);
            System.out.println("Found: " + order);
            return convertToResponse(order, order.getOrderItems());

        } catch (ResourceNotFoundException e) {
            log.warn("Order not found with number: {}", orderNumber);
            throw e;

        } catch (Exception e) {
            log.error("Error while fetching order: {}", orderNumber, e);
            throw new RuntimeException(e.getMessage());
        }
    }


    private String generateOrderNumber() {

        int number = (int) (Math.random() * 90000000) + 10000000;

        return "RD-" + number;
    }


    private BigDecimal calculateDiscount(BigDecimal cost)
    {

        if (cost.compareTo(new BigDecimal("5000")) >= 0) {
            return cost.multiply(new BigDecimal("0.10"));
        }

        if (cost.compareTo(new BigDecimal("3000")) >= 0) {
            return cost.multiply(new BigDecimal("0.05"));
        }

        return BigDecimal.ZERO;
    }


    private OrderResponse convertToResponse(Order order, List<OrderItems> orderItems)
    {

        OrderResponse response = new OrderResponse();

        BeanUtils.copyProperties(order, response);

        response.setOrderId(order.getId());
        response.setUserId(order.getUser().getId());
        response.setCustomerName(order.getUser().getName());
        response.setCustomerEmail(order.getUser().getEmail());
        response.setPhone(order.getUser().getPhone());

        List<OrderItemsResponse> itemResponses = new ArrayList<>();

        for (OrderItems orderItem : orderItems) {

            OrderItemsResponse itemResponse = new OrderItemsResponse();

            BeanUtils.copyProperties(orderItem, itemResponse);

            itemResponse.setOrderItemId(orderItem.getId());
            itemResponse.setProductId(orderItem.getProduct().getId());
            itemResponse.setProductName(orderItem.getProduct().getName());

            itemResponses.add(itemResponse);
        }

        response.setOrderItems(itemResponses);

        return response;
    }

}
