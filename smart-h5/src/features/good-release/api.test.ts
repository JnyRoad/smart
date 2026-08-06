import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { saveSession } from '@/lib/auth/token'
import { createOfficeReleaseDraft, getLiveRooms, getReleaseStaffInfo } from './api'

const fetchMock = vi.fn()

beforeEach(() => {
  localStorage.clear()
  fetchMock.mockReset()
	// 每次请求返回新的响应体，避免第二次读取已消费的 Response。
  fetchMock.mockImplementation(() => Promise.resolve(new Response(JSON.stringify({ code: 0, data: [] }))))
  vi.stubGlobal('fetch', fetchMock)
  saveSession({ accessToken: 'tok' })
})

afterEach(() => {
  vi.unstubAllGlobals()
})

describe('生活区物品放行本人房间接口', () => {
  it('does not put the employee badge in the room-list URL', async () => {
    await getLiveRooms()

    expect(fetchMock).toHaveBeenCalledOnce()
    const [url] = fetchMock.mock.calls[0]!
    expect(url).toBe('/platform/dormitory/staff/me/roomList')
  })
})

describe('办公区物品放行草稿人员查询', () => {
  it('先创建服务器草稿，再以草稿 ID 查询最小员工资料', async () => {
    await createOfficeReleaseDraft({ parkId: 1 })
    await getReleaseStaffInfo(17, 'A100')

    expect(fetchMock.mock.calls.map(([url]) => url)).toEqual([
      '/platform/articlesrelease/office/draft',
      '/platform/articlesrelease/17/staff/lookup?badge=A100',
    ])
    expect(fetchMock.mock.calls[0]?.[1]).toMatchObject({ method: 'POST' })
  })
})
