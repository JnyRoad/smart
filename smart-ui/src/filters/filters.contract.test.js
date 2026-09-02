import { afterEach, describe, expect, it, vi } from 'vitest'

// filters/index.js 经 '@/util/util' 间接 import '@/router/axios'，
// 后者会拉起 router / vuex / element-ui。这里把它 stub 掉，
// 本表征测试只关心 filters 的对外契约与纯函数输出。
vi.mock('@/router/axios', () => ({ default: {} }))

const filters = await import('./index')

// 拆分前 src/filters/index.js 的全部命名导出（24 个）。
// 这是「注册契约」：main.js 用 import * as filters 全量 Vue.filter 注册，
// 拆分后这份集合必须一字不差。
const EXPECTED_FILTER_NAMES = [
  'applyStatusClassFormat',
  'applyStatusFormat',
  'dateFormat',
  'dateFormat2',
  'deviceStatusClassFormat',
  'deviceStatusFormat',
  'entryExamineClassFormat',
  'entryExamineFormat',
  'formatNumber',
  'formatTime',
  'genderInit',
  'html2Text',
  'nFormatter',
  'parseTime',
  'parserDate',
  'recruitStatusClassFormat',
  'recruitStatusFormat',
  'roomGenderInit',
  'salaryStatusInit',
  'staffStatusInit',
  'timeAgo',
  'toThousandslsFilter',
  'visitorStatusClassFormat',
  'visitorStatusFormat'
]

describe('filters 注册契约', () => {
  it('导出 key 集合与拆分前完全一致（同名、不多不少）', () => {
    expect(Object.keys(filters).sort()).toStrictEqual([...EXPECTED_FILTER_NAMES].sort())
  })

  it('每个导出都是函数（可被 Vue.filter 注册）', () => {
    for (const name of EXPECTED_FILTER_NAMES) {
      expect(typeof filters[name], `${name} 应为 function`).toBe('function')
    }
  })

  it('无 default 导出（main.js 用 import * as 全量取命名导出）', () => {
    expect(filters.default).toBeUndefined()
  })
})

describe('visitorStatusFormat', () => {
  it('状态 7 显示为已作废，避免后台记录页出现空状态', () => {
    expect(filters.visitorStatusFormat(7)).toBe('已作废')
  })
})

// ---- spot-check：钉死现状真实返回值 ----
// 时间相关函数都用「本地时间」getter（getMonth/getDate/getHours...），
// 测试与实现同处一个时区，故用本地构造的 Date 断言不会随时区漂移。

describe('dateFormat（钉死现状）', () => {
  it('忽略入参 format，固定输出 yyyy-MM-dd hh:mm:ss', () => {
    const date = new Date(2026, 0, 5, 9, 8, 7, 45)
    expect(filters.dateFormat(date)).toBe('2026-01-05 09:08:07')
  })
})

describe('formatTime（跨调 parseTime，交叉引用关键点）', () => {
  afterEach(() => {
    vi.useRealTimers()
  })

  it('diff 分支：刚刚 / 分钟前 / 小时前 / 1天前', () => {
    // 固定 now 为某个本地时间，再用「秒级时间戳」回推，避开时区漂移。
    const now = new Date(2026, 5, 1, 12, 0, 0)
    vi.useFakeTimers()
    vi.setSystemTime(now)
    const sec = Math.floor(now.getTime() / 1000)

    expect(filters.formatTime(sec - 10)).toBe('刚刚')
    expect(filters.formatTime(sec - 120)).toBe('2分钟前')
    expect(filters.formatTime(sec - 7200)).toBe('2小时前')
    expect(filters.formatTime(sec - 3600 * 30)).toBe('1天前')
  })

  it('超过 2 天且带 option 时走 parseTime（交叉引用），按 option 格式化', () => {
    const now = new Date(2026, 5, 1, 12, 0, 0)
    vi.useFakeTimers()
    vi.setSystemTime(now)
    // 选一个固定的「秒级」时间戳：2023-11-14（本地）。距 now 远超 2 天，进入 option 分支。
    const sec = 1700000000
    // 同实现：formatTime 内部 time = +time*1000 再交给 parseTime，本地时区读取。
    const expected = filters.parseTime(sec, '{y}-{m}-{d}')
    expect(filters.formatTime(sec, '{y}-{m}-{d}')).toBe(expected)
    expect(expected).toMatch(/^\d{4}-\d{2}-\d{2}$/)
  })
})

describe('nFormatter（钉死现状）', () => {
  it('按千/百万缩写并保留指定小数', () => {
    expect(filters.nFormatter(1234, 1)).toBe('1.3k')
    expect(filters.nFormatter(1234567, 2)).toBe('1.33M')
  })

  it('小于 1000 原样 toString', () => {
    expect(filters.nFormatter(500, 1)).toBe('500')
  })
})

describe('toThousandslsFilter（钉死现状）', () => {
  it('给整数加千分位逗号，保留负号', () => {
    expect(filters.toThousandslsFilter(1234567)).toBe('1,234,567')
    expect(filters.toThousandslsFilter(-1234567)).toBe('-1,234,567')
  })

  it('非数字回退为 0', () => {
    expect(filters.toThousandslsFilter('abc')).toBe('0')
  })
})

describe('genderInit（status 类，钉死现状）', () => {
  it('0/1/2 映射 男/女/未知', () => {
    expect(filters.genderInit(0)).toBe('男')
    expect(filters.genderInit(1)).toBe('女')
    expect(filters.genderInit(2)).toBe('未知')
  })
})

describe('deviceStatusFormat（status 类，钉死现状）', () => {
  it('0/2/1 映射 未连接/接通/断开', () => {
    expect(filters.deviceStatusFormat(0)).toBe('未连接')
    expect(filters.deviceStatusFormat(2)).toBe('接通')
    expect(filters.deviceStatusFormat(1)).toBe('断开')
  })
})
