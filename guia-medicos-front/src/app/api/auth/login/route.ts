import { NextResponse } from "next/server";
import { proxyFetch } from "@/app/lib/proxyfetch";

export async function POST(req: Request) {
    try {
        console.log("api/auth/login proxy request:", req)

        const bodyText = await req.json();


        console.log("api/auth/login proxy body:", bodyText);
        return await proxyFetch(`/auth/login`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            credentials: "include",
            body: JSON.stringify(bodyText),
        });
    } catch (err) {
        console.error("api/clinicas POST proxy error:", err);
        return NextResponse.json({ message: "Erro interno", detail: String(err) }, { status: 500 });
    }
}
