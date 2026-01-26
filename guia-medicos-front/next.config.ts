/** @type {import('next').NextConfig} */
const nextConfig = {
  logging: {
    fetches: {
      fullUrl: true,
    },
  },
  allowedDevOrigins: [
    'local-origin.dev',
    'http://192.168.1.115:3000',
    '192.168.1.115',

    '*.local-origin.dev',
  ],
};

export default nextConfig;