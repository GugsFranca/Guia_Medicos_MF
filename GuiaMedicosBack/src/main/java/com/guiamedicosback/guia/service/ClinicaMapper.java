package com.guiamedicosback.guia.service;

import com.guiamedicosback.guia.entity.Clinica;
import com.guiamedicosback.guia.entity.Grupo;
import com.guiamedicosback.guia.entity.Subgrupo;
import com.guiamedicosback.guia.entity.dto.ClinicaDTO;
import com.guiamedicosback.guia.entity.dto.GrupoDTO;
import com.guiamedicosback.guia.entity.dto.SubgrupoDTO;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class ClinicaMapper {

    public ClinicaDTO toClinicaDTO(Clinica clinica) {
        if (clinica == null) {
            return null;
        }

        return ClinicaDTO.builder()
                .id(clinica.getId())
                .nome(clinica.getNome())
                .endereco(clinica.getEndereco())
                .municipio(clinica.getMunicipio())
                .telefone(clinica.getTelefone())
                .email(clinica.getEmail())
                .grupos(clinica.getGrupos().stream()
                        .map(this::toGrupoDTO)
                        .collect(Collectors.toList()))
                .build();
    }

    public GrupoDTO toGrupoDTO(Grupo grupo) {
        if (grupo == null) {
            return null;
        }

        return GrupoDTO.builder()
                .nome(grupo.getNome())
                .subgrupos(grupo.getSubgrupos().stream()
                .map(this::toSubgrupoDTO)
                .collect(Collectors.toList())).build();
    }

    public SubgrupoDTO toSubgrupoDTO(Subgrupo subgrupo) {
        if (subgrupo == null) {
            return null;
        }

        return SubgrupoDTO.builder()
                .nome(subgrupo.getNome())
                .procedimentos(subgrupo.getProcedimentos())
                .build();
    }

    public Clinica toClinica(ClinicaDTO dto) {
        if (dto == null) {
            return null;
        }

        Clinica clinica = new Clinica();
        clinica.setId(dto.id());
        clinica.setNome(dto.nome());
        clinica.setEndereco(dto.endereco());
        clinica.setMunicipio(dto.municipio());
        clinica.setTelefone(dto.telefone());
        clinica.setEmail(dto.email());

        // Mapear grupos
        if (dto.grupos() != null) {
            for (GrupoDTO grupoDTO : dto.grupos()) {
                Grupo grupo = new Grupo();
                grupo.setNome(grupoDTO.nome());
                grupo.setClinica(clinica);

                // Mapear subgrupos
                if (grupoDTO.subgrupos() != null) {
                    for (SubgrupoDTO subgrupoDTO : grupoDTO.subgrupos()) {
                        Subgrupo subgrupo = new Subgrupo();
                        subgrupo.setNome(subgrupoDTO.nome());
                        subgrupo.setProcedimentos(subgrupoDTO.procedimentos());
                        subgrupo.setGrupo(grupo);
                        grupo.getSubgrupos().add(subgrupo);
                    }
                }

                clinica.getGrupos().add(grupo);
            }
        }

        return clinica;
    }
}