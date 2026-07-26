// @vitest-environment jsdom
import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react'
import React from 'react'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import TruckBookingPage from './page'

const routerMock = vi.hoisted(() => ({ push: vi.fn() }))
const apiMock = vi.hoisted(() => ({
  getTruckCauseEnum: vi.fn(),
  saveTruckApply: vi.fn(),
  sendVisitorSms: vi.fn(),
  verifyTruckSms: vi.fn(),
}))

vi.mock('next/navigation', () => ({ useRouter: () => routerMock }))

vi.mock('@/features/visitor/api', () => apiMock)

vi.mock('@/lib/use-mounted', () => ({ useMounted: () => true }))

vi.mock('@/components/plate-input', () => ({
  PlateInput: ({ value, onChange }: { value: string; onChange: (value: string) => void }) => React.createElement('input', {
    placeholder: '请输入车牌号', value, onChange: (event: React.ChangeEvent<HTMLInputElement>) => onChange(event.target.value),
  }),
}))

vi.mock('@/components/sms-code-field', () => ({
  SMS_INPUT_CLASS: 'input',
  SmsCodeField: ({ phone, code, onPhoneChange, onCodeChange }: {
    phone: string
    code: string
    onPhoneChange: (value: string) => void
    onCodeChange: (value: string) => void
  }) => React.createElement(React.Fragment, null,
    React.createElement('input', {
      placeholder: '点击输入手机号', value: phone, onChange: (event: React.ChangeEvent<HTMLInputElement>) => onPhoneChange(event.target.value),
    }),
    React.createElement('input', {
      placeholder: '点击输入验证码', value: code, onChange: (event: React.ChangeEvent<HTMLInputElement>) => onCodeChange(event.target.value),
    }),
  ),
}))

vi.mock('antd-mobile', async () => {
  const ReactModule = await import('react')
  return {
    Toast: { show: vi.fn() },
    DatePicker: ({ visible, onConfirm }: { visible: boolean; onConfirm: (value: Date) => void }) => visible
      ? ReactModule.createElement('button', { type: 'button', onClick: () => onConfirm(new Date(2026, 0, 2, 3, 4)) }, '选择测试时间')
      : null,
    Picker: ({ visible, columns, onConfirm }: {
      visible: boolean
      columns: { label: string; value: string }[][]
      onConfirm: (value: string[]) => void
    }) => visible && columns[0]?.[0]
      ? ReactModule.createElement('button', { type: 'button', onClick: () => onConfirm([columns[0]![0]!.value]) }, columns[0]![0]!.label)
      : null,
  }
})

beforeEach(() => {
  apiMock.getTruckCauseEnum.mockReset()
  apiMock.saveTruckApply.mockReset()
  apiMock.sendVisitorSms.mockReset()
  apiMock.verifyTruckSms.mockReset()
  routerMock.push.mockReset()
})

afterEach(cleanup)

function fillSms() {
  fireEvent.change(screen.getByPlaceholderText('点击输入手机号'), { target: { value: '13900003333' } })
  fireEvent.change(screen.getByPlaceholderText('点击输入验证码'), { target: { value: '123456' } })
}

function fillApplication() {
  fireEvent.change(screen.getByPlaceholderText('请输入车牌号'), { target: { value: '豫B88888' } })
  fireEvent.change(screen.getByPlaceholderText('请输入访客姓名'), { target: { value: '货车司机' } })
  fireEvent.change(screen.getByPlaceholderText('请输入出发地'), { target: { value: '郑州仓库' } })
  fireEvent.click(screen.getByText('请选择预约时间'))
  fireEvent.click(screen.getByRole('button', { name: '选择测试时间' }))
}

describe('货车预约短信凭证失效恢复', () => {
  it('事由查询被服务端拒绝后，下一次选择会重新验证短信', async () => {
    apiMock.verifyTruckSms.mockResolvedValue({ code: 0, data: { proof: 'expired-proof' } })
    apiMock.getTruckCauseEnum.mockResolvedValue({ code: 1, message: '短信凭证已过期' })
    render(React.createElement(TruckBookingPage))
    fillSms()

    fireEvent.click(screen.getByText('请选择来访事由'))
    await waitFor(() => expect(apiMock.getTruckCauseEnum).toHaveBeenCalledTimes(1))

    fireEvent.click(screen.getByText('请选择来访事由'))
    await waitFor(() => expect(apiMock.verifyTruckSms).toHaveBeenCalledTimes(2))
  })

  it('提交被服务端拒绝后，下一次选择会重新验证短信', async () => {
    apiMock.verifyTruckSms.mockResolvedValue({ code: 0, data: { proof: 'expired-proof' } })
    apiMock.getTruckCauseEnum.mockResolvedValue({ code: 0, data: [{ code: 1, desc: '送货' }] })
    apiMock.saveTruckApply.mockResolvedValue({ code: 1, message: '短信凭证已过期' })
    render(React.createElement(TruckBookingPage))
    fillApplication()
    fillSms()

    fireEvent.click(screen.getByText('请选择来访事由'))
    await screen.findByRole('button', { name: '送货' })
    fireEvent.click(screen.getByRole('button', { name: '送货' }))
    fireEvent.click(screen.getByRole('button', { name: '提交申请' }))
    await waitFor(() => expect(apiMock.saveTruckApply).toHaveBeenCalledTimes(1))

    fireEvent.click(screen.getByText('请选择来访事由'))
    await waitFor(() => expect(apiMock.verifyTruckSms).toHaveBeenCalledTimes(2))
  })

  it('提交成功后也不保留已消费的短信凭证', async () => {
    apiMock.verifyTruckSms.mockResolvedValue({ code: 0, data: { proof: 'consumed-proof' } })
    apiMock.getTruckCauseEnum.mockResolvedValue({ code: 0, data: [{ code: 1, desc: '送货' }] })
    apiMock.saveTruckApply.mockResolvedValue({ code: 0 })
    render(React.createElement(TruckBookingPage))
    fillApplication()
    fillSms()

    fireEvent.click(screen.getByText('请选择来访事由'))
    fireEvent.click(await screen.findByRole('button', { name: '送货' }))
    fireEvent.click(screen.getByRole('button', { name: '提交申请' }))
    await waitFor(() => expect(routerMock.push).toHaveBeenCalledWith('/visitor/truck/result'))

    fireEvent.click(screen.getByText('请选择来访事由'))
    await waitFor(() => expect(apiMock.verifyTruckSms).toHaveBeenCalledTimes(2))
  })
})
