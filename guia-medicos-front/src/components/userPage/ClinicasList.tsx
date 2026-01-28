"use client";

import {
    Box, Stack, Heading, Input, Text, Card, Spinner, Button, NativeSelect,
    Separator,
    Badge,
    Flex,
    useBreakpointValue,
} from "@chakra-ui/react";

import { useEffect, useMemo, useState } from "react";
import type { Clinica } from "../types";
import { ColorModeButton } from "../ui/color-mode";
import { ClinicaModal } from "./ClinicaModal";

export const ClinicasList: React.FC = () => {
    const [mounted, setMounted] = useState(false);
    const [clinicas, setClinicas] = useState<Clinica[]>([]);
    const [loading, setLoading] = useState(true);

    const [search, setSearch] = useState("");
    const [grupoSelecionado, setGrupoSelecionado] = useState<string>("");
    const [subgrupoSelecionado, setSubgrupoSelecionado] = useState<string>("");
    const [procedimentoSelecionado, setProcedimentoSelecionado] = useState<string>("");
    const [municipio, setMunicipio] = useState<string>("");

    const [selectedClinica, setSelectedClinica] = useState<Clinica | null>(null);
    const [isModalOpen, setIsModalOpen] = useState(false);

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

    const handleOpenModal = (clinica: Clinica) => {

        setSelectedClinica(clinica);
        setIsModalOpen(true);
        document.body.style.overflow = "hidden";
    };

    const handleCloseModal = () => {
        setIsModalOpen(false);
        setSelectedClinica(null);
        // Devolve o scroll ao fundo
        document.body.style.overflow = "auto";
    };

    const subgruposCollection = useMemo(() => {
        const set = new Set<string>();
        clinicas.forEach(c => {
            c.grupos?.forEach(g => {
                g.subgrupos?.forEach(s => set.add(s.nome));
            });
        });
        return Array.from(set).sort();
    }, [clinicas]);

    // Extrai procedimentos baseados no grupo e subgrupo selecionados
    const procedimentosCollection = useMemo(() => {
        const set = new Set<string>();
        clinicas.forEach(c => {
            c.grupos?.forEach(g => {
                g.subgrupos?.forEach(s => {
                    // Se não há subgrupo selecionado, mostra todos. 
                    // Se há, mostra só os procedimentos daquele subgrupo.
                    if (!subgrupoSelecionado || s.nome === subgrupoSelecionado) {
                        s.procedimentos?.forEach(p => set.add(p));
                    }
                });
            });
        });
        return Array.from(set).sort();
    }, [clinicas, subgrupoSelecionado]);

    // Extrai municípios únicos
    const municipiosCollection = useMemo(() => {
        return Array.from(new Set(clinicas.map(c => c.municipio))).sort();
    }, [clinicas]);

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
            if (subgrupoSelecionado) {
                matchSubgrupo = c.grupos?.some(g =>
                    g.subgrupos?.some(s => s.nome === subgrupoSelecionado)
                ) || false;
            }

            // Filtro por procedimento
            let matchProcedimento = true;
            if (procedimentoSelecionado) {
                matchProcedimento = c.grupos?.some(g =>
                    g.subgrupos?.some(s =>
                        s.procedimentos?.some(p => p === procedimentoSelecionado)
                    )
                ) || false;
            }

            return matchText && matchMunicipio && matchSubgrupo && matchProcedimento;
        });
    }, [clinicas, search, subgrupoSelecionado, procedimentoSelecionado, municipio]);

    if (!mounted) return null;

    return (
        <Box
            maxW="container.lg"
            mx="auto"
            my={{ base: 2, md: 8, lg: 12 }}
            px={{ base: 3, sm: 4, md: 6 }}
            py={{ base: 4, md: 8 }}
            borderWidth="1px"
            borderRadius="lg"
            shadow={{ base: "sm", md: "md" }}
            bg="bg.panel"
            borderColor="border.emphasized"
            width="100%"
            boxSizing="border-box"
            fontSize={{ base: "md", md: "lg" }}
        >
            <Flex justifyContent="space-between" alignItems="center" mb={6} wrap="wrap" gap={3}>
                <Heading size={"xl"} color="fg.default" flex="1" minWidth="200px">
                    Carteira de Serviços
                </Heading>
                <Box>
                    <ColorModeButton />
                </Box>
            </Flex>

            <Stack gap={4} mb={8}>
                <Input
                    placeholder="Pesquisar por nome, endereço, procedimento..."
                    value={search}
                    onChange={(e) => setSearch(e.target.value)}
                    size={"lg"}
                    bg="bg.muted"
                    width="100%"
                />

                <Stack direction={{ base: "column", sm: "row" }} gap={3} width="full" flexWrap="wrap">


                    <Box flex={{ base: "1 1 100%", sm: "1 1 200px" }} minWidth="150px">
                        <NativeSelect.Root width="100%" >
                            <NativeSelect.Field
                                placeholder="Filtrar por grupo"
                                value={subgrupoSelecionado}
                                onChange={(e) => {
                                    setSubgrupoSelecionado(e.currentTarget.value);
                                    setProcedimentoSelecionado("");
                                }}
                                cursor="pointer"
                            >
                                {subgruposCollection.map((sub) => (
                                    <option key={sub} value={sub}>{sub}</option>
                                ))}
                            </NativeSelect.Field>
                            <NativeSelect.Indicator />
                        </NativeSelect.Root>
                    </Box>

                    {subgrupoSelecionado && procedimentosCollection.length > 0 && (
                        <Box flex={{ base: "1 1 100%", sm: "1 1 200px" }} minWidth="150px">
                            <NativeSelect.Root width="100%">
                                <NativeSelect.Field
                                    placeholder="Filtrar por Procedimento"
                                    value={procedimentoSelecionado}
                                    onChange={(e) => setProcedimentoSelecionado(e.currentTarget.value)}
                                    cursor="pointer"

                                >
                                    {procedimentosCollection.map((proc) => (
                                        <option key={proc} value={proc} >{proc}</option>
                                    ))}
                                </NativeSelect.Field>
                                <NativeSelect.Indicator />
                            </NativeSelect.Root>
                        </Box>
                    )}

                    <Box flex={{ base: "1 1 100%", sm: "1 1 200px" }} minWidth="150px">
                        <NativeSelect.Root width="100%">
                            <NativeSelect.Field
                                placeholder="Município"
                                value={municipio}
                                onChange={(e) => setMunicipio(e.currentTarget.value)}
                                cursor="pointer"

                            >
                                {municipiosCollection.map((m) => (
                                    <option key={m} value={m}>{m}</option>
                                ))}
                            </NativeSelect.Field>
                            <NativeSelect.Indicator />
                        </NativeSelect.Root>
                    </Box>

                    {hasFilters && (
                        <Box flex={{ base: "1 1 100%", sm: "0 0 auto" }} width={{ base: "100%", sm: "auto" }}>
                            <Button
                                variant="subtle"
                                colorPalette="red"
                                onClick={handleClearFilters}
                                width={{ base: "100%", sm: "auto" }}
                                size={"lg"}
                            >
                                Limpar Filtros
                            </Button>
                        </Box>
                    )}
                </Stack>
            </Stack>

            {loading ? (
                <Stack align="center" py={10}>
                    <Spinner size="xl" />
                </Stack>
            ) : (
                <Stack gap={3}>
                    {filteredClinicas.map((c, i) => (
                        <Card.Root key={i} variant="elevated" width="100%" as="button"
                            onClick={() => handleOpenModal(c)}
                            _hover={{ shadow: "md", transform: "translateY(-2px)", transition: "all 0.2s", cursor: "pointer" }} size="lg"
                        >

                            <Card.Body p={{ base: 3, sm: 4, md: 6 }} width="100%">
                                <Heading size="md" color="teal.fg" mb={2} whiteSpace="normal">{c.nome}</Heading>
                                <Text fontSize={{ base: "sm", sm: "md" }} color="fg.muted" mb={2} whiteSpace="normal">{c.endereco}</Text>

                                <Separator my={2} />

                                <Stack gap={1} fontSize={{ base: "sm", sm: "md" }}>
                                    {c.municipio && (
                                        <Text color="fg.default">
                                            <strong>Município:</strong> {c.municipio}
                                        </Text>
                                    )}
                                    {c.telefone && (
                                        <Text color="fg.default">
                                            <strong>Telefone:</strong> {c.telefone}
                                        </Text>
                                    )}
                                    {c.email && (
                                        <Text color="fg.default">
                                            <strong>Email:</strong> {c.email}
                                        </Text>
                                    )}
                                </Stack>

                                <Separator my={2} />
                                <Text fontSize={{ base: "sm", sm: "md" }} color="fg.muted">Clique para mais detalhes...</Text>

                            </Card.Body>
                        </Card.Root>
                    ))}

                    {!loading && filteredClinicas.length === 0 && (
                        <Text textAlign="center" py={10} color="fg.subtle" fontSize={{ base: "md", md: "lg" }}>
                            Nenhum resultado encontrado.
                        </Text>
                    )}
                </Stack>
            )}
            <ClinicaModal
                clinica={selectedClinica}
                isOpen={isModalOpen}
                onClose={handleCloseModal}
            />
        </Box >
    );
};

export default ClinicasList;
