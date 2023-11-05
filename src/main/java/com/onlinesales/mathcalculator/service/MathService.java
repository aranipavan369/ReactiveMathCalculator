package com.onlinesales.mathcalculator.service;

import com.onlinesales.mathcalculator.component.MathJsClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class MathService {

    private final MathJsClient mathJsClient;
    private final ConcurrentHashMap<String, TokenBucket> rateLimitMap = new ConcurrentHashMap<>();

    @Autowired
    public MathService(MathJsClient mathJsClient) {
        this.mathJsClient = mathJsClient;
    }

    public Flux<String> evaluateExpressions(List<String> expressions) {
        return Flux.fromIterable(expressions)
                .concatMap(expression -> {
                    if ("end".equalsIgnoreCase(expression)) {
                        return Mono.just("Evaluation ended.");
                    }
                    return evaluateExpression(expression)
                            .map(result -> expression + " => " + result+"\n")
                            .onErrorReturn("Error evaluating expression: " + expression);
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    private Mono<String> evaluateExpression(String expression) {
        try {
            // Rate limiting logic: Limit to 50 requests per second per client
            String clientKey = getClientKey(); // Replace with logic to get a unique client identifier

            if (!isAllowed(clientKey)) {
                return Mono.just("Rate limit exceeded for expression: " + expression);
            }

            // Call Math.js API using MathJsClient
            return mathJsClient.evaluate(expression)
                    .map(mathJsResponse -> {
                        if (mathJsResponse != null && mathJsResponse.getResult() != null && !mathJsResponse.getResult().isEmpty()) {
                            return expression +" => "+mathJsResponse.getResult().get(0);
                        } else {
                            return "Error evaluating expression: " + expression;
                        }
                    });
        } catch (Exception e) {
            return Mono.just("Error evaluating expression: " + expression);
        }
    }

    private boolean isAllowed(String clientKey) {
        TokenBucket bucket = rateLimitMap.computeIfAbsent(clientKey, k -> new TokenBucket(50, 1)); // 50 requests per second per client
        return bucket.tryConsume();
    }

    private String getClientKey() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        String userIpAddress = requestAttributes.getRequest().getRemoteAddr();
        return userIpAddress;
    }

    private static class TokenBucket {
        private final AtomicLong tokens;
        private final int capacity;
        private final int tokensPerSecond;
        private long lastRefillTime;

        public TokenBucket(int capacity, int tokensPerSecond) {
            this.capacity = capacity;
            this.tokensPerSecond = tokensPerSecond;
            this.tokens = new AtomicLong(capacity);
            this.lastRefillTime = System.currentTimeMillis();
        }

        public synchronized boolean tryConsume() {
            refillTokens();
            if (tokens.get() > 0) {
                tokens.decrementAndGet();
                return true;
            } else {
                return false;
            }
        }

        private void refillTokens() {
            long now = System.currentTimeMillis();
            long elapsedTime = now - lastRefillTime;
            if (elapsedTime > 0) {
                long tokensToAdd = (elapsedTime / 1000) * tokensPerSecond;
                tokens.set(Math.min(capacity, tokens.get() + tokensToAdd));
                lastRefillTime = now;
            }
        }
    }
}


