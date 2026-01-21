"use client";

import { ChakraProvider, createSystem, defaultConfig, defaultSystem } from "@chakra-ui/react";
import { ThemeProvider } from "next-themes";


const system = createSystem(defaultConfig, {
    preflight: false, // ← ISSO resolve o problema
});

export function Providers({ children }: { children: React.ReactNode }) {
    return (
        <ChakraProvider value={system}>
            <ThemeProvider attribute="class" defaultTheme="system" enableSystem disableTransitionOnChange>
                {children}
            </ThemeProvider>
        </ChakraProvider>
    );
}