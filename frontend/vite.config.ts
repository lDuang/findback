import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],

  test: {
    environment: 'jsdom'
  },

  server: {
    host: '0.0.0.0',
    port: 5173
  },

  build: {
    // 后台系统正常体积，关闭无意义的体积警告
    chunkSizeWarningLimit: 1500
  }
})
