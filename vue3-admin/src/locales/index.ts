import { createI18n } from 'vue-i18n'
import enUS from './en-US'
import zhCN from './zh-CN'

export const SUPPORTED_LOCALES = ['zh-CN', 'en-US'] as const
export type SupportedLocale = typeof SUPPORTED_LOCALES[number]

export const DEFAULT_LOCALE: SupportedLocale = 'zh-CN'
export const FALLBACK_LOCALE: SupportedLocale = 'zh-CN'

export const messages = {
  'en-US': enUS,
  'zh-CN': zhCN,
}

export const i18n = createI18n({
  legacy: false,
  globalInjection: true,
  locale: DEFAULT_LOCALE,
  fallbackLocale: FALLBACK_LOCALE,
  messages,
})

export default i18n
