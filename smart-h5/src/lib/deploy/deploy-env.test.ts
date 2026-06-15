import { mkdir, mkdtemp, rm, writeFile } from 'node:fs/promises'
import { tmpdir } from 'node:os'
import path from 'node:path'

import { afterEach, describe, expect, it } from 'vitest'

const { loadDeployEnv } = await import('../../../scripts/deploy-env.mjs')

const projectRoots: string[] = []
const isolatedBaseEnv = { NODE_ENV: 'test' } as NodeJS.ProcessEnv

async function createProjectRoot() {
  const projectRoot = await mkdtemp(path.join(tmpdir(), 'smart-h5-env-test-'))
  projectRoots.push(projectRoot)
  return projectRoot
}

async function writeProjectFile(projectRoot: string, filePath: string, content: string) {
  const fullPath = path.join(projectRoot, filePath)
  await mkdir(path.dirname(fullPath), { recursive: true })
  await writeFile(fullPath, content, { flag: 'w' })
}

afterEach(async () => {
  await Promise.all(projectRoots.splice(0).map((projectRoot) => rm(projectRoot, { recursive: true, force: true })))
})

describe('loadDeployEnv', () => {
  it('loads deploy env files and lets local files override committed defaults', async () => {
    const projectRoot = await createProjectRoot()

    await writeProjectFile(
      projectRoot,
      '.env.production',
      [
        'NEXT_PUBLIC_SECURITY_ENCODE_KEY=default-key-1234',
        'API_PROXY_TARGET=http://default.example.com',
      ].join('\n'),
    )
    await writeProjectFile(
      projectRoot,
      '.env.production.local',
      [
        'NEXT_PUBLIC_SECURITY_ENCODE_KEY=escaped\\$key-1234',
        'API_PROXY_TARGET=http://local.example.com',
      ].join('\n'),
    )

    const result = (await loadDeployEnv(projectRoot, 'production', isolatedBaseEnv)) as {
      securityEncodeKey: string
      env: Record<string, string>
      loadedFiles: string[]
    }

    expect(result.securityEncodeKey).toBe('escaped$key-1234')
    expect(result.env.NEXT_PUBLIC_SECURITY_ENCODE_KEY).toBe('escaped$key-1234')
    expect(result.env.API_PROXY_TARGET).toBe('http://local.example.com')
    expect(result.loadedFiles.map((filePath: string) => path.basename(filePath))).toEqual([
      '.env.production',
      '.env.production.local',
    ])
  })

  it('fails fast when a deploy env does not provide the security key', async () => {
    const projectRoot = await createProjectRoot()

    await writeProjectFile(projectRoot, '.env.test.local', 'API_PROXY_TARGET=http://test.example.com')

    await expect(loadDeployEnv(projectRoot, 'test', isolatedBaseEnv)).rejects.toThrow(
      'NEXT_PUBLIC_SECURITY_ENCODE_KEY is required for smart-h5 test builds.',
    )
  })

  it('fails fast when any deploy env does not provide the API proxy target', async () => {
    const projectRoot = await createProjectRoot()

    await writeProjectFile(projectRoot, '.env.test.local', 'NEXT_PUBLIC_SECURITY_ENCODE_KEY=1234567890abcdef')

    await expect(loadDeployEnv(projectRoot, 'test', isolatedBaseEnv)).rejects.toThrow(
      'API_PROXY_TARGET is required for smart-h5 test builds.',
    )
  })
})
