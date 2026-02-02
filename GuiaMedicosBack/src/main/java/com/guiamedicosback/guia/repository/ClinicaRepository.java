package com.guiamedicosback.guia.repository;

import com.guiamedicosback.guia.entity.Clinica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ClinicaRepository extends JpaRepository<Clinica, Long>, JpaSpecificationExecutor<Clinica> {
    Clinica findByNome(String nome);
}