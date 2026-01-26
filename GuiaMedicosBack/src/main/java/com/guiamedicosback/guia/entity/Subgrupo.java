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
public class Subgrupo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    @ManyToOne
    @JoinColumn(name = "grupo_id")
    private Grupo grupo;

    @ElementCollection
    @CollectionTable(name = "procedimentos", joinColumns = @JoinColumn(name = "subgrupo_id"))
    @Column(name = "procedimento")
    private List<String> procedimentos = new ArrayList<>();

    public Subgrupo(String nomeSubgrupo) {
        this.nome = nomeSubgrupo;
    }
}
