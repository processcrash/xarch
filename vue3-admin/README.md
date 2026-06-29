# xarch Frontend (Vue 3 Admin)

Vue 3 + TypeScript + Element Plus + Vite + Pinia admin for the xarch backend framework.

## Quick start

```bash
# pick the package manager that matches the lockfile in this directory
npm install
npm run dev
```

The app boots on `http://localhost:5173` (default Vite port).

## Internationalization (i18n)

The admin is fully internationalized with `vue-i18n@^9` and supports two locales out of the box:

- `zh-CN` - Simplified Chinese (default / fallback)
- `en-US` - English

User language is persisted to `localStorage` under the `xarch-locale` key. On first load, the app
falls back to the browser default (`navigator.language`).

### File layout

```
src/
  locales/
    index.ts            # exports `i18n` (createI18n instance) + SUPPORTED_LOCALES
    en-US.ts            # English message catalog
    zh-CN.ts            # Chinese message catalog
    element.ts          # maps our locales to element-plus locale packs
    types.ts            # vue-i18n type augmentation (DefineLocaleMessage)
  composables/
    useI18n.ts          # wraps vue-i18n's useI18n + syncs Element Plus locale
  stores/
    locale.ts           # Pinia store for the active locale
  components/
    LangSwitch.vue      # dropdown to switch between zh-CN and en-US
```

### How to add a new translation

1. Open `src/locales/en-US.ts` and `src/locales/zh-CN.ts`.
2. Add the same key path in both files, e.g.
   ```ts
   // en-US.ts
   export default {
     // ...
     myFeature: {
       title: 'My Feature',
       save: 'Save changes',
     },
   }
   ```
   ```ts
   // zh-CN.ts
   export default {
     // ...
     myFeature: {
       title: '我的功能',
       save: '保存修改',
     },
   }
   ```
3. If the new namespace is not yet declared in `src/locales/types.ts`, add it to the
   `DefineLocaleMessage` interface for type-safe `t()` calls.
4. Use the new key in any component: `{{ t('myFeature.title') }}`.

### How to switch language at runtime

The simplest way is to drop the `LangSwitch` component into a header, toolbar, or login page:

```vue
<script setup lang="ts">
import LangSwitch from '@/components/LangSwitch.vue'
</script>

<template>
  <LangSwitch />
</template>
```

`LangSwitch` is already mounted in the `Home` and `Login` views.

For programmatic switching, call the composable from any `<script setup>`:

```ts
import { useI18n } from '@/composables/useI18n'

const { setLocale, toggleLocale } = useI18n()

// Switch to a specific locale
setLocale('en-US')

// Toggle between zh-CN and en-US
toggleLocale()
```

Internally `setLocale` and `toggleLocale` route through the Pinia locale store, which:

1. Updates the store's `locale` ref
2. Persists the new value to `localStorage` (`xarch-locale`)
3. Re-syncs `vue-i18n`'s global locale
4. Triggers Element Plus to re-render in the matching language

### How Element Plus components are localized

Element Plus ships its own locale packs (e.g. `element-plus/es/locale/lang/en`,
`element-plus/es/locale/lang/zh-cn`). The mapping lives in `src/locales/element.ts`:

```ts
export const elementLocaleMap: Record<SupportedLocale, Language> = {
  'en-US': enUS,
  'zh-CN': zhCN,
}
```

`src/locales/element.ts` also exposes `setElementLocale(locale)` which returns the
matching Element Plus `Language` object. `src/composables/useI18n.ts` watches the active
locale and calls `setElementLocale` on every change, so all Element Plus components
(pagination, date picker, message box, etc.) re-render in the chosen language automatically.

The initial Element Plus locale is set in `src/main.ts` from the persisted / browser-detected
locale so the first paint is already localized.

## Scripts

| Script            | Description                                     |
| ----------------- | ----------------------------------------------- |
| `npm run dev`     | Start the Vite dev server                       |
| `npm run build`   | Type-check (`vue-tsc`) and produce a prod build |
| `npm run preview` | Serve the production build locally              |
| `npm run lint`    | Run ESLint with auto-fix                        |
| `npm run format`  | Format sources with Prettier                    |
```
