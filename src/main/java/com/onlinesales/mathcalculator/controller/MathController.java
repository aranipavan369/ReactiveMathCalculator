package com.onlinesales.mathcalculator.controller;

import com.onlinesales.mathcalculator.service.MathService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
public class MathController {

    private final MathService mathService;

    @Autowired
    public MathController(MathService mathService) {
        this.mathService = mathService;
    }

    @PostMapping("/evaluate")
    public ResponseEntity<Flux<String>> evaluate(@RequestBody List<String> expressions) {
        Flux<String> results = mathService.evaluateExpressions(expressions);
        return ResponseEntity.status(HttpStatus.OK).body(results);
    }
}
//application->receives list of strings->Math.js->return result
//50/client/second->500 concurrently per second.
//webflux->reactive application

