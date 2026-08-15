# Music WTF (Gaane) — Design System & Tokens Audit

Extracted directly from the Next.js codebase (`app/globals.css`, `app/layout.tsx`, `components/*`, `data/playlists.ts`).

---

## 1. Color Palette

### Root & Theme Colors (CSS Variables)
| Variable | Hex / Value | Description & Usage |
|---|---|---|
| `--night` | `#eef6fc` | Soft Sky Powder Blue / Ice White main page background |
| `--night-2` | `#ffffff` | Pure white background |
| `--night-3` | `#cfe3f2` | Light sky blue border & scrollbar track |
| `--moon` | `#12212e` | Dark navy primary heading & text color |
| `--moon-dim` | `#4d6578` | Muted slate blue subtitle & secondary text |
| `--lamp` | `#2489d3` | Bright lamp blue primary accent & active state |
| `--lamp-soft` | `rgba(36, 137, 211, 0.14)` | Soft lamp blue tint fill |

### Dark Deck & Metallic Colors (Retro Cassette / Player UI)
| Identifier | Hex / Value | Usage |
|---|---|---|
| Cassette Deck Dark Gradient | `linear-gradient(180deg, #222733, #151821 45%, #0f121a)` | Retro Cassette Tape Card outer shell |
| CRT Screen Outer | `#05070f` | Screen housing background |
| Drawer Background | `linear-gradient(180deg, #14171e 0%, #0d0f14 100%)` | Player bottom drawer panel |
| Dedicated Page Background | `#0a0c16` | Webview / iframe background container |
| Add Modal Panel | `linear-gradient(180deg, #181b24, #0f1118)` | Dark modal popup |

### Accent & Status Indicators
| Identifier | Hex / Value | Usage |
|---|---|---|
| Power LED Green / Live Dot | `#10b981` | Online status, power LED dot, live listener pulse |
| Amber Accent | `#f59e0b` | Tagline text, alternate power LED dot |
| Cassette Side Label Blue | `#38bdf8` | Cassette side label "SIDE A", play deck button |
| Active Chip Gradient | `linear-gradient(135deg, #3ea2e8, #1f7fc7)` | Selected category mood chip |
| Offline Warning Red | `#ef4444` | Heart icon, offline warning badge |
| Truck Horn Shadow Red | `#D62828` | Hero title text shadow accent |

---

## 2. Typography & Fonts

| Token | Font Family | Weights | Variable Name | Usage |
|---|---|---|---|---|
| `font-yatra` | `Yatra One`, cursive | `400` | `--font-yatra` | Hero main title ("Gaane"), retro modal headings |
| `font-mukta` | `Mukta`, sans-serif | `300`, `400`, `500`, `600`, `700` | `--font-mukta` | Primary body, card titles, descriptions, buttons, search input |
| `font-mono2` | `JetBrains Mono`, monospace | `400`, `500`, `700` | `--font-mono2` | Dev handles (`- @owner`), cassette labels, seek timer, view count, live listener count |

---

## 3. Motion & GSAP Timing Curves

| Motion Action | Duration | Easing Curve / Curve Type | Keyframe Details |
|---|---|---|---|
| Hero Title Parallax Tilt | `0.5s` | `power2.out` | `rotateX: -moveY * 0.5`, `rotateY: moveX * 0.5` |
| Hero Title Parallax Reset | `0.6s` | `power2.out` | `rotateX: 0`, `rotateY: 0`, `x: 0`, `y: 0` |
| Hero Background Scroll | Scrubbed | `none` | `yPercent: 20` via `ScrollTrigger` |
| Card 3D Tilt (Mousemove) | `0.25s` | `power2.out` | Max 10deg `rotateX`/`rotateY`, `scale: 1.03`, `perspective: 1000` |
| Card 3D Tilt Reset | `0.4s` | `power2.out` | `rotateX: 0`, `rotateY: 0`, `scale: 1` |
| Card Batch Scroll Reveal | `0.5s` (stagger `0.04s`) | `back.out(1.2)` | `opacity: 0 -> 1`, `y: 35 -> 0`, `scale: 0.96 -> 1` |
| Card Scroll Exit | `0.4s` (stagger `0.03s`) | `power2.in` | `opacity: 1 -> 0`, `y: 0 -> -30`, `scale: 1 -> 0.95` |
| Category Transition Out | `0.18s` | `power2.in` | `opacity: 0`, `y: 15`, `scale: 0.96` |
| Modal Pop-In | `0.3s` | `cubic-bezier(0.22, 1, 0.36, 1)` | `translate(-50%, -45%) scale(0.95) -> translate(-50%, -50%) scale(1)` |
| Drawer Slide-Up | `0.4s` | `cubic-bezier(0.22, 1, 0.36, 1)` | `translateY(100% -> 0)` |
| Scroll Hint Bounce | `2.0s` infinite | `ease-in-out` | `translateY(0 -> 8px -> 0)` |
| Star Twinkle | `3.5s` infinite | `ease-in-out` | `opacity: 0.25 -> 1 -> 0.25` |
| Live Listener Pulse Glow | `1.8s` infinite | `ease-in-out` | `scale: 1 -> 0.85`, `opacity: 1 -> 0.45` |
| Cassette Reel Spin | `2.0s` infinite | `linear` | `rotate(0deg -> 360deg)` |
| Crossfade Visual Indicator | `700ms` | `ease` | Visual opacity/crossfade flash |
| Audio Crossfade Ramp | `4.0s` | Linear volume ramp | Volume ramp from 100% to 0% in final 4 seconds |

---

## 4. Spacing, Layouts & Grid Responsive Rules

| Component | Dimensions / Breakpoint | Layout Behavior |
|---|---|---|
| Hero Section | `min-height: 88vh` | Center flex container with mountain background & cloud overlay |
| TV Card Grid (< 440px) | `1 column` | `gap: 0.75rem` |
| TV Card Grid (440px - 739px) | `2 columns` | `gap: 0.75rem` |
| TV Card Grid (740px - 1039px) | `3 columns` | `gap: 0.85rem` |
| TV Card Grid (1040px - 1439px) | `4 columns` | `gap: 0.95rem` |
| TV Card Grid (>= 1440px) | `5 columns` | `gap: 1.1rem` |
| TV Card Grid (>= 1600px) | `repeat(auto-fill, minmax(360px, 1fr))` | Large screen grid |
| Card Image Aspect Ratio | `16 / 9` | Standard TV / CRT aspect ratio |
| Card Shell Bounds | `contain-intrinsic-size: auto 280px` | Low-end device rendering optimization |

---

## 5. Schema & Data Model Audit

### Playlist Entry Model (`data/playlists.ts`)
```typescript
export interface PlaylistEntry {
  id: string;                 // e.g. "runable-1"
  numId: number;              // e.g. 1
  title: string;              // e.g. "Cutting Shop"
  titleHi?: string;           // Optional Hindi title, e.g. "नानी का घर"
  category: string;           // e.g. "Dukaan", "Safar", "Raat", etc.
  cover: string;              // Thumbnail cover URL
  playlistUrl: string;        // Audio playlist source URL (YouTube)
  originalSiteUrl: string;    // Original creator micro-site URL
  description: string;        // Short description
  brandLabel?: string;        // e.g. "DUKAAN · chakra5027"
  owner: string;              // Handle e.g. "@chakra5027"
  ownerTwitterUrl?: string;   // Creator Twitter link
  accentColor: string;        // Creator brand accent hex code
  views?: number;             // Total view count
  dead?: boolean;             // Offline / dead link flag
}
```

### Catalog Categories
1. `All`
2. `Safar`
3. `Raat`
4. `Bachpan`
5. `Rozmarra`
6. `Dukaan`
7. `Kshetriya`
8. `Shaadi`
9. `Tyohar`
10. `Bhakti`
