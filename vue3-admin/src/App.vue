<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'

onMounted(() => {
  console.log('xarch frontend mounted')
})
</script>

<style>
#app {
  height: 100%;
  width: 100%;
}
</style>