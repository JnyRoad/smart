/** 核对服务端返回的PDF字节，避免把错误页或损坏制品交给浏览器预览。 */
export async function verifyPreviewArtifact(blob, artifact) {
  const bytes = await blob.arrayBuffer()
  if (bytes.byteLength !== artifact.bytes || bytes.byteLength > 32 * 1024 * 1024 || new TextDecoder().decode(bytes.slice(0, 5)) !== '%PDF-') throw new Error('预览文件校验失败，请重新生成')
  if (!window.crypto.subtle) throw new Error('当前页面无法校验预览文件，请使用 HTTPS 或本机地址打开系统')
  const digest = await window.crypto.subtle.digest('SHA-256', bytes)
  const hash = 'sha256:' + Array.from(new Uint8Array(digest), byte => byte.toString(16).padStart(2, '0')).join('')
  if (hash !== artifact.sha256) throw new Error('预览文件校验失败，请重新生成')
  return new Blob([bytes], { type: 'application/pdf' })
}
