import { NextResponse } from "next/server";

const BackEndURL = process.env.NEXT_PUBLIC_BACKEND_URL;

export async function proxyFetch(path: string, init?: RequestInit) {
    try {
        const res = await fetch(`${BackEndURL}${path}`, {
            ...init,
            credentials: "include",
        });

        if (res.status === 204) {
            return new NextResponse(null, { status: 204 });
        }

        const bodyText = await res.text();

        const nextRes = new NextResponse(bodyText, {
            status: res.status,
            headers: {
                "content-type": res.headers.get("content-type") ?? "application/json",
            },
        });

        const setCookie = res.headers.get("set-cookie");
        if (setCookie) {
            nextRes.headers.set("set-cookie", setCookie);
        }

        return nextRes;
    } catch (err: any) {
        console.error("proxyFetch network error:", err);
        return NextResponse.json(
            { message: "Bad gateway", detail: err?.message ?? String(err) },
            { status: 502 }
        );
    }
}
