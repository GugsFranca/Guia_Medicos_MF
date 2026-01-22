package com.guiamedicosback.guia.service;

import com.guiamedicosback.guia.entity.Clinica;
import com.guiamedicosback.guia.entity.Procedimento;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExcelProcessorService {

    public List<Clinica> processarExcel(InputStream inputStream) throws IOException {
        List<Clinica> todasClinicas = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0); // Pega a primeira planilha

            // Pula o cabeçalho (linha 0)
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Clinica clinica = extrairClinicaDaLinha(row);
                if (clinica != null) {
                    todasClinicas.add(clinica);
                }
            }
        }

        // Agrupar clínicas com mesmo nome e endereço
        return agruparClinicas(todasClinicas);
    }

    private Clinica extrairClinicaDaLinha(Row row) {
        if (row.getCell(0) == null || row.getCell(0).getStringCellValue().trim().isEmpty()) {
            return null;
        }

        Clinica clinica = new Clinica();

        // Nome da clínica
        clinica.setNome(obterValorCelula(row.getCell(0)));

        // Endereço completo
        String enderecoCompleto = obterValorCelula(row.getCell(1));
        clinica.setEndereco(enderecoCompleto);

        // Extrair município do endereço (penúltimo elemento)
        clinica.setMunicipio(extrairMunicipio(enderecoCompleto));

        // Telefone
        clinica.setTelefone(obterValorCelula(row.getCell(2)));

        // Email
        clinica.setEmail(obterValorCelula(row.getCell(3)));

        // Especialização
        String especializacao = obterValorCelula(row.getCell(4));

        // Procedimento
        String procedimentoDesc = obterValorCelula(row.getCell(5));

        // Adiciona o procedimento à clínica
        Procedimento procedimento = new Procedimento(especializacao, procedimentoDesc);
        clinica.getProcedimentos().add(procedimento);

        return clinica;
    }

    private String extrairMunicipio(String endereco) {
        if (endereco == null || endereco.trim().isEmpty()) {
            return "";
        }

        // Remove o CEP e vírgulas
        String enderecoSemCep = endereco.replaceAll(",?\\s*CEP:\\s*\\d{5}-?\\d{3}", "");

        // Divide por vírgulas
        String[] partes = enderecoSemCep.split(",");

        // Pega o penúltimo elemento
        if (partes.length >= 2) {
            // Remove o estado (RJ) do penúltimo elemento se existir
            String municipioCompleto = partes[partes.length - 2].trim();
            return municipioCompleto.replaceAll("\\s*,\\s*RJ$", "").trim();
        }

        return "";
    }

    private List<Clinica> agruparClinicas(List<Clinica> clinicas) {
        Map<String, Clinica> clinicasAgrupadas = new LinkedHashMap<>();

        for (Clinica clinica : clinicas) {
            String chave = clinica.getNome() + "|" + clinica.getEndereco();

            if (clinicasAgrupadas.containsKey(chave)) {
                // Adiciona os procedimentos à clínica existente
                Clinica existente = clinicasAgrupadas.get(chave);
                existente.getProcedimentos().addAll(clinica.getProcedimentos());
            } else {
                clinicasAgrupadas.put(chave, clinica);
            }
        }

        return new ArrayList<>(clinicasAgrupadas.values());
    }

    private String obterValorCelula(Cell cell) {
        if (cell == null) {
            return "";
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    // Remove .0 de números inteiros
                    double num = cell.getNumericCellValue();
                    if (num == Math.floor(num)) {
                        return String.valueOf((int) num);
                    } else {
                        return String.valueOf(num);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (Exception e) {
                    try {
                        return String.valueOf(cell.getNumericCellValue());
                    } catch (Exception e2) {
                        return cell.getCellFormula();
                    }
                }
            default:
                return "";
        }
    }

    // Métod para processar arquivo MultipartFile
    public List<Clinica> processarExcel(MultipartFile file) throws IOException {
        return processarExcel(file.getInputStream());
    }
}
