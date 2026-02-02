//package com.guiamedicosback.guia.service;
//
//import com.guiamedicosback.guia.entity.Clinica;
//import com.guiamedicosback.guia.entity.Procedimento;
//import com.guiamedicosback.guia.entity.dto.ClinicaDTO;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@ExtendWith(MockitoExtension.class)
//class ClinicaMapperTest {
//
//    @InjectMocks
//    private ClinicaMapper clinicaMapper;
//
//    private Clinica clinica;
//    private ClinicaDTO clinicaDTO;
//
//    @BeforeEach
//    void setUp() {
//        // Configuração da clínica
//        Procedimento procedimento1 = new Procedimento("Imagem", "Raio-X");
//        Procedimento procedimento2 = new Procedimento("Laboratorial", "Exame de Sangue");
//
//        clinica = Clinica.builder()
//                .nome("Clínica Teste")
//                .email("clinica@teste.com")
//                .municipio("São Paulo")
//                .telefone("11999999999")
//                .endereco("Rua Teste, 123")
//                .procedimentos(Arrays.asList(procedimento1, procedimento2))
//                .build();
//
//        // Configuração da ClinicaDTO
//        Map<String, String> procedimentosMap = new HashMap<>();
//        procedimentosMap.put("Imagem", "Raio-X");
//        procedimentosMap.put("Laboratorial", "Exame de Sangue");
//
//        clinicaDTO = new ClinicaDTO(
//                1L,
//                "Clínica Teste",
//                "Rua Teste, 123",
//                "São Paulo",
//                "11999999999",
//                "clinica@teste.com",
//                procedimentosMap
//        );
//    }
//
//    @Test
//    void testToClinicaDTO_ShouldMapCorrectly() {
//        // Execução
//        ClinicaDTO result = clinicaMapper.toClinicaDTO(clinica);
//
//        // Verificações
//        assertNotNull(result);
//        assertEquals(clinica.getNome(), result.nome());
//        assertEquals(clinica.getEmail(), result.email());
//        assertEquals(clinica.getMunicipio(), result.municipio());
//        assertEquals(clinica.getTelefone(), result.telefone());
//        assertEquals(clinica.getEndereco(), result.endereco());
//
//        // Verifica mapeamento de procedimentos
//        Map<String, String> procedimentos = result.procedimentos();
//        assertEquals(2, procedimentos.size());
//        assertEquals("Raio-X", procedimentos.get("Imagem"));
//        assertEquals("Exame de Sangue", procedimentos.get("Laboratorial"));
//    }
//
//    @Test
//    void testToClinica_ShouldMapCorrectly() {
//        // Execução
//        Clinica result = clinicaMapper.toClinica(clinicaDTO);
//
//        // Verificações
//        assertNotNull(result);
//        assertEquals(clinicaDTO.nome(), result.getNome());
//        assertEquals(clinicaDTO.email(), result.getEmail());
//        assertEquals(clinicaDTO.municipio(), result.getMunicipio());
//        assertEquals(clinicaDTO.telefone(), result.getTelefone());
//        assertEquals(clinicaDTO.endereco(), result.getEndereco());
//
//        // Verifica mapeamento de procedimentos
//        List<Procedimento> procedimentos = result.getProcedimentos();
//        assertEquals(2, procedimentos.size());
//
//        // Verifica se os procedimentos foram mapeados corretamente
//        assertTrue(procedimentos.stream()
//                .anyMatch(p -> "Imagem".equals(p.getEspecializacao()) && "Raio-X".equals(p.getNome())));
//        assertTrue(procedimentos.stream()
//                .anyMatch(p -> "Laboratorial".equals(p.getEspecializacao()) && "Exame de Sangue".equals(p.getNome())));
//    }
//
//    @Test
//    void testToClinicaDTO_WithEmptyProcedimentos_ShouldMapEmptyMap() {
//        // Configuração
//        clinica.setProcedimentos(List.of());
//
//        // Execução
//        ClinicaDTO result = clinicaMapper.toClinicaDTO(clinica);
//
//        // Verificação
//        assertTrue(result.procedimentos().isEmpty());
//    }
//
//    @Test
//    void testToClinica_WithEmptyProcedimentos_ShouldMapEmptyList() {
//        // Configuração
//        clinicaDTO = new ClinicaDTO(
//                1L,
//                "Clínica Teste",
//                "Rua Teste, 123",
//                "São Paulo",
//                "11999999999",
//                "clinica@teste.com",
//                new HashMap<>()
//        );
//
//        // Execução
//        Clinica result = clinicaMapper.toClinica(clinicaDTO);
//
//        // Verificação
//        assertTrue(result.getProcedimentos().isEmpty());
//    }
//
//    @Test
//    void testToClinicaDTO_WithDuplicateSpecializations_ShouldConcatenate() {
//        // Configuração - Dois procedimentos com mesma especialização
//        Procedimento procedimento1 = new Procedimento("Imagem", "Raio-X");
//        Procedimento procedimento2 = new Procedimento("Imagem", "Ultrassom");
//        clinica.setProcedimentos(Arrays.asList(procedimento1, procedimento2));
//
//        // Execução
//        ClinicaDTO result = clinicaMapper.toClinicaDTO(clinica);
//
//        // Verificação
//        Map<String, String> procedimentos = result.procedimentos();
//        assertEquals(1, procedimentos.size());
//        assertEquals("Raio-X, Ultrassom", procedimentos.get("Imagem"));
//    }
//
//    @Test
//    void testToClinica_WithNullValues_ShouldHandleGracefully() {
//        // Configuração
//        clinicaDTO = new ClinicaDTO(
//                1L,
//                null,
//                null,
//                null,
//                null,
//                null,
//                null
//        );
//
//        // Execução
//        Clinica result = clinicaMapper.toClinica(clinicaDTO);
//
//        // Verificações
//        assertNotNull(result);
//        assertNull(result.getNome());
//        assertNull(result.getEndereco());
//        assertNull(result.getMunicipio());
//        assertNull(result.getTelefone());
//        assertNull(result.getEmail());
//        assertNull(result.getProcedimentos());
//    }
//
//    @Test
//    void testToClinicaDTO_WithNullValues_ShouldHandleGracefully() {
//        // Configuração
//        clinica.setNome(null);
//        clinica.setEmail(null);
//        clinica.setMunicipio(null);
//        clinica.setTelefone(null);
//        clinica.setEndereco(null);
//        clinica.setProcedimentos(null);
//
//        // Execução
//        ClinicaDTO result = clinicaMapper.toClinicaDTO(clinica);
//
//        // Verificações
//        assertNotNull(result);
//        assertNull(result.nome());
//        assertNull(result.endereco());
//        assertNull(result.municipio());
//        assertNull(result.telefone());
//        assertNull(result.email());
//        assertNull(result.procedimentos());
//    }
//}