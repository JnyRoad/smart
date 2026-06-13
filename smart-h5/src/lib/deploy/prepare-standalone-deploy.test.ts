import { mkdir, mkdtemp, readFile, rm, stat, writeFile } from 'node:fs/promises'
import { tmpdir } from 'node:os'
import path from 'node:path'

import { afterEach, describe, expect, it } from 'vitest'

const { prepareStandaloneDeploy } = await import('../../../scripts/prepare-standalone-deploy.mjs')

const projectRoots: string[] = []

async function createProjectRoot() {
  const projectRoot = await mkdtemp(path.join(tmpdir(), 'smart-h5-deploy-test-'))
  projectRoots.push(projectRoot)
  return projectRoot
}

afterEach(async () => {
  await Promise.all(projectRoots.splice(0).map((projectRoot) => rm(projectRoot, { recursive: true, force: true })))
})

async function writeProjectFile(projectRoot: string, filePath: string, content: string) {
  const fullPath = path.join(projectRoot, filePath)
  await mkdir(path.dirname(fullPath), { recursive: true })
  await writeFile(fullPath, content, { flag: 'w' })
}

describe('prepareStandaloneDeploy', () => {
  it('copies static assets and public files into the standalone deploy directory', async () => {
    const projectRoot = await createProjectRoot()

    await mkdir(path.join(projectRoot, '.next/standalone/.next'), { recursive: true })
    await writeProjectFile(projectRoot, '.next/static/chunks/app.js', 'console.log("app")')
    await writeProjectFile(projectRoot, 'public/config.js', 'window.__SMART_CONFIG__ = {}')

    await prepareStandaloneDeploy(projectRoot)

    await expect(
      readFile(path.join(projectRoot, '.next/standalone/.next/static/chunks/app.js'), 'utf8'),
    ).resolves.toBe('console.log("app")')
    await expect(
      readFile(path.join(projectRoot, '.next/standalone/public/config.js'), 'utf8'),
    ).resolves.toBe('window.__SMART_CONFIG__ = {}')
  })

  it('fails fast when next build has not produced static assets', async () => {
    const projectRoot = await createProjectRoot()

    await mkdir(path.join(projectRoot, '.next/standalone/.next'), { recursive: true })
    await writeProjectFile(projectRoot, 'public/config.js', 'window.__SMART_CONFIG__ = {}')

    await expect(prepareStandaloneDeploy(projectRoot)).rejects.toThrow(
      '.next/static does not exist. Run next build before preparing the deploy bundle.',
    )
    await expect(stat(path.join(projectRoot, '.next/standalone/public'))).rejects.toThrow()
  })

  it('fails fast when the deploy config file is missing', async () => {
    const projectRoot = await createProjectRoot()

    await mkdir(path.join(projectRoot, '.next/standalone/.next'), { recursive: true })
    await writeProjectFile(projectRoot, '.next/static/chunks/app.js', 'console.log("app")')
    await mkdir(path.join(projectRoot, 'public'), { recursive: true })

    await expect(prepareStandaloneDeploy(projectRoot)).rejects.toThrow(
      'public/config.js does not exist. The deploy bundle must include runtime tenant config.',
    )
    await expect(stat(path.join(projectRoot, '.next/standalone/public'))).rejects.toThrow()
  })
})
