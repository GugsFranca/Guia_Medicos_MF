package com.guiamedicosback.guia.entity.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record SubgrupoDTO(
        String nome,
        List<String> procedimentos
) {
}
