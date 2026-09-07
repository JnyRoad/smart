/** 编译器即使退出为零，也必须把已报告的原生样式错误视为失败。 */
import { test } from 'node:test'
import assert from 'node:assert/strict'
import { hasCompilerErrors } from '../scripts/compiler-diagnostics.mjs'

test('原生CSS错误不能被后续Build complete掩盖', () => {
  assert.equal(hasCompilerErrors('​﻿[plugin:uni:app-uvue-css] ERROR: Selector `.btn[disabled]` is not supported.\nDONE  Build complete.'), true)
  assert.equal(hasCompilerErrors('[plugin:uni:app-uvue-css] Invalid selector ".field:focus".\nDONE  Build complete.'), true)
})
test('普通完成消息与提示不产生虚假编译失败', () => {
  assert.equal(hasCompilerErrors('Compiler version: 5.24\nWARNING: source map disabled\nDONE  Build complete.'), false)
})
