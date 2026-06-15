import { spawn } from 'node:child_process'
import { fileURLToPath } from 'node:url'
import path from 'node:path'

import { loadDeployEnv } from './deploy-env.mjs'
import { prepareStandaloneDeploy } from './prepare-standalone-deploy.mjs'

function run(command, args, options) {
  return new Promise((resolve, reject) => {
    const child = spawn(command, args, { stdio: 'inherit', ...options })
    child.on('error', reject)
    child.on('exit', (code) => {
      if (code === 0) resolve()
      else reject(new Error(`${command} ${args.join(' ')} exited with code ${code}`))
    })
  })
}

async function build() {
  const projectRoot = process.cwd()
  const deployEnv = process.argv[2] ?? process.env.SMART_H5_ENV ?? 'production'
  const deployConfig = await loadDeployEnv(projectRoot, deployEnv)
  const loadedFiles = deployConfig.loadedFiles.map((filePath) => path.basename(filePath)).join(', ') || 'process.env only'

  console.log(`Loaded smart-h5 ${deployConfig.deployEnv} env: ${loadedFiles}`)

  await run('next', ['build'], {
    cwd: projectRoot,
    env: deployConfig.env,
  })
  await prepareStandaloneDeploy(projectRoot, {
    securityEncodeKey: deployConfig.securityEncodeKey,
  })

  console.log(`Prepared smart-h5 ${deployConfig.deployEnv} standalone bundle.`)
}

function isDirectRun() {
  return process.argv[1] && fileURLToPath(import.meta.url) === path.resolve(process.argv[1])
}

if (isDirectRun()) {
  build().catch((error) => {
    console.error(error instanceof Error ? error.message : error)
    process.exit(1)
  })
}
