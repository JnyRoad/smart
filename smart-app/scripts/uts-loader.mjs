import { readFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import { registerHooks, stripTypeScriptTypes } from 'node:module'

/**
 * 为 Node 行为测试加载可擦除 TypeScript 语法的 UTS 文件。
 * 仅处理文件内容，不注入任何平台运行时对象。
 */
registerHooks({
	load(url, context, nextLoad) {
		if (!url.endsWith('.uts')) {
			return nextLoad(url, context)
		}

		const source = readFileSync(fileURLToPath(url), 'utf8')
		return {
			format: 'module',
			source: stripTypeScriptTypes(source, { mode: 'strip' }),
			shortCircuit: true,
		}
	},
})
