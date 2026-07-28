# ADV099 — CSS custom properties (design tokens)

## What was the task?
Replace every hardcoded colour, spacing, radius, and shadow value in the stylesheet with CSS custom properties on `:root`. No component rule should contain a raw hex or pixel value.

## How was it solved?
- Defined all tokens on `:root`: brand colours (`--color-primary`, `--color-gold`, etc.), surfaces (`--color-bg`, `--color-surface`), spacing scale (`--space-1` through `--space-6`), radii, shadows, and font sizes
- Replaced every `#fff`, raw `px` font-size, and `opacity` in component rules with `var()` references
- Added dark-mode overrides in `[data-theme="dark"]` for the new tokens
- Pure refactor — the page looks identical before and after

## Technologies used
- CSS Custom Properties (`var()` on `:root`)
- BEM naming convention for token names (`--color-*`, `--space-*`, `--font-size-*`)

## Files changed
- `static-dashboard/css/style.css`
