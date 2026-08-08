package com.vk42.cbp.firstmodule.dto;

public record ErrorResponse(
        String errorCode,
        String message
) {}