import Image from 'next/image'
import type { CSSProperties } from 'react'
import type { HomeIconName } from './home-icon-rules'

type HomeTileIconProps = {
  name: HomeIconName
  imageSrc?: string
}

type HomeIconTone = {
  bg: string
  fg: string
  accent: string
}

type HomeIconStyle = CSSProperties & {
  '--home-icon-bg': string
  '--home-icon-fg': string
  '--home-icon-accent': string
}

const ICON_TONES: Record<HomeIconName, HomeIconTone> = {
  approval: { bg: '#fff1e3', fg: '#ec6c00', accent: '#d83b36' },
  checkIn: { bg: '#fff4df', fg: '#d95f00', accent: '#f5b84b' },
  dorm: { bg: '#fff4df', fg: '#c98416', accent: '#f5b84b' },
  exit: { bg: '#fff1e3', fg: '#d95f00', accent: '#d83b36' },
  fallback: { bg: '#fff1e3', fg: '#ec6c00', accent: '#f5b84b' },
  release: { bg: '#fff1e3', fg: '#ec6c00', accent: '#4f8cff' },
  repair: { bg: '#eef5ff', fg: '#2376d9', accent: '#8c8a8a' },
  returnFactory: { bg: '#ecf8f3', fg: '#16a673', accent: '#ec6c00' },
  scan: { bg: '#f1f3f5', fg: '#595757', accent: '#ec6c00' },
  workRelease: { bg: '#fff1e3', fg: '#d95f00', accent: '#4f8cff' },
}

function iconStyle(name: HomeIconName): HomeIconStyle {
  const tone = ICON_TONES[name]
  return {
    '--home-icon-bg': tone.bg,
    '--home-icon-fg': tone.fg,
    '--home-icon-accent': tone.accent,
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

export function HomeTileIcon({ name, imageSrc }: HomeTileIconProps) {
  const fallbackImageSrc = name === 'fallback' ? imageSrc : undefined

  return (
    <span
      aria-hidden
      className="grid h-12 w-12 place-items-center overflow-hidden rounded-[18px] bg-[var(--home-icon-bg)] text-[var(--home-icon-fg)] shadow-[inset_0_1px_0_rgba(255,255,255,0.65)]"
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
        <HomeIconSvg name={name} />
      )}
    </span>
  )
}

function HomeIconSvg({ name }: { name: HomeIconName }) {
  switch (name) {
    case 'approval':
      return (
        <svg viewBox="0 0 32 32" className="h-7 w-7" fill="none">
          <rect x="8" y="5" width="16" height="22" rx="4" fill="currentColor" opacity="0.16" />
          <path d="M12 12h8M12 17h5M12 22h4" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
          <path d="m20 22 2 2 4-5" stroke="var(--home-icon-accent)" strokeWidth="2.4" strokeLinecap="round" strokeLinejoin="round" />
        </svg>
      )
    case 'checkIn':
      return (
        <svg viewBox="0 0 32 32" className="h-7 w-7" fill="none">
          <path d="M7 15h18a3 3 0 0 1 3 3v6H4v-6a3 3 0 0 1 3-3Z" fill="currentColor" opacity="0.18" />
          <path d="M8 15V9.5A2.5 2.5 0 0 1 10.5 7h11A2.5 2.5 0 0 1 24 9.5V15M4 24v3M28 24v3M11 15v-3h10v3" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
          <path d="M24 7v6M21 10h6" stroke="var(--home-icon-accent)" strokeWidth="2" strokeLinecap="round" />
        </svg>
      )
    case 'dorm':
      return (
        <svg viewBox="0 0 32 32" className="h-7 w-7" fill="none">
          <path d="M5 17h22v7H5v-7Z" fill="currentColor" opacity="0.18" />
          <path d="M6 17V9.8A2.8 2.8 0 0 1 8.8 7h14.4A2.8 2.8 0 0 1 26 9.8V17M5 24v3M27 24v3M10 16v-4h12v4" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
          <path d="M10 12h5" stroke="var(--home-icon-accent)" strokeWidth="2" strokeLinecap="round" />
        </svg>
      )
    case 'exit':
      return (
        <svg viewBox="0 0 32 32" className="h-7 w-7" fill="none">
          <path d="M7 7h11v18H7V7Z" fill="currentColor" opacity="0.16" />
          <path d="M18 25H7V7h11M14 16h12m0 0-4-4m4 4-4 4" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round" />
          <circle cx="13" cy="16" r="1.4" fill="var(--home-icon-accent)" />
        </svg>
      )
    case 'release':
      return (
        <svg viewBox="0 0 32 32" className="h-7 w-7" fill="none">
          <path d="M7 12h18v12H7V12Z" fill="currentColor" opacity="0.17" />
          <path d="M7 12h18v12H7V12ZM10 9h12l3 3H7l3-3Z" stroke="currentColor" strokeWidth="2" strokeLinejoin="round" />
          <circle cx="24" cy="23" r="5" fill="var(--home-icon-accent)" />
          <path d="m21.8 23 1.5 1.5 3-3.3" stroke="#fff" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" />
        </svg>
      )
    case 'repair':
      return (
        <svg viewBox="0 0 32 32" className="h-7 w-7" fill="none">
          <path d="m10 23 8.4-8.4" stroke="currentColor" strokeWidth="2.4" strokeLinecap="round" />
          <path d="M20.7 6.8a6 6 0 0 0 4.5 8.5L15.3 25.2a3.1 3.1 0 0 1-4.5-4.5l9.9-9.9a6 6 0 0 1 0-4Z" fill="currentColor" opacity="0.18" />
          <path d="M21.3 6.5a5.4 5.4 0 0 0 4.2 8.1l-10.3 10a3.2 3.2 0 1 1-4.5-4.5L20.8 9.9a5.5 5.5 0 0 0 .5-3.4Z" stroke="currentColor" strokeWidth="2" strokeLinejoin="round" />
          <path d="m7 8 5 5M8 7l5 5" stroke="var(--home-icon-accent)" strokeWidth="2" strokeLinecap="round" />
        </svg>
      )
    case 'returnFactory':
      return (
        <svg viewBox="0 0 32 32" className="h-7 w-7" fill="none">
          <path d="M5 14 13 9v5l8-5v5h6v12H5V14Z" fill="currentColor" opacity="0.17" />
          <path d="M5 26V14l8-5v5l8-5v5h6v12H5Z" stroke="currentColor" strokeWidth="2" strokeLinejoin="round" />
          <path d="M10 21h4M18 21h4M24 8l3 3-3 3M18 11h9" stroke="var(--home-icon-accent)" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
        </svg>
      )
    case 'scan':
      return (
        <svg viewBox="0 0 32 32" className="h-7 w-7" fill="none">
          <path d="M8 11V8h3M21 8h3v3M24 21v3h-3M11 24H8v-3" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round" />
          <path d="M12 12h3v3h-3v-3ZM18 12h3v3h-3v-3ZM12 18h3v3h-3v-3ZM18 18h2v2h2" fill="var(--home-icon-accent)" />
          <path d="M9 16h14" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" opacity="0.55" />
        </svg>
      )
    case 'workRelease':
      return (
        <svg viewBox="0 0 32 32" className="h-7 w-7" fill="none">
          <path d="M6 12h20v12H6V12Z" fill="currentColor" opacity="0.17" />
          <path d="M6 12h20v12H6V12ZM12 12V9h8v3M6 17h20" stroke="currentColor" strokeWidth="2" strokeLinejoin="round" />
          <circle cx="24" cy="23" r="5" fill="var(--home-icon-accent)" />
          <path d="m21.8 23 1.5 1.5 3-3.3" stroke="#fff" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" />
        </svg>
      )
    case 'fallback':
    default:
      return (
        <svg viewBox="0 0 32 32" className="h-7 w-7" fill="none">
          <rect x="8" y="8" width="6" height="6" rx="1.6" fill="currentColor" opacity="0.82" />
          <rect x="18" y="8" width="6" height="6" rx="1.6" fill="var(--home-icon-accent)" opacity="0.85" />
          <rect x="8" y="18" width="6" height="6" rx="1.6" fill="var(--home-icon-accent)" opacity="0.7" />
          <rect x="18" y="18" width="6" height="6" rx="1.6" fill="currentColor" opacity="0.68" />
        </svg>
      )
  }
}
