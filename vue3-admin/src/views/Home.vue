<template>
  <div class="home">
    <el-container>
      <el-header>
        <div class="header-content">
          <h2>xarch Backend Framework</h2>
          <el-button type="primary" @click="handleLogout">Logout</el-button>
        </div>
      </el-header>
      <el-container>
        <el-aside width="200px">
          <el-menu default-active="1" class="el-menu-vertical">
            <el-menu-item index="1">
              <el-icon><User /></el-icon>
              <span>User Management</span>
            </el-menu-item>
            <el-menu-item index="2">
              <el-icon><Setting /></el-icon>
              <span>System Settings</span>
            </el-menu-item>
          </el-menu>
        </el-aside>
        <el-main>
          <div class="content">
            <h3>Welcome to xarch</h3>
            <el-card class="box-card">
              <template #header>
                <div class="card-header">
                  <span>Quick Start</span>
                </div>
              </template>
              <div class="card-content">
                <p>Enterprise Backend Development Framework</p>
                <ul>
                  <li>Spring Boot 4.0 + JDK 25</li>
                  <li>MySQL / PostgreSQL Support</li>
                  <li>Redis Cache Support</li>
                  <li>Vue 3 Frontend</li>
                </ul>
              </div>
            </el-card>
          </div>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { User, Setting } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const handleLogout = () => {
  localStorage.removeItem('token')
  ElMessage.success('Logged out successfully')
}