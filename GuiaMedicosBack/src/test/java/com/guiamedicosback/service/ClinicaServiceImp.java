package com.guiamedicosback.service;

import com.guiamedicosback.guia.entity.Clinica;
import com.guiamedicosback.guia.entity.Procedimento;
import com.guiamedicosback.guia.entity.dto.ClinicaDTO;
import com.guiamedicosback.guia.repository.ClinicaRepository;
import com.guiamedicosback.guia.service.ClinicaMapper;
import com.guiamedicosback.guia.service.ClinicaServiceImp;
import com.guiamedicosback.guia.service.ExcelProcessorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClinicaServiceImpTest {

    @Mock
    private ClinicaRepository clinicaRepository;

    @Mock
    private ExcelProcessorService processorService;

    @Mock
    private ClinicaMapper clinicaMapper;

    @InjectMocks
    private ClinicaServiceImp clinicaService;

    @Captor
    private ArgumentCaptor<Specification<Clinica>> specificationCaptor;

    private Clinica clinica;
    private ClinicaDTO clinicaDTO;
    private MultipartFile multipartFile;

    @BeforeEach
    void setUp() {
        // Configuração da clínica
        List<Procedimento> procedimentos = Arrays.asList(
                new Procedimento("Imagem", "Raio-X"),
                new Procedimento("Laboratorial", "Exame de Sangue")
        );

        clinica = Clinica.builder()
                .id(1L)
                .nome("Clínica Teste")
                .email("clinica@teste.com")
                .municipio("São Paulo")
                .telefone("11999999999")
                .endereco("Rua Teste, 123")
                .procedimentos(procedimentos)
                .build();

        // Configuração da ClinicaDTO
        Map<String, String> procedimentosMap = new HashMap<>();
        procedimentosMap.put("Imagem", "Raio-X");
        procedimentosMap.put("Laboratorial", "Exame de Sangue");

        clinicaDTO = ClinicaDTO.builder()
                .nome("Clínica Teste")
                .email("clinica@teste.com")
                .municipio("São Paulo")
                .telefone("11999999999")
                .endereco("Rua Teste, 123")
                .procedimentos(procedimentosMap)
                .build();

        multipartFile = mock(MultipartFile.class);
    }

    @Test
    void addClinicaFromFile_WithValidFile_ShouldDeleteAllAndSaveAll() throws IOException {
        // Arrange
        List<Clinica> clinicas = Arrays.asList(clinica);
        when(processorService.processarExcel(multipartFile)).thenReturn(clinicas);
        when(clinicaRepository.saveAll(clinicas)).thenReturn(clinicas);

        // Act
        clinicaService.addClinicaFromFile(multipartFile);

        // Assert
        verify(processorService, times(1)).processarExcel(multipartFile);
        verify(clinicaRepository, times(1)).deleteAll();
        verify(clinicaRepository, times(1)).saveAll(clinicas);
    }

    @Test
    void addClinicaFromFile_WithEmptyFile_ShouldDoNothing() throws IOException {
        // Arrange
        when(multipartFile.isEmpty()).thenReturn(true);

        // Act
        clinicaService.addClinicaFromFile(multipartFile);

        // Assert
        verify(processorService, never()).processarExcel((InputStream) any());
        verify(clinicaRepository, never()).deleteAll();
        verify(clinicaRepository, never()).saveAll(anyList());
    }

    @Test
    void addClinicaFromFile_WhenProcessorThrowsIOException_ShouldThrowIOException() throws IOException {
        // Arrange
        when(multipartFile.isEmpty()).thenReturn(false);
        when(processorService.processarExcel(multipartFile)).thenThrow(new IOException("Erro no arquivo"));

        // Act & Assert
        assertThrows(IOException.class, () -> clinicaService.addClinicaFromFile(multipartFile));
        verify(clinicaRepository, never()).deleteAll();
        verify(clinicaRepository, never()).saveAll(anyList());
    }

    @Test
    void addClinicaFromFile_WhenProcessorThrowsGenericException_ShouldWrapInIOException() throws IOException {
        // Arrange
        when(multipartFile.isEmpty()).thenReturn(false);
        when(processorService.processarExcel(multipartFile)).thenThrow(new RuntimeException("Erro genérico"));

        // Act & Assert
        IOException exception = assertThrows(IOException.class,
                () -> clinicaService.addClinicaFromFile(multipartFile));
        assertTrue(exception.getMessage().contains("Erro ao processar o arquivo Excel"));
    }

    @Test
    void addClinica_WithValidDTO_ShouldSaveAndReturnDTO() {
        // Arrange
        when(clinicaMapper.toClinica(clinicaDTO)).thenReturn(clinica);
        when(clinicaRepository.save(clinica)).thenReturn(clinica);
        when(clinicaMapper.toClinicaDTO(clinica)).thenReturn(clinicaDTO);

        // Act
        ClinicaDTO result = clinicaService.addClinica(clinicaDTO);

        // Assert
        assertNotNull(result);
        assertEquals(clinicaDTO, result);
        verify(clinicaMapper, times(1)).toClinica(clinicaDTO);
        verify(clinicaRepository, times(1)).save(clinica);
        verify(clinicaMapper, times(1)).toClinicaDTO(clinica);
    }

    @Test
    void addClinica_WithNullDTO_ShouldReturnNull() {
        // Act
        ClinicaDTO result = clinicaService.addClinica(null);

        // Assert
        assertNull(result);
        verify(clinicaMapper, never()).toClinica(any());
        verify(clinicaRepository, never()).save(any());
    }

    @Test
    void addClinica_WhenMapperThrowsException_ShouldThrowRuntimeException() {
        // Arrange
        when(clinicaMapper.toClinica(clinicaDTO)).thenThrow(new RuntimeException("Erro no mapper"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> clinicaService.addClinica(clinicaDTO));
        assertEquals("Erro ao adicionar clínica: Erro no mapper", exception.getMessage());
    }

    @Test
    void deleteClinica_WithValidId_ShouldDelete() {
        // Arrange
        Long id = 1L;

        // Act
        clinicaService.deleteClinica(id);

        // Assert
        verify(clinicaRepository, times(1)).deleteById(id);
    }

    @Test
    void deleteClinica_WithNullId_ShouldDoNothing() {
        // Act
        clinicaService.deleteClinica(null);

        // Assert
        verify(clinicaRepository, never()).deleteById(any());
    }

    @Test
    void deleteClinica_WhenRepositoryThrowsException_ShouldThrowRuntimeException() {
        // Arrange
        Long id = 1L;
        doThrow(new RuntimeException("Erro no banco")).when(clinicaRepository).deleteById(id);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> clinicaService.deleteClinica(id));
        assertEquals("Erro ao deletar clínica: Erro no banco", exception.getMessage());
    }

    @Test
    void updateClinica_WithValidIdAndDTO_ShouldUpdateAndReturnDTO() {
        // Arrange
        Long id = 1L;

        // Criar uma clínica mock separada para evitar efeitos colaterais
        Clinica clinicaMock = Clinica.builder()
                .id(1L)
                .nome("Clínica Original")
                .email("original@teste.com")
                .municipio("São Paulo")
                .telefone("11999999999")
                .endereco("Rua Original, 123")
                .procedimentos(new ArrayList<>(Arrays.asList(
                        new Procedimento("Imagem", "Raio-X Antigo"),
                        new Procedimento("Laboratorial", "Exame Antigo")
                )))
                .build();

        // Mock do repositório
        when(clinicaRepository.findById(id)).thenReturn(Optional.of(clinicaMock));

        // Mock do save - usar ArgumentCaptor para capturar a clínica que será salva
        when(clinicaRepository.save(any(Clinica.class))).thenAnswer(invocation -> {
            Clinica savedClinica = invocation.getArgument(0);
            savedClinica.setId(1L); // Garantir que tem ID
            return savedClinica;
        });

        // Mock do mapper
        when(clinicaMapper.toClinicaDTO(any(Clinica.class))).thenReturn(clinicaDTO);

        // Act
        ClinicaDTO result = clinicaService.updateClinica(id, clinicaDTO);

        // Assert
        assertNotNull(result);
        assertEquals(clinicaDTO.nome(), result.nome());
        assertEquals(clinicaDTO.email(), result.email());

        // Verificar que os métodos foram chamados
        verify(clinicaRepository, times(1)).findById(id);
        verify(clinicaRepository, times(1)).save(any(Clinica.class));
        verify(clinicaMapper, times(1)).toClinicaDTO(any(Clinica.class));

        // Opcional: verificar que a clínica mock foi modificada
        // (mas cuidado com efeitos colaterais)
        assertEquals(0, clinicaMock.getProcedimentos().size()); // Foi limpo
    }

    @Test
    void updateClinica_ShouldCreateProcedimentosWithCorrectParameterOrder() {
        // Arrange
        Long id = 1L;

        // DTO com um procedimento específico para testar a ordem
        Map<String, String> procedimentosMap = new HashMap<>();
        procedimentosMap.put("Especializacao Teste", "Nome do Procedimento");

        ClinicaDTO clinicaDTO = ClinicaDTO.builder()
                .nome("Clínica Teste")
                .procedimentos(procedimentosMap)
                .build();

        Clinica existingClinica = Clinica.builder()
                .id(id)
                .nome("Original")
                .procedimentos(new ArrayList<>())
                .build();

        when(clinicaRepository.findById(id)).thenReturn(Optional.of(existingClinica));

        ArgumentCaptor<Clinica> clinicaCaptor = ArgumentCaptor.forClass(Clinica.class);
        when(clinicaRepository.save(clinicaCaptor.capture())).thenReturn(existingClinica);

        when(clinicaMapper.toClinicaDTO(any(Clinica.class))).thenReturn(clinicaDTO);

        // Act
        clinicaService.updateClinica(id, clinicaDTO);

        // Assert
        Clinica savedClinica = clinicaCaptor.getValue();
        Procedimento procedimento = savedClinica.getProcedimentos().get(0);

        // Verificar a ORDEM CORRETA dos parâmetros
        // especializacao deve ser a KEY do map
        // nome deve ser o VALUE do map
        assertEquals("Especializacao Teste", procedimento.getEspecializacao());
        assertEquals("Nome do Procedimento", procedimento.getNome());
    }

    @Test
    void updateClinica_ShouldClearExistingProcedimentosAndAddNewOnes() {
        // Arrange
        Long id = 1L;
        Clinica existingClinica = Clinica.builder()
                .id(id)
                .nome("Nome Antigo")
                .procedimentos(new ArrayList<>(Arrays.asList(
                        new Procedimento("Antiga", "Procedimento Antigo")
                )))
                .build();

        when(clinicaRepository.findById(id)).thenReturn(Optional.of(existingClinica));
        when(clinicaRepository.save(existingClinica)).thenReturn(existingClinica);
        when(clinicaMapper.toClinicaDTO(existingClinica)).thenReturn(clinicaDTO);

        // Act
        clinicaService.updateClinica(id, clinicaDTO);

        // Assert
        // Verifica se os procedimentos antigos foram removidos
        assertTrue(existingClinica.getProcedimentos().isEmpty());

        // Verifica se os campos foram atualizados
        assertEquals(clinicaDTO.nome(), existingClinica.getNome());
        assertEquals(clinicaDTO.email(), existingClinica.getEmail());
    }

    @Test
    void updateClinica_WithNonExistingId_ShouldThrowRuntimeException() {
        // Arrange
        Long id = 1L;
        when(clinicaRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> clinicaService.updateClinica(id, clinicaDTO));
        assertEquals("Erro ao atualizar clínica: Clínica não encontrada", exception.getMessage());
    }

    @Test
    void updateClinica_WithNullId_ShouldReturnNull() {
        // Act
        ClinicaDTO result = clinicaService.updateClinica(null, clinicaDTO);

        // Assert
        assertNull(result);
        verify(clinicaRepository, never()).findById(any());
    }

    @Test
    void updateClinica_WithNullDTO_ShouldReturnNull() {
        // Act
        ClinicaDTO result = clinicaService.updateClinica(1L, null);

        // Assert
        assertNull(result);
        verify(clinicaRepository, never()).findById(any());
    }

    @Test
    void updateClinica_WhenRepositoryThrowsException_ShouldThrowRuntimeException() {
        // Arrange
        Long id = 1L;
        when(clinicaRepository.findById(id)).thenReturn(Optional.of(clinica));
        when(clinicaRepository.save(clinica)).thenThrow(new RuntimeException("Erro no banco"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> clinicaService.updateClinica(id, clinicaDTO));
        assertEquals("Erro ao atualizar clínica: Erro no banco", exception.getMessage());
    }

    @Test
    void getClinicaById_WithValidId_ShouldReturnDTO() {
        // Arrange
        Long id = 1L;
        when(clinicaRepository.findById(id)).thenReturn(Optional.of(clinica));
        when(clinicaMapper.toClinicaDTO(clinica)).thenReturn(clinicaDTO);

        // Act
        ClinicaDTO result = clinicaService.getClinicaById(id);

        // Assert
        assertNotNull(result);
        assertEquals(clinicaDTO, result);
        verify(clinicaRepository, times(1)).findById(id);
        verify(clinicaMapper, times(1)).toClinicaDTO(clinica);
    }

    @Test
    void getClinicaById_WithNonExistingId_ShouldThrowRuntimeException() {
        // Arrange
        Long id = 1L;
        when(clinicaRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> clinicaService.getClinicaById(id));
        assertEquals("Erro ao buscar clínica: Clínica não encontrada", exception.getMessage());
    }

    @Test
    void getClinicaById_WithNullId_ShouldReturnNull() {
        // Act
        ClinicaDTO result = clinicaService.getClinicaById(null);

        // Assert
        assertNull(result);
        verify(clinicaRepository, never()).findById(any());
    }

    @Test
    void getClinicaById_WhenRepositoryThrowsException_ShouldThrowRuntimeException() {
        // Arrange
        Long id = 1L;
        when(clinicaRepository.findById(id)).thenThrow(new RuntimeException("Erro no repositório"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> clinicaService.getClinicaById(id));
        assertEquals("Erro ao buscar clínica: Erro no repositório", exception.getMessage());
    }

    @Test
    void getClinicas_ShouldReturnListOfDTOs() {
        // Arrange
        List<Clinica> clinicas = Arrays.asList(clinica);
        when(clinicaRepository.findAll()).thenReturn(clinicas);
        when(clinicaMapper.toClinicaDTO(clinica)).thenReturn(clinicaDTO);

        // Act
        List<ClinicaDTO> result = clinicaService.getClinicas();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(clinicaDTO, result.get(0));
        verify(clinicaRepository, times(1)).findAll();
        verify(clinicaMapper, times(1)).toClinicaDTO(clinica);
    }

    @Test
    void getClinicas_WhenRepositoryReturnsEmpty_ShouldReturnEmptyList() {
        // Arrange
        when(clinicaRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<ClinicaDTO> result = clinicaService.getClinicas();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void searchClinicas_WithAllEmptyParams_ShouldReturnAllClinicas() {
        // Arrange
        List<Clinica> clinicas = Arrays.asList(clinica);
        when(clinicaRepository.findAll()).thenReturn(clinicas);
        when(clinicaMapper.toClinicaDTO(clinica)).thenReturn(clinicaDTO);

        // Act
        List<ClinicaDTO> result = clinicaService.searchClinicas("", "", "", "", "");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(clinicaRepository, times(1)).findAll();
        verify(clinicaMapper, times(1)).toClinicaDTO(clinica);
    }

    @Test
    void searchClinicas_WithOnlyNome_ShouldApplySpecification() {
        // Arrange
        List<Clinica> clinicas = Arrays.asList(clinica);
        when(clinicaRepository.findAll(any(Specification.class))).thenReturn(clinicas);
        when(clinicaMapper.toClinicaDTO(clinica)).thenReturn(clinicaDTO);

        // Act
        List<ClinicaDTO> result = clinicaService.searchClinicas("Teste", "", "", "", "");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(clinicaRepository, times(1)).findAll(any(Specification.class));
        verify(clinicaMapper, times(1)).toClinicaDTO(clinica);
    }

    @Test
    void searchClinicas_WithMultipleParams_ShouldApplyAllSpecifications() {
        // Arrange
        List<Clinica> clinicas = Arrays.asList(clinica);
        when(clinicaRepository.findAll(any(Specification.class))).thenReturn(clinicas);
        when(clinicaMapper.toClinicaDTO(clinica)).thenReturn(clinicaDTO);

        // Act
        List<ClinicaDTO> result = clinicaService.searchClinicas("Teste", "São Paulo", "Rua Teste", "Raio-X", "Imagem");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(clinicaRepository, times(1)).findAll(any(Specification.class));
        verify(clinicaMapper, times(1)).toClinicaDTO(clinica);
    }

    @Test
    void searchClinicas_WithProcedimento_ShouldJoinProcedimentos() {
        // Arrange
        List<Clinica> clinicas = Arrays.asList(clinica);
        when(clinicaRepository.findAll(specificationCaptor.capture())).thenReturn(clinicas);
        when(clinicaMapper.toClinicaDTO(clinica)).thenReturn(clinicaDTO);

        // Act
        clinicaService.searchClinicas("", "", "", "Raio-X", "");

        // Assert
        verify(clinicaRepository, times(1)).findAll(any(Specification.class));
        // A specification deve ter sido aplicada
        assertNotNull(specificationCaptor.getValue());
    }

    @Test
    void searchClinicas_WhenRepositoryThrowsException_ShouldThrowRuntimeException() {
        // Arrange
        when(clinicaRepository.findAll(any(Specification.class))).thenThrow(new RuntimeException("Erro na busca"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> clinicaService.searchClinicas("Teste", "", "", "", ""));
        assertEquals("Erro ao buscar clínicas: Erro na busca", exception.getMessage());
    }

    @Test
    void searchClinicas_WithNullParams_ShouldTreatAsEmpty() {
        // Arrange
        List<Clinica> clinicas = Arrays.asList(clinica);
        when(clinicaRepository.findAll()).thenReturn(clinicas);
        when(clinicaMapper.toClinicaDTO(clinica)).thenReturn(clinicaDTO);

        // Act
        List<ClinicaDTO> result = clinicaService.searchClinicas(null, null, null, null, null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        // Deve chamar findAll() porque todos os parâmetros são nulos/empty
        verify(clinicaRepository, times(1)).findAll();
    }

    @Test
    void searchClinicas_WithMixedNullAndEmpty_ShouldApplyOnlyNonEmpty() {
        // Arrange
        List<Clinica> clinicas = Arrays.asList(clinica);
        when(clinicaRepository.findAll(any(Specification.class))).thenReturn(clinicas);
        when(clinicaMapper.toClinicaDTO(clinica)).thenReturn(clinicaDTO);

        // Act
        List<ClinicaDTO> result = clinicaService.searchClinicas("Teste", null, "", null, "");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        // Deve aplicar specification apenas para o nome
        verify(clinicaRepository, times(1)).findAll(any(Specification.class));
    }
}