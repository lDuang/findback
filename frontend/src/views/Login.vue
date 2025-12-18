<template>
  <div class="page">
    <el-card class="login-card" shadow="hover">
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

<script lang="ts" setup>
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
    if (!auth.user) {
      throw new Error('登录状态异常：未获取到用户信息');
    }
    ElMessage.success('登录成功');
    const redirect = (route.query.redirect as string) || '/';
    router.push(redirect);
  } catch (error: unknown) {
    const backendMessage = (error as { response?: { data?: { message?: string } } })?.response?.data?.message;
    const message = translateErrorMessage(backendMessage) || '登录失败，请稍后重试';
    ElMessage.error(message);
  } finally {
    loading.value = false;
  }
}

function translateErrorMessage(message?: string | null): string | null {
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
  padding: 64px 16px;
  background: radial-gradient(circle at 20% 20%, rgba(99, 102, 241, 0.08), transparent 40%),
    radial-gradient(circle at 80% 30%, rgba(16, 185, 129, 0.08), transparent 42%),
    #f8fafc;
}

.login-card {
  width: 480px;
  border-radius: 16px;
  box-shadow: 0 20px 50px -28px rgba(15, 23, 42, 0.45);
}

.card-header {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.title {
  font-weight: 700;
  font-size: 18px;
}

.subtitle {
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

@media (max-width: 560px) {
  .login-card {
    width: 100%;
  }
}
</style>
