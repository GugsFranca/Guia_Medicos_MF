/** @type {import('next').NextConfig} */
const nextConfig = {
  logging: {
    fetches: {
      fullUrl: true,
    },
  },
  output: 'standalone',
  allowedDevOrigins: [
    'localhost:3000',
    'http://192.168.1.184:3000',
    '192.168.1.184',
    '*.local-origin.dev',
  ],
};

export default nextConfig;