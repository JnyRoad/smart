import { parentPort, workerData } from 'node:worker_threads';
import { renderEnvelope } from './envelope.mjs';
// 单次工作线程在输出后结束，主进程可以在超时后强制回收第三方渲染器。
try { parentPort.postMessage({ result: await renderEnvelope(workerData) }); }
catch (error) {
  if (error?.code !== 'RENDER_VALIDATION_FAILED') throw error;
  parentPort.postMessage({ error: { code: error.code, message: '模板渲染失败，请检查字段、资源与版面', details: error.details || [] } });
}
