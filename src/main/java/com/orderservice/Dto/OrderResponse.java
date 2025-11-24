package com.orderservice.Dto;

import java.util.List;

public record OrderResponse(Long id, Long userId, Double totalAmount, String status, List<OrderItemRequest> items) {
}
