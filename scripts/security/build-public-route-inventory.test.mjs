import assert from 'node:assert/strict'
import { mkdir, mkdtemp, rm, writeFile } from 'node:fs/promises'
import { tmpdir } from 'node:os'
import path from 'node:path'

import {
  buildInventory,
  classifyRoute,
  parseControllerSource,
} from './build-public-route-inventory.mjs'

assert.deepEqual(
  classifyRoute({ path: '/compare', annotations: ['Inner'], caller: 'platform-feign' }),
  { exposure: 'internal', nacosIgnoreUrl: false, requires: ['service-token', 'FROM_IN'] },
)

assert.deepEqual(
  classifyRoute({
    path: '/callback/vendor',
    annotations: ['SignatureVerified'],
    caller: 'vendor',
    signatureEvidence: ['signature', 'timestamp', 'nonce'],
  }),
  {
    exposure: 'callback-signed',
    nacosIgnoreUrl: true,
    requires: ['signature', 'timestamp', 'nonce'],
  },
)

assert.deepEqual(
  classifyRoute({ path: '/callback/vendor', annotations: [], caller: 'vendor' }),
  {
    exposure: 'external-authenticated',
    nacosIgnoreUrl: false,
    requires: ['user-or-client-token'],
  },
)

const parsedRoutes = parseControllerSource(
  [
    '@RestController',
    '@RequestMapping("/bridge")',
    'public class ExampleController {',
    '  @Inner',
    '  @PostMapping("/dispatch")',
    '  public void dispatch() {}',
    '  @PostMapping("/handle")',
    '  public void handle() {}',
    '  @PostMapping("/inner/legacy")',
    '  public void legacy() {}',
    '}',
  ].join('\n'),
  'ExampleController.java',
)

assert.deepEqual(
  parsedRoutes.map((route) => ({ path: route.path, annotations: route.annotations })),
  [
    { path: '/bridge/dispatch', annotations: ['Inner'] },
    { path: '/bridge/handle', annotations: [] },
    { path: '/bridge/inner/legacy', annotations: [] },
  ],
)

const fixtureRoot = await mkdtemp(path.join(tmpdir(), 'smart-route-inventory-'))
try {
  const controllerDirectory = path.join(fixtureRoot, 'module', 'src', 'main', 'java')
  const configDirectory = path.join(fixtureRoot, 'config')
  await mkdir(configDirectory, { recursive: true })
  await writeFile(
    path.join(configDirectory, 'smart-bridge-isc.yml'),
    [
      'security:',
      '  oauth2:',
      '    client:',
      '      ignore-urls:',
      '        - /**',
    ].join('\n'),
  )
  await mkdir(controllerDirectory, { recursive: true })
  await writeFile(
    path.join(controllerDirectory, 'BridgeISCController.java'),
    [
      '@RestController',
      '@RequestMapping("/bridge")',
      'public class BridgeISCController {',
      '  @Inner',
      '  @PostMapping("/dispatch")',
      '  public void dispatch() {}',
      '  @PostMapping("/handle")',
      '  public void handle() {}',
      '  @PostMapping("/inner/legacy")',
      '  public void legacy() {}',
      '}',
    ].join('\n'),
  )

  const inventory = await buildInventory({
    configDirectory,
    targets: [
      {
        service: 'smart-bridge-isc',
        configNames: ['smart-bridge-isc.yml'],
        controllerDirectory,
      },
    ],
  })

  assert.equal(inventory.hasBlockingFindings, true)
  assert.deepEqual(
    inventory.routes.map((route) => ({ path: route.path, exposure: route.exposure, status: route.status })),
    [
      { path: '/bridge/dispatch', exposure: 'internal', status: 'BLOCKED' },
      { path: '/bridge/handle', exposure: 'external-authenticated', status: 'BLOCKED' },
      { path: '/bridge/inner/legacy', exposure: 'internal', status: 'BLOCKED' },
    ],
  )
  assert.match(inventory.markdown, /\/bridge\/handle.*BLOCKED.*签名、时间窗和 nonce 重放检查/s)
  assert.match(inventory.markdown, /@OpenApi\(server\)/)
  assert.match(inventory.markdown, /内部候选路径缺少 @Inner 与 @OpenApi\(server\) 静态证据/)
} finally {
  await rm(fixtureRoot, { force: true, recursive: true })
}
