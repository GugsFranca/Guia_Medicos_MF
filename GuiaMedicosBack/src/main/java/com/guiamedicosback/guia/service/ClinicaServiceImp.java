package com.guiamedicosback.guia.service;

import com.guiamedicosback.guia.entity.Clinica;
import com.guiamedicosback.guia.entity.Grupo;
import com.guiamedicosback.guia.entity.Subgrupo;
import com.guiamedicosback.guia.entity.dto.ClinicaDTO;
import com.guiamedicosback.guia.repository.ClinicaRepository;
import jakarta.persistence.criteria.Join;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClinicaServiceImp implements ClinicaService {
    private final ClinicaRepository clinicaRepository;
    private final ExcelProcessorService processorService;
    private final ClinicaMapper clinicaMapper;

    @Override
    @Transactional
    public void addClinicaFromFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return;
        }
        try {
            var clinicas = processorService.processarExcel(file);
            clinicaRepository.deleteAll();
            clinicaRepository.saveAll(clinicas);
        } catch (Exception e) {
            throw new IOException("Erro ao processar o arquivo Excel: " + e.getMessage());
        }
    }

    @Override
    public ClinicaDTO addClinica(ClinicaDTO clinicaDTO) {
        if (clinicaDTO == null) {
            return null;
        }
        try {
            Clinica clinica = clinicaMapper.toClinica(clinicaDTO);
            Clinica savedClinica = clinicaRepository.save(clinica);
            return clinicaMapper.toClinicaDTO(savedClinica);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao adicionar clínica: " + e.getMessage());
        }
    }

    @Override
    public void deleteClinica(Long id) {
        if (id == null) {
            return;
        }
        try {
            clinicaRepository.deleteById(id);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao deletar clínica: " + e.getMessage());
        }
    }

    @Override
    public ClinicaDTO updateClinica(Long id, ClinicaDTO clinicaDTO) {
        if (id == null || clinicaDTO == null) {
            return null;
        }
        try {
            Clinica existingClinica = clinicaRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Clínica não encontrada"));

            existingClinica.setNome(clinicaDTO.nome());
            existingClinica.setEmail(clinicaDTO.email());
            existingClinica.setEndereco(clinicaDTO.endereco());
            existingClinica.setMunicipio(clinicaDTO.municipio());
            existingClinica.setTelefone(clinicaDTO.telefone());


            // Limpar grupos existentes
            existingClinica.getGrupos().clear();

            // Adicionar novos grupos
            if (clinicaDTO.grupos() != null) {
                for (var grupoDTO : clinicaDTO.grupos()) {
                    Grupo grupo = new Grupo();
                    grupo.setNome(grupoDTO.nome());
                    grupo.setClinica(existingClinica);

                    // Adicionar subgrupos
                    if (grupoDTO.subgrupos() != null) {
                        for (var subgrupoDTO : grupoDTO.subgrupos()) {
                            Subgrupo subgrupo = new Subgrupo();
                            subgrupo.setNome(subgrupoDTO.nome());
                            subgrupo.setProcedimentos(subgrupoDTO.procedimentos());
                            subgrupo.setGrupo(grupo);
                            grupo.getSubgrupos().add(subgrupo);
                        }
                    }

                    existingClinica.getGrupos().add(grupo);
                }
            }

            Clinica updatedClinica = clinicaRepository.save(existingClinica);
            return clinicaMapper.toClinicaDTO(updatedClinica);
        } catch (Exception e) {
            String errorMsg = e.getMessage() != null ? e.getMessage() : "Erro desconhecido";
            throw new RuntimeException("Erro ao atualizar clínica: " + errorMsg, e);
        }
    }

    @Override
    public ClinicaDTO getClinicaById(Long id) {
        if (id == null) {
            return null;
        }
        try {
            var clinica = clinicaRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Clínica não encontrada"));
            return clinicaMapper.toClinicaDTO(clinica);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao buscar clínica: " + e.getMessage());
        }
    }

    @Override
    public List<ClinicaDTO> getClinicas() {
        var clinicas = clinicaRepository.findAll();
        List<ClinicaDTO> clinicaDTOS = new ArrayList<>();
        for (Clinica clinica : clinicas) {
            var dto = clinicaMapper.toClinicaDTO(clinica);
            clinicaDTOS.add(dto);
        }
        return clinicaDTOS;
    }

    @Override
    public List<ClinicaDTO> searchClinicas(String nome, String municipio, String endereco,
                                           String procedimento, String grupo, String subgrupo) {

        if (nome == null && municipio == null && endereco == null
                && procedimento == null && grupo == null && subgrupo == null) {
            return getClinicas();
        }

        try {
            // Começa com uma Specification vazia
            Specification<Clinica> spec = (_, _, _) -> null;

            if (nome != null && !nome.isEmpty()) {
                spec = spec.and((root, _, cb) ->
                        cb.like(cb.lower(root.get("nome")), "%" + nome.toLowerCase() + "%"));
            }

            if (municipio != null && !municipio.isEmpty()) {
                spec = spec.and((root, _, cb) ->
                        cb.like(cb.lower(root.get("municipio")), "%" + municipio.toLowerCase() + "%"));
            }

            if (endereco != null && !endereco.isEmpty()) {
                spec = spec.and((root, _, cb) ->
                        cb.like(cb.lower(root.get("endereco")), "%" + endereco.toLowerCase() + "%"));
            }

            // Busca por procedimento (dentro da lista de procedimentos do subgrupo)
            if (procedimento != null && !procedimento.isEmpty()) {
                spec = spec.and((root, _, cb) -> {
                    Join<Clinica, Grupo> grupos = root.join("grupos");
                    Join<Grupo, Subgrupo> subgrupos = grupos.join("subgrupos");
                    return cb.isMember(procedimento.toLowerCase(), subgrupos.get("procedimentos"));
                });
            }

            // Busca por grupo
            if (grupo != null && !grupo.isEmpty()) {
                spec = spec.and((root, _, cb) -> {
                    Join<Clinica, Grupo> grupos = root.join("grupos");
                    return cb.like(cb.lower(grupos.get("nome")), "%" + grupo.toLowerCase() + "%");
                });
            }

            // Busca por subgrupo
            if (subgrupo != null && !subgrupo.isEmpty()) {
                spec = spec.and((root, _, cb) -> {
                    Join<Clinica, Grupo> grupos = root.join("grupos");
                    Join<Grupo, Subgrupo> subgrupos = grupos.join("subgrupos");
                    return cb.like(cb.lower(subgrupos.get("nome")), "%" + subgrupo.toLowerCase() + "%");
                });
            }

            return clinicaRepository.findAll(spec).stream()
                    .map(clinicaMapper::toClinicaDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Erro ao buscar clínicas: " + e.getMessage());
        }
    }
}