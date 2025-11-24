package com.orderservice.Dto;

public record OrderItemRequest(
        Long productId,
        String productName,
        Double price,
        Integer quantity,
        Double subtotal
) {}

