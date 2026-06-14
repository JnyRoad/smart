import { fireEvent, render, screen } from '@testing-library/react'
import React from 'react'
import { describe, expect, it, vi } from 'vitest'
import { StaffFilterPopup } from './staff-filter-popup'

vi.mock('antd-mobile', async () => {
  const ReactModule = await import('react')

  return {
    Popup: ({ visible, children }: { visible: boolean; children: React.ReactNode }) =>
      visible ? ReactModule.createElement('div', null, children) : null,
    Picker: () => null,
    DatePicker: ({
      visible,
      onClose,
      onConfirm,
    }: {
      visible: boolean
      onClose: () => void
      onConfirm: (date: Date) => void
    }) =>
      visible
        ? ReactModule.createElement(
            'button',
            {
              type: 'button',
              onClick: () => {
                onConfirm(new Date(2026, 5, 13, 9, 7, 0))
                onClose()
              },
            },
            'confirm date',
          )
        : null,
  }
})

describe('StaffFilterPopup', () => {
  it('formats approval search times with seconds for backend hh24:mi:ss filters', () => {
    const onSearch = vi.fn()

    render(React.createElement(StaffFilterPopup, { visible: true, onClose: vi.fn(), onSearch, withTimeRange: true }))
    fireEvent.click(screen.getByRole('button', { name: '申请开始时间' }))
    fireEvent.click(screen.getByRole('button', { name: 'confirm date' }))
    fireEvent.click(screen.getByRole('button', { name: '确定' }))

    expect(onSearch).toHaveBeenCalledWith({ startTime: '2026-06-13 09:07:00' })
  })
})
