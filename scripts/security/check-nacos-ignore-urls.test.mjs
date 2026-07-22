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

  const cliResult = spawnSync(
    process.execPath,
    [fileURLToPath(new URL('./check-nacos-ignore-urls.mjs', import.meta.url)), fixtureDirectory],
    { encoding: 'utf8' },
  )
  assert.equal(cliResult.status, 1)
  assert.match(cliResult.stderr, /smart-data\.yml:5 ignore-urls \/api\/\*\*/)
  assert.match(cliResult.stderr, /smart-upms-biz\.yml:4 ignore-urls \/api\/\*\*/)
} finally {
  await rm(fixtureDirectory, { force: true, recursive: true })
}

console.log('check-nacos-ignore-urls tests passed')
