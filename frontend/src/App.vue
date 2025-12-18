<template>
  <div v-if="isRestoring" class="restore-screen">
    <el-skeleton :rows="2" animated class="restore-skeleton" />
    <p class="restore-text">正在恢复会话...</p>
  </div>
  <el-container v-else class="app-shell">
    <el-header class="app-header">
      <div class="brand">失物招领系统</div>
      <el-menu mode="horizontal" :default-active="activeRoute" router class="app-menu">
        <el-menu-item index="/">首页</el-menu-item>
        <el-menu-item index="/items">物品列表</el-menu-item>
        <el-menu-item v-if="!isLoggedIn" index="/login">登录</el-menu-item>
        <el-menu-item v-if="isLoggedIn" index="/user">用户中心</el-menu-item>
        <el-menu-item v-if="isAdmin" index="/admin">管理员面板</el-menu-item>
      </el-menu>
      <div class="user-panel">
        <template v-if="user">
          <div class="user-meta">
            <p class="user-name">{{ displayName }}</p>
            <p class="user-role">{{ roleLabel }}</p>
          </div>
          <el-button type="primary" link @click="logout">退出</el-button>
        </template>
        <template v-else>
          <span class="user-role">访客</span>
        </template>
      </div>
    </el-header>

    <el-main class="app-main">
      <router-view />
    </el-main>
  </el-container>
</template>

<script lang="ts" setup>
import { computed } from 'vue';
import { storeToRefs } from 'pinia';
import { useRoute, useRouter } from 'vue-router';
import { useAuthStore } from './stores/auth';
import { normalizeRole } from './utils/auth';

const route = useRoute();
const router = useRouter();
const auth = useAuthStore();
const { user, isLoggedIn, isRestoring } = storeToRefs(auth);

const activeRoute = computed(() => route.path);
const normalizedRole = computed(() => normalizeRole(user.value?.role));
const displayName = computed(() => user.value?.username || `ID：${user.value?.userId ?? ''}`);
const isAdmin = computed(() => normalizedRole.value === 'ADMIN');
const roleLabel = computed(() => {
  if (normalizedRole.value === 'ADMIN') return '管理员';
  if (normalizedRole.value === 'USER') return '普通用户';
  return normalizedRole.value || '未知身份';
});

function logout() {
  auth.logout();
  router.push('/');
}
</script>

<style scoped>
.app-shell {
  min-height: 100vh;
  background: #f8fafc;
}

.restore-screen {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  background: #f8fafc;
}

.restore-skeleton {
  width: 360px;
  max-width: 90vw;
}

.restore-text {
  margin: 0;
  color: var(--el-text-color-secondary);
}

.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  border-bottom: 1px solid var(--el-border-color);
  background: linear-gradient(90deg, rgba(79, 70, 229, 0.08), rgba(16, 185, 129, 0.08));
  backdrop-filter: blur(12px);
  box-shadow: 0 8px 24px -16px rgba(15, 23, 42, 0.3);
}

.brand {
  font-weight: 800;
  letter-spacing: 0.04em;
}

.app-menu {
  flex: 1;
  background: transparent;
}

.user-panel {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.user-meta {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  line-height: 1.2;
}

.user-name {
  margin: 0;
  font-weight: 700;
}

.user-role {
  margin: 0;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.app-main {
  padding: 0;
}

@media (max-width: 768px) {
  .app-header {
    flex-wrap: wrap;
    padding: 12px 8px;
  }

  .user-panel {
    width: 100%;
    justify-content: flex-end;
  }
}
</style>
