package com.guiamedicosback.entity;

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
    @ElementCollection(fetch = FetchType.EAGER)
    private List<Procedimento> procedimentos = new ArrayList<>();

}
