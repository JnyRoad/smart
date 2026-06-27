import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import store from './store'
import { reportCaughtError } from './error-reporter'

vi.mock('./store', () => ({ default: { commit: vi.fn() } }))

const REDACTED_CREDENTIAL = '[REDACTED_CREDENTIAL]'
let consoleError

beforeEach(() => {
  vi.clearAllMocks()
  consoleError = vi.spyOn(console, 'error').mockImplementation(() => {})
})

afterEach(() => {
  consoleError.mockRestore()
})

describe('reportCaughtError', () => {
  it('records Error instances and returns the same error for reject chaining', () => {
    const error = new Error('接口失败')
    const reportedError = reportCaughtError(error, 'axios:request')

    expect(reportedError).toBe(error)
    expect(consoleError).toHaveBeenCalledWith('[axios:request]', {
      type: 'error',
      message: '接口失败',
      stack: error.stack,
      info: 'axios:request'
    })
    expect(store.commit).toHaveBeenCalledWith('ADD_LOGS', {
      type: 'error',
      message: '接口失败',
      stack: error.stack,
      info: 'axios:request'
    })
  })

  it('records non-Error reasons without inventing a log stack', () => {
    const reportedError = reportCaughtError('cancel', 'unhandledrejection')

    expect(reportedError).toBeInstanceOf(Error)
    expect(reportedError.message).toBe('cancel')
    expect(store.commit).toHaveBeenCalledWith('ADD_LOGS', {
      type: 'error',
      message: 'cancel',
      stack: undefined,
      info: 'unhandledrejection'
    })
  })

  it('redacts credential fragments before writing console or vuex logs', () => {
    const error = new Error('request failed with Authorization: Bearer secret-token')
    error.stack = 'Error: failed\nAuthorization: Basic basic-secret\naccess_token=token-secret\n{"token":"json-token","password":"hunter2"}'
    error.config = {
      headers: {
        Authorization: 'Bearer secret-token'
      }
    }

    reportCaughtError(error, 'axios:request')

    const output = JSON.stringify([consoleError.mock.calls, store.commit.mock.calls])
    expect(output).not.toContain('secret-token')
    expect(output).not.toContain('basic-secret')
    expect(output).not.toContain('token-secret')
    expect(output).not.toContain('json-token')
    expect(output).not.toContain('hunter2')
    expect(output).not.toMatch(/authorization/i)
    expect(output).not.toMatch(/bearer/i)
    expect(output).not.toMatch(/basic/i)
    expect(output).not.toMatch(/access_token/i)
    expect(output).not.toMatch(/password/i)
    expect(output).not.toMatch(/secret/i)
    expect(output).toContain(REDACTED_CREDENTIAL)
    expect(store.commit).toHaveBeenCalledWith('ADD_LOGS', expect.objectContaining({
      type: 'error',
      message: expect.stringContaining(REDACTED_CREDENTIAL),
      stack: expect.stringContaining(REDACTED_CREDENTIAL),
      info: 'axios:request'
    }))
  })
})
