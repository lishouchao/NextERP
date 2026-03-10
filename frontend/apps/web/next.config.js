/** @type {import('next').NextConfig} */
const nextConfig = {
  reactStrictMode: true,
  swcMinify: true,

  // 编译选项
  compiler: {
    // 移除 console.log
    removeConsole: process.env.NODE_ENV === 'production' ? { exclude: ['error', 'warn'] } : false
  },

  // 环境变量
  env: {
    NEXT_PUBLIC_API_URL: process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'
  },

  // 图片优化
  images: {
    domains: ['localhost'],
    formats: ['image/avif', 'image/webp']
  },

  // 输出配置
  output: 'standalone'
};

module.exports = nextConfig;
