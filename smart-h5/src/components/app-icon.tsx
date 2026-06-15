import Image from 'next/image'
import type { CSSProperties } from 'react'

export type AppIconName =
  | 'approval'
  | 'checkIn'
  | 'dorm'
  | 'exit'
  | 'fallback'
  | 'help'
  | 'location'
  | 'lock'
  | 'release'
  | 'repair'
  | 'returnFactory'
  | 'scan'
  | 'waterElec'
  | 'wechat'
  | 'workRelease'

export type StatusIconName =
  | 'close'
  | 'denied'
  | 'delete'
  | 'edit'
  | 'expired'
  | 'failure'
  | 'pending'
  | 'search'
  | 'success'
  | 'user'

type AppTileIconProps = {
  name: AppIconName
  imageSrc?: string
  className?: string
}

type StatusIconProps = {
  name: StatusIconName
  className?: string
}

type AppIconTone = {
  bg: string
  fg: string
  accent: string
}

type AppIconStyle = CSSProperties & {
  '--app-icon-bg': string
  '--app-icon-fg': string
  '--app-icon-accent': string
}

const ICON_TONES: Record<AppIconName, AppIconTone> = {
  approval: { bg: '#fff1e3', fg: '#ec6c00', accent: '#d83b36' },
  checkIn: { bg: '#fff4df', fg: '#d95f00', accent: '#f5b84b' },
  dorm: { bg: '#fff4df', fg: '#c98416', accent: '#f5b84b' },
  exit: { bg: '#fff1e3', fg: '#d95f00', accent: '#d83b36' },
  fallback: { bg: '#fff1e3', fg: '#ec6c00', accent: '#f5b84b' },
  help: { bg: '#eef5ff', fg: '#2376d9', accent: '#f5b84b' },
  location: { bg: '#fff1e3', fg: '#ec6c00', accent: '#d83b36' },
  lock: { bg: '#fff4df', fg: '#d95f00', accent: '#4f8cff' },
  release: { bg: '#fff1e3', fg: '#ec6c00', accent: '#4f8cff' },
  repair: { bg: '#eef5ff', fg: '#2376d9', accent: '#8c8a8a' },
  returnFactory: { bg: '#ecf8f3', fg: '#16a673', accent: '#ec6c00' },
  scan: { bg: '#f1f3f5', fg: '#595757', accent: '#ec6c00' },
  waterElec: { bg: '#eef5ff', fg: '#2376d9', accent: '#16a673' },
  wechat: { bg: '#ecf8f3', fg: '#16a673', accent: '#2376d9' },
  workRelease: { bg: '#fff1e3', fg: '#d95f00', accent: '#4f8cff' },
}

function iconStyle(name: AppIconName): AppIconStyle {
  const tone = ICON_TONES[name]
  return {
    '--app-icon-bg': tone.bg,
    '--app-icon-fg': tone.fg,
    '--app-icon-accent': tone.accent,
  }
}

export function NoticeIcon() {
  return (
    <span
      aria-hidden
      className="grid h-7 w-7 flex-none place-items-center rounded-full bg-accent-soft text-brand"
    >
      <svg viewBox="0 0 24 24" className="h-[17px] w-[17px]" fill="none">
        <path
          d="M5.5 13.3v-2.6l8.8-4.1v10.8L5.5 13.3Z"
          fill="currentColor"
          opacity="0.2"
        />
        <path
          d="M5.5 10.7v2.6m0-2.6 8.8-4.1v10.8l-8.8-4.1m0-2.6H4.1a1.6 1.6 0 0 0 0 3.2h1.4m10.8-5.6a4.1 4.1 0 0 1 0 7.4M7.8 14.3l1 3"
          stroke="currentColor"
          strokeWidth="1.8"
          strokeLinecap="round"
          strokeLinejoin="round"
        />
      </svg>
    </span>
  )
}

export function AppTileIcon({ name, imageSrc, className = '' }: AppTileIconProps) {
  const fallbackImageSrc = name === 'fallback' ? imageSrc : undefined

  return (
    <span
      aria-hidden
      data-app-tile-icon={name}
      className={`grid h-12 w-12 flex-none place-items-center overflow-hidden rounded-[18px] bg-[var(--app-icon-bg)] text-[var(--app-icon-fg)] shadow-[inset_0_1px_0_rgba(255,255,255,0.65)] ${className}`}
      style={iconStyle(name)}
    >
      {fallbackImageSrc ? (
        <Image
          src={fallbackImageSrc}
          alt=""
          width={36}
          height={36}
          unoptimized
          className="h-9 w-9 object-contain"
        />
      ) : (
        <AppIconSvg name={name} />
      )}
    </span>
  )
}

export function StatusIcon({ name, className = 'h-5 w-5' }: StatusIconProps) {
  return (
    <span aria-hidden data-status-icon={name} className={`inline-grid place-items-center ${className}`}>
      <StatusIconSvg name={name} />
    </span>
  )
}

function AppIconSvg({ name }: { name: AppIconName }) {
  switch (name) {
    case 'approval':
      return (
        <svg viewBox="0 0 32 32" className="h-7 w-7" fill="none">
          <rect x="8" y="5" width="16" height="22" rx="4" fill="currentColor" opacity="0.16" />
          <path d="M12 12h8M12 17h5M12 22h4" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
          <path d="m20 22 2 2 4-5" stroke="var(--app-icon-accent)" strokeWidth="2.4" strokeLinecap="round" strokeLinejoin="round" />
        </svg>
      )
    case 'checkIn':
      return (
        <svg viewBox="0 0 32 32" className="h-7 w-7" fill="none">
          <path d="M7 15h18a3 3 0 0 1 3 3v6H4v-6a3 3 0 0 1 3-3Z" fill="currentColor" opacity="0.18" />
          <path d="M8 15V9.5A2.5 2.5 0 0 1 10.5 7h11A2.5 2.5 0 0 1 24 9.5V15M4 24v3M28 24v3M11 15v-3h10v3" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
          <path d="M24 7v6M21 10h6" stroke="var(--app-icon-accent)" strokeWidth="2" strokeLinecap="round" />
        </svg>
      )
    case 'dorm':
      return (
        <svg viewBox="0 0 32 32" className="h-7 w-7" fill="none">
          <path d="M5 17h22v7H5v-7Z" fill="currentColor" opacity="0.18" />
          <path d="M6 17V9.8A2.8 2.8 0 0 1 8.8 7h14.4A2.8 2.8 0 0 1 26 9.8V17M5 24v3M27 24v3M10 16v-4h12v4" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
          <path d="M10 12h5" stroke="var(--app-icon-accent)" strokeWidth="2" strokeLinecap="round" />
        </svg>
      )
    case 'exit':
      return (
        <svg viewBox="0 0 32 32" className="h-7 w-7" fill="none">
          <path d="M7 7h11v18H7V7Z" fill="currentColor" opacity="0.16" />
          <path d="M18 25H7V7h11M14 16h12m0 0-4-4m4 4-4 4" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round" />
          <circle cx="13" cy="16" r="1.4" fill="var(--app-icon-accent)" />
        </svg>
      )
    case 'help':
      return (
        <svg viewBox="0 0 32 32" className="h-7 w-7" fill="none">
          <path d="M16 5a9 9 0 0 0-5.2 16.3V26h10.4v-4.7A9 9 0 0 0 16 5Z" fill="currentColor" opacity="0.18" />
          <path d="M10.8 21.3A9 9 0 1 1 21.2 21.3V26H10.8v-4.7Z" stroke="currentColor" strokeWidth="2" strokeLinejoin="round" />
          <path d="M13 29h6M14 15a2.8 2.8 0 1 1 4.5 2.2c-.8.6-1.2 1.1-1.2 2" stroke="var(--app-icon-accent)" strokeWidth="2" strokeLinecap="round" />
        </svg>
      )
    case 'location':
      return (
        <svg viewBox="0 0 32 32" className="h-7 w-7" fill="none">
          <path d="M16 27s8-7.2 8-14a8 8 0 1 0-16 0c0 6.8 8 14 8 14Z" fill="currentColor" opacity="0.18" />
          <path d="M16 27s8-7.2 8-14a8 8 0 1 0-16 0c0 6.8 8 14 8 14Z" stroke="currentColor" strokeWidth="2" strokeLinejoin="round" />
          <circle cx="16" cy="13" r="2.8" fill="var(--app-icon-accent)" />
        </svg>
      )
    case 'lock':
      return (
        <svg viewBox="0 0 32 32" className="h-7 w-7" fill="none">
          <rect x="8" y="14" width="16" height="12" rx="3" fill="currentColor" opacity="0.18" />
          <path d="M10 14v-3.2a6 6 0 0 1 12 0V14M8 14h16v12H8V14Z" stroke="currentColor" strokeWidth="2" strokeLinejoin="round" />
          <path d="M16 19v3" stroke="var(--app-icon-accent)" strokeWidth="2.4" strokeLinecap="round" />
        </svg>
      )
    case 'release':
      return (
        <svg viewBox="0 0 32 32" className="h-7 w-7" fill="none">
          <path d="M7 12h18v12H7V12Z" fill="currentColor" opacity="0.17" />
          <path d="M7 12h18v12H7V12ZM10 9h12l3 3H7l3-3Z" stroke="currentColor" strokeWidth="2" strokeLinejoin="round" />
          <circle cx="24" cy="23" r="5" fill="var(--app-icon-accent)" />
          <path d="m21.8 23 1.5 1.5 3-3.3" stroke="#fff" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" />
        </svg>
      )
    case 'repair':
      return (
        <svg viewBox="0 0 32 32" className="h-7 w-7" fill="none">
          <path d="m10 23 8.4-8.4" stroke="currentColor" strokeWidth="2.4" strokeLinecap="round" />
          <path d="M20.7 6.8a6 6 0 0 0 4.5 8.5L15.3 25.2a3.1 3.1 0 0 1-4.5-4.5l9.9-9.9a6 6 0 0 1 0-4Z" fill="currentColor" opacity="0.18" />
          <path d="M21.3 6.5a5.4 5.4 0 0 0 4.2 8.1l-10.3 10a3.2 3.2 0 1 1-4.5-4.5L20.8 9.9a5.5 5.5 0 0 0 .5-3.4Z" stroke="currentColor" strokeWidth="2" strokeLinejoin="round" />
          <path d="m7 8 5 5M8 7l5 5" stroke="var(--app-icon-accent)" strokeWidth="2" strokeLinecap="round" />
        </svg>
      )
    case 'returnFactory':
      return (
        <svg viewBox="0 0 32 32" className="h-7 w-7" fill="none">
          <path d="M5 14 13 9v5l8-5v5h6v12H5V14Z" fill="currentColor" opacity="0.17" />
          <path d="M5 26V14l8-5v5l8-5v5h6v12H5Z" stroke="currentColor" strokeWidth="2" strokeLinejoin="round" />
          <path d="M10 21h4M18 21h4M24 8l3 3-3 3M18 11h9" stroke="var(--app-icon-accent)" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
        </svg>
      )
    case 'scan':
      return (
        <svg viewBox="0 0 32 32" className="h-7 w-7" fill="none">
          <path d="M8 11V8h3M21 8h3v3M24 21v3h-3M11 24H8v-3" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round" />
          <path d="M12 12h3v3h-3v-3ZM18 12h3v3h-3v-3ZM12 18h3v3h-3v-3ZM18 18h2v2h2" fill="var(--app-icon-accent)" />
          <path d="M9 16h14" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" opacity="0.55" />
        </svg>
      )
    case 'waterElec':
      return (
        <svg viewBox="0 0 32 32" className="h-7 w-7" fill="none">
          <path d="M11 7 8 15.5a6.5 6.5 0 0 0 13 0L18 7h-7Z" fill="currentColor" opacity="0.18" />
          <path d="M11 7 8 15.5a6.5 6.5 0 0 0 13 0L18 7h-7Z" stroke="currentColor" strokeWidth="2" strokeLinejoin="round" />
          <path d="m23 10-3 6h4l-3 6" stroke="var(--app-icon-accent)" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
        </svg>
      )
    case 'wechat':
      return (
        <svg viewBox="0 0 32 32" className="h-7 w-7" fill="none">
          <path d="M7 15.5c0-4.2 4-7.5 9-7.5s9 3.3 9 7.5-4 7.5-9 7.5c-1 0-2-.1-2.9-.4L9 24l1.2-3.1A7 7 0 0 1 7 15.5Z" fill="currentColor" opacity="0.18" />
          <path d="M7 15.5c0-4.2 4-7.5 9-7.5s9 3.3 9 7.5-4 7.5-9 7.5c-1 0-2-.1-2.9-.4L9 24l1.2-3.1A7 7 0 0 1 7 15.5Z" stroke="currentColor" strokeWidth="2" strokeLinejoin="round" />
          <circle cx="13" cy="15" r="1.3" fill="var(--app-icon-accent)" />
          <circle cx="19" cy="15" r="1.3" fill="var(--app-icon-accent)" />
        </svg>
      )
    case 'workRelease':
      return (
        <svg viewBox="0 0 32 32" className="h-7 w-7" fill="none">
          <path d="M6 12h20v12H6V12Z" fill="currentColor" opacity="0.17" />
          <path d="M6 12h20v12H6V12ZM12 12V9h8v3M6 17h20" stroke="currentColor" strokeWidth="2" strokeLinejoin="round" />
          <circle cx="24" cy="23" r="5" fill="var(--app-icon-accent)" />
          <path d="m21.8 23 1.5 1.5 3-3.3" stroke="#fff" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" />
        </svg>
      )
    case 'fallback':
    default:
      return (
        <svg viewBox="0 0 32 32" className="h-7 w-7" fill="none">
          <rect x="8" y="8" width="6" height="6" rx="1.6" fill="currentColor" opacity="0.82" />
          <rect x="18" y="8" width="6" height="6" rx="1.6" fill="var(--app-icon-accent)" opacity="0.85" />
          <rect x="8" y="18" width="6" height="6" rx="1.6" fill="var(--app-icon-accent)" opacity="0.7" />
          <rect x="18" y="18" width="6" height="6" rx="1.6" fill="currentColor" opacity="0.68" />
        </svg>
      )
  }
}

function StatusIconSvg({ name }: { name: StatusIconName }) {
  switch (name) {
    case 'close':
      return (
        <svg viewBox="0 0 24 24" className="h-full w-full" fill="none">
          <path d="m7 7 10 10M17 7 7 17" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" />
        </svg>
      )
    case 'denied':
      return (
        <svg viewBox="0 0 24 24" className="h-full w-full" fill="none">
          <circle cx="12" cy="12" r="8.5" fill="currentColor" opacity="0.14" />
          <circle cx="12" cy="12" r="8.5" stroke="currentColor" strokeWidth="2" />
          <path d="m8 8 8 8" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" />
        </svg>
      )
    case 'delete':
      return (
        <svg viewBox="0 0 24 24" className="h-full w-full" fill="none">
          <path d="M8 8h8l-.6 10.2A2 2 0 0 1 13.4 20h-2.8a2 2 0 0 1-2-1.8L8 8Z" fill="currentColor" opacity="0.14" />
          <path d="M6.5 8h11M10 8V5.8h4V8M9 11v5M15 11v5" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" />
        </svg>
      )
    case 'edit':
      return (
        <svg viewBox="0 0 24 24" className="h-full w-full" fill="none">
          <path d="M6 17.5 7 13l8.8-8.8 4 4L11 17l-5 1Z" fill="currentColor" opacity="0.14" />
          <path d="M7 13 15.8 4.2l4 4L11 17l-5 1 1-5ZM14.5 5.5l4 4" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" />
        </svg>
      )
    case 'expired':
      return (
        <svg viewBox="0 0 24 24" className="h-full w-full" fill="none">
          <circle cx="12" cy="12" r="8.5" fill="currentColor" opacity="0.14" />
          <path d="M12 7v5l3 2M5.8 5.8l2-2M16.2 3.8l2 2" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
          <circle cx="12" cy="12" r="8.5" stroke="currentColor" strokeWidth="2" />
        </svg>
      )
    case 'failure':
      return (
        <svg viewBox="0 0 24 24" className="h-full w-full" fill="none">
          <circle cx="12" cy="12" r="8.5" fill="currentColor" opacity="0.14" />
          <path d="m8.5 8.5 7 7M15.5 8.5l-7 7" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" />
          <circle cx="12" cy="12" r="8.5" stroke="currentColor" strokeWidth="2" />
        </svg>
      )
    case 'pending':
      return (
        <svg viewBox="0 0 24 24" className="h-full w-full" fill="none">
          <path d="M8 4h8v4l-3.2 4L16 16v4H8v-4l3.2-4L8 8V4Z" fill="currentColor" opacity="0.14" />
          <path d="M8 4h8v4l-3.2 4L16 16v4H8v-4l3.2-4L8 8V4Z" stroke="currentColor" strokeWidth="1.9" strokeLinejoin="round" />
        </svg>
      )
    case 'search':
      return (
        <svg viewBox="0 0 24 24" className="h-full w-full" fill="none">
          <circle cx="10.8" cy="10.8" r="6.2" fill="currentColor" opacity="0.12" />
          <circle cx="10.8" cy="10.8" r="6.2" stroke="currentColor" strokeWidth="2" />
          <path d="m15.5 15.5 4 4" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" />
        </svg>
      )
    case 'success':
      return (
        <svg viewBox="0 0 24 24" className="h-full w-full" fill="none">
          <circle cx="12" cy="12" r="8.5" fill="currentColor" opacity="0.14" />
          <path d="m7.8 12.5 2.6 2.6 5.8-6.2" stroke="currentColor" strokeWidth="2.3" strokeLinecap="round" strokeLinejoin="round" />
          <circle cx="12" cy="12" r="8.5" stroke="currentColor" strokeWidth="2" />
        </svg>
      )
    case 'user':
    default:
      return (
        <svg viewBox="0 0 24 24" className="h-full w-full" fill="none">
          <circle cx="12" cy="8.5" r="3.5" fill="currentColor" opacity="0.16" />
          <path d="M6 20a6 6 0 0 1 12 0M12 12a3.5 3.5 0 1 0 0-7 3.5 3.5 0 0 0 0 7Z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
        </svg>
      )
  }
}
