import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import router from './router'
import i18n from './locales'
import { useLocaleStore } from './stores/locale'
import { setElementLocale } from './locales/element'
import App from './App.vue'
import './styles/common.css'

const app = createApp(App)
const pinia = createPinia()

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// Register i18n before pinia so the locale store can leverage it.
app.use(i18n)
app.use(pinia)
app.use(router)

// Sync Element Plus locale with the persisted / browser-detected locale.
const localeStore = useLocaleStore()
if (i18n.global.locale.value !== localeStore.locale) {
  ;(i18n.global.locale as unknown as { value: string }).value = localeStore.locale
}
app.use(ElementPlus, { locale: setElementLocale(localeStore.locale) })

app.mount('#app')
