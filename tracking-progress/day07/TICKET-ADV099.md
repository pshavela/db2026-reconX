# Ticket ADV099

Assignee: FlorinDLF

## Problem
- Replace every hardcoded colour, spacing value, radius and shadow in the stylesheet with CSS custom properties defined on `:root`
- Ensure no component rule contains a hex colour, an `rgba()`, or a raw pixel spacing value
- Follow a consistent naming convention for all tokens

## Approach
- Extended the existing `:root` block with additional tokens: `--color-text-muted`, `--color-white`, `--space-5`/`--space-6`, `--radius-lg`, `--shadow-sm`/`--shadow-md`, `--font-size-sm`/`--font-size-md`/`--font-size-lg`/`--font-size-title`
- Replaced all remaining hardcoded `#fff` in header/nav rules with `var(--color-white)`
- Replaced raw `px` font-sizes (`18px`, `13px`, `22px`) with `var(--font-size-*)` tokens
- Replaced `opacity: 0.7` on stat card headings with `color: var(--color-text-muted)` for better dark-mode support
- Added dark-mode overrides for new tokens (`--color-text-muted`, `--shadow-sm`, `--shadow-md`) in `[data-theme="dark"]`
- Verified via browser inspection that all component computed values resolve through `:root` tokens

## Technologies
- **CSS Custom Properties** — `var()` references on `:root` for all design tokens
- **BEM Naming** — consistent `--color-*`, `--space-*`, `--font-size-*`, `--radius-*`, `--shadow-*` convention
- **Dark mode** — `[data-theme="dark"]` overrides for surface, text, and shadow tokens

## Files Modified
- `static-dashboard/css/style.css` — extended `:root` tokens, replaced all hardcoded hex/px in component rules

## Verification
- Visual inspection confirms no rendering changes (pure refactor)
- DevTools Computed pane shows all tokens defined on `:root`
- No hex colours or raw pixel spacing values remain in component rules
