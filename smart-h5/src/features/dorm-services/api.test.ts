import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { saveSession } from '@/lib/auth/token'
import { getMyRooms } from './api'

const fetchMock = vi.fn()

beforeEach(() => {
  localStorage.clear()
  fetchMock.mockReset()
  fetchMock.mockResolvedValue(new Response(JSON.stringify({ code: 0, data: [] })))
  vi.stubGlobal('fetch', fetchMock)
  saveSession({ accessToken: 'tok' })
})

afterEach(() => {
  vi.unstubAllGlobals()
})

describe('退宿本人房间接口', () => {
  it('does not put the employee badge in the room-list URL', async () => {
    await getMyRooms()

    expect(fetchMock).toHaveBeenCalledOnce()
    const [url] = fetchMock.mock.calls[0]!
    expect(url).toBe('/platform/dormitory/staff/me/roomList')
  })
})
