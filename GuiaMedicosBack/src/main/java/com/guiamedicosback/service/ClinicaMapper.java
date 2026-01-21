package com.guiamedicosback.service;

import com.guiamedicosback.entity.Clinica;
import com.guiamedicosback.entity.Procedimento;
import com.guiamedicosback.entity.dto.ClinicaDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ClinicaMapper {

    protected ClinicaDTO toClinicaDTO(Clinica clinica) {
        if (clinica == null) return null;
        return ClinicaDTO.builder()
                .id(clinica.getId())
                .nome(clinica.getNome() != null ? clinica.getNome() : "")
                .email(clinica.getEmail() != null ? clinica.getEmail() : "")
                .municipio(clinica.getMunicipio() != null ? clinica.getMunicipio() : "")
                .telefone(clinica.getTelefone() != null ? clinica.getTelefone() : "")
                .endereco(clinica.getEndereco() != null ? clinica.getEndereco() : "")
                .procedimentos(mapProcedimentos(clinica) != null ? mapProcedimentos(clinica) : Map.of())
                .build();
    }

    protected List<ClinicaDTO> toClinicaDTOList(List<Clinica> clinicas) {
        if (clinicas == null) return null;
        return clinicas.stream()
                .map(this::toClinicaDTO)
                .collect(Collectors.toList());
    }

    protected Clinica toClinica(ClinicaDTO clinicaDTO) {
        if (clinicaDTO == null) return null;

        return Clinica.builder()
                .nome(clinicaDTO.nome() != null ? clinicaDTO.nome() : "")
                .email(clinicaDTO.email() != null ? clinicaDTO.email() : "")
                .municipio(clinicaDTO.municipio() != null ? clinicaDTO.municipio() : "")
                .telefone(clinicaDTO.telefone() != null ? clinicaDTO.telefone() : "")
                .endereco(clinicaDTO.endereco() != null ? clinicaDTO.endereco() : "")
                .procedimentos(mapProcedimentos(clinicaDTO))
                .build();
    }
    private Map<String, String> mapProcedimentos(Clinica clinica) {
        if (clinica == null) return null;
        if (clinica.getProcedimentos() == null) return null;
        return clinica.getProcedimentos().stream()
                .collect(Collectors.toMap(
                        Procedimento::getEspecializacao,
                        Procedimento::getNome,
                        (existing, replacement) -> existing + ", " + replacement
                ));
    }
    private List<Procedimento> mapProcedimentos(ClinicaDTO clinicaDTO) {
        if (clinicaDTO == null) return null;
        if (clinicaDTO.procedimentos() == null) return null;
        return clinicaDTO.procedimentos().entrySet().stream()
                .map(entry -> new Procedimento(entry.getKey(), entry.getValue()))
                .toList();
    }
}
