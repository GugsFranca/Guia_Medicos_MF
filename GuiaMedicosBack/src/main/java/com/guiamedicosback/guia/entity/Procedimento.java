package com.guiamedicosback.guia.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@Builder
public class Procedimento {
    @Column(columnDefinition = "TEXT")
    private String nome;
    @Column(columnDefinition = "TEXT")
    private String especializacao; // Ex: "Imagem", "Laboratorial", "Fisioterapia"

    public Procedimento(String especializacao, String procedimentoDesc) {
        this.especializacao = especializacao;
        this.nome = procedimentoDesc;
    }
}
