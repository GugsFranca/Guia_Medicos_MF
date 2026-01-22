package com.guiamedicosback.guia.service;

import com.guiamedicosback.guia.entity.Clinica;
import com.guiamedicosback.guia.entity.Procedimento;
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
            var clinica = processorService.processarExcel(file);
            clinicaRepository.deleteAll();
            clinicaRepository.saveAll(clinica);
        } catch (Exception e) {
            throw new IOException("Erro ao processar o arquivo Excel: " + e.getMessage());
        }
    }

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

    public ClinicaDTO updateClinica(Long id, ClinicaDTO clinicaDTO) {
        if (id == null || clinicaDTO == null) {
            return null;
        }
        try {
            Clinica existingClinica = clinicaRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Clínica não encontrada"));

            existingClinica.setNome(clinicaDTO.nome());
            existingClinica.setEmail(clinicaDTO.email());
            existingClinica.setMunicipio(clinicaDTO.municipio());
            existingClinica.setTelefone(clinicaDTO.telefone());
            existingClinica.setEndereco(clinicaDTO.endereco());

            existingClinica.getProcedimentos().clear();

            clinicaDTO.procedimentos().forEach((especializacao, nome) ->
                    existingClinica.getProcedimentos().add(new Procedimento(especializacao, nome)));

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
            return  null;
        }
        try {
            var clinica = clinicaRepository.findById(id).orElseThrow(() -> new RuntimeException("Clínica não encontrada"));
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
            var dtos = clinicaMapper.toClinicaDTO(clinica);
            clinicaDTOS.add(dtos);
        }
        return clinicaDTOS;
    }

    @Override
    public List<ClinicaDTO> searchClinicas(String nome, String municipio, String endereco,
                                           String procedimento, String especializacao) {

        if (nome == null && municipio == null && endereco == null
                && procedimento == null  && especializacao == null) {
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

            if (procedimento != null && !procedimento.isEmpty()) {
                spec = spec.and((root, _, cb) -> {
                    Join<Clinica, Procedimento> procedimentos = root.join("procedimentos");
                    return cb.like(cb.lower(procedimentos.get("nome")),
                            "%" + procedimento.toLowerCase() + "%");
                });
            }

            if (especializacao != null && !especializacao.isEmpty()) {
                spec = spec.and((root, _, cb) -> {
                    Join<Clinica, Procedimento> procedimentos = root.join("procedimentos");
                    return cb.equal(cb.lower(procedimentos.get("especializacao")),
                            especializacao.toLowerCase());
                });
            }

            return clinicaRepository.findAll(spec).stream()
                    .map(clinicaMapper::toClinicaDTO)
                    .collect(Collectors.toList());
        }catch (Exception e) {
            throw new RuntimeException("Erro ao buscar clínicas: " + e.getMessage());
        }
    }
}
