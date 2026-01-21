"use client";

import {
    Box, Stack, Heading, Input, Text, Card, Spinner, Button,
    Separator, Flex, Table
} from "@chakra-ui/react";
import { useEffect, useState } from "react";
import type { Clinica } from "../types";

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
        procedimentos: {}
    });

    // Estados auxiliares para adicionar novos procedimentos ao Map
    const [newEsp, setNewEsp] = useState("");
    const [newProc, setNewProc] = useState("");

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
        setFormData({ nome: "", endereco: "", municipio: "", telefone: "", email: "", procedimentos: {} });
    };

    const addProcedimento = () => {
        if (!newEsp || !newProc) return;
        setFormData({
            ...formData,
            procedimentos: { ...formData.procedimentos, [newEsp]: newProc }
        });
        setNewEsp("");
        setNewProc("");
    };

    const removeProcedimento = (key: string) => {
        const updated = { ...formData.procedimentos };
        delete updated[key];
        setFormData({ ...formData, procedimentos: updated });
    };

    return (
        <Box maxW="container.lg" mx={{ base: "10px", md: "auto" }} my={8} p={6} bg="bg.panel" borderRadius="lg" shadow="md" borderWidth="1px" borderColor="border.emphasized">
            <Heading size="xl" mb={6} textAlign="center">Gestão de Clínicas</Heading>

            {/* FORMULÁRIO DE INSERÇÃO/EDIÇÃO */}
            <Card.Root mb={10} variant="outline" p={4}>
                <Card.Header>
                    <Heading size="md">{editId ? "Editar Clínica" : "Nova Clínica"}</Heading>
                </Card.Header>
                <Card.Body>
                    <Stack gap={4}>
                        <Input placeholder="Nome da Clínica" value={formData.nome} onChange={e => setFormData({ ...formData, nome: e.target.value })} />
                        <Input placeholder="Endereço Completo" value={formData.endereco} onChange={e => setFormData({ ...formData, endereco: e.target.value })} />
                        <Flex gap={4} direction={{ base: "column", md: "row" }}>
                            <Input placeholder="Município" value={formData.municipio} onChange={e => setFormData({ ...formData, municipio: e.target.value })} />
                            <Input placeholder="Telefone" value={formData.telefone} onChange={e => setFormData({ ...formData, telefone: e.target.value })} />
                        </Flex>
                        <Input placeholder="E-mail" value={formData.email} onChange={e => setFormData({ ...formData, email: e.target.value })} />

                        <Separator my={2} />
                        <Text fontWeight="bold">Procedimentos</Text>
                        <Flex gap={2} direction={{ base: "column", md: "row" }}>
                            <Input placeholder="Especialidade (ex: Geral)" value={newEsp} onChange={e => setNewEsp(e.target.value)} />
                            <Input placeholder="Procedimentos (separados por vírgula)" value={newProc} onChange={e => setNewProc(e.target.value)} />
                            <Button onClick={addProcedimento} colorPalette="teal">Adicionar</Button>
                        </Flex>

                        <Stack gap={2} mt={2}>
                            {Object.entries(formData.procedimentos || {}).map(([esp, proc]) => (
                                <Flex key={esp} justify="space-between" bg="bg.muted" p={2} borderRadius="md" align="center">
                                    <Text fontSize="sm"><strong>{esp}:</strong> {proc}</Text>
                                    <Button size="xs" colorPalette="red" variant="ghost" onClick={() => removeProcedimento(esp)}>Remover</Button>
                                </Flex>
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
                    <Table.Root size="sm" variant={"outline"} striped>
                        <Table.Header>
                            <Table.Row borderBottom="none"> {/* Remove a linha abaixo do cabeçalho */}
                                <Table.ColumnHeader>Nome</Table.ColumnHeader>
                                <Table.ColumnHeader>Município</Table.ColumnHeader>
                                <Table.ColumnHeader textAlign="right">Ações</Table.ColumnHeader>
                            </Table.Row>
                        </Table.Header>
                        <Table.Body>
                            {clinicas.map((c) => (
                                <Table.Row key={c.id} borderBottom="none"> {/* Remove a linha de cada registro */}
                                    <Table.Cell fontWeight="medium">{c.nome}</Table.Cell>
                                    <Table.Cell>{c.municipio}</Table.Cell>
                                    <Table.Cell textAlign="right">
                                        <Flex gap={2} justify="flex-end">
                                            <Button size="xs" variant="outline" onClick={() => {
                                                setEditId(c.id!);
                                                setFormData(c);
                                                window.scrollTo({ top: 0, behavior: "smooth" });
                                            }}>Editar</Button>
                                            <Button size="xs" colorPalette="red" onClick={() => handleDelete(c.id!)}>Excluir</Button>
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