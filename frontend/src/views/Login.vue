<template>
  <div class="page">
    <el-card class="login-card">
      <template #header>
        <div class="card-header">
          <span class="title">账号登录</span>
          <span class="subtitle">请输入用户名和密码完成登录</span>
        </div>
      </template>
      <el-form :model="form" label-width="120px" @submit.prevent>
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="login">登录</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { useAuthStore } from '../stores/auth';

const router = useRouter();
const route = useRoute();
const auth = useAuthStore();

const form = reactive({
  username: '',
  password: ''
});
const loading = ref(false);

async function login() {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入用户名和密码');
    return;
  }

  loading.value = true;
  try {
    await auth.login(form.username, form.password);
    ElMessage.success('登录成功');
    const redirect = route.query.redirect || '/';
    router.push(redirect);
  } catch (error) {
    const backendMessage = error.response?.data?.message;
    const message = translateErrorMessage(backendMessage) || '登录失败，请稍后重试';
    ElMessage.error(message);
  } finally {
    loading.value = false;
  }
}

function translateErrorMessage(message) {
  if (!message) return null;
  const lower = message.toLowerCase();
  if (lower.includes('invalid username or password')) {
    return '用户名或密码错误';
  }
  if (lower.includes('required')) {
    return '请输入用户名和密码';
  }
  return message;
}
</script>

<style scoped>
.page {
  display: flex;
  justify-content: center;
  margin-top: 60px;
}

.login-card {
  width: 480px;
}

.card-header {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.title {
  font-weight: 600;
}

.subtitle {
  color: var(--el-text-color-secondary);
  font-size: 13px;
}
</style>
