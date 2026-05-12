/** @type {import('next').NextConfig} */
const nextConfig = {
  reactStrictMode: true,
  output: 'standalone', // Enable standalone output for Docker deployment
  async rewrites() {
    return [
      {
        source: '/api/:path*',
        destination: 'http://172.20.10.6:8082/api/:path*',
      },
    ];
  },
};

module.exports = nextConfig;
