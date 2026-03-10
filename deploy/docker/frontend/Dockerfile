# Multi-stage build for NextERP Frontend (Next.js)
# Stage 1: Dependencies
FROM node:20-alpine AS deps

WORKDIR /app

# Install pnpm
RUN npm install -g pnpm@8.14.1

# Copy all frontend files (excluding node_modules via .dockerignore)
COPY frontend/package.json frontend/pnpm-workspace.yaml ./
COPY frontend/ ./

# Install dependencies
RUN pnpm install

# Stage 2: Builder
FROM node:20-alpine AS builder

WORKDIR /app

# Install pnpm
RUN npm install -g pnpm@8.14.1

# Copy from deps
COPY --from=deps /app ./app

WORKDIR /app/app

# Set build environment
ENV NEXT_TELEMETRY_DISABLED=1
ENV NODE_ENV=production

# Build application
RUN pnpm build --filter=@nexterp/web

# Stage 3: Runner
FROM node:20-alpine AS runner

WORKDIR /app

# Install dumb-init
RUN apk add --no-cache dumb-init

# Create non-root user
RUN addgroup -g 1001 -S nodejs && \
    adduser -S nextjs -u 1001

# Copy necessary files
COPY --from=builder /app/app/apps/web/next.config.js ./
COPY --from=builder /app/app/apps/web/public ./public
COPY --from=builder /app/app/apps/web/package.json ./package.json

# Copy standalone output
COPY --from=builder --chown=nextjs:nodejs /app/app/apps/web/.next/standalone ./
COPY --from=builder --chown=nextjs:nodejs /app/app/apps/web/.next/static ./.next/static

USER nextjs

EXPOSE 3000

ENV PORT=3000
ENV HOSTNAME="0.0.0.0"

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:3000 || exit 1

ENTRYPOINT ["dumb-init", "--"]
CMD ["node", "server.js"]
