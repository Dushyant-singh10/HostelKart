package com.example.TTN_E_Commerce.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse {

    private final String message;
    private final Object data;

    public ApiResponse(String message) {
        this.message = message;
        this.data = null;
    }

    public ApiResponse(String message, Object data) {
        this.message = message;
        this.data = data;
    }
}