import { readFile, stat } from 'node:fs/promises'
import path from 'node:path'

const VALID_DEPLOY_ENVS = new Set(['production', 'test'])
const SECURITY_KEY_ENV_NAMES = [
  'NEXT_PUBLIC_SECURITY_ENCODE_KEY',
  'SMART_H5_SECURITY_ENCODE_KEY',
  'VUE_APP_SECURITY_ENCODE_KEY',
]
const AES_KEY_LENGTHS = new Set([16, 24, 32])

function normalizeDeployEnv(deployEnv) {
  const value = (deployEnv || 'production').toLowerCase()
  if (value === 'prod') return 'production'
  if (value === 'testing') return 'test'
  if (!VALID_DEPLOY_ENVS.has(value)) {
    throw new Error(`Unsupported smart-h5 deploy env "${deployEnv}". Use production or test.`)
  }
  return value
}

async function fileExists(filePath) {
  try {
    const fileStats = await stat(filePath)
    return fileStats.isFile()
  } catch (error) {
    if (error && error.code === 'ENOENT') return false
    throw error
  }
}

function parseEnvLine(line) {
  const trimmed = line.trim()
  if (!trimmed || trimmed.startsWith('#')) return null
  const match = /^(?:export\s+)?([A-Za-z_][A-Za-z0-9_]*)=(.*)$/.exec(trimmed)
  if (!match) return null

  const [, key, rawValue] = match
  let value = rawValue.trim()
  if (
    (value.startsWith('"') && value.endsWith('"')) ||
    (value.startsWith("'") && value.endsWith("'"))
  ) {
    value = value.slice(1, -1)
  }
  return [key, value.replaceAll('\\$', '$')]
}

function parseEnvFile(content) {
  const values = {}
  for (const line of content.split(/\r?\n/)) {
    const parsed = parseEnvLine(line)
    if (parsed) values[parsed[0]] = parsed[1]
  }
  return values
}

function resolveSecurityEncodeKey(env, deployEnv) {
  const key = SECURITY_KEY_ENV_NAMES.map((envName) => env[envName]).find(Boolean)
  if (!key) {
    throw new Error(`NEXT_PUBLIC_SECURITY_ENCODE_KEY is required for smart-h5 ${deployEnv} builds.`)
  }
  const keyBytes = Buffer.byteLength(key)
  if (!AES_KEY_LENGTHS.has(keyBytes)) {
    throw new Error(
      `NEXT_PUBLIC_SECURITY_ENCODE_KEY must be 16, 24, or 32 bytes for AES; got ${keyBytes} bytes.`,
    )
  }
  return key
}

export async function loadDeployEnv(projectRoot = process.cwd(), deployEnvInput = 'production', baseEnv = process.env) {
  const deployEnv = normalizeDeployEnv(deployEnvInput)
  const root = path.resolve(projectRoot)
  const envFiles = [
    '.env',
    '.env.local',
    `.env.${deployEnv}`,
    `.env.${deployEnv}.local`,
  ].map((fileName) => path.join(root, fileName))

  const fileEnv = {}
  const loadedFiles = []
  for (const envFile of envFiles) {
    if (!(await fileExists(envFile))) continue
    Object.assign(fileEnv, parseEnvFile(await readFile(envFile, 'utf8')))
    loadedFiles.push(envFile)
  }

  const env = { ...fileEnv, ...baseEnv, SMART_H5_ENV: deployEnv }
  const securityEncodeKey = resolveSecurityEncodeKey(env, deployEnv)
  env.NEXT_PUBLIC_SECURITY_ENCODE_KEY = securityEncodeKey
  if (!env.API_PROXY_TARGET) {
    throw new Error(`API_PROXY_TARGET is required for smart-h5 ${deployEnv} builds.`)
  }

  return { deployEnv, env, loadedFiles, securityEncodeKey }
}
