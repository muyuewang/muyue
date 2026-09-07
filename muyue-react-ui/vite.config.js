import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const baseApi = env.VITE_APP_BASE_API || '/dev-api'
  return {
    plugins: [react()],
    server: {
      port: Number(env.VITE_APP_PORT || 2024),
      proxy: {
        [baseApi]: {
          target: env.VITE_BACKEND_URL || 'http://localhost:8080',
          changeOrigin: true,
          rewrite: (p) => p.replace(new RegExp('^' + baseApi), '')
        }
      }
    },
    build: {
      chunkSizeWarningLimit: 4096
    }
  }
})
