import assert from 'node:assert/strict'
import { mkdtemp, rm, writeFile } from 'node:fs/promises'
import { tmpdir } from 'node:os'
import path from 'node:path'

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

  assert.deepEqual(await scanConfigDirectory(fixtureDirectory), [
    {
      dataId: 'smart-platform.yml',
      fileName: 'smart-platform.yml',
      line: 6,
      path: '/staff/**',
    },
  ])
} finally {
  await rm(fixtureDirectory, { force: true, recursive: true })
}

console.log('check-nacos-ignore-urls tests passed')
