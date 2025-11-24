package com.orderservice;

import com.orderservice.Dto.OrderCreateRequest;
import com.orderservice.Dto.OrderResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    private Long getUserId(HttpServletRequest request) {
        return Long.valueOf(request.getHeader("X-User-Id"));
    }

    @PostMapping
    public ResponseEntity<Long> createOrder(HttpServletRequest request, @RequestBody OrderCreateRequest req) {
        req.userId();
        Long orderId = orderService.createOrder(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(orderId);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getOrder(orderId));
    }

    @GetMapping("/user")
    public ResponseEntity<List<OrderResponse>> getOrdersForUser(HttpServletRequest request) {
        Long userId = getUserId(request);
        return ResponseEntity.ok(orderService.getOrdersForUser(userId));
    }
}
