import { NextResponse } from "next/server";
import { proxyFetch } from "@/app/lib/proxyfetch";

export async function GET(req: Request) {
    try {

        return await proxyFetch(`/clinicas`, {
            cache: "no-store",
        });
    } catch (err) {
        console.error("api/clinicas GET proxy error:", err);
        return NextResponse.json({ message: "Erro interno", detail: String(err) }, { status: 500 });
    }
}

export async function POST(req: Request) {
    try {

        const bodyText = await req.text();
        return await proxyFetch(`/clinicas`, {
            method: "POST",
            headers: {
                "Content-Type": req.headers.get("content-type") ?? "application/json",
            },
            body: bodyText,
        });
    } catch (err) {
        console.error("api/clinicas POST proxy error:", err);
        return NextResponse.json({ message: "Erro interno", detail: String(err) }, { status: 500 });
    }
}
