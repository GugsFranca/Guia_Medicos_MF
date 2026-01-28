package com.guiamedicosback.guia.controller;

import com.guiamedicosback.guia.entity.dto.ClinicaDTO;
import com.guiamedicosback.guia.service.ClinicaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/clinicas")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Clínicas", description = "API para gerenciamento de clínicas médicas")
@Slf4j
public class ClinicaController {

    private final ClinicaService clinicaService;

    @Operation(summary = "Upload de arquivo Excel com clínicas",
            description = "Processa um arquivo Excel contendo dados de clínicas e salva no banco de dados")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadClinicas(@RequestParam("file") MultipartFile file) {

        log.debug("Recebendo arquivo para upload: {}", file.getOriginalFilename());

        try {
            // Validação básica do arquivo
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("O arquivo está vazio");
            }

            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || (!originalFilename.toLowerCase().endsWith(".xlsx") && !originalFilename.toLowerCase().endsWith(".xls"))) {
                return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body("Apenas arquivos Excel (.xlsx, .xls) são suportados");
            }

            clinicaService.addClinicaFromFile(file);

            log.debug("Arquivo {} processado com sucesso", originalFilename);
            return ResponseEntity.ok("Arquivo processado e clínicas salvas com sucesso!");

        } catch (IOException e) {
            log.error("Erro de IO ao processar arquivo: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao processar o arquivo: " + e.getMessage());
        } catch (Exception e) {
            log.error("Erro interno ao processar arquivo: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno ao processar o arquivo: " + e.getMessage());
        }
    }

    @Operation(summary = "Adicionar nova clínica", description = "Adiciona uma nova clínica ao sistema")
    @PostMapping
    public ResponseEntity<ClinicaDTO> addClinica(@RequestBody @Valid ClinicaDTO clinicaDTO) {
        log.debug("Adicionando nova clínica: {}", clinicaDTO.nome());

        try {
            log.debug("Procedimentos: {} ", clinicaDTO.grupos());
            ClinicaDTO createdClinica = clinicaService.addClinica(clinicaDTO);
            log.debug("Clínica adicionada com sucesso: {}", createdClinica.nome());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdClinica);
        } catch (Exception e) {
            log.error("Erro ao adicionar clínica: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Listar todas as clínicas", description = "Retorna uma lista com todas as clínicas cadastradas")
    @GetMapping
    public ResponseEntity<List<ClinicaDTO>> getAllClinicas() {
        log.debug("Buscando todas as clínicas");

        try {
            List<ClinicaDTO> clinicas = clinicaService.getClinicas();
            return ResponseEntity.ok(clinicas);
        } catch (Exception e) {
            log.error("Erro ao buscar todas as clínicas: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Deletar clínica por ID", description = "Remove uma clínica específica pelo seu ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClinica(@PathVariable String id) {

        log.debug("Deletando clínica com ID: {}", id);
        Long parseId = Long.parseLong(id);
        try {
            clinicaService.deleteClinica(parseId);
            log.debug("Clínica com ID {} deletada com sucesso", id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Erro ao deletar clínica com ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Atualizar clínica por ID", description = "Atualiza os dados de uma clínica específica pelo seu ID")
    @PutMapping("/{id}")
    public ResponseEntity<ClinicaDTO> updateClinica(@PathVariable String id, @RequestBody @Valid ClinicaDTO clinicaDTO) {

        Long parseId = Long.parseLong(id);
        try {
            ClinicaDTO updatedClinica = clinicaService.updateClinica(parseId, clinicaDTO);
            log.debug("Clínica com ID {} atualizada com sucesso", id);
            return ResponseEntity.ok(updatedClinica);
        } catch (RuntimeException e) {
            log.warn("Clínica não atualizada com ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            log.error("Erro ao atualizar clínica com ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Buscar clínica por ID", description = "Retorna os detalhes de uma clínica específica pelo seu ID")
    @GetMapping("/{id}")
    public ResponseEntity<ClinicaDTO> getClinicaById(@PathVariable String id) {

        log.debug("Buscando clínica com ID: {}", id);
        Long parseId = Long.parseLong(id);

        try {
            ClinicaDTO clinica = clinicaService.getClinicaById(parseId);
            log.debug("Clínica encontrada: {}", clinica.nome());
            return ResponseEntity.ok(clinica);
        } catch (RuntimeException e) {
            log.warn("Clínica não encontrada com ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            log.error("Erro ao buscar clínica por ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Busca combinada de clínicas", description = "Retorna clínicas filtradas por múltiplos critérios (todos os parâmetros são opcionais)")
    @GetMapping("/busca")
    public ResponseEntity<List<ClinicaDTO>> searchClinicas(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String municipio,
            @RequestParam(required = false) String endereco,
            @RequestParam(required = false) String procedimento,
            @RequestParam(required = false) String grupo,
            @RequestParam(required = false) String subgrupo) {


        log.debug("Buscando clínicas com critérios - Nome: {}, Município: {}, Endereço: {}, Procedimento: {}, Grupo: {}, Subgrupo {}", nome, municipio, endereco, procedimento, grupo,subgrupo );

        try {
            List<ClinicaDTO> clinicas = clinicaService.searchClinicas(
                    nome, municipio, endereco, procedimento, grupo, subgrupo);
            log.debug("Encontradas {} clínicas com os critérios especificados", clinicas.size());
            return ResponseEntity.ok(clinicas);

        } catch (IllegalArgumentException e) {
            log.warn("Parâmetros de busca inválidos: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Erro na busca combinada: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}