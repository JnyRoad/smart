import { request } from '@/lib/api/http'

interface Envelope<T> {
  code: number
  data?: T
  message?: string
}

export interface HelpQuestion {
  questionId?: number | string
  questionTitle?: string
}

/** Pagination goes in the query string (the legacy page put it in a GET body — recorded defect). */
export function getHelpQuestions(params: {
  current: number
  size: number
}): Promise<Envelope<{ records?: HelpQuestion[]; pages?: number }>> {
  return request({ module: 'app', url: '/guide/help/question/list', params })
}

export function getHelpAnswer(
  id: string,
): Promise<Envelope<{ answerContent?: string; questionTitle?: string }>> {
  return request({ module: 'app', url: `/guide/help/question/answer/${id}` })
}
