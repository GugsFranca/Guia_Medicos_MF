package com.guiamedicosback.guia.entity.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record ClinicaDTO(
        Long id,
        String nome,
        String endereco,
        String municipio,
        String telefone,
        String email,
        List<GrupoDTO> grupos

) {
}
