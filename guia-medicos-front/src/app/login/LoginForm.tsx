'use client';

import { ColorModeButton } from '@/components/ui/color-mode';
import {
    Box,
    Button,
    Field,
    Input,
    Stack,
    Heading,
    Text,
    Flex,
} from '@chakra-ui/react';
import { useRouter } from 'next/navigation';
import { useState } from 'react';

export default function LoginForm() {
    const [username, setUser] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState<string | null>(null);
    const [loading, setLoading] = useState(false);
    const router = useRouter();

    async function handleSubmit(e: React.FormEvent) {
        e.preventDefault();
        setError(null);
        setLoading(true);

        try {
            const res = await fetch("/api/auth/login", { method: "POST", body: JSON.stringify({ username, password }) });

            if (!res.ok) {
                const j = await res.json().catch(() => null);
                setError(j?.error || 'Usuário ou senha inválidos');
                return;
            }
            console.log("Login bem-sucedido!");

            router.push('/admin');
        } catch (err) {
            setError('Erro de conexão com o servidor');
        } finally {
            setLoading(false);
        }
    }

    return (
        <Box
            minH="80vh"
            display="flex"
            alignItems="center"
            justifyContent="center"
            bgGradient="linear(to-br, red.600, red.800)"
        >
            <Box
                maxW="md"
                w="100%"
                p="8"
                rounded="2xl"
                borderWidth="1px"
                boxShadow="2xl"
                bg="bg.panel"
                position="relative"
            >
                {/* detalhe amarelo */}
                <Box
                    position="absolute"
                    top="0"
                    left="0"
                    right="0"
                    h="2"
                    roundedTop="2xl"
                />

                {/* Header */}
                <Flex justify="space-between" align="center" mb="6">
                    <Box>
                        <Heading size="lg" color="red.600">
                            Carteira de Serviços
                        </Heading>
                        <Text fontSize="sm" color="fg.muted">
                            Painel administrativo
                        </Text>
                    </Box>
                    <ColorModeButton />
                </Flex>

                {/* Form */}
                <form onSubmit={handleSubmit}>
                    <Stack gap="4">
                        <Field.Root invalid={!!error}>
                            <Field.Label>Usuário</Field.Label>
                            <Input placeholder="Digite seu usuário" value={username} onChange={(e) => setUser(e.target.value)} />
                        </Field.Root>

                        <Field.Root invalid={!!error}>
                            <Field.Label>Senha</Field.Label>
                            <Input type="password" placeholder="Digite sua senha" value={password} onChange={(e) => setPassword(e.target.value)} />
                            {error && <Field.ErrorText>{error}</Field.ErrorText>}
                        </Field.Root>

                        <Button
                            type="submit"
                            width="100%"
                            colorPalette="red"
                            loading={loading}
                        >
                            Acessar painel
                        </Button>
                    </Stack>
                </form>
            </Box>
        </Box>
    );
}
