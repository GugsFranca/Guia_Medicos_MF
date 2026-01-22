import { cookies } from "next/headers";
import { NextResponse } from "next/server";
import { proxyFetch } from "@/app/lib/proxyfetch";

export async function GET(req: Request) {
    try {

        const body = await req.json();

        // Converte o JSON para query string
        const queryParams = new URLSearchParams();
        Object.entries(body).forEach(([key, value]) => {
            if (value) queryParams.append(key, String(value));
        });

        const queryString = queryParams.toString();
        const url = queryString ? `/busca?${queryString}` : '/busca';

        return await proxyFetch(url, {
            method: "GET", // Agora é GET
        });
    } catch (err) {
        console.error("api/clinica/busca POST proxy error:", err);
        return NextResponse.json({
            message: "Erro interno",
            detail: String(err)
        }, { status: 500 });
    }
}