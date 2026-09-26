package com.guvi.ecommerceApi.Service;

import com.guvi.ecommerceApi.DTO.OrderItemDTO;
import com.guvi.ecommerceApi.DTO.OrderResponseDTO;
import com.guvi.ecommerceApi.Entity.OrderStatus;
import com.guvi.ecommerceApi.Exception.BadRequestException;
import com.guvi.ecommerceApi.Exception.InsufficientStockException;
import com.guvi.ecommerceApi.Exception.InvalidOrderStateException;
import com.guvi.ecommerceApi.Exception.ResourceNotFoundException;
import com.guvi.ecommerceApi.Model.*;
import com.guvi.ecommerceApi.Repository.CartRepository;
import com.guvi.ecommerceApi.Repository.OrderRepository;
import com.guvi.ecommerceApi.Repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService{

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private MongoTemplate mongoTemplate;

    public OrderServiceImpl(MongoTemplate mongoTemplate,OrderRepository orderRepository, CartRepository cartRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public OrderResponseDTO placeOrder(String userId) {
        Cart cart = cartRepository.findByUserId(userId);

        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new BadRequestException("Cart is empty, cannot place order");
        }

        //Validating the stock
        for (CartItems items : cart.getItems()) {
            Product product = productRepository.findByProductId(items.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + items.getProductId()));
            Query query = new Query(Criteria.where("productId").is(items.getProductId())
                    .and("stockQuantity").gte(items.getQuantity()));
            Update update = new Update().inc("stockQuantity", -items.getQuantity());

            Product updated = mongoTemplate.findAndModify(query, update, Product.class);
            if (updated == null) {
                throw new InsufficientStockException("Insufficient stock for product: " + items.getProductId());
            }
        }

        // creating the order
        List<OrderItem> orderitems =  cart.getItems().stream()
                .map(i -> new OrderItem(i.getProductId(),i.getQuantity(),i.getPrice()))
                .toList();

        BigDecimal totalAmount = orderitems.stream()
                .map(i -> i.getPriceAtPurchase().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = new Order(null, userId, orderitems, totalAmount, OrderStatus.PLACED);
        Order savedOrder = orderRepository.save(order);

        log.info("Order successfully placed! orderId: {}, userId: {}, totalAmount: {}",
                savedOrder.getOrderId(), userId, totalAmount);

        //clear cart
        cart.setItems(List.of());
        cartRepository.save(cart);

        return mapToResponseDTO(savedOrder);
    }

    @Override
    public OrderResponseDTO cancelOrder(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new InvalidOrderStateException("Order is already cancelled");
        }
        if (order.getStatus() == OrderStatus.SHIPPED) {
            throw new InvalidOrderStateException("Shipped orders cannot be cancelled");
        }

        // Restore stock for each item
        for (OrderItem item : order.getOrderItems()) {
            Query query = new Query(Criteria.where("productId").is(item.getProductId()));
            Update update = new Update().inc("stockQuantity", item.getQuantity());
            mongoTemplate.updateFirst(query, update, Product.class);
            log.info("Order {} successfully CANCELLED and stock restored", orderId);
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        return mapToResponseDTO(order);
    }

    @Override
    public OrderResponseDTO viewOrder(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        return mapToResponseDTO(order);
    }

    @Override
    public List<OrderResponseDTO> viewOrdersByUser(String userId) {
        return orderRepository.findByUserId(userId).stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    private OrderResponseDTO mapToResponseDTO(Order order) {
        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setOrderId(order.getOrderId());
        dto.setUserId(order.getUserId());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus());

        List<OrderItemDTO> items = order.getOrderItems().stream().map(i -> {
            OrderItemDTO itemDto = new OrderItemDTO();
            itemDto.setProductId(i.getProductId());
            itemDto.setQuantity(i.getQuantity());
            itemDto.setPriceAtPurchase(i.getPriceAtPurchase());
            return itemDto;
        }).toList();

        dto.setItems(items);
        return dto;
    }
}
