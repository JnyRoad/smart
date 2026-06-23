import { fileURLToPath } from 'node:url'
import { defineConfig } from 'vitest/config'
import vue2 from '@vitejs/plugin-vue2'

export default defineConfig({
  plugins: [vue2()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
    // 对齐 vue-cli/webpack 的隐式扩展名解析：源码里存在 webpack 风格的无扩展名
    // import('../page/index')（实际指向 .vue 文件/目录索引），Vite 默认不含 .vue，
    // 需显式补上 .vue 才能在测试环境解析这些路径。
    extensions: ['.mjs', '.js', '.mts', '.ts', '.jsx', '.tsx', '.json', '.vue']
  },
  test: {
    environment: 'jsdom',
    include: ['src/**/*.test.js', 'scripts/**/*.test.{js,mjs}'],
    // The webpack build (vue-cli) stays the production pipeline; vitest only runs tests.
    css: false
  }
})
