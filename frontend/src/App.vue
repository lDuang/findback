<template>
  <el-container class="app-shell">
    <el-header class="app-header">
      <div class="brand">失物招领系统</div>
      <el-menu mode="horizontal" :default-active="activeRoute" router class="app-menu">
        <el-menu-item index="/">首页</el-menu-item>
        <el-menu-item index="/items">物品列表</el-menu-item>
        <el-menu-item index="/login">登录</el-menu-item>
        <el-menu-item index="/user" v-if="auth.user">用户中心</el-menu-item>
        <el-menu-item index="/admin" v-if="isAdmin">管理员面板</el-menu-item>
      </el-menu>
      <div class="user-panel">
        <template v-if="auth.user">
          <span class="user-meta">ID：{{ auth.user.userId }} · 身份：{{ roleLabel }}</span>
          <el-button type="text" @click="logout">退出登录</el-button>
        </template>
        <template v-else>
          <span class="user-meta">访客</span>
        </template>
      </div>
    </el-header>

    <el-main>
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup>
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAuthStore } from './stores/auth';
import { normalizeRole } from './utils/auth';

const route = useRoute();
const router = useRouter();
const auth = useAuthStore();

const activeRoute = computed(() => route.path);
const normalizedRole = computed(() => normalizeRole(auth.user?.role));
const isAdmin = computed(() => normalizedRole.value === 'ADMIN');
const roleLabel = computed(() => {
  if (normalizedRole.value === 'ADMIN') return '管理员';
  if (normalizedRole.value === 'USER') return '普通用户';
  return normalizedRole.value || '未知';
});

function logout() {
  auth.logout();
  router.push('/');
}
</script>

<style scoped>
.app-shell {
  min-height: 100vh;
}

.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  border-bottom: 1px solid var(--el-border-color);
}

.brand {
  font-weight: 700;
}

.app-menu {
  flex: 1;
}

.user-panel {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.user-meta {
  color: var(--el-text-color-secondary);
}
</style>
