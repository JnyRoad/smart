import assert from 'node:assert/strict'
import { spawnSync } from 'node:child_process'
import { mkdtemp, rm, writeFile } from 'node:fs/promises'
import { tmpdir } from 'node:os'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

import {
  findForbiddenIgnoreUrls,
  scanConfigDirectory,
} from './check-nacos-ignore-urls.mjs'

assert.deepEqual(
  findForbiddenIgnoreUrls(['/**', '/staff/**', '/articlesrelease/**', '/api/**', '/actuator/**']),
  ['/**', '/staff/**', '/articlesrelease/**', '/api/**'],
)
assert.deepEqual(findForbiddenIgnoreUrls(['/actuator/**', '/v2/api-docs']), [])

const scannerScriptPath = fileURLToPath(new URL('./check-nacos-ignore-urls.mjs', import.meta.url))

function runScannerCli(directory) {
  return spawnSync(process.execPath, [scannerScriptPath, directory], { encoding: 'utf8' })
}

const fixtureDirectory = await mkdtemp(path.join(tmpdir(), 'smart-nacos-ignore-urls-'))

try {
  await writeFile(
    path.join(fixtureDirectory, 'smart-platform.yml'),
    [
      'security:',
      '  oauth2:',
      '    client:',
      '      ignore-urls:',
      '        - /actuator/**',
      '        - /staff/**',
      '        # - /api/**',
    ].join('\n'),
  )

  await writeFile(
    path.join(fixtureDirectory, 'smart-upms-biz.yml'),
    [
      'security:',
      '  oauth2:',
      '    client:',
      '      ignore-urls: ["/actuator/**", "/api/**"]',
    ].join('\n'),
  )

  await writeFile(
    path.join(fixtureDirectory, 'smart-data.yml'),
    [
      'security:',
      '  oauth2:',
      '    client:',
      '      ignore-urls: [',
      '        "/api/**"',
      '      ]',
    ].join('\n'),
  )

  assert.deepEqual(await scanConfigDirectory(fixtureDirectory), [
    {
      dataId: 'smart-data.yml',
      fileName: 'smart-data.yml',
      line: 5,
      path: '/api/**',
    },
    {
      dataId: 'smart-platform.yml',
      fileName: 'smart-platform.yml',
      line: 6,
      path: '/staff/**',
    },
    {
      dataId: 'smart-upms-biz.yml',
      fileName: 'smart-upms-biz.yml',
      line: 4,
      path: '/api/**',
    },
  ])

  const cliResult = runScannerCli(fixtureDirectory)
  assert.equal(cliResult.status, 1)
  assert.match(cliResult.stderr, /smart-data\.yml:5 ignore-urls \/api\/\*\*/)
  assert.match(cliResult.stderr, /smart-upms-biz\.yml:4 ignore-urls \/api\/\*\*/)
} finally {
  await rm(fixtureDirectory, { force: true, recursive: true })
}

const unsupportedHeaderDirectory = await mkdtemp(path.join(tmpdir(), 'smart-nacos-unsupported-header-'))

try {
  await writeFile(
    path.join(unsupportedHeaderDirectory, 'smart-unsupported.yml'),
    [
      'security:',
      '  oauth2:',
      '    client:',
      '      ignore-urls: !!seq ["/api/**"]',
    ].join('\n'),
  )

  await assert.rejects(
    scanConfigDirectory(unsupportedHeaderDirectory),
    /Unsupported ignore-urls syntax in smart-unsupported\.yml:4/,
  )
  const cliResult = runScannerCli(unsupportedHeaderDirectory)
  assert.equal(cliResult.status, 2)
  assert.match(cliResult.stderr, /Unsupported ignore-urls syntax in smart-unsupported\.yml:4/)
} finally {
  await rm(unsupportedHeaderDirectory, { force: true, recursive: true })
}

const escapedScalarDirectory = await mkdtemp(path.join(tmpdir(), 'smart-nacos-escaped-scalar-'))

try {
  await writeFile(
    path.join(escapedScalarDirectory, 'smart-escaped.yml'),
    [
      'security:',
      '  oauth2:',
      '    client:',
      '      ignore-urls:',
      '        - "/api/\\u002a\\u002a"',
    ].join('\n'),
  )

  assert.deepEqual(await scanConfigDirectory(escapedScalarDirectory), [
    {
      dataId: 'smart-escaped.yml',
      fileName: 'smart-escaped.yml',
      line: 5,
      path: '/api/**',
    },
  ])
  const cliResult = runScannerCli(escapedScalarDirectory)
  assert.equal(cliResult.status, 1)
  assert.match(cliResult.stderr, /smart-escaped\.yml:5 ignore-urls \/api\/\*\*/)
} finally {
  await rm(escapedScalarDirectory, { force: true, recursive: true })
}

console.log('check-nacos-ignore-urls tests passed')
