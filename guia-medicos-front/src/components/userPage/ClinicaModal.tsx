"use client";

import {
    Box,
    Stack,
    Heading,
    Text,
    Button,
    Portal,
    Flex,
    useBreakpointValue,
} from "@chakra-ui/react";
import type { Clinica } from "../types";

interface ClinicaModalProps {
    clinica: Clinica | null;
    isOpen: boolean;
    onClose: () => void;
}

export const ClinicaModal: React.FC<ClinicaModalProps> = ({
    clinica,
    isOpen,
    onClose,
}) => {
    const modalSize = useBreakpointValue({ base: "95vw", sm: "90vw", md: "700px" });
    const modalPadding = useBreakpointValue({ base: 4, md: 6 });

    if (!isOpen || !clinica) return null;

    return (
        <Portal>
            <Box
                position="fixed"
                top="0"
                left="0"
                w="100vw"
                h="100vh"
                bg="rgba(0,0,0,0.6)"
                zIndex="1000"
                display="flex"
                alignItems="center"
                justifyContent="center"
                onClick={onClose}
                p={3}
            >
                <Box
                    bg="bg.panel"
                    width={modalSize}
                    maxH={{ base: "90vh", md: "85vh" }}
                    borderRadius="xl"
                    shadow="2xl"
                    p={modalPadding}
                    position="relative"
                    overflow="hidden"
                    display="flex"
                    flexDirection="column"
                    onClick={(e) => e.stopPropagation()}
                >
                    <Button
                        position="absolute"
                        top={{ base: 2, md: 4 }}
                        right={{ base: 2, md: 4 }}
                        size="md"
                        variant="ghost"
                        onClick={onClose}
                        zIndex={10}
                    >
                        ✕
                    </Button>

                    <Box flex="1" overflowY="auto" pr={1} mt={1}>
                        <Heading size={{ base: "lg", md: "xl" }} color="teal.fg" mb={4} pr={8}>
                            {clinica.nome}
                        </Heading>

                        <Stack gap={4}>
                            <Box bg="bg.muted" p={3} borderRadius="md">
                                <Stack gap={2} fontSize={{ base: "md", md: "lg" }}>
                                    <Text><strong>Endereço:</strong> {clinica.endereco}</Text>
                                    <Text><strong>Cidade:</strong> {clinica.municipio}</Text>
                                    {clinica.telefone && (
                                        <Text><strong>Contato:</strong> {clinica.telefone}</Text>
                                    )}
                                </Stack>
                            </Box>

                            <Box>
                                <Heading size={{ base: "md", md: "lg" }} mb={3} borderBottomWidth="1px" pb={2}>
                                    Serviços Disponíveis
                                </Heading>

                                <Stack gap={4}>
                                    {clinica.grupos?.map((grupo, gIdx) => (
                                        <Box key={gIdx}>
                                            <Text
                                                fontWeight="bold"
                                                color="teal.fg"
                                                mb={2}
                                                fontSize={{ base: "md", md: "lg" }}
                                            >
                                                {grupo.nome}
                                            </Text>

                                            {grupo.subgrupos?.map((sub, sIdx) => (
                                                <Box key={sIdx} mb={3} pl={1}>
                                                    <Text
                                                        fontSize={{ base: "sm", md: "md" }}
                                                        fontWeight="bold"
                                                        mb={1}
                                                        color="fg.muted"
                                                    >
                                                        {sub.nome}
                                                    </Text>

                                                    <Flex wrap="wrap" gap={2} flexDir="column">
                                                        {sub.procedimentos?.map((p, pIdx) => (
                                                            <Box
                                                                key={pIdx}
                                                                px={3}
                                                                py={2}
                                                                borderRadius="lg"
                                                                bg="bg.muted"
                                                                borderWidth="1px"
                                                                borderColor="border.subtle"
                                                                fontSize={{ base: "sm", md: "md" }}
                                                                lineHeight="short"
                                                                maxW="100%"
                                                                _hover={{
                                                                    shadow: "md",
                                                                    transform: "translateY(-2px)",
                                                                    transition: "all 0.2s",
                                                                }}
                                                            >
                                                                {p}
                                                            </Box>
                                                        ))}
                                                    </Flex>
                                                </Box>
                                            ))}
                                        </Box>
                                    ))}
                                </Stack>
                            </Box>
                        </Stack>
                    </Box>
                </Box>
            </Box>
        </Portal>
    );
};
