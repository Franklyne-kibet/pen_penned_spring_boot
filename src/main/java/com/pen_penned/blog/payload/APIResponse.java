package com.pen_penned.blog.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class APIResponse {

    public String message;
    public boolean status;
    private Object data;

    public APIResponse(String message, boolean status) {
        this.message = message;
        this.status = status;
    }
}
