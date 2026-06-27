import store from './store'

const REDACTED_CREDENTIAL = '[REDACTED_CREDENTIAL]'
const AUTHORIZATION_PAIR_RE = /["']?authorization["']?\s*[:=]\s*["']?[^,\]})]+["']?/gi
const CREDENTIAL_PAIR_RE = /["']?(?:access[_-]?token|refresh[_-]?token|token|password|secret)["']?\s*[:=]\s*["']?[^,\s\]})"']+["']?/gi
const AUTH_SCHEME_RE = /\b(?:Bearer|Basic)\s+[^,\s\]})"']+/gi

function redactSensitiveText(value) {
  return value
    .replace(AUTHORIZATION_PAIR_RE, REDACTED_CREDENTIAL)
    .replace(CREDENTIAL_PAIR_RE, REDACTED_CREDENTIAL)
    .replace(AUTH_SCHEME_RE, REDACTED_CREDENTIAL)
}

function getErrorMessage(error) {
  return redactSensitiveText(error instanceof Error ? error.message : String(error))
}

function getErrorStack(error) {
  return error instanceof Error && error.stack ? redactSensitiveText(error.stack) : undefined
}

export function reportCaughtError(error, info) {
  const logEntry = {
    type: 'error',
    message: getErrorMessage(error),
    stack: getErrorStack(error),
    info
  }

  console.error(`[${info}]`, logEntry)
  store.commit('ADD_LOGS', logEntry)

  if (error instanceof Error) {
    return error
  }

  return new Error(String(error))
}
