package com.orderservice;

import com.orderservice.Dto.OrderCreateRequest;
import com.orderservice.Dto.OrderItemRequest;
import com.orderservice.Dto.OrderResponse;
import com.orderservice.Enum.OrderStatus;
import com.orderservice.client.ProductClient;
import com.orderservice.entity.Order;
import com.orderservice.entity.OrderItem;
import com.orderservice.exception.ResourceNotFoundException;
import com.orderservice.exception.BadRequestException;
import com.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductClient productClient;

    @Transactional
    public Long createOrder(OrderCreateRequest req) {

        if (req.items() == null || req.items().isEmpty()) {
            throw new BadRequestException("Order must contain at least one item");
        }

        Map<Long, Integer> reserved = new HashMap<>();

        try {
            for (OrderItemRequest itemReq : req.items()) {
                boolean ok = productClient.reserveStock(itemReq.productId(), itemReq.quantity());
                if (!ok) {
                    throw new BadRequestException("Insufficient stock for product " + itemReq.productId());
                }
                reserved.merge(itemReq.productId(), itemReq.quantity(), Integer::sum);
            }
        } catch (RuntimeException ex) {
            reserved.forEach((productId, qty) -> {
                try {
                    productClient.releaseStock(productId, qty);
                } catch (Exception ignored) {
                }
            });
            throw ex;
        }

        Order order = new Order();
        order.setUserId(req.userId());
        order.setTotalAmount(req.total());
        order.setStatus(OrderStatus.PENDING);

        List<OrderItem> items = new ArrayList<>();
        for (OrderItemRequest itemReq : req.items()) {
            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProductId(itemReq.productId());
            item.setProductName(itemReq.productName());
            item.setPrice(itemReq.price());
            item.setQuantity(itemReq.quantity());
            item.setSubtotal(itemReq.subtotal());
            items.add(item);
        }

        order.setItems(items);
        Order saved = orderRepository.save(order);

        // 4) Change status after saving successfully
        saved.setStatus(OrderStatus.CONFIRMED);

        return saved.getId();
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long id) {
        Order o = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        List<OrderItemRequest> items = o.getItems().stream()
                .map(i -> new OrderItemRequest(
                        i.getProductId(),
                        i.getProductName(),
                        i.getPrice(),
                        i.getQuantity(),
                        i.getSubtotal()
                ))
                .toList();

        return new OrderResponse(
                o.getId(),
                o.getUserId(),
                o.getTotalAmount(),
                o.getStatus().name(),
                items
        );
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersForUser(Long userId) {
        return orderRepository.findByUserId(userId).stream()
                .map(o -> new OrderResponse(
                        o.getId(),
                        o.getUserId(),
                        o.getTotalAmount(),
                        o.getStatus().name(),
                        o.getItems().stream()
                                .map(i -> new OrderItemRequest(
                                        i.getProductId(),
                                        i.getProductName(),
                                        i.getPrice(),
                                        i.getQuantity(),
                                        i.getSubtotal()
                                ))
                                .toList()
                ))
                .toList();
    }
}
