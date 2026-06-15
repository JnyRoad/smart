import createDOMPurify from 'dompurify'

/**
 * 富文本白名单（纵深防御）。
 *
 * 三类富文本——公告 bbsContent、帮助 answerContent、访客须知 content——
 * 都由后台管理端（smart-ui）的 wangEditor v3 编写，菜单固定为：
 * head / bold / fontSize / fontName / italic / underline / strikeThrough /
 * foreColor / backColor / link / list / justify / quote / emoticon / image / table。
 *
 * 这些菜单产出的标签集即下方白名单：标题、段落、加粗斜体下划线删除线、
 * 链接、图片、列表、表格、引用；字号/字体/颜色/对齐均由内联 style 承载，
 * 故保留 style 属性。宁可略宽（如多列出 thead/caption）也不漏掉现有内容标签，
 * 重点是不能误删 img / a / table。
 *
 * DOMPurify 默认已能挡常规 XSS；显式收紧白名单只是再加一道防线，
 * 自动清除脚本、内联事件属性（onerror/onclick…）与 javascript: 协议。
 */
const ALLOWED_TAGS = [
  // 标题（wangEditor head 产出 h1-h5）
  'h1',
  'h2',
  'h3',
  'h4',
  'h5',
  'h6',
  // 段落与换行
  'p',
  'br',
  'div',
  'span',
  // 加粗 / 斜体 / 下划线 / 删除线（兼容 b/strong、i/em、s/strike 多种产出）
  'b',
  'strong',
  'i',
  'em',
  'u',
  's',
  'strike',
  'sub',
  'sup',
  // 引用
  'blockquote',
  // 链接与图片
  'a',
  'img',
  // 列表
  'ul',
  'ol',
  'li',
  // 表格
  'table',
  'thead',
  'tbody',
  'tfoot',
  'tr',
  'td',
  'th',
  'caption',
  'colgroup',
  'col',
] as const

const ALLOWED_ATTR = [
  // 链接
  'href',
  'target',
  'rel',
  'title',
  // 图片
  'src',
  'alt',
  'width',
  'height',
  // 字号 / 字体 / 颜色 / 对齐全部走内联 style
  'style',
  'class',
  // 表格结构
  'colspan',
  'rowspan',
] as const

let browserPurifier: ReturnType<typeof createDOMPurify> | null = null

function getBrowserPurifier(): ReturnType<typeof createDOMPurify> | null {
  if (typeof window === 'undefined') return null

  if (!browserPurifier) {
    browserPurifier = createDOMPurify(window)

    // wangEditor 的链接固定带 target="_blank" 但不带 rel；给这类链接补上
    // rel="noopener noreferrer"，堵住反向 tabnabbing（新开页通过 window.opener 反控原页）。
    browserPurifier.addHook('afterSanitizeAttributes', (node) => {
      if (node instanceof window.Element && node.getAttribute('target') === '_blank') {
        node.setAttribute('rel', 'noopener noreferrer')
      }
    })
  }

  return browserPurifier
}

/** 用统一白名单清洗富文本，返回安全 HTML 字符串。 */
export function sanitizeRichText(html: string): string {
  const purifier = getBrowserPurifier()
  // SSR / prerender 阶段没有浏览器 DOM，不能输出未清洗的 HTML；客户端挂载后再渲染清洗结果。
  if (!purifier) return ''

  return purifier.sanitize(html, {
    ALLOWED_TAGS: [...ALLOWED_TAGS],
    ALLOWED_ATTR: [...ALLOWED_ATTR],
  })
}
