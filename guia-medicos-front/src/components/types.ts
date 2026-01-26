export interface Clinica {
    id?: number;
    nome: string;
    endereco: string;
    municipio: string;
    telefone: string;
    email: string;
    grupos: Grupo[];
}

export interface Grupo {
    nome: string;
    subgrupos: Subgrupo[];
}

export interface Subgrupo {
    nome: string;
    procedimentos: string[];
}