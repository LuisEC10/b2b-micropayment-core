package com.vk42.cbp.firstmodule.shared.dto;

public record ErrorResponse(
        String errorCode,
        String message
) {}