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

const unsupportedValueDirectory = await mkdtemp(path.join(tmpdir(), 'smart-nacos-unsupported-value-'))

try {
  await writeFile(
    path.join(unsupportedValueDirectory, 'smart-unsupported.yml'),
    [
      'security:',
      '  oauth2:',
      '    client:',
      '      ignore-urls: { path: "/api/**" }',
    ].join('\n'),
  )

  await assert.rejects(
    scanConfigDirectory(unsupportedValueDirectory),
    /Unsupported ignore-urls value in smart-unsupported\.yml:4/,
  )
  const cliResult = runScannerCli(unsupportedValueDirectory)
  assert.equal(cliResult.status, 2)
  assert.match(cliResult.stderr, /Unsupported ignore-urls value in smart-unsupported\.yml:4/)
} finally {
  await rm(unsupportedValueDirectory, { force: true, recursive: true })
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

const semanticYamlDirectory = await mkdtemp(path.join(tmpdir(), 'smart-nacos-semantic-yaml-'))

try {
  await writeFile(
    path.join(semanticYamlDirectory, 'smart-anchor.yml'),
    [
      'security:',
      '  oauth2:',
      '    client:',
      '      ignore-urls: &public_paths',
      '        - "/api/**"',
    ].join('\n'),
  )
  await writeFile(
    path.join(semanticYamlDirectory, 'smart-quoted-key.yml'),
    [
      'security:',
      '  oauth2:',
      '    client:',
      '      "ignore-urls": ["/staff/**"]',
    ].join('\n'),
  )
  await writeFile(
    path.join(semanticYamlDirectory, 'smart-tag.yml'),
    [
      'security:',
      '  oauth2:',
      '    client:',
      '      ignore-urls: !!seq ["/articlesrelease/**"]',
    ].join('\n'),
  )
  await writeFile(
    path.join(semanticYamlDirectory, 'smart-unicode-key.yml'),
    [
      'security:',
      '  oauth2:',
      '    client:',
      '      "\\u0069gnore-urls": ["/**"]',
    ].join('\n'),
  )

  assert.deepEqual(await scanConfigDirectory(semanticYamlDirectory), [
    { dataId: 'smart-anchor.yml', fileName: 'smart-anchor.yml', line: 5, path: '/api/**' },
    { dataId: 'smart-quoted-key.yml', fileName: 'smart-quoted-key.yml', line: 4, path: '/staff/**' },
    { dataId: 'smart-tag.yml', fileName: 'smart-tag.yml', line: 4, path: '/articlesrelease/**' },
    { dataId: 'smart-unicode-key.yml', fileName: 'smart-unicode-key.yml', line: 4, path: '/**' },
  ])

  const cliResult = runScannerCli(semanticYamlDirectory)
  assert.equal(cliResult.status, 1)
  assert.match(cliResult.stderr, /smart-anchor\.yml:5 ignore-urls \/api\/\*\*/)
  assert.match(cliResult.stderr, /smart-quoted-key\.yml:4 ignore-urls \/staff\/\*\*/)
  assert.match(cliResult.stderr, /smart-tag\.yml:4 ignore-urls \/articlesrelease\/\*\*/)
  assert.match(cliResult.stderr, /smart-unicode-key\.yml:4 ignore-urls \/\*\*/)
} finally {
  await rm(semanticYamlDirectory, { force: true, recursive: true })
}

const invalidValueDirectory = await mkdtemp(path.join(tmpdir(), 'smart-nacos-invalid-value-'))

try {
  await writeFile(
    path.join(invalidValueDirectory, 'smart-invalid-value.yml'),
    [
      'security:',
      '  oauth2:',
      '    client:',
      '      ignore-urls: [123]',
    ].join('\n'),
  )

  await assert.rejects(
    scanConfigDirectory(invalidValueDirectory),
    /Unsupported ignore-urls value in smart-invalid-value\.yml:4/,
  )
  const cliResult = runScannerCli(invalidValueDirectory)
  assert.equal(cliResult.status, 2)
  assert.match(cliResult.stderr, /Unsupported ignore-urls value in smart-invalid-value\.yml:4/)
} finally {
  await rm(invalidValueDirectory, { force: true, recursive: true })
}

const invalidYamlDirectory = await mkdtemp(path.join(tmpdir(), 'smart-nacos-invalid-yaml-'))

try {
  await writeFile(
    path.join(invalidYamlDirectory, 'smart-invalid-yaml.yml'),
    [
      'security:',
      '  oauth2:',
      '    client:',
      '      ignore-urls: ["/api/**"',
    ].join('\n'),
  )

  await assert.rejects(scanConfigDirectory(invalidYamlDirectory), /Invalid YAML in smart-invalid-yaml\.yml/)
  const cliResult = runScannerCli(invalidYamlDirectory)
  assert.equal(cliResult.status, 2)
  assert.match(cliResult.stderr, /Invalid YAML in smart-invalid-yaml\.yml/)
} finally {
  await rm(invalidYamlDirectory, { force: true, recursive: true })
}

console.log('check-nacos-ignore-urls tests passed')
