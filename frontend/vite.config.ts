import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'
import { viteExternalsPlugin } from "vite-plugin-externals";

// https://vite.dev/config/
export default defineConfig({
  plugins: [
      viteExternalsPlugin({
        'luna': '__LUNA_COMPONENTS__',
        'vue': 'Vue',
        'axios': 'axios'
      }),
      vue(),
  ],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),
    }
  },
  build: {
    sourcemap: true,
    minify: 'terser',
    lib: {
      entry: [
      	resolve(__dirname, 'src/views/ConsoleView.vue'),
      	resolve(__dirname, 'src/components/CodeEditor.vue')
      ],
      formats: ['es'],
    },
    rollupOptions: {
      external: ['vue', 'axios'],
      output: {
        entryFileNames: '[name].js',
        exports: 'named',
        globals: {
          vue: 'Vue',
          axios: 'axios'
        },
      },
    },
    outDir: resolve(__dirname, '../src/main/resources/frontend'),
    emptyOutDir: true,
  },
})
