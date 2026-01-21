'use client';
import { AdminPage } from "@/components/adminPage/admin_page";
import { Footer } from "@/components/Footer";
import Header from "@/components/Header";

export default function Home() {

  return (
    <div>
      <Header />
      <main className="flex flex-col items-center justify-center pt-8 p-2">
        <AdminPage />
      </main>
      <Footer />
    </div>
  );
}
