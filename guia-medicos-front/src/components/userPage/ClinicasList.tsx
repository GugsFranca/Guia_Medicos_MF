"use client";

import {
    Box, Stack, Heading, Input, Text, Card, Spinner, Button, NativeSelect,
    Separator,
} from "@chakra-ui/react";

import { useEffect, useMemo, useState } from "react";
import type { Clinica } from "../types";
import { ColorModeButton } from "../ui/color-mode";
export const ClinicasList: React.FC = () => {

    const [mounted, setMounted] = useState(false);
    const [clinicas, setClinicas] = useState<Clinica[]>([]);
    const [loading, setLoading] = useState(true);

    const [search, setSearch] = useState("");
    const [especializacao, setEspecializacao] = useState<string[]>([]);
    const [procedimento, setProcedimento] = useState(""); // Novo estado para o segundo select
    const [municipio, setMunicipio] = useState<string[]>([]);


    useEffect(() => {
        setMounted(true);
        async function loadClinicas() {
            try {
                const res = await fetch("/api/clinicas");
                const data = await res.json();
                setClinicas(Array.isArray(data) ? data : []);
            } finally {
                setLoading(false);
            }
        }
        loadClinicas();
    }, []);

    // 1. Gera a lista de especialidades únicas
    const especializacoesCollection = useMemo(() => {
        const set = new Set<string>();
        clinicas.forEach(c => c.procedimentos && Object.keys(c.procedimentos).forEach(e => set.add(e)));
        return Array.from(set).sort();
    }, [clinicas]);

    // 2. 🔹 Lógica para extrair procedimentos da especialidade selecionada
    const procedimentosDisponiveis = useMemo(() => {
        if (especializacao.length === 0) return [];
        const espSelecionada = especializacao[0];
        const todosProcedimentos = new Set<string>();

        clinicas.forEach(c => {
            const procString = c.procedimentos[espSelecionada];
            if (procString) {
                // Separa por vírgula, remove espaços e adiciona ao Set
                procString.split(",").forEach(p => todosProcedimentos.add(p.trim()));
            }
        });

        return Array.from(todosProcedimentos).sort();
    }, [clinicas, especializacao]);

    const municipiosCollection = useMemo(() => {
        return Array.from(new Set(clinicas.map(c => c.municipio))).sort();
    }, [clinicas]);

    const handleClearFilters = () => {
        setSearch("");
        setEspecializacao([]);
        setProcedimento("");
        setMunicipio([]);
    };

    const hasFilters = search !== "" || especializacao.length > 0 || municipio.length > 0 || procedimento !== "";

    // 3. 🔹 Filtro atualizado
    const filteredClinicas = useMemo(() => {
        return clinicas.filter(c => {
            const matchText = c.nome.toLowerCase().includes(search.toLowerCase()) ||
                c.endereco.toLowerCase().includes(search.toLowerCase()) ||
                c.municipio.toLowerCase().includes(search.toLowerCase()) ||
                c.email.toLowerCase().includes(search.toLowerCase()) ||
                c.telefone.toLowerCase().includes(search.toLowerCase()) ||
                c.procedimentos && Object.values(c.procedimentos).some(proc =>
                    proc.toLowerCase().includes(search.toLowerCase())
                );

            const espSelecionada = especializacao[0];
            const matchEsp = !espSelecionada || !!c.procedimentos[espSelecionada];

            // Filtra pelo procedimento dentro da string separada por vírgulas
            const matchProc = !procedimento ||
                (c.procedimentos[espSelecionada]?.toLowerCase().includes(procedimento.toLowerCase()));

            const matchMun = municipio.length === 0 || municipio.some(m => c.municipio === m);

            return matchText && matchEsp && matchProc && matchMun;
        });
    }, [clinicas, search, especializacao, procedimento, municipio]);

    if (!mounted) return null;

    return (
        <Box
            maxW="container.lg"
            mx={{ base: "10px", md: "auto", lg: "auto" }} // Margem pequena no celular, auto no desktop
            my={{ base: 4, md: 12 }} // Espaçamento vertical adaptável
            p={{ base: 4, md: 30 }} // Padding interno menor no celular
            borderWidth="1px"
            borderRadius="lg"
            shadow="md"
            // bg="bg.panel" garante que o fundo mude no dark mode
            bg="bg.panel"
            // borderColor mudo para não ficar muito forte no dark mode
            borderColor="border.emphasized"
            width={{ base: "auto", md: "auto", lg: "1400px" }}
        >
            <Heading size={{ base: "lg", md: "xl" }} mb={6} textAlign="center" color="fg.default">
                Carteira de Serviços
                <ColorModeButton />

            </Heading>

            <Stack gap={4} mb={8}>
                <Input
                    placeholder="Pesquisar por nome, endereço, procedimento..."
                    value={search}
                    onChange={(e) => setSearch(e.target.value)}
                    size={{ base: "md", md: "lg" }}
                    // bg="bg.muted" ajuda a destacar o input do fundo
                    bg="bg.muted"
                />

                <Stack direction={{ base: "column", md: "row" }} gap={4} width="full">
                    {/* Select de Especialização */}
                    <NativeSelect.Root width="full">
                        <NativeSelect.Field
                            placeholder="Especialização"
                            value={especializacao[0] || ""}
                            onChange={(e) => {
                                const val = e.currentTarget.value;
                                setEspecializacao(val ? [val] : []);
                                setProcedimento(""); // Reseta o procedimento ao mudar a especialidade
                            }}
                        >
                            {especializacoesCollection.map((esp) => (
                                <option key={esp} value={esp}>{esp}</option>
                            ))}
                        </NativeSelect.Field>
                        <NativeSelect.Indicator />
                    </NativeSelect.Root>

                    {/* 🔹 Segundo Select: Só aparece se houver procedimentos para a especialidade */}
                    {procedimentosDisponiveis.length > 1 && (
                        <NativeSelect.Root width="full">
                            <NativeSelect.Field
                                placeholder="Filtrar por Procedimento"
                                value={procedimento}
                                onChange={(e) => setProcedimento(e.currentTarget.value)}
                            >
                                {procedimentosDisponiveis.map((proc) => (
                                    <option key={proc} value={proc}>{proc}</option>
                                ))}
                            </NativeSelect.Field>
                            <NativeSelect.Indicator />
                        </NativeSelect.Root>
                    )}

                    <NativeSelect.Root width="full">
                        <NativeSelect.Field
                            placeholder="Município"
                            value={municipio[0] || ""}
                            onChange={(e) => {
                                const val = e.currentTarget.value;
                                setMunicipio(val ? [val] : []);
                            }}
                        >
                            {municipiosCollection.map((m) => (
                                <option key={m} value={m}>{m}</option>
                            ))}
                        </NativeSelect.Field>
                        <NativeSelect.Indicator />
                    </NativeSelect.Root>

                    {hasFilters && (
                        <Button
                            variant="subtle"
                            colorPalette="red"
                            onClick={handleClearFilters}
                            width={{ base: "full", md: "auto" }} // Botão ocupa tudo no celular
                        >
                            Limpar
                        </Button>
                    )}
                </Stack>
            </Stack>

            {loading ? (
                <Stack align="center" py={10}><Spinner size="xl" /></Stack>
            ) : (
                <Stack gap={4}>
                    {filteredClinicas.map((c, i) => (
                        <Card.Root key={i} variant="elevated">
                            <Card.Body p={{ base: 4, md: 6 }}>
                                {/* color="teal.fg" ou "green.fg" funciona bem nos dois modos */}
                                <Heading size="md" color="teal.fg" mb={2}>{c.nome}</Heading>
                                <Text fontSize="sm" color="fg.muted">{c.endereco}</Text>
                                <Separator my={2} />
                                <Text fontSize="sm" color="fg.default"><strong>Município:</strong> {c.municipio}</Text>
                                {c.telefone && <Text fontSize="sm" color="fg.default"><strong>Telefone:</strong> {c.telefone}</Text>}
                                {c.email && <Text fontSize="sm" color="fg.default"><strong>Email:</strong> {c.email}</Text>}
                                <Box mt={3}>
                                    {/* <Text fontWeight="bold">Procedimentos:</Text>
                                    {Object.entries(c.procedimentos).map(([esp, proc]) => (
                                        <Text key={esp} fontSize="sm">
                                            <strong>{esp}:</strong> {proc}
                                        </Text>
                                    ))} */}
                                </Box>
                            </Card.Body>
                        </Card.Root>
                    ))}

                    {!loading && filteredClinicas.length === 0 && (
                        <Text textAlign="center" py={10} color="fg.subtle">Nenhum resultado encontrado.</Text>
                    )}
                </Stack>
            )}
        </Box>
    );
};

export default ClinicasList;