import { NextResponse } from "next/server";
import { proxyFetch } from "@/app/lib/proxyfetch";
import { cookies } from "next/headers";

export async function GET(req: Request) {
    try {
        const cookieStore = await cookies();
        const token = cookieStore.get("auth_token")?.value;

        return await proxyFetch(`/clinicas`, {
            cache: "no-store",
            headers: {
                "Authorization": `Bearer ${token}`,
            },
        });
    } catch (err) {
        console.error("api/clinicas GET proxy error:", err);
        return NextResponse.json({ message: "Erro interno", detail: String(err) }, { status: 500 });
    }
}

export async function POST(req: Request) {
    try {

        const cookieStore = await cookies();
        const token = cookieStore.get("auth_token")?.value;

        const bodyText = await req.text();
        return await proxyFetch(`/clinicas`, {
            method: "POST",
            headers: {
                "Content-Type": req.headers.get("content-type") ?? "application/json",
                "Authorization": `Bearer ${token}`,
            },
            body: bodyText,
        });
    } catch (err) {
        console.error("api/clinicas POST proxy error:", err);
        return NextResponse.json({ message: "Erro interno", detail: String(err) }, { status: 500 });
    }
}
