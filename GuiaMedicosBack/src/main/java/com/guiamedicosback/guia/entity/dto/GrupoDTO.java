package com.guiamedicosback.guia.entity.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record GrupoDTO(
        String nome,
        List<SubgrupoDTO> subgrupos
) {
}
