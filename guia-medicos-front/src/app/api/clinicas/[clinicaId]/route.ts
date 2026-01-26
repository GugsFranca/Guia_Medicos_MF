import { cookies } from "next/headers";
import { NextResponse } from "next/server";
import { proxyFetch } from "@/app/lib/proxyfetch";

export async function GET(req: Request, { params }: { params: Promise<{ clinicaId: string }> }) {
    try {

        const { clinicaId } = await params;
        const cookieStore = await cookies();
        const token = cookieStore.get("auth_token")?.value;

        return await proxyFetch(`/clinicas/${encodeURIComponent(clinicaId)}`, {
            cache: "no-store",
            headers: {
                "Authorization": `Bearer ${token}`,
            },
        });
    } catch (err) {
        console.error("api/clinicas/[clinicaId] GET proxy error:", err);
        return NextResponse.json({ message: "Erro interno", detail: String(err) }, { status: 500 });
    }
}

export async function PUT(req: Request, { params }: { params: Promise<{ clinicaId: string }> }) {
    try {

        const { clinicaId } = await params;
        const cookieStore = await cookies();
        const token = cookieStore.get("auth_token")?.value;
        const bodyText = await req.text();

        console.log("api/clinicas/[clinicaId] PUT proxy error:", "clinicaId", clinicaId, "token", token, "body", bodyText)

        return await proxyFetch(`/clinicas/${encodeURIComponent(clinicaId)}`, {
            method: "PUT",
            headers: {
                "Content-Type": req.headers.get("content-type") ?? "application/json",
                "Authorization": `Bearer ${token}`,
            },
            body: bodyText,
        });
    } catch (err) {
        console.error("api/clinicas/[clinicaId] PUT proxy error:", err);
        return NextResponse.json({ message: "Erro interno", detail: String(err) }, { status: 500 });
    }
}

export async function DELETE(req: Request, { params }: { params: Promise<{ clinicaId: number }> }) {
    try {

        const { clinicaId } = await params;
        const cookieStore = await cookies();
        const token = cookieStore.get("auth_token")?.value;

        console.log("api/clinicas/[clinicaId] DELETE proxy error:", "clinicaId", clinicaId, "token", token)


        return await proxyFetch(`/clinicas/${encodeURIComponent(clinicaId)}`, {
            method: "DELETE",
            headers: {
                "Authorization": `Bearer ${token}`,
            },
        });
    } catch (err) {
        console.error("api/clinicas/[clinicaId] DELETE proxy error:", err);
        return NextResponse.json({ message: "Erro interno", detail: String(err) }, { status: 500 });
    }
}