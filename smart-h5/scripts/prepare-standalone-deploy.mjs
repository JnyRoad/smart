import { cp, rm, stat } from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

async function assertDirectoryExists(directoryPath, missingMessage) {
  try {
    const directoryStats = await stat(directoryPath)
    if (!directoryStats.isDirectory()) throw new Error(`${directoryPath} is not a directory.`)
  } catch (error) {
    if (error && error.code === 'ENOENT') throw new Error(missingMessage)
    throw error
  }
}

async function assertFileExists(filePath, missingMessage) {
  try {
    const fileStats = await stat(filePath)
    if (!fileStats.isFile()) throw new Error(`${filePath} is not a file.`)
  } catch (error) {
    if (error && error.code === 'ENOENT') throw new Error(missingMessage)
    throw error
  }
}

async function replaceDirectory(sourcePath, targetPath, missingMessage) {
  await assertDirectoryExists(sourcePath, missingMessage)
  await rm(targetPath, { recursive: true, force: true })
  await cp(sourcePath, targetPath, { recursive: true })
}

export async function prepareStandaloneDeploy(projectRoot = process.cwd()) {
  const root = path.resolve(projectRoot)
  const nextDir = path.join(root, '.next')
  const standaloneDir = path.join(nextDir, 'standalone')
  const standaloneNextDir = path.join(standaloneDir, '.next')

  await assertDirectoryExists(
    standaloneDir,
    '.next/standalone does not exist. Run next build before preparing the deploy bundle.',
  )
  await assertDirectoryExists(
    standaloneNextDir,
    '.next/standalone/.next does not exist. The standalone build output is incomplete.',
  )

  await replaceDirectory(
    path.join(nextDir, 'static'),
    path.join(standaloneNextDir, 'static'),
    '.next/static does not exist. Run next build before preparing the deploy bundle.',
  )

  await assertFileExists(
    path.join(root, 'public', 'config.js'),
    'public/config.js does not exist. The deploy bundle must include runtime tenant config.',
  )
  await replaceDirectory(
    path.join(root, 'public'),
    path.join(standaloneDir, 'public'),
    'public does not exist. The deploy bundle must include public/config.js.',
  )
}

function isDirectRun() {
  return process.argv[1] && fileURLToPath(import.meta.url) === path.resolve(process.argv[1])
}

if (isDirectRun()) {
  prepareStandaloneDeploy().catch((error) => {
    console.error(error instanceof Error ? error.message : error)
    process.exit(1)
  })
}
