package com.guiamedicosback.guia.service;

import com.guiamedicosback.guia.entity.Clinica;
import com.guiamedicosback.guia.entity.Grupo;
import com.guiamedicosback.guia.entity.Subgrupo;
import com.guiamedicosback.guia.repository.ClinicaRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.Normalizer;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ExcelProcessorProced {

    private final ClinicaRepository clinicaRepository;

    public void importarProcedimentos(MultipartFile file) throws IOException {

        Map<String, Clinica> clinicasMap = clinicaRepository.findAll()
                .stream()
                .collect(Collectors.toMap(
                        c -> normalize(c.getNome()),
                        c -> c,
                        (a, b) -> a
                ));

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {

            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String nomeClinica = getCellValue(row.getCell(0));
                String nomeGrupo = getCellValue(row.getCell(1));
                String nomeSubgrupo = getCellValue(row.getCell(2));
                String nomeProcedimento = getCellValue(row.getCell(3));

                if (nomeClinica.isBlank()) continue;

                Clinica clinica = clinicasMap.computeIfAbsent(
                        normalize(nomeClinica),
                        k -> Clinica.builder().nome(nomeClinica).build()
                );

                Grupo grupo = clinica.encontrarOuCriarGrupo(nomeGrupo);
                Subgrupo subgrupo = grupo.encontrarOuCriarSubgrupo(nomeSubgrupo);

                if (!subgrupo.getProcedimentos().contains(nomeProcedimento)) {
                    subgrupo.getProcedimentos().add(nomeProcedimento);
                }
            }

            clinicaRepository.saveAll(clinicasMap.values());
        }
    }

    private String normalize(String s) {
        if (s == null) return "";
        String t = Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return t.toLowerCase().trim().replaceAll("\\s+", " ");
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }
}
