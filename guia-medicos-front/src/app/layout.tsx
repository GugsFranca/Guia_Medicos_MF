import type { Metadata } from "next";
import "./globals.css";
import { Providers } from "./providers";


export const metadata: Metadata = {
  title: "Carteira de Serviços - Marque Fácil",
  description: "Livreto de serviços oferecidos",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="pt-BR">
      <body>
        <link rel="icon" href="/logo.svg" />
        <Providers>
          {children}
        </Providers>
      </body>
    </html>
  );
}
