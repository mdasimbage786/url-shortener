package com.mdasim.urlshortener.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateUrlRequest {
    @NotBlank(message = "URL cannot be blank")
    @Size(max = 2048, message = "URL too long")
    private String originalUrl;
}