package com.onlinesales.mathcalculator;

import com.onlinesales.mathcalculator.controller.MathController;
import com.onlinesales.mathcalculator.service.MathService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.List;

public class MathControllerTests {

    @Mock
    private MathService mathService;

    private MathController mathController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mathController = new MathController(mathService);
    }

    @Test
    public void testEvaluateExpressions() {
        List<String> expressions = Arrays.asList("2 + 4", "2 * 4 * 4", "end");
        List<String> expectedResults = Arrays.asList("2 + 4 => 6", "2 * 4 * 4 => 32", "Evaluation ended.");

        Mockito.when(mathService.evaluateExpressions(expressions))
                .thenReturn(Flux.fromIterable(expectedResults)); // Use Flux, not Mono
    }
}

