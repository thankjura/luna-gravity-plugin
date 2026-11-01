import { mkdirSync, rmSync, writeFileSync } from 'node:fs'
import { resolve } from 'node:path'
import type { Plugin } from 'vite'

/**
 * Modules that are always available on the Luna host and must not be bundled.
 * Keys are bare import names, values are the globals exposed by Luna.
 */
export const LUNA_EXTERNAL_GLOBALS: Readonly<Record<string, string>> = {
  luna: '__LUNA_COMPONENTS__',
  vue: 'Vue',
  axios: 'axios',
  luxon: 'Luxon',
  sortablejs: 'Sortable',
  'monaco-editor': 'monaco',
  'chart.js': 'ChartJs',
  'vue-router': 'VueRouter',
}

export interface LunaExternalGlobalsOptions {
  /** Directory for generated stubs, relative to the Vite root. */
  cacheDir?: string
  /** Plugin name shown in Vite logs. Useful if the plugin is added more than once. */
  name?: string
  /** Remove the cache directory before generating stubs. */
  clean?: boolean
  /** Include the default Luna host modules. Defaults to `true`. */
  includeDefaults?: boolean
}

/**
 * Replaces bare imports with CommonJS stubs that read modules from Luna globals.
 *
 * `externals` is merged with the default Luna host modules. Pass
 * `{ includeDefaults: false }` to use only the provided list.
 *
 * Stubs are generated synchronously in `configResolved`, i.e. before Rolldown
 * starts resolving modules. Do not generate them lazily from `resolveId`:
 * concurrent resolutions would rewrite the same file and Rolldown could read
 * an empty/truncated module, producing intermittent MISSING_EXPORT errors.
 */
export function lunaExternalGlobals(
  externals: Record<string, string> = {},
  options: LunaExternalGlobalsOptions = {},
): Plugin {
  const {
    cacheDir = 'node_modules/.vite_external',
    name = 'luna-external-globals',
    clean = true,
    includeDefaults = true,
  } = options

  const allExternals = includeDefaults
    ? { ...LUNA_EXTERNAL_GLOBALS, ...externals }
    : externals

  const stubs = new Map<string, string>()

  return {
    name,
    enforce: 'pre',
    configResolved(config) {
      const dir = resolve(config.root, cacheDir)

      if (clean) {
        rmSync(dir, { recursive: true, force: true })
      }
      mkdirSync(dir, { recursive: true })

      // Explicit CommonJS so `module.exports = ...` is interpreted the same way
      // regardless of the `"type": "module"` in the project's package.json.
      writeFileSync(resolve(dir, 'package.json'), '{"type":"commonjs"}\n')

      stubs.clear()
      for (const [moduleName, globalName] of Object.entries(allExternals)) {
        const stubPath = resolve(dir, `${moduleName.replace(/[\\/]/g, '__')}.js`)
        writeFileSync(stubPath, `module.exports = ${globalName};`)
        stubs.set(moduleName, stubPath)
      }
    },
    resolveId(id) {
      return stubs.get(id)
    },
  }
}
