import { defineStore } from 'pinia'
import { ref } from 'vue'
import { DEFAULT_LOCALE, SUPPORTED_LOCALES, type SupportedLocale } from '@/locales'

const STORAGE_KEY = 'xarch-locale'

function isSupportedLocale(value: string | null | undefined): value is SupportedLocale {
  return !!value && (SUPPORTED_LOCALES as readonly string[]).includes(value)
}

function readPersistedLocale(): SupportedLocale {
  try {
    const stored = localStorage.getItem(STORAGE_KEY)
    if (isSupportedLocale(stored)) return stored
  } catch {
    // ignore
  }

  // Try browser default language
  try {
    const browser = (navigator.language || '').toLowerCase()
    if (browser.startsWith('zh')) return 'zh-CN'
    if (browser.startsWith('en')) return 'en-US'
  } catch {
    // ignore
  }

  return DEFAULT_LOCALE
}

function persistLocale(locale: SupportedLocale) {
  try {
    localStorage.setItem(STORAGE_KEY, locale)
  } catch {
    // ignore
  }
}

export const useLocaleStore = defineStore('locale', () => {
  const locale = ref<SupportedLocale>(readPersistedLocale())

  const setLocale = (next: SupportedLocale) => {
    if (!isSupportedLocale(next)) return
    if (locale.value === next) return
    locale.value = next
    persistLocale(next)
  }

  const toggleLocale = () => {
    const next: SupportedLocale = locale.value === 'zh-CN' ? 'en-US' : 'zh-CN'
    setLocale(next)
  }

  return {
    locale,
    setLocale,
    toggleLocale,
  }
})
