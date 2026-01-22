package com.guiamedicosback.guia.entity.dto;

import lombok.Builder;
import lombok.ToString;

import java.util.Map;

@Builder
public record ClinicaDTO(
        Long id,
         String nome,
         String endereco,
         String municipio,
         String telefone,
         String email,
         Map<String, String> procedimentos

) {
}
