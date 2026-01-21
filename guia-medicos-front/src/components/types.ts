export type Clinica = {
    id: number;
    nome: string;
    endereco: string;
    municipio: string;
    telefone: string;
    email: string;
    procedimentos: Record<string, string>;
};