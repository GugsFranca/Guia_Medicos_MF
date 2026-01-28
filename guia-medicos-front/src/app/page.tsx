'use client';
import { Footer } from "@/components/Footer";
import Header from "@/components/Header";
import ClinicasList from "@/components/userPage/ClinicasList";

export default function Home() {

  return (
    <div className="flex flex-col min-h-screen">
      <Header />
      <main className="grow flex flex-col items-center justify-center pt-8 p-2">
        <ClinicasList />
      </main>
      <Footer />
    </div>
  );
}
