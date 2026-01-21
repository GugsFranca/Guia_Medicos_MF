'use client';
import { Footer } from "@/components/Footer";
import Header from "@/components/Header";
import Form from "@/components/userPage/ClinicasList";

export default function Home() {

  return (
    <div>
      <Header />
      <main className="flex flex-col items-center justify-center pt-8 p-2">
        <Form />
      </main>
      <Footer />
    </div>
  );
}
