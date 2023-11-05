package com.onlinesales.mathcalculator.model;

import java.util.List;

public class MathJsResponse {
    private List<String> result;
    private String error;

    public List<String> getResult() {
        return result;
    }

    public void setResult(List<String> result) {
        this.result = result;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}

