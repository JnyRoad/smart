import test from 'node:test'
import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'

import {
  buildBadgeEntries,
  buildAuthorizedAreaText,
  createRecordBadgePreview,
  validatePngBase64,
  validateRecordId
} from './record-badge-print.mjs'

const PNG_BASE64 =
  'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII='

class FakeElement {
  constructor(tagName) {
    this.tagName = tagName.toUpperCase()
    this.children = []
    this.listeners = {}
    this.attributes = {}
    this.textContent = ''
    this.disabled = false
    this.className = ''
    this.clientHeight = 0
    this.scrollHeight = 0
  }

  set innerHTML(value) {
    throw new Error(`不允许写入innerHTML: ${value}`)
  }

  appendChild(child) {
    child.parentNode = this
    this.children.push(child)
    return child
  }

  append(...children) {
    children.forEach(child => this.appendChild(child))
  }

  setAttribute(name, value) {
    this.attributes[name] = String(value)
  }

  addEventListener(name, listener) {
    this.listeners[name] = listener
  }

  dispatch(name) {
    if (this.listeners[name]) this.listeners[name]({ currentTarget: this })
  }
}

class FakeDocument {
  constructor() {
    this.head = new FakeElement('head')
    this.body = new FakeElement('body')
    this.title = ''
  }

  createElement(tagName) {
    return new FakeElement(tagName)
  }
}

function descendants(root, tagName) {
  const found = []
  const expected = tagName.toUpperCase()
  const visit = node => {
    if (node.tagName === expected) found.push(node)
    node.children.forEach(visit)
  }
  visit(root)
  return found
}

function popupHarness() {
  const document = new FakeDocument()
  const popup = {
    document,
    opener: {},
    printCount: 0,
    focus() {},
    print() {
      this.printCount += 1
    }
  }
  const browserWindow = {
    atob,
    openCount: 0,
    open() {
      this.openCount += 1
      return popup
    }
  }
  return { browserWindow, popup }
}

function visitor(overrides = {}) {
  return {
    company: '合成单位',
    parkName: '合成园区',
    receptionistName: '接待人甲',
    startTime: '2026-09-05 08:00',
    endTime: '2026-09-05 18:00',
    authorizedArea: '新工厂：A区；老工厂：B区',
    ...overrides
  }
}

function member(id, overrides = {}) {
  return {
    id,
    fellowName: '合成访客',
    recordQrCode: PNG_BASE64,
    ...overrides
  }
}

test('记录ID按1至19位正Long十进制字符串原样保留，不经过Number转换', () => {
  assert.equal(validateRecordId('1'), '1')
  assert.equal(validateRecordId('123456'), '123456')
  assert.equal(validateRecordId('1000000'), '1000000')
  assert.equal(validateRecordId('9223372036854775807'), '9223372036854775807')
  assert.throws(() => validateRecordId(9223372036854775807), /字符串/)
  assert.throws(() => validateRecordId('0'), /1至19位/)
  assert.throws(() => validateRecordId('9223372036854775808'), /Long范围/)
})

test('逐人条目使用各自member.id且不从smsCode回退', () => {
  const entries = buildBadgeEntries(visitor({ smsCode: '654321' }), [member('123456'), member('2')], atob)
  assert.deepEqual(entries.map(item => item.recordId), ['123456', '2'])

  assert.throws(
    () => buildBadgeEntries(visitor({ smsCode: '654321' }), [member(undefined)], atob),
    /第1位访客缺少有效记录ID/
  )
})

test('详情响应为空或省略随行人员列表时统一归一化为空数组', () => {
  const componentSource = readFileSync(new URL('./index.vue', import.meta.url), 'utf8')
  const normalizations = componentSource.match(
    /this\.memberList = Array\.isArray\(this\.visitorData\.fellowVisitorList\)\s*\? this\.visitorData\.fellowVisitorList\s*:\s*\[\]/g
  ) || []
  assert.equal(normalizations.length, 2)
})

test('授权区域按新旧工厂映射展示，并在无映射时保留接口展示值', () => {
  assert.equal(
    buildAuthorizedAreaText(
      { areaType: [0, 7], permitArea: '自由文字不参与映射' },
      [{ code: 0, desc: 'A区' }],
      [{ code: 7, desc: 'B区' }]
    ),
    '新工厂：A区；老工厂：B区'
  )
  assert.equal(buildAuthorizedAreaText({ areaType: [], permitArea: '访客中心' }, [], []), '访客中心')
})

test('二维码只接受可解码且带PNG文件头的规范base64', () => {
  assert.equal(validatePngBase64(PNG_BASE64, atob), PNG_BASE64)
  assert.throws(() => validatePngBase64('data:image/png;base64,' + PNG_BASE64, atob), /PNG Base64/)
  assert.throws(() => validatePngBase64('SGVsbG8=', atob), /PNG Base64/)
  assert.throws(() => validatePngBase64('%%%'), /PNG Base64/)
})

test('预览用textContent承载不可信字段且每名访客独立一页', () => {
  const { browserWindow, popup } = popupHarness()
  createRecordBadgePreview(
    browserWindow,
    visitor({ company: '<img src=x onerror=alert(1)>' }),
    [member('1000001', { fellowName: '<script>alert(1)</script>' }), member('1000002')]
  )

  assert.equal(descendants(popup.document.body, 'article').length, 2)
  const texts = descendants(popup.document.body, 'span').map(node => node.textContent)
  assert.ok(texts.includes('<img src=x onerror=alert(1)>'))
  assert.ok(texts.includes('<script>alert(1)</script>'))
})

test('园区被访人期限和授权区域位于全宽下部信息区', () => {
  const { browserWindow, popup } = popupHarness()
  createRecordBadgePreview(browserWindow, visitor(), [member('1000001')])

  const area = descendants(popup.document.body, 'span').find(node => node.textContent === '新工厂：A区；老工厂：B区')
  assert.equal(area.parentNode.parentNode.className, 'badge-bottom')
  assert.deepEqual(
    area.parentNode.parentNode.children.map(row => row.children[0].textContent),
    ['园区', '被访人', '期限', '区域']
  )
})

test('图片全部加载前不打印，用户点击启用后的按钮才调用print', () => {
  const { browserWindow, popup } = popupHarness()
  createRecordBadgePreview(
    browserWindow,
    visitor(),
    [member('9223372036854775807', { fellowPhotoIdUrl: 'https://example.test/photo.png' })]
  )

  const button = descendants(popup.document.body, 'button')[0]
  const images = descendants(popup.document.body, 'img')
  assert.equal(images.length, 2)
  assert.equal(button.disabled, true)
  assert.equal(popup.printCount, 0)

  images.forEach(image => image.dispatch('load'))
  assert.equal(button.disabled, false)
  assert.equal(popup.printCount, 0)
  button.dispatch('click')
  assert.equal(popup.printCount, 1)
})

test('任一打印字段发生真实布局溢出时禁用打印并明确提示', () => {
  const { browserWindow, popup } = popupHarness()
  createRecordBadgePreview(browserWindow, visitor(), [member('1000001')])

  const area = descendants(popup.document.body, 'span').find(node => node.textContent === '新工厂：A区；老工厂：B区')
  area.clientHeight = 18
  area.scrollHeight = 36
  descendants(popup.document.body, 'img').forEach(image => image.dispatch('load'))

  const button = descendants(popup.document.body, 'button')[0]
  const status = descendants(popup.document.body, 'span').find(node => node.className === 'print-status')
  assert.equal(button.disabled, true)
  assert.match(status.textContent, /第1张厂牌内容超出80×62mm/)
  button.dispatch('click')
  assert.equal(popup.printCount, 0)
})

test('任一访客缺二维码时在打开新窗口前阻断整次预览', () => {
  const { browserWindow } = popupHarness()
  assert.throws(
    () => createRecordBadgePreview(browserWindow, visitor({ smsCode: '123456' }), [member('1000001', { recordQrCode: '' })]),
    /第1位访客缺少有效记录二维码/
  )
  assert.equal(browserWindow.openCount, 0)
})
