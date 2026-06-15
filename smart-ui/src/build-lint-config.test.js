import { afterEach, describe, expect, it } from 'vitest'
import { createRequire } from 'node:module'

const require = createRequire(import.meta.url)
const vueConfigPath = require.resolve('../vue.config.js')
const originalNodeEnv = process.env.NODE_ENV

function loadVueConfigWithNodeEnv(nodeEnv) {
  delete require.cache[vueConfigPath]
  process.env.NODE_ENV = nodeEnv
  return require('../vue.config.js')
}

afterEach(() => {
  delete require.cache[vueConfigPath]
  if (originalNodeEnv === undefined) {
    delete process.env.NODE_ENV
  } else {
    process.env.NODE_ENV = originalNodeEnv
  }
})

describe('vue build lint configuration', () => {
  it('skips eslint-loader during production builds', () => {
    const config = loadVueConfigWithNodeEnv('production')

    expect(config.lintOnSave).toBe(false)
  })

  it('keeps eslint-loader enabled during local development', () => {
    const config = loadVueConfigWithNodeEnv('development')

    expect(config.lintOnSave).toBe(true)
  })
})
