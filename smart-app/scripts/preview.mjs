/** 只在本机提供已构建Web产物，路径被限制在输出目录内。 */
import http from 'node:http'
import path from 'node:path'
import { readFile, stat } from 'node:fs/promises'
import { fileURLToPath } from 'node:url'
const root = fileURLToPath(new URL('../unpackage/dist/build/h5/', import.meta.url))
const mime = { '.html': 'text/html; charset=utf-8', '.js': 'text/javascript; charset=utf-8', '.css': 'text/css; charset=utf-8', '.png': 'image/png', '.svg': 'image/svg+xml', '.json': 'application/json' }
const server = http.createServer(async (req, res) => {
  try {
    const pathname = decodeURIComponent(new URL(req.url, 'http://localhost').pathname)
    const file = path.resolve(root, '.' + (pathname === '/' ? '/index.html' : pathname))
    if (!file.startsWith(root) || !(await stat(file)).isFile()) { res.writeHead(404).end(); return }
    res.writeHead(200, { 'Content-Type': mime[path.extname(file)] || 'application/octet-stream', 'Cache-Control': 'no-store' })
    res.end(await readFile(file))
  } catch { res.writeHead(404).end() }
})
server.listen(Number(process.env.PORT || 5179), '127.0.0.1', () => console.log('预览地址 http://127.0.0.1:' + server.address().port))
