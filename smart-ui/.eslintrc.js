// Lint baseline strategy (CR-C3-001):
// - Extend eslint:recommended + plugin:vue/recommended for parser/plugin setup and rule coverage.
// - Demote every inherited rule to "warn" so the ~150k-line legacy codebase keeps building;
//   warnings make violations visible in review without blocking lint/build.
// - no-debugger and no-empty are hard errors (legacy empty blocks were cleaned up in the
//   no-empty-cleanup task; intentional empty catches carry explanatory comments instead).
// - Existing violation counts are recorded in docs/lint-baseline.md; new code must not add to them.

// Walks a config "extends" chain (eslint-plugin-vue@4 uses require.resolve paths) and
// merges rules child-over-parent, matching ESLint's own resolution order.
function collectRules(configPath) {
  const config = require(configPath)
  const parentRules = config.extends ? collectRules(config.extends) : {}
  return Object.assign(parentRules, config.rules)
}

function demoteToWarn(rules) {
  const demoted = {}
  for (const name of Object.keys(rules)) {
    const value = rules[name]
    if (value === 'off' || value === 0) continue
    demoted[name] = Array.isArray(value) ? ['warn'].concat(value.slice(1)) : 'warn'
  }
  return demoted
}

const inheritedRules = demoteToWarn(Object.assign({}, require('eslint/conf/eslint-recommended.js').rules, collectRules('eslint-plugin-vue/lib/configs/recommended.js')))

module.exports = {
  root: true,
  env: {
    node: true,
    browser: true
  },
  globals: {
    // Real runtime globals, not undefined identifiers:
    // tce: window.tce assigned in src/main.js and src/mixins/index.js
    // axios: script tag in public/index.html + webpack externals in vue.config.js
    // CryptoJS: crypto-js script tags in public/index.html
    // (eslint 4 syntax: false = read-only)
    tce: false,
    axios: false,
    CryptoJS: false,
    // globalThis 是 ES2020 标准全局对象，仅因 env 未声明 es2020 才被 no-undef 误报，非项目自定义全局
    globalThis: false
  },
  extends: ['eslint:recommended', 'plugin:vue/recommended'],
  rules: Object.assign(inheritedRules, {
    'no-debugger': 'error',
    'no-empty': ['error', { allowEmptyCatch: false }],
    // no-empty only covers try/catch blocks; these selectors ban empty Promise
    // .catch(() => {}) callbacks, which swallow errors silently (CR-C14-003).
    'no-restricted-syntax': [
      'error',
      {
        selector: "CallExpression[callee.property.name='catch'] > ArrowFunctionExpression[body.body.length=0]",
        message: 'Empty .catch() swallows errors. At least console.error the reason (CR-C14-003).'
      },
      {
        selector: "CallExpression[callee.property.name='catch'] > FunctionExpression[body.body.length=0]",
        message: 'Empty .catch() swallows errors. At least console.error the reason (CR-C14-003).'
      }
    ],
    // console.error/warn are the sanctioned error-visibility channel (CR-C14-003);
    // plain console.log remains a warning.
    'no-console': ['warn', { allow: ['error', 'warn'] }],
    complexity: ['warn', 15],
    'max-lines': ['warn', { max: 300, skipBlankLines: true, skipComments: true }],
    'id-length': ['warn', { min: 2, exceptions: ['i', 'j', '_', '$'] }]
  }),
  overrides: [
    {
      // Pages must go through the src/api layer instead of calling axios directly (CR-C5-005).
      files: ['src/views/**/*.js', 'src/views/**/*.vue'],
      rules: {
        'no-restricted-imports': [
          'warn',
          {
            paths: [
              {
                name: 'axios',
                message: 'Import an api module from @/api instead of using axios directly in views.'
              }
            ]
          }
        ]
      }
    }
  ],
  parserOptions: {
    parser: 'babel-eslint'
  }
}
