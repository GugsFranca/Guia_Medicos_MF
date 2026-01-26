package com.guiamedicosback.guia.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Grupo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    @ManyToOne
    @JoinColumn(name = "clinica_id")
    private Clinica clinica;

    @OneToMany(mappedBy = "grupo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Subgrupo> subgrupos = new ArrayList<>();

    public Grupo(String nomeGrupo) {
        this.nome = nomeGrupo;
    }

    // Método auxiliar para encontrar ou criar um subgrupo
    public Subgrupo encontrarOuCriarSubgrupo(String nomeSubgrupo) {
        for (Subgrupo subgrupo : subgrupos) {
            if (subgrupo.getNome().equals(nomeSubgrupo)) {
                return subgrupo;
            }
        }
        Subgrupo novoSubgrupo = new Subgrupo(nomeSubgrupo);
        novoSubgrupo.setGrupo(this);
        subgrupos.add(novoSubgrupo);
        return novoSubgrupo;
    }
}