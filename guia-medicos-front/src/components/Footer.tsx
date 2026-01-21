import React from 'react';
import {
    Box,
    Container,
    Stack,
    Heading,
    Text,
    Link,
} from '@chakra-ui/react';
import { FaFacebook, FaInstagram } from 'react-icons/fa';

export const Footer: React.FC = () => {
    return (
        <Box
            as="footer"
            bottom={0}
            left={0}
            width="100%"
            bg="#106583"
            color="white"
            py={3}
            zIndex="overlay"
            boxShadow="md"
        >
            <Container maxW="container.lg">
                <Stack
                    direction={{ base: 'column', md: 'row' }}
                    gap={6}
                    align="center"
                    justify="space-between"
                >
                    <Box textAlign={{ base: 'center', md: 'left' }}>
                        <Heading as="h6" size="sm" mb={2}>
                            ENDEREÇO
                        </Heading>
                        <Text fontSize="sm" mb={1}>
                            Av. Governador Roberto da Silveira, n° 2.012
                        </Text>
                        <Text fontSize="sm" mb={1}>
                            Bairro: Posse • Cidade: Nova Iguaçu - RJ
                        </Text>
                        <Text fontSize="sm" mb={1}>
                            CEP: 26020-740
                        </Text>
                        <Text fontSize="sm">Telefones: (21) 3102-0460 / 3102-1067</Text>
                    </Box>

                    <Stack direction="row" gap={3}>
                        <Link
                            href="https://www.facebook.com/cisbaf"
                            target="_blank"
                            rel="noopener noreferrer"
                            display="inline-flex"
                            alignItems="center"
                            justifyContent="center"
                            w="40px"
                            h="40px"
                            borderRadius="8px"
                            bg="white"
                            color="#106583"
                            _hover={{ bg: "#106583", color: "white" }}
                            aria-label="Facebook"
                        >
                            <FaFacebook />
                        </Link>

                        <Link
                            href="https://www.instagram.com/cisbaf/"
                            target="_blank"
                            rel="noopener noreferrer"
                            display="inline-flex"
                            alignItems="center"
                            justifyContent="center"
                            w="40px"
                            h="40px"
                            borderRadius="8px"
                            bg="white"
                            color="#106583"
                            _hover={{ bg: "#106583", color: "white" }}
                            aria-label="Instagram"
                        >
                            <FaInstagram />
                        </Link>
                    </Stack>

                </Stack>
            </Container>
        </Box>
    );
};