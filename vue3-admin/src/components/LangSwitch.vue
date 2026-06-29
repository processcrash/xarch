<template>
  <el-dropdown
    trigger="click"
    @command="handleCommand"
    class="lang-switch"
  >
    <span class="lang-trigger">
      <span class="lang-flag">{{ currentFlag }}</span>
      <span class="lang-name">{{ currentName }}</span>
      <el-icon class="el-icon--right"><ArrowDown /></el-icon>
    </span>
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item
          v-for="item in options"
          :key="item.value"
          :command="item.value"
          :disabled="item.value === locale"
        >
          <span class="lang-flag">{{ item.flag }}</span>
          <span>{{ item.label }}</span>
        </el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { ArrowDown } from '@element-plus/icons-vue'
import { useI18n } from '@/composables/useI18n'
import type { SupportedLocale } from '@/locales'

const { locale, setLocale, t } = useI18n()

const options: { value: SupportedLocale; label: string; flag: string }[] = [
  { value: 'zh-CN', label: t('lang.zhCN'), flag: 'CN' },
  { value: 'en-US', label: t('lang.enUS'), flag: 'EN' },
]

const currentName = computed(
  () => options.find((o) => o.value === locale.value)?.label ?? ''
)
const currentFlag = computed(
  () => options.find((o) => o.value === locale.value)?.flag ?? ''
)

const handleCommand = (command: string) => {
  setLocale(command as SupportedLocale)
}
</script>

<style scoped>
.lang-switch {
  display: inline-block;
}

.lang-trigger {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  color: inherit;
  font-size: 14px;
  line-height: 1;
  user-select: none;
}

.lang-flag {
  display: inline-block;
  padding: 0 6px;
  border-radius: 3px;
  background: rgba(255, 255, 255, 0.12);
  color: #fff;
  font-size: 12px;
  font-weight: 600;
  line-height: 18px;
}

.lang-name {
  white-space: nowrap;
}
</style>
