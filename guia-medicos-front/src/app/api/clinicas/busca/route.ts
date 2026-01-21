import { cookies } from "next/headers";
import { NextResponse } from "next/server";

const BackEndURL = process.env.NEXT_PUBLIC_BACKEND_URL;

async function proxyFetch(path: string, init?: RequestInit) {
    try {
        const res = await fetch(`${BackEndURL}${path}`, init);
        const status = res.status;
        const contentType = res.headers.get("content-type") || "";

        if (status === 204) return new NextResponse(null, { status });

        const bodyText = await res.text();
        return new NextResponse(bodyText, {
            status,
            headers: { "content-type": contentType },
        });
    } catch (err: any) {
        console.error("proxyFetch network error:", err);
        return NextResponse.json({ message: "Bad gateway", detail: err?.message ?? String(err) }, { status: 502 });
    }
}

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