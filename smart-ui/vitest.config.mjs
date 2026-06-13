import { fileURLToPath } from 'node:url'
import { defineConfig } from 'vitest/config'
import vue2 from '@vitejs/plugin-vue2'

export default defineConfig({
  plugins: [vue2()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  test: {
    environment: 'jsdom',
    include: ['src/**/*.test.js'],
    // The webpack build (vue-cli) stays the production pipeline; vitest only runs tests.
    css: false
  }
})
