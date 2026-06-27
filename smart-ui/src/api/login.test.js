import { beforeEach, describe, expect, it, vi } from 'vitest'
import request from '@/router/axios'
import { loginByMobile, loginBySocial, loginByUsername, refreshToken } from './login'

vi.mock('@/router/axios', () => ({ default: vi.fn((config) => config) }))

beforeEach(() => {
  vi.clearAllMocks()
  delete window.__SMART_CONFIG__
  delete process.env.VUE_APP_LOGIN_TENANT_ID
  delete process.env.VUE_APP_LOGIN_AUTHORIZATION
})

describe('login api request signatures', () => {
  it('keeps the legacy username-login request signature by default', () => {
    loginByUsername('u', 'p', 'c', 'r')

    expect(request).toHaveBeenCalledWith({
      url: '/auth/oauth/token',
      headers: {
        isToken: false,
        TENANT_ID: '1',
        Authorization: 'Basic c21hcnQ6c21hcnQ='
      },
      method: 'post',
      params: { username: 'u', password: 'p', randomStr: 'r', code: 'c', grant_type: 'password', scope: 'server' }
    })
  })

  it('applies runtime login header overrides to every token request', () => {
    window.__SMART_CONFIG__ = {
      loginTenantId: 'tenant-runtime',
      loginAuthorization: 'Basic runtime-token'
    }

    refreshToken('refresh-token')
    loginByMobile('13800138000', '1234')
    loginBySocial('wechat', 'code')

    expect(request).toHaveBeenNthCalledWith(1, {
      url: '/auth/oauth/token',
      headers: {
        isToken: false,
        TENANT_ID: 'tenant-runtime',
        Authorization: 'Basic runtime-token'
      },
      method: 'post',
      params: { refresh_token: 'refresh-token', grant_type: 'refresh_token', scope: 'server' }
    })
    expect(request).toHaveBeenNthCalledWith(2, {
      url: '/auth/mobile/token/sms',
      headers: {
        TENANT_ID: 'tenant-runtime',
        Authorization: 'Basic runtime-token'
      },
      method: 'post',
      params: { mobile: 'SMS@13800138000', code: '1234', grant_type: 'mobile' }
    })
    expect(request).toHaveBeenNthCalledWith(3, {
      url: '/auth/mobile/token/social',
      headers: {
        TENANT_ID: 'tenant-runtime',
        Authorization: 'Basic runtime-token'
      },
      method: 'post',
      params: { mobile: 'wechat@code', grant_type: 'mobile' }
    })
  })
})
