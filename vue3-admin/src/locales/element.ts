import type { Language } from 'element-plus/es/locale'
import enUS from 'element-plus/es/locale/lang/en'
import zhCN from 'element-plus/es/locale/lang/zh-cn'
import type { SupportedLocale } from './index'

export const elementLocaleMap: Record<SupportedLocale, Language> = {
  'en-US': enUS,
  'zh-CN': zhCN,
}

let currentElementLocale: Language = elementLocaleMap['zh-CN']

export function getElementLocale(): Language {
  return currentElementLocale
}

export function setElementLocale(locale: SupportedLocale): Language {
  const next = elementLocaleMap[locale] ?? elementLocaleMap['zh-CN']
  currentElementLocale = next
  return next
}

export function resolveElementLocale(locale: SupportedLocale): Language {
  return setElementLocale(locale)
}
