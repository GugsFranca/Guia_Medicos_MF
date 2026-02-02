package com.guiamedicosback.guia.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Clinica {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String endereco;
    private String municipio;
    private String telefone;
    private String email;

    @OneToMany(mappedBy = "clinica", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Grupo> grupos = new ArrayList<>();

    // Método auxiliar para encontrar ou criar um grupo
    public Grupo encontrarOuCriarGrupo(String nomeGrupo) {
        for (Grupo grupo : grupos) {
            if (grupo.getNome().equals(nomeGrupo)) {
                return grupo;
            }
        }
        Grupo novoGrupo = new Grupo(nomeGrupo);
        novoGrupo.setClinica(this);
        grupos.add(novoGrupo);
        return novoGrupo;
    }
}
