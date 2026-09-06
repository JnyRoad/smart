import { createServer } from 'node:http';
import { createHash, timingSafeEqual } from 'node:crypto';
import { Worker } from 'node:worker_threads';
import { pathToFileURL } from 'node:url';
const digest = value => createHash('sha256').update(value).digest();
/** 私有服务仅接受运行时服务凭据；限制上传并发、内存体积和渲染时间，无浏览器CORS。 */
export function createRenderServer({ token, maxBodyBytes = 52 * 1024 * 1024, concurrency = 2, renderTimeoutMs = 30000 } = {}) {
  if (typeof token !== 'string' || token.length < 32) throw new Error('必须配置至少32字符的运行时渲染服务凭据');
  if (![maxBodyBytes, concurrency, renderTimeoutMs].every(value => Number.isSafeInteger(value) && value > 0)) throw new Error('渲染服务限制配置无效');
  let active = 0;
  const server = createServer(async (req, res) => {
    const send = (status, body) => { if (!res.destroyed) { res.writeHead(status, { 'Content-Type': 'application/json; charset=utf-8', 'Cache-Control': 'no-store', 'X-Content-Type-Options': 'nosniff', Connection: 'close' }); res.end(JSON.stringify(body)); } };
    const reject = (status, code) => send(status, { error: { code, message: '渲染请求被拒绝', retryable: status === 503 } });
    if (req.url !== '/internal/print-renderer/v1/render' || req.method !== 'POST') return reject(404, 'NOT_FOUND');
    if (!timingSafeEqual(digest(req.headers.authorization || ''), digest(`Bearer ${token}`))) return reject(401, 'RENDERER_IDENTITY_REQUIRED');
    if (!(req.headers['content-type'] || '').startsWith('application/json')) return reject(415, 'CONTENT_TYPE_INVALID');
    if (active >= concurrency) return reject(503, 'RENDERER_BUSY');
    if (Number(req.headers['content-length']) > maxBodyBytes) return reject(413, 'PAYLOAD_LIMIT_EXCEEDED');
    active++;
    try {
      let size = 0; const chunks = [];
      for await (const chunk of req) { size += chunk.length; if (size > maxBodyBytes) return reject(413, 'PAYLOAD_LIMIT_EXCEEDED'); chunks.push(chunk); }
      let body; try { body = JSON.parse(Buffer.concat(chunks).toString('utf8')); } catch { return reject(400, 'JSON_INVALID'); }
      const message = await runWorker(body, renderTimeoutMs, res);
      if (message.error) send(422, message); else send(200, message.result);
    } catch { reject(503, 'RENDERER_UNAVAILABLE'); }
    finally { active--; }
  });
  server.requestTimeout = 15000;
  server.headersTimeout = 10000;
  return server;
}
/** 渲染超时或请求方离开时终止线程，释放并发名额；不记录请求正文或服务凭据。 */
function runWorker(body, timeoutMs, response) {
  return new Promise((resolve, reject) => {
    // 内联模块入口的 input-type 只适用于字符串，不能继承给文件 Worker；保留其余运行时参数。
    const hasInputType = process.execArgv.some(arg => arg === '--input-type' || arg.startsWith('--input-type='));
    const entryOptions = hasInputType ? { execArgv: process.execArgv.filter((arg, index, args) => arg !== '--input-type' && !arg.startsWith('--input-type=') && args[index - 1] !== '--input-type') } : {};
    const worker = new Worker(new URL('./render-worker.mjs', import.meta.url), { ...entryOptions, workerData: body, resourceLimits: { maxOldGenerationSizeMb: 256 } });
    let finished = false;
    const finish = (error, value) => { if (finished) return; finished = true; clearTimeout(timer); response.off('close', closed); worker.terminate(); if (error) reject(error); else resolve(value); };
    const closed = () => finish(new Error('请求已关闭'));
    const timer = setTimeout(() => finish(new Error('渲染超时')), timeoutMs);
    response.on('close', closed); worker.once('message', value => finish(null, value)); worker.once('error', error => finish(error)); worker.once('exit', code => { if (!finished) finish(new Error(`渲染进程结束 ${code}`)); });
  });
}
if (process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) {
  const port = Number(process.env.PRINT_RENDERER_PORT || 18764);
  createRenderServer({ token: process.env.PRINT_RENDERER_TOKEN }).listen(port, process.env.PRINT_RENDERER_HOST || '127.0.0.1');
}
