import path from 'node:path'
import tailwindcss from '@tailwindcss/vite'
import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), tailwindcss()],
  resolve: {
    alias: {
      '@': path.resolve(import.meta.dirname, './src'),
    },
  },
  server: {
    port: 5173,
    // The browser only talks to the Vite dev server, so the auth cookie stays same-origin.
    proxy: {
      '/api/auth': 'http://localhost:8081',
      '/api/users': 'http://localhost:8081',
      '/api': 'http://localhost:8082',
    },
  },
})
