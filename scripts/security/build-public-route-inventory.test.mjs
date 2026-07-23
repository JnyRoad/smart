import assert from 'node:assert/strict'
import { mkdir, mkdtemp, rm, writeFile } from 'node:fs/promises'
import { tmpdir } from 'node:os'
import path from 'node:path'

import {
  buildInventory,
  classifyRoute,
  parseControllerSource,
  parseControllerSourceDetailed,
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

assert.deepEqual(
  classifyRoute({
    path: '/callback/vendor',
    annotations: ['SignatureVerified'],
    signatureEvidence: ['signature'],
  }),
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

const detailedRoutes = parseControllerSourceDetailed(
  [
    '@RequestMapping(',
    '  path = { "/api", "/legacy" }',
    ')',
    'public class MultiPathController {',
    '  @Inner',
    '  @OpenApi("server")',
    '  @PostMapping(',
    '    value = { "/one", "/two" }',
    '  )',
    '  public void mapped() {}',
    '  @PostMapping(path = ROUTE_CONSTANT)',
    '  public void unresolved() {}',
    '  @GetMapping(path = { "/mixed", ROUTE_CONSTANT })',
    '  public void mixed() {}',
    '  @SignatureVerified',
    '  @PostMapping("/callback")',
    '  public void callback() {',
    '    signatureVerifier.verifySignature(event);',
    '    replayGuard.validateTimestamp(event);',
    '    replayGuard.checkNonce(event);',
    '  }',
    '}',
  ].join('\n'),
  'MultiPathController.java',
)

assert.deepEqual(
  detailedRoutes.routes
    .filter((route) => route.path.includes('/one') || route.path.includes('/two'))
    .map((route) => ({ path: route.path, annotations: route.annotations, openApiScopes: route.openApiScopes }))
    .sort((left, right) => left.path.localeCompare(right.path)),
  [
    { path: '/api/one', annotations: ['Inner', 'OpenApi'], openApiScopes: ['server'] },
    { path: '/api/two', annotations: ['Inner', 'OpenApi'], openApiScopes: ['server'] },
    { path: '/legacy/one', annotations: ['Inner', 'OpenApi'], openApiScopes: ['server'] },
    { path: '/legacy/two', annotations: ['Inner', 'OpenApi'], openApiScopes: ['server'] },
  ],
)
assert.deepEqual(detailedRoutes.unparsedMappings, [
  {
    annotation: 'PostMapping',
    line: 11,
    reason: 'path or value is not a string literal',
    sourcePath: 'MultiPathController.java',
  },
  {
    annotation: 'GetMapping',
    line: 13,
    reason: 'path or value is not a string literal',
    sourcePath: 'MultiPathController.java',
  },
])
assert.deepEqual(
  detailedRoutes.routes.find((route) => route.path === '/api/callback').signatureEvidence,
  ['signature', 'timestamp', 'nonce'],
)

assert.deepEqual(
  parseControllerSourceDetailed(
    [
      '@RequestMapping("/scope")',
      'public class ScopeController {',
      '  @Inner',
      '  @OpenApi(name = "server")',
      '  @PostMapping("/invalid")',
      '  public void invalid() {}',
      '}',
    ].join('\n'),
    'ScopeController.java',
  ).routes[0].openApiScopes,
  [],
)

const commentOnlyEvidence = parseControllerSourceDetailed(
  [
    '@RequestMapping("/comments")',
    'public class CommentOnlyController {',
    '  @PostMapping("/callback")',
    '  public void callback() {',
    '    // signatureVerifier.verifySignature(event);',
    '    // replayGuard.validateTimestamp(event);',
    '    // replayGuard.checkNonce(event);',
    '    String ignored = "signatureVerifier.verifySignature replayGuard.validateTimestamp replayGuard.checkNonce";',
    '  }',
    '}',
  ].join('\n'),
  'CommentOnlyController.java',
).routes[0]

assert.deepEqual(commentOnlyEvidence.signatureEvidence, [])
assert.equal(classifyRoute(commentOnlyEvidence).exposure, 'external-authenticated')

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

const unparsedFixtureRoot = await mkdtemp(path.join(tmpdir(), 'smart-route-unparsed-'))
try {
  const controllerDirectory = path.join(unparsedFixtureRoot, 'module', 'src', 'main', 'java')
  const configDirectory = path.join(unparsedFixtureRoot, 'config')
  await mkdir(controllerDirectory, { recursive: true })
  await mkdir(configDirectory, { recursive: true })
  await writeFile(
    path.join(configDirectory, 'smart-fixture.yml'),
    ['security:', '  oauth2:', '    client:', '      ignore-urls: []'].join('\n'),
  )
  await writeFile(
    path.join(controllerDirectory, 'UnparsedController.java'),
    [
      '@RestController',
      '@RequestMapping("/fixture")',
      'public class UnparsedController {',
      '  @Inner',
      '  @OpenApi("open:fixture:read")',
      '  @PostMapping("/internal")',
      '  public void internal() {}',
      '  @PostMapping(path = ROUTE_CONSTANT)',
      '  public void unresolved() {}',
      '  @PostMapping("/callback")',
      '  public void callback() {',
      '    signatureVerifier.verifySignature(event);',
      '    replayGuard.validateTimestamp(event);',
      '    replayGuard.checkNonce(event);',
      '  }',
      '}',
    ].join('\n'),
  )
  const inventory = await buildInventory({
    configDirectory,
    targets: [{ service: 'smart-fixture', configNames: ['smart-fixture.yml'], controllerDirectory }],
  })
  assert.equal(inventory.hasBlockingFindings, true)
  assert.match(inventory.markdown, /UnparsedController\.java:8 PostMapping: path or value is not a string literal/)
  assert.match(inventory.markdown, /\/fixture\/internal.*BLOCKED.*缺少 @OpenApi\(server\)/s)
  assert.deepEqual(
    inventory.routes.find((route) => route.path === '/fixture/callback').exposure,
    'callback-signed',
  )
} finally {
  await rm(unparsedFixtureRoot, { force: true, recursive: true })
}

const zeroRouteFixtureRoot = await mkdtemp(path.join(tmpdir(), 'smart-route-zero-'))
try {
  const controllerDirectory = path.join(zeroRouteFixtureRoot, 'module', 'src', 'main', 'java')
  const configDirectory = path.join(zeroRouteFixtureRoot, 'config')
  await mkdir(controllerDirectory, { recursive: true })
  await mkdir(configDirectory, { recursive: true })
  await writeFile(
    path.join(configDirectory, 'smart-zero-route.yml'),
    ['security:', '  oauth2:', '    client:', '      ignore-urls: []'].join('\n'),
  )
  await writeFile(
    path.join(controllerDirectory, 'NoRouteController.java'),
    ['@RestController', 'public class NoRouteController {}'].join('\n'),
  )
  const inventory = await buildInventory({
    configDirectory,
    targets: [{ service: 'smart-zero-route', configNames: ['smart-zero-route.yml'], controllerDirectory }],
  })
  assert.equal(inventory.hasBlockingFindings, true)
  assert.match(inventory.markdown, /发现 Controller 源码但未解析到任何路由/)
} finally {
  await rm(zeroRouteFixtureRoot, { force: true, recursive: true })
}
