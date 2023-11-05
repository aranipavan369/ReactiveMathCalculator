package com.onlinesales.mathcalculator.component;

import com.onlinesales.mathcalculator.model.MathJsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class MathJsClient {

    private final WebClient mathJsWebClient;

    @Autowired
    public MathJsClient(WebClient.Builder webClientBuilder) {
        this.mathJsWebClient = webClientBuilder.baseUrl("http://api.mathjs.org/v4/").build();
    }

    public Mono<MathJsResponse> evaluate(String expression) {
        return mathJsWebClient.post()
                .uri("/evaluate")
                .header("Content-Type", "application/json")
                .body(BodyInserters.fromValue("{\"expr\": [\"" + expression + "\"]}"))
                .retrieve()
                .bodyToMono(MathJsResponse.class);
    }
}

