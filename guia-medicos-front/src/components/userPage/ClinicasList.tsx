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
    const [grupoSelecionado, setGrupoSelecionado] = useState<string>("");
    const [subgrupoSelecionado, setSubgrupoSelecionado] = useState<string>("");
    const [procedimentoSelecionado, setProcedimentoSelecionado] = useState<string>("");
    const [municipio, setMunicipio] = useState<string>("");

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

    // Extrai todos os grupos únicos
    const gruposCollection = useMemo(() => {
        const set = new Set<string>();
        clinicas.forEach(c =>
            c.grupos?.forEach(g => set.add(g.nome))
        );
        return Array.from(set).sort();
    }, [clinicas]);

    // Extrai subgrupos baseados no grupo selecionado
    const subgruposCollection = useMemo(() => {
        if (!grupoSelecionado) return [];
        const set = new Set<string>();

        clinicas.forEach(c => {
            c.grupos?.forEach(g => {
                if (g.nome === grupoSelecionado) {
                    g.subgrupos?.forEach(s => set.add(s.nome));
                }
            });
        });

        return Array.from(set).sort();
    }, [clinicas, grupoSelecionado]);

    // Extrai procedimentos baseados no grupo e subgrupo selecionados
    const procedimentosCollection = useMemo(() => {
        if (!grupoSelecionado) return [];

        const set = new Set<string>();

        clinicas.forEach(c => {
            c.grupos?.forEach(g => {
                if (g.nome === grupoSelecionado) {
                    g.subgrupos?.forEach(s => {
                        // Se há subgrupo selecionado, só pega procedimentos desse subgrupo
                        if (!subgrupoSelecionado || s.nome === subgrupoSelecionado) {
                            s.procedimentos?.forEach(p => set.add(p));
                        }
                    });
                }
            });
        });

        return Array.from(set).sort();
    }, [clinicas, grupoSelecionado, subgrupoSelecionado]);

    // Extrai municípios únicos
    const municipiosCollection = useMemo(() => {
        return Array.from(new Set(clinicas.map(c => c.municipio))).sort();
    }, [clinicas]);

    // Função para formatar procedimentos para exibição
    const formatarProcedimentos = (clinica: Clinica) => {
        const procedimentosPorGrupo: Record<string, string> = {};

        clinica.grupos?.forEach(grupo => {
            const procedimentosDoGrupo: string[] = [];

            grupo.subgrupos?.forEach(subgrupo => {
                procedimentosDoGrupo.push(...(subgrupo.procedimentos || []));
            });

            if (procedimentosDoGrupo.length > 0) {
                // Limita a 5 procedimentos por grupo para não ficar muito extenso
                const procedimentosExibidos = procedimentosDoGrupo.slice(0, 5);
                const sufixo = procedimentosDoGrupo.length > 5 ? '...' : '';
                procedimentosPorGrupo[grupo.nome] = procedimentosExibidos.join(', ') + sufixo;
            }
        });

        return procedimentosPorGrupo;
    };

    // Conta total de procedimentos por clínica
    const contarProcedimentos = (clinica: Clinica) => {
        let total = 0;
        let gruposCount = 0;
        let subgruposCount = 0;

        clinica.grupos?.forEach(grupo => {
            gruposCount++;
            grupo.subgrupos?.forEach(subgrupo => {
                subgruposCount++;
                total += subgrupo.procedimentos?.length || 0;
            });
        });

        return { total, gruposCount, subgruposCount };
    };

    const handleClearFilters = () => {
        setSearch("");
        setGrupoSelecionado("");
        setSubgrupoSelecionado("");
        setProcedimentoSelecionado("");
        setMunicipio("");
    };

    const hasFilters = search !== "" || grupoSelecionado !== "" ||
        subgrupoSelecionado !== "" || procedimentoSelecionado !== "" ||
        municipio !== "";

    // Filtro atualizado para a nova estrutura
    const filteredClinicas = useMemo(() => {
        return clinicas.filter(c => {
            // Filtro por texto (busca em todos os campos)
            const matchText = search === "" ||
                c.nome.toLowerCase().includes(search.toLowerCase()) ||
                c.endereco.toLowerCase().includes(search.toLowerCase()) ||
                c.municipio.toLowerCase().includes(search.toLowerCase()) ||
                c.email.toLowerCase().includes(search.toLowerCase()) ||
                c.telefone.toLowerCase().includes(search.toLowerCase()) ||
                // Busca nos procedimentos também
                (() => {
                    let encontrouProcedimento = false;
                    c.grupos?.forEach(g => {
                        g.subgrupos?.forEach(s => {
                            s.procedimentos?.forEach(p => {
                                if (p.toLowerCase().includes(search.toLowerCase())) {
                                    encontrouProcedimento = true;
                                }
                            });
                        });
                    });
                    return encontrouProcedimento;
                })();

            // Filtro por município
            const matchMunicipio = municipio === "" || c.municipio === municipio;

            // Filtro por grupo
            let matchGrupo = true;
            if (grupoSelecionado) {
                matchGrupo = c.grupos?.some(g => g.nome === grupoSelecionado) || false;
            }

            // Filtro por subgrupo (só aplica se grupo também foi selecionado)
            let matchSubgrupo = true;
            if (grupoSelecionado && subgrupoSelecionado) {
                matchSubgrupo = false;
                c.grupos?.forEach(g => {
                    if (g.nome === grupoSelecionado) {
                        if (g.subgrupos?.some(s => s.nome === subgrupoSelecionado)) {
                            matchSubgrupo = true;
                        }
                    }
                });
            }

            // Filtro por procedimento selecionado
            let matchProcedimento = true;
            if (procedimentoSelecionado) {
                matchProcedimento = false;
                c.grupos?.forEach(g => {
                    // Se há grupo selecionado, verifica apenas esse grupo
                    if (grupoSelecionado && g.nome !== grupoSelecionado) return;

                    g.subgrupos?.forEach(s => {
                        // Se há subgrupo selecionado, verifica apenas esse subgrupo
                        if (subgrupoSelecionado && s.nome !== subgrupoSelecionado) return;

                        if (s.procedimentos?.some(p => p === procedimentoSelecionado)) {
                            matchProcedimento = true;
                        }
                    });
                });
            }

            return matchText && matchMunicipio && matchGrupo && matchSubgrupo && matchProcedimento;
        });
    }, [clinicas, search, grupoSelecionado, subgrupoSelecionado, procedimentoSelecionado, municipio]);

    if (!mounted) return null;

    return (
        <Box
            maxW="container.lg"
            mx={{ base: "10px", md: "auto", lg: "auto" }}
            my={{ base: 4, md: 12 }}
            p={{ base: 4, md: 30 }}
            borderWidth="1px"
            borderRadius="lg"
            shadow="md"
            bg="bg.panel"
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
                    bg="bg.muted"
                />

                <Stack direction={{ base: "column", md: "row" }} gap={4} width="full">
                    {/* Select de Grupo */}
                    <NativeSelect.Root width="full">
                        <NativeSelect.Field
                            placeholder="Grupo"
                            value={grupoSelecionado}
                            onChange={(e) => {
                                setGrupoSelecionado(e.currentTarget.value);
                                setSubgrupoSelecionado(""); // Reseta subgrupo
                                setProcedimentoSelecionado(""); // Reseta procedimento
                            }}
                        >
                            {gruposCollection.map((grupo) => (
                                <option key={grupo} value={grupo}>{grupo}</option>
                            ))}
                        </NativeSelect.Field>
                        <NativeSelect.Indicator />
                    </NativeSelect.Root>

                    {/* Select de Subgrupo (aparece quando grupo for selecionado) */}
                    {grupoSelecionado && subgruposCollection.length > 0 && (
                        <NativeSelect.Root width="full">
                            <NativeSelect.Field
                                placeholder="Subgrupo"
                                value={subgrupoSelecionado}
                                onChange={(e) => {
                                    setSubgrupoSelecionado(e.currentTarget.value);
                                    setProcedimentoSelecionado(""); // Reseta procedimento
                                }}
                            >
                                {subgruposCollection.map((subgrupo) => (
                                    <option key={subgrupo} value={subgrupo}>{subgrupo}</option>
                                ))}
                            </NativeSelect.Field>
                            <NativeSelect.Indicator />
                        </NativeSelect.Root>
                    )}

                    {/* Select de Procedimento (aparece quando grupo for selecionado) */}
                    {grupoSelecionado && procedimentosCollection.length > 0 && (
                        <NativeSelect.Root width="full">
                            <NativeSelect.Field
                                placeholder="Procedimento"
                                value={procedimentoSelecionado}
                                onChange={(e) => setProcedimentoSelecionado(e.currentTarget.value)}
                            >
                                {procedimentosCollection.map((proc) => (
                                    <option key={proc} value={proc}>{proc}</option>
                                ))}
                            </NativeSelect.Field>
                            <NativeSelect.Indicator />
                        </NativeSelect.Root>
                    )}

                    {/* Input de texto para procedimento se nenhum grupo selecionado */}
                    {!grupoSelecionado && (
                        <Input
                            placeholder="Buscar procedimento..."
                            value={procedimentoSelecionado}
                            onChange={(e) => setProcedimentoSelecionado(e.target.value)}
                            size={{ base: "md", md: "lg" }}
                            bg="bg.muted"
                        />
                    )}

                    <NativeSelect.Root width="full">
                        <NativeSelect.Field
                            placeholder="Município"
                            value={municipio}
                            onChange={(e) => setMunicipio(e.currentTarget.value)}
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
                            width={{ base: "full", md: "auto" }}
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
                    {filteredClinicas.map((c, i) => {

                        return (
                            <Card.Root key={i} variant="elevated">
                                <Button flex={"content"} textAlign={"left"} >

                                    <Card.Body p={{ base: 4, md: 6 }}>
                                        {/* color="teal.fg" ou "green.fg" funciona bem nos dois modos */}
                                        <Heading size="md" color="teal.fg" mb={2}>{c.nome}</Heading>
                                        <Text fontSize="sm" color="fg.muted">{c.endereco}</Text>
                                        <Separator my={2} />
                                        <Text fontSize="sm" color="fg.default"><strong>Município:</strong> {c.municipio}</Text>
                                        {c.telefone && <Text fontSize="sm" color="fg.default"><strong>Telefone:</strong> {c.telefone}</Text>}
                                        {c.email && <Text fontSize="sm" color="fg.default"><strong>Email:</strong> {c.email}</Text>}

                                    </Card.Body>
                                </Button>

                            </Card.Root>
                        );
                    })}

                    {!loading && filteredClinicas.length === 0 && (
                        <Text textAlign="center" py={10} color="fg.subtle">
                            Nenhum resultado encontrado.
                        </Text>
                    )}
                </Stack>
            )}
        </Box>
    );
};

export default ClinicasList;