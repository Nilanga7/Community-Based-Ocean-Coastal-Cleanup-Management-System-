import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      // Talks to the Spring Boot API without a cross-origin request in development.
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        // Drop the browser's Origin so the API sees proxied calls as same-origin. Otherwise POSTs
        // fail CORS whenever Vite falls back to a port the backend doesn't list (e.g. 5174).
        configure: (proxy) => {
          proxy.on('proxyReq', (proxyReq) => proxyReq.removeHeader('origin'));
        },
      },
    },
  },
})
