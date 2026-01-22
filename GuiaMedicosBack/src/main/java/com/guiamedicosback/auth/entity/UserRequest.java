package com.guiamedicosback.auth.entity;

import lombok.Builder;

@Builder
public record UserRequest(
        String username,
        String password
) {
}
