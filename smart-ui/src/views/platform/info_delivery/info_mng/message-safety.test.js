import { describe, expect, it } from 'vitest'
import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'

const files = ['add.vue', 'edit.vue']
const expectedMessages = [
  '请上传正确格式的文件',
  '图片大小不超过500kb',
  '请上传mp4格式的视频',
  '视频大小不超过50mb',
  '请上传正确的pdf文件',
  '文件大小不超过20mb'
]

function readSource(fileName) {
  return readFileSync(resolve(process.cwd(), `src/views/platform/info_delivery/info_mng/${fileName}`), 'utf8')
}

describe('info delivery upload validation messages', () => {
  it('renders upload validation warnings as plain text', () => {
    files.forEach(fileName => {
      expect(readSource(fileName)).not.toContain('dangerouslyUseHTMLString')
    })
  })

  it('keeps the existing validation messages unchanged', () => {
    files.forEach(fileName => {
      const source = readSource(fileName)
      expectedMessages.forEach(message => {
        expect(source).toContain(`message: '${message}'`)
      })
    })
  })
})
