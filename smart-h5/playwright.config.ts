import { defineConfig, devices } from '@playwright/test'

export default defineConfig({
  testDir: './e2e',
  use: { baseURL: 'http://localhost:3100', ...devices['Pixel 5'] },
  webServer: {
    command: 'pnpm dev --port 3100',
    url: 'http://localhost:3100/login',
    reuseExistingServer: !process.env.CI,
  },
})
