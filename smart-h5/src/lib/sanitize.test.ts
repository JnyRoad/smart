// happy-dom（全局测试环境）解析 table/heading 等需要父级上下文的标签时会失真，
// 导致 DOMPurify 拿不到这些节点；这里改用更贴近真实浏览器的 jsdom 才能真实验证白名单。
// @vitest-environment jsdom
import { describe, expect, it } from 'vitest'
import { sanitizeRichText } from './sanitize'

/**
 * 富文本白名单测试。
 *
 * 内容来源：后台管理端（smart-ui）统一用 wangEditor v3 编写公告 bbsContent、
 * 帮助 answerContent、访客须知 content，菜单固定为 head/bold/fontSize/fontName/
 * italic/underline/strikeThrough/foreColor/backColor/link/list/justify/quote/
 * emoticon/image/table。因此白名单必须覆盖这些菜单产出的标签与属性，
 * 同时清除脚本、事件属性与 javascript: 协议。
 */
describe('sanitizeRichText', () => {
  describe('清除恶意内容（纵深防御）', () => {
    it('移除 <script> 标签', () => {
      const out = sanitizeRichText('<p>hi</p><script>alert(1)</script>')
      expect(out).toContain('hi')
      expect(out).not.toContain('<script')
      expect(out).not.toContain('alert(1)')
    })

    it('移除内联事件属性 onerror/onclick', () => {
      const out = sanitizeRichText('<img src="x" onerror="alert(1)"><div onclick="evil()">x</div>')
      expect(out).not.toContain('onerror')
      expect(out).not.toContain('onclick')
    })

    it('清除链接里的 javascript: 协议', () => {
      const out = sanitizeRichText('<a href="javascript:alert(1)">click</a>')
      expect(out).not.toContain('javascript:')
    })

    it('清除图片 src 里的 javascript: 协议', () => {
      const out = sanitizeRichText('<img src="javascript:alert(1)">')
      expect(out).not.toContain('javascript:')
    })

    it('给 target=_blank 链接补上 rel=noopener，防反向 tabnabbing', () => {
      const out = sanitizeRichText('<a href="https://example.com" target="_blank">L</a>')
      expect(out).toContain('rel="noopener noreferrer"')
    })

    it('移除 <iframe> 等不在白名单的危险标签', () => {
      const out = sanitizeRichText('<iframe src="https://evil.example"></iframe>')
      expect(out).not.toContain('<iframe')
    })

    it('移除 <style> 标签', () => {
      const out = sanitizeRichText('<style>body{display:none}</style><p>ok</p>')
      expect(out).not.toContain('<style')
      expect(out).toContain('ok')
    })
  })

  describe('保留 wangEditor 富文本现有标签（不可误删）', () => {
    it('保留图片 img 及 src/alt 属性', () => {
      const out = sanitizeRichText('<img src="https://cdn.example/a.png" alt="图">')
      expect(out).toContain('<img')
      expect(out).toContain('src="https://cdn.example/a.png"')
      expect(out).toContain('alt="图"')
    })

    it('保留链接 a 及 href/target 属性', () => {
      const out = sanitizeRichText('<a href="https://example.com" target="_blank">链接</a>')
      expect(out).toContain('href="https://example.com"')
      expect(out).toContain('target="_blank"')
      expect(out).toContain('链接')
    })

    it('保留表格 table/tbody/tr/td/th', () => {
      const html = '<table><thead><tr><th>列</th></tr></thead><tbody><tr><td>格</td></tr></tbody></table>'
      const out = sanitizeRichText(html)
      expect(out).toContain('<table')
      expect(out).toContain('<tr')
      expect(out).toContain('<td')
      expect(out).toContain('<th')
    })

    it('保留有序/无序列表 ul/ol/li', () => {
      const out = sanitizeRichText('<ul><li>a</li></ul><ol><li>b</li></ol>')
      expect(out).toContain('<ul')
      expect(out).toContain('<ol')
      expect(out).toContain('<li')
    })

    it('保留标题 h1-h5', () => {
      const out = sanitizeRichText('<h1>一</h1><h2>二</h2><h3>三</h3><h4>四</h4><h5>五</h5>')
      expect(out).toContain('<h1')
      expect(out).toContain('<h5')
    })

    it('保留加粗/斜体/下划线/删除线', () => {
      const out = sanitizeRichText('<b>粗</b><strong>粗</strong><i>斜</i><em>斜</em><u>下</u><s>删</s><strike>删</strike>')
      expect(out).toContain('<b>')
      expect(out).toContain('<strong>')
      expect(out).toContain('<i>')
      expect(out).toContain('<em>')
      expect(out).toContain('<u>')
    })

    it('保留段落与引用 p/blockquote/br', () => {
      const out = sanitizeRichText('<p>段</p><blockquote>引</blockquote><br>')
      expect(out).toContain('<p>')
      expect(out).toContain('<blockquote')
      expect(out).toContain('<br')
    })

    it('保留 span 的内联 style（字号/字体/颜色/对齐由 style 承载）', () => {
      const out = sanitizeRichText('<span style="color: red; font-size: 18px;">字</span>')
      expect(out).toContain('<span')
      expect(out).toContain('style=')
      expect(out).toContain('字')
    })

    it('保留 p 上的 text-align 对齐 style', () => {
      const out = sanitizeRichText('<p style="text-align: center;">居中</p>')
      expect(out).toContain('text-align')
    })
  })
})
