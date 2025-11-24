package com.orderservice.client;

import com.orderservice.exception.RemoteServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;


@Service
@RequiredArgsConstructor
public class ProductClient {

    private final RestTemplate restTemplate;

    @Value("${product.service.url}")
    private String productServiceUrl;

    public boolean reserveStock(Long productId, int qty) {
        String url = productServiceUrl + "/internal/" + productId + "/reserve";
        ReserveRequest req = new ReserveRequest(qty);
        try {
            ReserveResponse resp = restTemplate.postForObject(url, req, ReserveResponse.class);
            return resp != null && resp.success();
        } catch (RestClientException e) {
            throw new RemoteServiceException("Failed to reserve stock for product " + productId, e);
        }
    }

    public void releaseStock(Long productId, int qty) {
        String url = productServiceUrl + "/internal/" + productId + "/release";
        ReserveRequest req = new ReserveRequest(qty);
        try {
            restTemplate.postForObject(url, req, Void.class);
        } catch (RestClientException e) {
            throw new RemoteServiceException("Failed to release stock for product " + productId, e);
        }
    }

    public record ReserveRequest(Integer quantity) {}
    public record ReserveResponse(boolean success, String message) {}
}
