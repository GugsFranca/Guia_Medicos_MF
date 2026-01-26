"use client";

import {
    Box, Stack, Heading, Input, Text, Card, Spinner, Button,
    Separator, Flex, Table, IconButton
} from "@chakra-ui/react";
import { useEffect, useState } from "react";
import type { Clinica, Grupo, Subgrupo } from "../types";
import { ColorModeButton } from "../ui/color-mode";
import { MdEdit, MdDelete, MdCheck, MdClose } from "react-icons/md"; // Material Design icons

export const AdminPage = () => {
    const [clinicas, setClinicas] = useState<Clinica[]>([]);
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);

    // Estado para o formulário
    const [editId, setEditId] = useState<number | null>(null);
    const [formData, setFormData] = useState<Partial<Clinica>>({
        nome: "",
        endereco: "",
        municipio: "",
        telefone: "",
        email: "",
        grupos: []
    });

    // Estados auxiliares para adicionar novos grupos/subgrupos
    const [newGrupo, setNewGrupo] = useState("");
    const [newSubgrupo, setNewSubgrupo] = useState("");
    const [newProcedimentos, setNewProcedimentos] = useState("");

    // Estados para edição inline
    const [editingProcedimento, setEditingProcedimento] = useState<{
        grupoIndex: number;
        subgrupoIndex: number;
        procedimentoIndex: number;
    } | null>(null);
    const [editProcedimentoText, setEditProcedimentoText] = useState("");

    useEffect(() => {
        loadClinicas();
    }, []);

    async function loadClinicas() {
        setLoading(true);
        try {
            const res = await fetch("/api/clinicas");
            const data = await res.json();
            setClinicas(Array.isArray(data) ? data : []);
            console.log("Clínicas carregadas:", data);
        } finally {
            setLoading(false);
        }
    }

    const handleSave = async () => {
        setSaving(true);
        const method = editId ? "PUT" : "POST";
        const url = editId ? `/api/clinicas/${editId}` : "/api/clinicas";

        try {
            const res = await fetch(url, {
                method,
                body: JSON.stringify(formData),
                headers: { "Content-Type": "application/json" }
            });

            if (res.ok) {
                resetForm();
                loadClinicas();
                alert("Salvo com sucesso!");
            } else {
                alert("Erro ao salvar");
            }
        } finally {
            setSaving(false);
        }
    };

    const handleDelete = async (id: number) => {
        if (!confirm("Tem certeza que deseja excluir?")) return;
        try {
            const res = await fetch(`/api/clinicas/${id}`, { method: "DELETE" });
            if (res.ok) loadClinicas();
        } catch (err) {
            alert("Erro ao deletar");
        }
    };

    const resetForm = () => {
        setEditId(null);
        setFormData({
            nome: "",
            endereco: "",
            municipio: "",
            telefone: "",
            email: "",
            grupos: []
        });
        setNewGrupo("");
        setNewSubgrupo("");
        setNewProcedimentos("");
        setEditingProcedimento(null);
    };

    const addGrupoComSubgrupo = () => {
        if (!newGrupo || !newSubgrupo || !newProcedimentos) return;

        const procedimentosArray = newProcedimentos
            .split(",")
            .map(p => p.trim())
            .filter(p => p !== "");

        const novoSubgrupo: Subgrupo = {
            nome: newSubgrupo,
            procedimentos: procedimentosArray
        };

        // Verifica se o grupo já existe
        const gruposAtualizados = [...(formData.grupos || [])];
        const grupoIndex = gruposAtualizados.findIndex(g => g.nome === newGrupo);

        if (grupoIndex >= 0) {
            // Adiciona subgrupo ao grupo existente
            gruposAtualizados[grupoIndex].subgrupos.push(novoSubgrupo);
        } else {
            // Cria novo grupo
            const novoGrupoItem: Grupo = {
                nome: newGrupo,
                subgrupos: [novoSubgrupo]
            };
            gruposAtualizados.push(novoGrupoItem);
        }

        setFormData({
            ...formData,
            grupos: gruposAtualizados
        });

        setNewGrupo("");
        setNewSubgrupo("");
        setNewProcedimentos("");
    };

    const addProcedimentoToSubgrupo = (grupoIndex: number, subgrupoIndex: number) => {
        if (!newProcedimentos.trim()) return;

        const gruposAtualizados = [...(formData.grupos || [])];
        const novosProcedimentos = newProcedimentos
            .split(",")
            .map(p => p.trim())
            .filter(p => p !== "");

        gruposAtualizados[grupoIndex].subgrupos[subgrupoIndex].procedimentos.push(...novosProcedimentos);

        setFormData({
            ...formData,
            grupos: gruposAtualizados
        });

        setNewProcedimentos("");
    };

    const removeGrupo = (grupoIndex: number) => {
        const gruposAtualizados = [...(formData.grupos || [])];
        gruposAtualizados.splice(grupoIndex, 1);
        setFormData({ ...formData, grupos: gruposAtualizados });
    };

    const removeSubgrupo = (grupoIndex: number, subgrupoIndex: number) => {
        const gruposAtualizados = [...(formData.grupos || [])];
        gruposAtualizados[grupoIndex].subgrupos.splice(subgrupoIndex, 1);

        if (gruposAtualizados[grupoIndex].subgrupos.length === 0) {
            gruposAtualizados.splice(grupoIndex, 1);
        }

        setFormData({ ...formData, grupos: gruposAtualizados });
    };

    const removeProcedimento = (grupoIndex: number, subgrupoIndex: number, procedimentoIndex: number) => {
        const gruposAtualizados = [...(formData.grupos || [])];
        gruposAtualizados[grupoIndex].subgrupos[subgrupoIndex].procedimentos.splice(procedimentoIndex, 1);

        setFormData({ ...formData, grupos: gruposAtualizados });
    };

    const startEditProcedimento = (grupoIndex: number, subgrupoIndex: number, procedimentoIndex: number) => {
        const grupos = formData.grupos || [];
        setEditingProcedimento({ grupoIndex, subgrupoIndex, procedimentoIndex });
        setEditProcedimentoText(grupos[grupoIndex].subgrupos[subgrupoIndex].procedimentos[procedimentoIndex]);
    };

    const saveEditProcedimento = () => {
        if (!editingProcedimento || !editProcedimentoText.trim()) return;

        const { grupoIndex, subgrupoIndex, procedimentoIndex } = editingProcedimento;
        const gruposAtualizados = [...(formData.grupos || [])];

        gruposAtualizados[grupoIndex].subgrupos[subgrupoIndex].procedimentos[procedimentoIndex] = editProcedimentoText.trim();

        setFormData({ ...formData, grupos: gruposAtualizados });
        setEditingProcedimento(null);
        setEditProcedimentoText("");
    };

    const cancelEditProcedimento = () => {
        setEditingProcedimento(null);
        setEditProcedimentoText("");
    };

    return (
        <Box maxW="container.lg" mx={{ base: "10px", md: "auto" }} my={8} p={6} bg="bg.panel" borderRadius="lg" shadow="md" borderWidth="1px" borderColor="border.emphasized">
            <Heading size="xl" mb={6} textAlign="center">Gestão de Clínicas <ColorModeButton /></Heading>

            {/* FORMULÁRIO DE INSERÇÃO/EDIÇÃO */}
            <Card.Root mb={10} variant="outline" p={4}>
                <Card.Header>
                    <Heading size="md">{editId ? "Editar Clínica" : "Nova Clínica"}</Heading>
                </Card.Header>
                <Card.Body>
                    <Stack gap={4}>
                        <Input
                            placeholder="Nome da Clínica"
                            value={formData.nome || ""}
                            onChange={e => setFormData({ ...formData, nome: e.target.value })}
                        />
                        <Input
                            placeholder="Endereço Completo"
                            value={formData.endereco || ""}
                            onChange={e => setFormData({ ...formData, endereco: e.target.value })}
                        />
                        <Flex gap={4} direction={{ base: "column", md: "row" }}>
                            <Input
                                placeholder="Município"
                                value={formData.municipio || ""}
                                onChange={e => setFormData({ ...formData, municipio: e.target.value })}
                            />
                            <Input
                                type="tel"
                                placeholder="Telefone"
                                value={formData.telefone || ""}
                                onChange={e => setFormData({ ...formData, telefone: e.target.value })}
                            />
                        </Flex>
                        <Input
                            placeholder="E-mail"
                            value={formData.email || ""}
                            onChange={e => setFormData({ ...formData, email: e.target.value })}
                        />

                        <Separator my={2} />
                        <Text fontWeight="bold">Grupos e Procedimentos</Text>

                        <Flex gap={2} direction={{ base: "column", md: "row" }}>
                            <Input
                                placeholder="Nome do Grupo (ex: Geral)"
                                value={newGrupo}
                                onChange={e => setNewGrupo(e.target.value)}
                            />
                            <Input
                                placeholder="Nome do Subgrupo (ex: Consultas)"
                                value={newSubgrupo}
                                onChange={e => setNewSubgrupo(e.target.value)}
                            />
                            <Input
                                placeholder="Procedimentos (separados por vírgula)"
                                value={newProcedimentos}
                                onChange={e => setNewProcedimentos(e.target.value)}
                            />
                            <Button onClick={addGrupoComSubgrupo} colorPalette="teal">Adicionar</Button>
                        </Flex>

                        {/* Exibição dos Grupos/Subgrupos adicionados */}
                        <Stack gap={4} mt={4}>
                            {formData.grupos?.map((grupo, grupoIndex) => (
                                <Box key={grupoIndex} p={3} borderWidth="1px" borderRadius="md" bg="bg.muted">
                                    <Flex justify="space-between" align="center" mb={2}>
                                        <Text fontWeight="bold">{grupo.nome}</Text>
                                        <Button
                                            size="xs"
                                            colorPalette="red"
                                            variant="ghost"
                                            onClick={() => removeGrupo(grupoIndex)}
                                        >
                                            Remover Grupo
                                        </Button>
                                    </Flex>

                                    <Stack gap={3}>
                                        {grupo.subgrupos.map((subgrupo, subgrupoIndex) => (
                                            <Box key={subgrupoIndex} pl={4} pb={3} borderBottomWidth="1px" borderColor="border.subtle">
                                                <Flex justify="space-between" align="center" mb={2}>
                                                    <Text fontSize="sm" fontWeight="medium">{subgrupo.nome}</Text>
                                                    <Button
                                                        size="xs"
                                                        colorPalette="red"
                                                        variant="ghost"
                                                        onClick={() => removeSubgrupo(grupoIndex, subgrupoIndex)}
                                                    >
                                                        Remover Subgrupo
                                                    </Button>
                                                </Flex>

                                                {/* Lista de procedimentos com edição individual */}
                                                <Stack gap={1} mb={3}>
                                                    {subgrupo.procedimentos.map((procedimento, procedimentoIndex) => {
                                                        const isEditing = editingProcedimento?.grupoIndex === grupoIndex &&
                                                            editingProcedimento?.subgrupoIndex === subgrupoIndex &&
                                                            editingProcedimento?.procedimentoIndex === procedimentoIndex;

                                                        return (
                                                            <Flex key={procedimentoIndex} gap={2} align="center">
                                                                {isEditing ? (
                                                                    <>
                                                                        <Input
                                                                            size="xs"
                                                                            value={editProcedimentoText}
                                                                            onChange={e => setEditProcedimentoText(e.target.value)}
                                                                            autoFocus
                                                                        />
                                                                        <IconButton
                                                                            size="xs"
                                                                            colorPalette="green"
                                                                            variant="ghost"
                                                                            onClick={saveEditProcedimento}
                                                                            aria-label="Salvar"
                                                                        >
                                                                            <MdCheck />
                                                                        </IconButton>
                                                                        <IconButton
                                                                            size="xs"
                                                                            variant="ghost"
                                                                            onClick={cancelEditProcedimento}
                                                                            aria-label="Cancelar"
                                                                        >
                                                                            <MdClose />
                                                                        </IconButton>
                                                                    </>
                                                                ) : (
                                                                    <>
                                                                        <Text fontSize="xs" flex={1}>
                                                                            {procedimento}
                                                                        </Text>
                                                                        <IconButton
                                                                            size="xs"
                                                                            variant="ghost"
                                                                            onClick={() => startEditProcedimento(grupoIndex, subgrupoIndex, procedimentoIndex)}
                                                                            aria-label="Editar procedimento"
                                                                        >
                                                                            <MdEdit />
                                                                        </IconButton>
                                                                        <IconButton
                                                                            size="xs"
                                                                            colorPalette="red"
                                                                            variant="ghost"
                                                                            onClick={() => removeProcedimento(grupoIndex, subgrupoIndex, procedimentoIndex)}
                                                                            aria-label="Excluir procedimento"
                                                                        >
                                                                            <MdDelete />
                                                                        </IconButton>
                                                                    </>
                                                                )}
                                                            </Flex>
                                                        );
                                                    })}
                                                </Stack>

                                                {/* Adicionar mais procedimentos a este subgrupo */}
                                                <Flex gap={2} mt={2}>
                                                    <Input
                                                        size="xs"
                                                        placeholder="Adicionar procedimentos (separados por vírgula)"
                                                        value={newProcedimentos}
                                                        onChange={e => setNewProcedimentos(e.target.value)}
                                                    />
                                                    <Button
                                                        size="xs"
                                                        colorPalette="teal"
                                                        onClick={() => addProcedimentoToSubgrupo(grupoIndex, subgrupoIndex)}
                                                    >
                                                        Adicionar
                                                    </Button>
                                                </Flex>
                                            </Box>
                                        ))}
                                    </Stack>
                                </Box>
                            ))}
                        </Stack>
                    </Stack>
                </Card.Body>
                <Card.Footer gap={3}>
                    <Button colorPalette="blue" onClick={handleSave} loading={saving}>
                        {editId ? "Atualizar Clínica" : "Cadastrar Clínica"}
                    </Button>
                    {editId && <Button variant="ghost" onClick={resetForm}>Cancelar</Button>}
                </Card.Footer>
            </Card.Root>

            <Separator my={6} />

            {/* LISTAGEM PARA GERENCIAMENTO */}
            <Heading size="md" mb={4}>Clínicas Cadastradas</Heading>
            {loading ? <Spinner /> : (
                <Box overflowX="auto">
                    <Table.Root size="sm" variant="outline" striped>
                        <Table.Header>
                            <Table.Row borderBottom="none">
                                <Table.ColumnHeader>Nome</Table.ColumnHeader>
                                <Table.ColumnHeader>Município</Table.ColumnHeader>
                                <Table.ColumnHeader>Telefone</Table.ColumnHeader>
                                <Table.ColumnHeader>Grupos</Table.ColumnHeader>
                                <Table.ColumnHeader textAlign="right">Ações</Table.ColumnHeader>
                            </Table.Row>
                        </Table.Header>
                        <Table.Body>
                            {clinicas.map((c) => (
                                <Table.Row key={c.id} borderBottom="none">
                                    <Table.Cell fontWeight="medium">{c.nome}</Table.Cell>
                                    <Table.Cell>{c.municipio || "-"}</Table.Cell>
                                    <Table.Cell>{c.telefone || "-"}</Table.Cell>
                                    <Table.Cell>
                                        {c.grupos?.length > 0 ? (
                                            <Text fontSize="xs">
                                                {c.grupos.length} grupo(s)
                                            </Text>
                                        ) : "-"}
                                    </Table.Cell>
                                    <Table.Cell textAlign="right">
                                        <Flex gap={2} justify="flex-end">
                                            <Button
                                                size="xs"
                                                variant="outline"
                                                onClick={() => {
                                                    setEditId(c.id!);
                                                    setFormData(c);
                                                    window.scrollTo({ top: 0, behavior: "smooth" });
                                                }}
                                            >
                                                Editar
                                            </Button>
                                            <Button
                                                size="xs"
                                                colorPalette="red"
                                                onClick={() => handleDelete(c.id!)}
                                            >
                                                Excluir
                                            </Button>
                                        </Flex>
                                    </Table.Cell>
                                </Table.Row>
                            ))}
                        </Table.Body>
                    </Table.Root>
                </Box>
            )}
        </Box>
    );
};

export default AdminPage;