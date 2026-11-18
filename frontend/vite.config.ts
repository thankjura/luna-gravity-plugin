import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'node:path'
import { lunaExternalGlobals } from './luna-external-globals.ts'

// https://vite.dev/config/
export default defineConfig({
  base: './',
  plugins: [
    lunaExternalGlobals(),
    vue()
  ],
  resolve: {
    alias: {
      '@': resolve(import.meta.dirname, 'src'),
    }
  },
  build: {
    sourcemap: true,
    minify: 'terser',
    lib: {
      entry: [
        resolve(import.meta.dirname, 'src/views/ConsoleView.vue'),
        resolve(import.meta.dirname, 'src/views/WorkflowScriptsView.vue'),
        resolve(import.meta.dirname, 'src/views/ListenerScriptsView.vue'),
        resolve(import.meta.dirname, 'src/views/ScriptFilesView.vue'),
        resolve(import.meta.dirname, 'src/components/workflow/BaseViewScriptComponent.vue'),
        resolve(import.meta.dirname, 'src/components/workflow/BaseEditScriptComponent.vue'),
      ],
      formats: ['es'],
    },
    rolldownOptions: {
      output: {
        entryFileNames: '[name].js',
        exports: 'named',
      },
    },
    outDir: resolve(import.meta.dirname, '../src/main/resources/frontend'),
    emptyOutDir: true,
  },
})
