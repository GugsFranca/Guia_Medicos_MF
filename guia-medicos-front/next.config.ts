import type { NextConfig } from "next";

const nextConfig: NextConfig = {
    output: "standalone",
    logging: {
        fetches: {
            fullUrl: true,
        },
    },
    experimental: {
        serverActions: {
            allowedOrigins: [
                'localhost:3000',
                'http://192.168.1.184:3000',
                '192.168.1.184',
                '*.local-origin.dev',
            ]
        }
    }
};

export default nextConfig;