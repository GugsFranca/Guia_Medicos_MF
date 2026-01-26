package com.guiamedicosback.guia.service;

import com.guiamedicosback.guia.entity.Clinica;
import com.guiamedicosback.guia.entity.Grupo;
import com.guiamedicosback.guia.entity.Subgrupo;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Slf4j
@Service
public class ExcelProcessorService {

    public List<Clinica> processarExcel(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Arquivo não pode ser nulo ou vazio");
        }

        String originalFilename = file.getOriginalFilename();
        log.info("Processando arquivo: {}, Tamanho: {} bytes, Tipo MIME: {}",
                originalFilename, file.getSize(), file.getContentType());

        // Validação do tipo de arquivo
        if (originalFilename == null ||
                (!originalFilename.toLowerCase().endsWith(".xlsx") &&
                        !originalFilename.toLowerCase().endsWith(".xls"))) {
            throw new IllegalArgumentException("Formato de arquivo inválido. Apenas arquivos .xlsx ou .xls são suportados.");
        }

        try (InputStream inputStream = file.getInputStream()) {
            return processarExcelInputStream(inputStream);
        } catch (Exception e) {
            log.error("Erro ao processar arquivo Excel: {}", e.getMessage(), e);
            throw new IOException("Erro ao processar arquivo Excel: " + e.getMessage(), e);
        }
    }

    private List<Clinica> processarExcelInputStream(InputStream inputStream) throws IOException {
        List<Clinica> clinicas = new ArrayList<>();

        // Tenta abrir como XSSF (formato .xlsx) primeiro
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            log.info("Arquivo aberto como XSSFWorkbook (formato .xlsx)");
            processarWorkbook(workbook, clinicas);
        } catch (Exception e1) {
            log.warn("Falha ao abrir como XSSFWorkbook, tentando WorkbookFactory...");

            // Fecha e recria o InputStream
            inputStream.close();
            // Note: Não podemos reutilizar o InputStream, então o código abaixo é apenas para referência
            throw new IOException("Não foi possível processar o arquivo. Certifique-se de que é um arquivo Excel válido (.xlsx).");
        }

        return clinicas;
    }

    private void processarWorkbook(Workbook workbook, List<Clinica> clinicas) {
        int totalSheets = workbook.getNumberOfSheets();
        log.info("Total de planilhas no arquivo: {}", totalSheets);

        // Processa todas as planilhas
        for (int i = 0; i < totalSheets; i++) {
            Sheet sheet = workbook.getSheetAt(i);
            String sheetName = sheet.getSheetName();
            log.info("Processando planilha {}: '{}'", i + 1, sheetName);

            processarPlanilha(sheet, clinicas);
        }

        log.info("Processamento concluído. Total de prestadores encontrados: {}", clinicas.size());
    }

    private void processarPlanilha(Sheet sheet, List<Clinica> clinicas) {
        Iterator<Row> rowIterator = sheet.iterator();
        int linhaAtual = 0;
        int linhasProcessadas = 0;

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            linhaAtual++;

            // Pula a linha do cabeçalho (linha 1)
            if (linhaAtual == 1) {
                log.debug("Pulando linha de cabeçalho na linha {}", linhaAtual);
                continue;
            }

            // Verifica se a linha está vazia
            if (isRowEmpty(row)) {
                continue;
            }

            // Processa a linha
            if (processarLinha(row, clinicas)) {
                linhasProcessadas++;
            }

            // Log a cada 100 linhas processadas
            if (linhasProcessadas % 100 == 0) {
                log.debug("Linhas processadas na planilha {}: {}", sheet.getSheetName(), linhasProcessadas);
            }
        }

        log.info("Planilha '{}': {} linhas de dados processadas", sheet.getSheetName(), linhasProcessadas);
    }

    private boolean processarLinha(Row row, List<Clinica> clinicas) {
        try {
            String prestador = getCellStringValue(row.getCell(0)).trim();
            String grupoNome = getCellStringValue(row.getCell(1)).trim();
            String subgrupoNome = getCellStringValue(row.getCell(2)).trim();
            String procedimento = getCellStringValue(row.getCell(3)).trim();

            // Validação básica
            if (prestador.isEmpty() || procedimento.isEmpty()) {
                return false;
            }

            // 1. Buscar ou criar clínica
            Clinica clinica = encontrarOuCriarClinica(clinicas, prestador);

            // 2. Buscar ou criar grupo
            Grupo grupo = encontrarOuCriarGrupo(clinica, grupoNome);

            // 3. Buscar ou criar subgrupo
            Subgrupo subgrupo = encontrarOuCriarSubgrupo(grupo, subgrupoNome);

            // 4. Adicionar procedimento (evitar duplicatas)
            adicionarProcedimentoSeNovo(subgrupo, procedimento);

            return true;

        } catch (Exception e) {
            log.warn("Erro ao processar linha {}: {}", row.getRowNum() + 1, e.getMessage());
            return false;
        }
    }

    private Clinica encontrarOuCriarClinica(List<Clinica> clinicas, String nome) {
        return clinicas.stream()
                .filter(c -> c.getNome().equalsIgnoreCase(nome))
                .findFirst()
                .orElseGet(() -> {
                    Clinica novaClinica = new Clinica();
                    novaClinica.setNome(nome);
                    novaClinica.setEndereco("");
                    novaClinica.setMunicipio("");
                    novaClinica.setTelefone("");
                    novaClinica.setEmail("");
                    novaClinica.setGrupos(new ArrayList<>());
                    clinicas.add(novaClinica);
                    return novaClinica;
                });
    }

    private Grupo encontrarOuCriarGrupo(Clinica clinica, String grupoNome) {
        return clinica.getGrupos().stream()
                .filter(g -> g.getNome().equals(grupoNome))
                .findFirst()
                .orElseGet(() -> {
                    Grupo novoGrupo = new Grupo();
                    novoGrupo.setNome(grupoNome);
                    novoGrupo.setClinica(clinica);
                    novoGrupo.setSubgrupos(new ArrayList<>());
                    clinica.getGrupos().add(novoGrupo);
                    return novoGrupo;
                });
    }

    private Subgrupo encontrarOuCriarSubgrupo(Grupo grupo, String subgrupoNome) {
        return grupo.getSubgrupos().stream()
                .filter(s -> s.getNome().equals(subgrupoNome))
                .findFirst()
                .orElseGet(() -> {
                    Subgrupo novoSubgrupo = new Subgrupo();
                    novoSubgrupo.setNome(subgrupoNome);
                    novoSubgrupo.setGrupo(grupo);
                    novoSubgrupo.setProcedimentos(new ArrayList<>());
                    grupo.getSubgrupos().add(novoSubgrupo);
                    return novoSubgrupo;
                });
    }

    private void adicionarProcedimentoSeNovo(Subgrupo subgrupo, String procedimento) {
        if (!subgrupo.getProcedimentos().contains(procedimento)) {
            subgrupo.getProcedimentos().add(procedimento);
        }
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) {
            return true;
        }

        for (int i = 0; i < 4; i++) { // Verifica apenas as 4 colunas que usamos
            Cell cell = row.getCell(i);
            if (cell != null) {
                String value = getCellStringValue(cell).trim();
                if (!value.isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    private String getCellStringValue(Cell cell) {
        if (cell == null) {
            return "";
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    // Remove .0 de números inteiros
                    double num = cell.getNumericCellValue();
                    if (num == Math.floor(num) && !Double.isInfinite(num)) {
                        return String.valueOf((long) num);
                    } else {
                        return String.valueOf(num);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    // Tenta obter o valor da fórmula
                    switch (cell.getCachedFormulaResultType()) {
                        case NUMERIC:
                            return String.valueOf(cell.getNumericCellValue());
                        case STRING:
                            return cell.getStringCellValue();
                        case BOOLEAN:
                            return String.valueOf(cell.getBooleanCellValue());
                        default:
                            return cell.getCellFormula();
                    }
                } catch (Exception e) {
                    return cell.getCellFormula();
                }
            case BLANK:
                return "";
            default:
                return "";
        }
    }

    // Método para debug - exibe estrutura processada
    public void logarEstruturaProcessada(List<Clinica> clinicas) {
        if (clinicas == null || clinicas.isEmpty()) {
            log.info("Nenhuma clínica processada.");
            return;
        }

        log.info("=== ESTRUTURA DE DADOS PROCESSADA ===");
        log.info("Total de Prestadores: {}", clinicas.size());

        int totalGrupos = 0;
        int totalSubgrupos = 0;
        int totalProcedimentos = 0;

        for (Clinica clinica : clinicas) {
            log.info("\nPrestador: {}", clinica.getNome());
            log.info("Grupos: {}", clinica.getGrupos().size());
            totalGrupos += clinica.getGrupos().size();

            for (Grupo grupo : clinica.getGrupos()) {
                log.info("  └─ Grupo: {}", grupo.getNome());
                log.info("    Subgrupos: {}", grupo.getSubgrupos().size());
                totalSubgrupos += grupo.getSubgrupos().size();

                for (Subgrupo subgrupo : grupo.getSubgrupos()) {
                    log.info("      └─ Subgrupo: {}", subgrupo.getNome());
                    log.info("        Procedimentos: {}", subgrupo.getProcedimentos().size());
                    totalProcedimentos += subgrupo.getProcedimentos().size();
                }
            }
        }

        log.info("\n=== RESUMO ===");
        log.info("Prestadores: {}", clinicas.size());
        log.info("Grupos: {}", totalGrupos);
        log.info("Subgrupos: {}", totalSubgrupos);
        log.info("Procedimentos únicos: {}", totalProcedimentos);
    }
}