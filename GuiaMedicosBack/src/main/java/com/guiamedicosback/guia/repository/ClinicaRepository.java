package com.guiamedicosback.guia.repository;

import com.guiamedicosback.guia.entity.Clinica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClinicaRepository extends JpaRepository<Clinica, Long> , JpaSpecificationExecutor<Clinica> {

    Optional<Clinica> findByNomeAndEndereco(String nome, String endereco);

    List<Clinica> findByMunicipioContainingIgnoreCase(String municipio);

    List<Clinica> findByNomeContainingIgnoreCase(String nome);

    @Query("SELECT DISTINCT c.municipio FROM Clinica c ORDER BY c.municipio")
    List<String> findDistinctMunicipios();

    @Query("SELECT DISTINCT c FROM Clinica c JOIN c.procedimentos p WHERE LOWER(p.nome) LIKE LOWER(CONCAT('%', :procedimento, '%'))")
    List<Clinica> findByProcedimentosNomeContainingIgnoreCase(@Param("procedimento") String procedimento);

    @Query("SELECT c FROM Clinica c JOIN c.procedimentos p WHERE LOWER(p.especializacao) = LOWER(:especializacao)")
    List<Clinica> findByEspecializacao(@Param("especializacao") String especializacao);
}