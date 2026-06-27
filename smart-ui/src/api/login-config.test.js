import { afterEach, describe, expect, it } from 'vitest'
import { createLoginHeaders, getLoginAuthorization, getLoginTenantId } from './login-config'

const originalTenantId = process.env.VUE_APP_LOGIN_TENANT_ID
const originalAuthorization = process.env.VUE_APP_LOGIN_AUTHORIZATION

function resetConfig() {
  if (originalTenantId === undefined) {
    delete process.env.VUE_APP_LOGIN_TENANT_ID
  } else {
    process.env.VUE_APP_LOGIN_TENANT_ID = originalTenantId
  }
  if (originalAuthorization === undefined) {
    delete process.env.VUE_APP_LOGIN_AUTHORIZATION
  } else {
    process.env.VUE_APP_LOGIN_AUTHORIZATION = originalAuthorization
  }
  delete window.__SMART_CONFIG__
}

afterEach(() => {
  resetConfig()
})

describe('login auth config', () => {
  it('keeps the legacy tenant and Basic authorization when no override is configured', () => {
    resetConfig()
    expect(getLoginTenantId()).toBe('1')
    expect(getLoginAuthorization()).toBe('Basic c21hcnQ6c21hcnQ=')
  })

  it('uses runtime config before env config', () => {
    process.env.VUE_APP_LOGIN_TENANT_ID = 'env-tenant'
    process.env.VUE_APP_LOGIN_AUTHORIZATION = 'Basic env-token'
    window.__SMART_CONFIG__ = {
      loginTenantId: 'runtime-tenant',
      loginAuthorization: 'Basic runtime-token'
    }

    expect(getLoginTenantId()).toBe('runtime-tenant')
    expect(getLoginAuthorization()).toBe('Basic runtime-token')
  })

  it('creates login headers without mutating caller-provided headers', () => {
    process.env.VUE_APP_LOGIN_TENANT_ID = 'tenant-from-env'
    process.env.VUE_APP_LOGIN_AUTHORIZATION = 'Basic env-token'
    const extraHeaders = { isToken: false }

    expect(createLoginHeaders(extraHeaders)).toEqual({
      isToken: false,
      TENANT_ID: 'tenant-from-env',
      Authorization: 'Basic env-token'
    })
    expect(extraHeaders).toEqual({ isToken: false })
  })
})
