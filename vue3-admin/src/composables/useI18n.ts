import { computed, watch } from 'vue'
import { useI18n as useVueI18n } from 'vue-i18n'
import { useLocaleStore } from '@/stores/locale'
import { setElementLocale } from '@/locales/element'
import type { SupportedLocale } from '@/locales'

/**
 * Wraps vue-i18n's useI18n with a locale store and Element Plus locale
 * synchronization. Use this inside setup() or <script setup>.
 */
export function useI18n() {
  const { t, locale, d, n, ...rest } = useVueI18n()
  const localeStore = useLocaleStore()

  // Keep vue-i18n locale in sync with the store
  watch(
    () => localeStore.locale,
    (newLocale) => {
      if (locale.value !== newLocale) {
        locale.value = newLocale
      }
      setElementLocale(newLocale)
    },
    { immediate: true }
  )

  // Also keep the store in sync if vue-i18n locale is changed elsewhere
  watch(
    () => locale.value,
    (newLocale) => {
      if (newLocale !== localeStore.locale) {
        localeStore.setLocale(newLocale as SupportedLocale)
      }
    }
  )

  const currentLocale = computed<SupportedLocale>(() => localeStore.locale)
  const isZhCN = computed(() => localeStore.locale === 'zh-CN')

  const switchLocale = (target: SupportedLocale) => {
    localeStore.setLocale(target)
  }

  const toggleLocale = () => {
    localeStore.toggleLocale()
  }

  return {
    t,
    d,
    n,
    locale: currentLocale,
    isZhCN,
    setLocale: switchLocale,
    toggleLocale,
    ...rest,
  }
}
