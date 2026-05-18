<template>
  <div class="not-found">
    <el-result icon="warning" title="404" sub-title="Sorry, the page you visited does not exist.">
      <template #extra>
        <el-button type="primary" @click="goHome">Back to Home</el-button>
      </template>
    </el-result>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const goHome = () => {
  router.push('/')
}