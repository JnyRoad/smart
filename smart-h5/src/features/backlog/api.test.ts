import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { saveSession } from '@/lib/auth/token'
import { getWorkApprovalPage, securityUpdateRelease } from './api'

const fetchMock = vi.fn()

function jsonResponse(body: unknown) {
  return new Response(JSON.stringify(body), {
    headers: { 'Content-Type': 'application/json' },
  })
}

beforeEach(() => {
  localStorage.clear()
  fetchMock.mockReset()
  fetchMock.mockResolvedValue(jsonResponse({ code: 0, data: { records: [], pages: 1 } }))
  vi.stubGlobal('fetch', fetchMock)
})

afterEach(() => {
  vi.unstubAllGlobals()
})

describe('backlog approval APIs', () => {
  it('uses the backend office approval endpoint for work-release approvals', async () => {
    saveSession({ accessToken: 'tok' })

    await getWorkApprovalPage({
      approvalStatus: 0,
      current: 1,
      size: 10,
      badge: 'YT9',
      releaseItem: 1,
    })

    const [url] = fetchMock.mock.calls[0] as [string, RequestInit]
    expect(url).toBe(
      '/platform/articlesrelease/office/approval/page?approvalStatus=0&current=1&size=10&badge=YT9&releaseItem=1',
    )
  })

	 it('sends only guard business fields for security confirmation', async () => {
		 saveSession({ accessToken: 'tok' })

		 await securityUpdateRelease({
			 id: 17,
			 status: 4,
			 guardOneImg: 'image-1',
			 guardTwoImg: '',
			 guardThreeImg: '',
			 remark: '已核验',
		 })

		 const [url, init] = fetchMock.mock.calls[0] as [string, RequestInit]
		 expect(url).toBe('/platform/articlesrelease/status/security/update')
		 expect(JSON.parse(String(init.body))).toEqual({
			 id: 17,
			 status: 4,
			 guardOneImg: 'image-1',
			 guardTwoImg: '',
			 guardThreeImg: '',
			 remark: '已核验',
		 })
	 })
})
