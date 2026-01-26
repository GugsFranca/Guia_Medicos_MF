import { AdminPage } from "@/components/adminPage/admin_page";
import { Footer } from "@/components/Footer";
import Header from "@/components/Header";
import { cookies } from "next/headers";
import { decodeJwtPayload } from "../lib/decodeJwt";
import { redirect } from "next/navigation";

export default async function Home() {

  const cookieStore = await cookies();
  const token = cookieStore.get('auth_token')?.value;
  const claims = decodeJwtPayload(token as string | undefined);
  const expired = claims && claims.exp * 1000 < Date.now();


  if (!claims || expired) {
    redirect('/login');
  }
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
