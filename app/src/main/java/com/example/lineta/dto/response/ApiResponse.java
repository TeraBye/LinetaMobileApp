package com.example.lineta.dto.response;

public class ApiResponse<T> {
    int code;
    private String message;
    private T result;

    public int getCode(){
        return code;
    }

    public String getMessage() {
        return message;
    }

    public T getResult() {
        return result;
    }
}

