const RECORD_ID_PATTERN = /^[1-9]\d{0,18}$/
const LONG_MAX_VALUE = '9223372036854775807'
const PNG_SIGNATURE = [137, 80, 78, 71, 13, 10, 26, 10]

export class RecordBadgePrintError extends Error {
  constructor(message) {
    super(message)
    this.name = 'RecordBadgePrintError'
  }
}

export function validateRecordId(value) {
  if (typeof value !== 'string') {
    throw new RecordBadgePrintError('访客记录ID必须由服务端以字符串返回，无法安全打印')
  }
  if (!RECORD_ID_PATTERN.test(value)) {
    throw new RecordBadgePrintError('访客记录ID必须是1至19位十进制正整数字符串')
  }
  if (value.length === LONG_MAX_VALUE.length && value > LONG_MAX_VALUE) {
    throw new RecordBadgePrintError('访客记录ID超出Long范围')
  }
  return value
}

export function validatePngBase64(value, atobFn) {
  if (
    typeof value !== 'string' ||
    value.length === 0 ||
    value.length % 4 !== 0 ||
    !/^[A-Za-z0-9+/]+={0,2}$/.test(value)
  ) {
    throw new RecordBadgePrintError('记录二维码必须是有效的PNG Base64字符串')
  }

  let binary
  try {
    binary = atobFn(value)
  } catch (error) {
    throw new RecordBadgePrintError('记录二维码必须是有效的PNG Base64字符串')
  }
  if (
    binary.length < PNG_SIGNATURE.length ||
    PNG_SIGNATURE.some((byte, index) => binary.charCodeAt(index) !== byte)
  ) {
    throw new RecordBadgePrintError('记录二维码必须是有效的PNG Base64字符串')
  }
  return value
}

function textOrEmpty(value) {
  if (value === null || value === undefined) return ''
  return String(value).trim()
}

function photoUrl(value) {
  const url = textOrEmpty(value)
  if (!url) return ''
  if (/^(?:https?:|blob:|\/)/i.test(url)) return url
  if (/^data:image\/(?:png|jpe?g|gif);base64,[A-Za-z0-9+/]+={0,2}$/i.test(url)) return url
  return ''
}

export function buildAuthorizedAreaText(visitor, newAreaTypes, oldAreaTypes) {
  const source = visitor || {}
  const codes = Array.isArray(source.areaType) ? source.areaType : []
  const descriptions = (types, code) => {
    if (!Array.isArray(types)) return ''
    const match = types.find(item => item && String(item.code) === String(code))
    return match ? textOrEmpty(match.desc) : ''
  }
  const newAreas = codes.map(code => descriptions(newAreaTypes, code)).filter(Boolean)
  const oldAreas = codes.map(code => descriptions(oldAreaTypes, code)).filter(Boolean)
  const groups = []
  if (newAreas.length > 0) groups.push(`新工厂：${newAreas.join('、')}`)
  if (oldAreas.length > 0) groups.push(`老工厂：${oldAreas.join('、')}`)
  return groups.length > 0 ? groups.join('；') : textOrEmpty(source.permitArea)
}

/** 接口缺省或非数组的随行人员字段一律按空列表处理，避免打印页持有不可信响应类型。 */
export function normalizeFellowVisitorList(value) {
  return Array.isArray(value) ? value : []
}

/** 二维码厂牌只允许为有效访客记录生成，失效或缺少状态的记录必须拒绝。 */
export function isActiveVisitorRecord(value) {
  return value != null && value.delFlag === 0
}

export function buildBadgeEntries(visitor, members, atobFn) {
  if (!Array.isArray(members) || members.length === 0) {
    throw new RecordBadgePrintError('当前没有可打印的访客人员')
  }
  if (typeof atobFn !== 'function') {
    throw new RecordBadgePrintError('当前浏览器无法校验记录二维码')
  }

  return members.map((member, index) => {
    let recordId
    try {
      recordId = validateRecordId(member && member.id)
    } catch (error) {
      throw new RecordBadgePrintError(`第${index + 1}位访客缺少有效记录ID：${error.message}`)
    }

    let qrCode
    try {
      qrCode = validatePngBase64(member && member.recordQrCode, atobFn)
    } catch (error) {
      throw new RecordBadgePrintError(`第${index + 1}位访客缺少有效记录二维码：${error.message}`)
    }

    return {
      recordId,
      qrDataUrl: `data:image/png;base64,${qrCode}`,
      fellowName: textOrEmpty(member.fellowName),
      fellowPhotoIdUrl: photoUrl(member.fellowPhotoIdUrl),
      company: textOrEmpty(visitor && visitor.company),
      parkName: textOrEmpty(visitor && visitor.parkName),
      receptionistName: textOrEmpty(visitor && visitor.receptionistName),
      startTime: textOrEmpty(visitor && visitor.startTime),
      endTime: textOrEmpty(visitor && visitor.endTime),
      authorizedArea: textOrEmpty(visitor && visitor.authorizedArea)
    }
  })
}

function appendTextRow(document, parent, label, value, emptyText = '暂无') {
  const row = document.createElement('div')
  row.className = 'badge-row'
  const name = document.createElement('strong')
  name.textContent = label
  const content = document.createElement('span')
  content.textContent = value || emptyText
  row.append(name, content)
  parent.appendChild(row)
  return content
}

function appendStyle(document) {
  const style = document.createElement('style')
  style.textContent = `
    @page { size: 80mm 62mm; margin: 0; }
    * { box-sizing: border-box; }
    body { margin: 0; color: #111; background: #eceff1; font-family: Arial, "Microsoft YaHei", sans-serif; }
    .print-toolbar { position: sticky; top: 0; z-index: 2; display: flex; align-items: center; gap: 12px; padding: 10px 16px; background: #fff; border-bottom: 1px solid #d8dde3; }
    .print-toolbar button { padding: 8px 18px; border: 0; border-radius: 4px; color: #fff; background: #d76500; cursor: pointer; }
    .print-toolbar button:disabled { color: #777; background: #d7d7d7; cursor: not-allowed; }
    .print-status { font-size: 13px; color: #555; }
    .badge-page { display: grid; grid-template-rows: 6mm 27mm 19mm 4mm; width: 80mm; height: 62mm; margin: 10px auto; padding: 3mm 4mm; overflow: hidden; background: #fff; break-after: page; page-break-after: always; }
    .badge-page:last-child { break-after: auto; page-break-after: auto; }
    .badge-header { display: flex; align-items: baseline; justify-content: space-between; min-width: 0; border-bottom: 0.35mm solid #222; }
    .badge-header h1 { margin: 0; font-size: 15px; letter-spacing: 1px; white-space: nowrap; }
    .badge-record { display: block; min-width: 0; max-height: 4mm; overflow: hidden; font-size: 8px; white-space: nowrap; }
    .badge-top { display: grid; grid-template-columns: 18mm minmax(0, 1fr) 25mm; gap: 2mm; min-height: 0; padding-top: 1mm; }
    .badge-photo { display: flex; align-items: center; justify-content: center; width: 18mm; height: 22mm; overflow: hidden; border: 0.3mm solid #555; font-size: 9px; color: #777; text-align: center; }
    .badge-photo img { width: 100%; height: 100%; object-fit: cover; }
    .badge-identity { min-width: 0; padding-top: 1mm; font-size: 9px; line-height: 1.3; }
    .badge-row { display: grid; grid-template-columns: 7mm minmax(0, 1fr); gap: 1mm; min-height: 0; overflow-wrap: anywhere; }
    .badge-row strong { font-weight: 700; }
    .badge-row span { display: block; min-width: 0; overflow: hidden; }
    .badge-identity .badge-row { margin-bottom: 2mm; }
    .badge-identity .badge-row span { max-height: 8mm; }
    .badge-qr { display: flex; flex-direction: column; align-items: center; justify-content: flex-start; min-width: 0; }
    .badge-qr img { display: block; width: 25mm; height: 22mm; object-fit: contain; image-rendering: pixelated; }
    .badge-qr span { display: block; width: 25mm; max-height: 3mm; overflow: hidden; font-size: 7px; line-height: 1.2; white-space: nowrap; text-align: center; }
    .badge-bottom { display: grid; grid-template-columns: minmax(0, 1fr) minmax(0, 1fr); grid-template-rows: 5mm 5mm 8mm; gap: 0.5mm 2mm; min-height: 0; font-size: 8px; line-height: 1.2; }
    .badge-bottom .badge-row { grid-template-columns: 8mm minmax(0, 1fr); }
    .badge-bottom .badge-row-wide { grid-column: 1 / -1; }
    .badge-bottom .badge-row span { max-height: 100%; }
    .badge-notice { align-self: end; min-width: 0; max-height: 3mm; overflow: hidden; font-size: 7px; color: #555; line-height: 1.2; white-space: nowrap; text-align: center; }
    .badge-overflow { outline: 0.4mm solid #c62828; outline-offset: -0.4mm; }
    @media print {
      body { background: #fff; }
      .print-toolbar { display: none; }
      .badge-page { margin: 0; }
    }
  `
  document.head.appendChild(style)
}

export function createRecordBadgePreview(browserWindow, visitor, members) {
  if (!browserWindow || typeof browserWindow.open !== 'function') {
    throw new RecordBadgePrintError('当前浏览器不支持厂牌预览窗口')
  }
  const atobFn = typeof browserWindow.atob === 'function' ? browserWindow.atob.bind(browserWindow) : null
  const entries = buildBadgeEntries(visitor, members, atobFn)
  const popup = browserWindow.open('', '_blank')
  if (!popup) {
    throw new RecordBadgePrintError('厂牌预览窗口被浏览器拦截，请允许弹出窗口后重试')
  }
  try {
    popup.opener = null
  } catch (error) {
    // 某些旧浏览器不允许修改opener，不影响当前预览。
  }

  const document = popup.document
  document.title = '二维码厂牌预览（80×62mm）'
  const meta = document.createElement('meta')
  meta.setAttribute('charset', 'utf-8')
  document.head.appendChild(meta)
  appendStyle(document)

  const toolbar = document.createElement('div')
  toolbar.className = 'print-toolbar'
  const printButton = document.createElement('button')
  printButton.setAttribute('type', 'button')
  printButton.textContent = '打印 / 保存为PDF'
  printButton.disabled = true
  const status = document.createElement('span')
  status.className = 'print-status'
  status.textContent = '正在加载厂牌图片…'
  toolbar.append(printButton, status)
  document.body.appendChild(toolbar)

  let pendingImages = 0
  let qrLoadFailed = false
  const printLayouts = []
  const isOverflowing = element => {
    if (!element) return false
    const vertical = element.clientHeight > 0 && element.scrollHeight > element.clientHeight + 1
    const horizontal = element.clientWidth > 0 && element.scrollWidth > element.clientWidth + 1
    return vertical || horizontal
  }
  const overflowingPageIndex = () => {
    for (let index = 0; index < printLayouts.length; index += 1) {
      const layout = printLayouts[index]
      const overflowingField = layout.fields.find(isOverflowing)
      if (isOverflowing(layout.page) || overflowingField) {
        const target = overflowingField || layout.page
        target.className = `${target.className} badge-overflow`.trim()
        return index
      }
    }
    return -1
  }
  const updatePrintState = () => {
    if (pendingImages > 0) return
    const overflowIndex = overflowingPageIndex()
    if (overflowIndex >= 0) {
      printButton.disabled = true
      status.textContent = `第${overflowIndex + 1}张厂牌内容超出80×62mm，已阻止打印，请缩短展示字段后重试`
      return
    }
    printButton.disabled = qrLoadFailed
    status.textContent = qrLoadFailed
      ? '二维码图片加载失败，无法打印，请关闭窗口后重试'
      : `已加载${entries.length}张厂牌，请确认纸张尺寸为80×62mm后打印`
  }
  const trackImage = (image, required, onLoad, onError) => {
    pendingImages += 1
    let settled = false
    const settle = loaded => {
      if (settled) return
      settled = true
      pendingImages -= 1
      if (!loaded && required) qrLoadFailed = true
      if (loaded && onLoad) onLoad()
      if (!loaded && onError) onError()
      updatePrintState()
    }
    image.addEventListener('load', () => settle(true))
    image.addEventListener('error', () => settle(false))
  }

  entries.forEach(entry => {
    const page = document.createElement('article')
    page.className = 'badge-page'
    const boundedFields = []

    const header = document.createElement('header')
    header.className = 'badge-header'
    const title = document.createElement('h1')
    title.textContent = '访客二维码厂牌'
    const record = document.createElement('span')
    record.className = 'badge-record'
    record.textContent = `记录ID：${entry.recordId}`
    boundedFields.push(record)
    header.append(title, record)
    page.appendChild(header)

    const top = document.createElement('div')
    top.className = 'badge-top'
    const photo = document.createElement('div')
    photo.className = 'badge-photo'
    const photoState = document.createElement('span')
    photoState.textContent = entry.fellowPhotoIdUrl ? '照片加载中…' : '暂无照片'
    photo.appendChild(photoState)
    if (entry.fellowPhotoIdUrl) {
      const photoImage = document.createElement('img')
      photoImage.setAttribute('alt', `${entry.fellowName || '访客'}照片`)
      trackImage(
        photoImage,
        false,
        () => {
          photoState.textContent = ''
        },
        () => {
          photoState.textContent = '照片加载失败'
        }
      )
      photoImage.src = entry.fellowPhotoIdUrl
      photo.appendChild(photoImage)
    }
    top.appendChild(photo)

    const identity = document.createElement('div')
    identity.className = 'badge-identity'
    boundedFields.push(appendTextRow(document, identity, '姓名', entry.fellowName))
    boundedFields.push(appendTextRow(document, identity, '公司', entry.company))
    top.appendChild(identity)

    const qr = document.createElement('div')
    qr.className = 'badge-qr'
    const qrImage = document.createElement('img')
    qrImage.setAttribute('alt', `访客记录ID ${entry.recordId} 二维码`)
    trackImage(qrImage, true)
    qrImage.src = entry.qrDataUrl
    const qrId = document.createElement('span')
    qrId.textContent = entry.recordId
    boundedFields.push(qrId)
    qr.append(qrImage, qrId)
    top.appendChild(qr)
    page.appendChild(top)

    const bottom = document.createElement('div')
    bottom.className = 'badge-bottom'
    boundedFields.push(appendTextRow(document, bottom, '园区', entry.parkName))
    boundedFields.push(appendTextRow(document, bottom, '被访人', entry.receptionistName))
    const validity = appendTextRow(
      document,
      bottom,
      '期限',
      entry.startTime && entry.endTime ? `${entry.startTime} 至 ${entry.endTime}` : ''
    )
    validity.parentNode.className = `${validity.parentNode.className} badge-row-wide`
    boundedFields.push(validity)
    const area = appendTextRow(document, bottom, '区域', entry.authorizedArea)
    area.parentNode.className = `${area.parentNode.className} badge-row-wide`
    boundedFields.push(area)
    page.appendChild(bottom)

    const notice = document.createElement('div')
    notice.className = 'badge-notice'
    notice.textContent = '授权详情以扫码后的当前资格核验结果为准'
    boundedFields.push(notice)
    page.appendChild(notice)
    document.body.appendChild(page)
    printLayouts.push({ page, fields: boundedFields })
  })

  printButton.addEventListener('click', () => {
    if (printButton.disabled) return
    popup.focus()
    popup.print()
  })
  updatePrintState()
  return popup
}
