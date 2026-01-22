import { cookies } from "next/headers";
import { NextResponse } from "next/server";
import { proxyFetch } from "@/app/lib/proxyfetch";

export async function GET(req: Request, { params }: { params: Promise<{ clinicaId: string }> }) {
    try {

        const { clinicaId } = await params;

        return await proxyFetch(`/clinicas/${encodeURIComponent(clinicaId)}`, {
            cache: "no-store",
        });
    } catch (err) {
        console.error("api/clinicas/[clinicaId] GET proxy error:", err);
        return NextResponse.json({ message: "Erro interno", detail: String(err) }, { status: 500 });
    }
}

export async function PUT(req: Request, { params }: { params: Promise<{ clinicaId: string }> }) {
    try {

        const { clinicaId } = await params;
        const bodyText = await req.text();
        return await proxyFetch(`/clinicas/${encodeURIComponent(clinicaId)}`, {
            method: "PUT",
            headers: {
                "Content-Type": req.headers.get("content-type") ?? "application/json",
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
        return await proxyFetch(`/clinicas/${encodeURIComponent(clinicaId)}`, {
            method: "DELETE",
        });
    } catch (err) {
        console.error("api/clinicas/[clinicaId] DELETE proxy error:", err);
        return NextResponse.json({ message: "Erro interno", detail: String(err) }, { status: 500 });
    }
}