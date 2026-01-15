package com.guiamedicosback.service;

import com.guiamedicosback.entity.dto.ClinicaDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ClinicaService {

    void addClinicaFromFile(MultipartFile file) throws IOException;

    ClinicaDTO addClinica(ClinicaDTO clinicaDTO);

    void deleteClinica(Long id);

    ClinicaDTO updateClinica(Long id, ClinicaDTO clinicaDTO);

    ClinicaDTO getClinicaById(Long id);

    List<ClinicaDTO> getClinicas();

    List<ClinicaDTO> searchClinicas(String nome, String municipio, String endereco, String procedimento, String especializacao);
}
