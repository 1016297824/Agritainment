# Design System Master File

> **LOGIC:** When building a specific page, first check `design-system/pages/[page-name].md`.
> If that file exists, its rules **override** this Master file.
> If not, strictly follow the rules below.

---

**Project:** Agritainment (农家乐小程序)
**Generated:** 2026-05-05
**Category:** Agriculture / Farm Service

---

## Global Rules

### Color Palette

| Role | Hex | CSS Variable | Usage |
|------|-----|--------------|-------|
| Primary | `#15803D` | `--color-primary` | 主色调-自然绿 |
| Secondary | `#22C55E` | `--color-secondary` | 辅助色-嫩绿 |
| CTA/Accent | `#CA8A04` | `--color-cta` | 行动按钮-丰收金 |
| Background | `#F0FDF4` | `--color-background` | 背景色-淡绿白 |
| Text | `#14532D` | `--color-text` | 正文色-深绿 |
| Surface | `#FFFFFF` | `--color-surface` | 卡片/面板色 |
| Muted | `#6B7280` | `--color-muted` | 辅助文字 |
| Border | `#D1D5DB` | `--color-border` | 边框色 |
| Error | `#DC2626` | `--color-error` | 错误/警告 |
| Success | `#16A34A` | `--color-success` | 成功状态 |

**Color Notes:** Earth green + harvest gold (大地绿 + 丰收金)

### Typography

- **Heading Font:** Inter / system-ui
- **Body Font:** Inter / system-ui
- **Mood:** 清爽自然 + 易读信息

### Spacing Variables

| Token | Value | Usage |
|-------|-------|-------|
| `--space-xs` | `4px` / `0.25rem` | Tight gaps |
| `--space-sm` | `8px` / `0.5rem` | Icon gaps, inline spacing |
| `--space-md` | `16px` / `1rem` | Standard padding |
| `--space-lg` | `24px` / `1.5rem` | Section padding |
| `--space-xl` | `32px` / `2rem` | Large gaps |
| `--space-2xl` | `48px` / `3rem` | Section margins |

### Shadow Depths

| Level | Value | Usage |
|-------|-------|-------|
| `--shadow-sm` | `0 1px 2px rgba(0,0,0,0.05)` | Subtle lift |
| `--shadow-md` | `0 4px 6px rgba(0,0,0,0.08)` | Cards, buttons |
| `--shadow-lg` | `0 10px 15px rgba(0,0,0,0.1)` | Modals, dropdowns |

### Border Radius

| Token | Value | Usage |
|-------|-------|-------|
| `--radius-sm` | `8px` | Buttons, inputs |
| `--radius-md` | `12px` | Cards |
| `--radius-lg` | `16px` | Modals, panels |
| `--radius-xl` | `24px` | Hero sections |

---

## Component Specs

### Buttons

```css
.btn-primary {
  background: #CA8A04;
  color: white;
  padding: 12px 24px;
  border-radius: 8px;
  font-weight: 600;
  transition: all 200ms ease;
  cursor: pointer;
}
.btn-primary:hover { opacity: 0.9; }

.btn-secondary {
  background: transparent;
  color: #15803D;
  border: 2px solid #15803D;
  padding: 12px 24px;
  border-radius: 8px;
  font-weight: 600;
  transition: all 200ms ease;
  cursor: pointer;
}
```

### Cards

```css
.card {
  background: #FFFFFF;
  border-radius: 12px;
  padding: 16px;
  box-shadow: var(--shadow-md);
  transition: all 200ms ease;
  cursor: pointer;
}
.card:hover { box-shadow: var(--shadow-lg); }
```

### Inputs

```css
.input {
  padding: 12px 16px;
  border: 1px solid #D1D5DB;
  border-radius: 8px;
  font-size: 16px;
  transition: border-color 200ms ease;
}
.input:focus {
  border-color: #15803D;
  outline: none;
  box-shadow: 0 0 0 3px #15803D20;
}
```

---

## Style Guidelines

**Style:** Organic Biophilic (有机亲自然)

**Keywords:** Nature, organic shapes, green, sustainable, rounded, flowing, earthy, natural textures

**Key Effects:** Rounded corners (8-16px), organic curves, natural shadows, flowing SVG shapes

### Page Pattern

**Pattern Name:** Feature-Rich Showcase

- **CTA Placement:** Above fold
- **Section Order:** Hero > Features > CTA

---

## Anti-Patterns (Do NOT Use)

- ❌ Generic design
- ❌ Ignored accessibility
- ❌ AI purple/pink gradients
- ❌ Emojis as icons — Use SVG icons (Heroicons, Lucide)
- ❌ Missing cursor:pointer
- ❌ Layout-shifting hovers
- ❌ Low contrast text (< 4.5:1)
- ❌ Instant state changes
- ❌ Invisible focus states

---

## Pre-Delivery Checklist

- [ ] No emojis used as icons (use SVG instead)
- [ ] All icons from consistent icon set (Heroicons/Lucide)
- [ ] `cursor-pointer` on all clickable elements
- [ ] Hover states with smooth transitions (150-300ms)
- [ ] Light mode: text contrast 4.5:1 minimum
- [ ] Focus states visible for keyboard navigation
- [ ] `prefers-reduced-motion` respected
- [ ] Responsive: 375px (mobile primary)
- [ ] No horizontal scroll on mobile
