<template>
  <div class="page">
    <el-card>
      <template #header>
        <span>管理员面板</span>
      </template>
      <el-alert
        class="admin-alert"
        title="管理员视角：已加载全量数据"
        description="当前列表展示所有用户、物品与认领记录，请谨慎执行批量操作。"
        type="success"
        show-icon
      />
      <el-tabs v-model="activeTab">
        <el-tab-pane label="用户管理" name="users">
          <div class="toolbar">
            <el-input v-model="newUser.username" placeholder="用户名" size="small" style="width: 200px" />
            <el-input
              v-model="newUser.password"
              placeholder="密码"
              size="small"
              type="password"
              show-password
              style="width: 200px"
            />
            <el-select v-model="newUser.role" placeholder="角色" size="small" style="width: 140px">
              <el-option label="普通用户" value="USER" />
              <el-option label="管理员" value="ADMIN" />
            </el-select>
            <el-button type="primary" size="small" @click="addUser">新增用户</el-button>
          </div>
          <el-table :data="users" style="width: 100%">
            <el-table-column prop="id" label="用户 ID" />
            <el-table-column prop="username" label="用户名" />
            <el-table-column prop="role" label="角色" :formatter="formatRole" />
            <el-table-column label="操作" width="120">
              <template #default="scope">
                <el-button type="text" size="small" @click="removeUser(scope.row)">移除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="物品管理" name="items">
          <item-form @created="(item) => items.unshift(item)" />
          <el-table :data="items" style="width: 100%; margin-top: 16px">
            <el-table-column prop="title" label="物品名称" />
            <el-table-column prop="location" label="地点" />
            <el-table-column prop="status" label="状态" :formatter="formatStatus" />
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="认领管理" name="claims">
          <el-table :data="claims" style="width: 100%">
            <el-table-column prop="itemId" label="物品 ID" />
            <el-table-column prop="id" label="认领 ID" />
            <el-table-column prop="reason" label="认领理由" />
            <el-table-column prop="status" label="状态" :formatter="formatStatus" />
            <el-table-column label="操作" width="200">
              <template #default="scope">
                <el-button size="small" type="success" @click="updateClaim(scope.row, 'APPROVED')">通过</el-button>
                <el-button size="small" type="danger" @click="updateClaim(scope.row, 'REJECTED')">拒绝</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="公告管理" name="announcements">
          <announcement-manager />
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script lang="ts" setup>
import { onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import client from '../api/client';
import ItemForm from '../components/ItemForm.vue';
import AnnouncementManager from '../components/AnnouncementManager.vue';
import { buildDisplayError } from '../utils/error';
import { statusLabel } from '../utils/status';
import type { Claim, LostItem, AuthUser } from '../types';

type AdminTab = 'users' | 'items' | 'claims' | 'announcements';
type AdminUser = AuthUser & { id: number | string; password?: string };

const activeTab = ref<AdminTab>('users');
const newUser = reactive<Required<Pick<AdminUser, 'username' | 'password' | 'role'>> & { id?: number | string }>(
  {
    username: '',
    password: '',
    role: 'USER'
  }
);
const users = ref<AdminUser[]>([]);
const items = ref<LostItem[]>([]);
const claims = ref<Claim[]>([]);

onMounted(async () => {
  await Promise.all([loadUsers(), loadItems(), loadClaims()]);
});

async function addUser() {
  if (!newUser.username || !newUser.password) {
    ElMessage.warning('请输入用户名和密码');
    return;
  }
  try {
    const { data } = await client.post<AdminUser>('/admin/users', newUser);
    users.value.unshift(data);
    newUser.username = '';
    newUser.password = '';
    newUser.role = 'USER';
    ElMessage.success('用户创建成功');
  } catch (error) {
    console.error(error);
    const message = buildDisplayError('创建用户失败', error);
    ElMessage.error(message || '创建用户失败');
  }
}

async function removeUser(user: AdminUser) {
  try {
    await client.delete(`/admin/users/${user.id}`);
    users.value = users.value.filter((u) => u.id !== user.id);
    ElMessage.success('用户已移除');
  } catch (error) {
    console.error(error);
    const message = buildDisplayError('删除用户失败', error);
    ElMessage.error(message || '删除用户失败');
  }
}

async function updateClaim(claim: Claim, status: string) {
  if (!claim?.id) {
    ElMessage.error('缺少认领 ID');
    return;
  }
  const previousStatus = claim.status;
  claim.status = status.toUpperCase();
  try {
    const { data } = await client.put<Claim>(`/claims/${claim.id}/status`, { status: claim.status });
    claim.status = data?.status || claim.status;
    ElMessage.success(`认领状态：${formatStatus(claim, null, claim.status)}`);
  } catch (error) {
    console.warn('Failed to persist claim status', error);
    claim.status = previousStatus;
    const message = buildDisplayError('更新认领状态失败', error);
    ElMessage.error(message || '更新认领状态失败');
  }
}

async function loadUsers() {
  try {
    const { data } = await client.get<AdminUser[]>('/admin/users');
    if (Array.isArray(data)) users.value = data;
  } catch (error) {
    console.error('Failed to load users', error);
    const message = buildDisplayError('加载用户失败', error);
    ElMessage.error(message || '加载用户失败');
  }
}

async function loadItems() {
  try {
    const { data } = await client.get<LostItem[]>('/items');
    if (Array.isArray(data)) items.value = data;
  } catch (error) {
    console.warn('Using empty items list in admin', error);
    const message = buildDisplayError('加载物品失败', error);
    ElMessage.error(message || '加载物品失败');
  }
}

async function loadClaims() {
  try {
    const { data } = await client.get<Claim[]>('/claims');
    if (Array.isArray(data)) claims.value = data;
  } catch (error) {
    console.warn('Using empty claims list in admin', error);
    const message = buildDisplayError('加载认领失败', error);
    ElMessage.error(message || '加载认领失败');
  }
}

function formatStatus(_row: Claim | LostItem, _column: unknown, cellValue: string) {
  return statusLabel(cellValue);
}

function formatRole(_row: AdminUser, _column: unknown, cellValue: string) {
  const role = cellValue?.toString?.().toUpperCase?.();
  if (role === 'ADMIN') return '管理员';
  if (role === 'USER') return '普通用户';
  return cellValue || '未知角色';
}
</script>

<style scoped>
.page {
  max-width: 1100px;
  margin: 0 auto;
  padding: 24px 16px 48px;
  background: radial-gradient(circle at 18% 20%, rgba(79, 70, 229, 0.05), transparent 32%),
    radial-gradient(circle at 82% 16%, rgba(16, 185, 129, 0.05), transparent 30%),
    #f9fafb;
}

.admin-alert {
  margin-bottom: 12px;
}

.toolbar {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}
</style>
