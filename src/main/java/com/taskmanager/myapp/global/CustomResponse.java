package com.taskmanager.myapp.global;

import lombok.Getter;

@Getter
public class CustomResponse<T> {

    private T data;
    private String message;

    private CustomResponse(T data, String message) {
        this.data = data;
        this.message = message;
    }

    public static <T> CustomResponse<T> res(T data, String message) {
        return new CustomResponse<>(data, message);
    }
}
