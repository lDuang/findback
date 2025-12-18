<template>
  <div class="page">
    <el-alert
      class="user-alert"
      title="个人视图：仅展示属于当前账号的数据"
      description="您可以在此创建和维护自己的物品与认领记录，若需查看全量数据请使用管理员面板。"
      type="info"
      show-icon
    />
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的物品</span>
          <el-tag v-if="auth.user" type="success">{{ auth.user.userId }}</el-tag>
        </div>
      </template>
      <el-table :data="myItems" :empty-text="itemEmptyText" style="width: 100%">
        <el-table-column prop="title" label="物品名称" />
        <el-table-column prop="status" label="状态" :formatter="formatStatus" />
        <el-table-column prop="location" label="地点" />
      </el-table>
      <el-divider />
      <h4>新增物品</h4>
      <item-form @created="(item) => myItems.unshift(item)" />
    </el-card>

    <el-card style="margin-top: 16px">
      <template #header>
        <span>我的认领</span>
      </template>
      <claim-form @submitted="(claim) => myClaims.unshift(claim)" />
      <el-table :data="myClaims" :empty-text="claimEmptyText" style="width: 100%; margin-top: 16px">
        <el-table-column prop="itemId" label="物品 ID" />
        <el-table-column prop="status" label="状态" :formatter="formatStatus" />
        <el-table-column prop="reason" label="认领理由" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { useAuthStore } from '../stores/auth';
import client from '../api/client';
import ItemForm from '../components/ItemForm.vue';
import ClaimForm from '../components/ClaimForm.vue';
import { statusLabel } from '../utils/status';
import { extractErrorMessage } from '../utils/error';

const auth = useAuthStore();
const myItems = ref([]);
const myClaims = ref([]);
const itemEmptyText = ref('请登录后查看我的物品。');
const claimEmptyText = ref('请登录后查看我的认领。');

onMounted(async () => {
  if (!auth.user?.userId) return;

  itemEmptyText.value = '正在加载我的物品...';
  claimEmptyText.value = '正在加载我的认领...';

  try {
    const { data } = await client.get('/items', { params: { userId: auth.user.userId } });
    if (Array.isArray(data)) {
      myItems.value = data;
      itemEmptyText.value = data.length ? '' : '暂无物品记录';
    } else {
      itemEmptyText.value = '暂无物品记录';
    }
  } catch (error) {
    console.warn('Failed to load my items', error);
    const message = extractErrorMessage(error);
    itemEmptyText.value = message ? `加载我的物品失败：${message}` : '加载我的物品失败，请稍后重试。';
  }

  try {
    const { data } = await client.get('/claims', { params: { userId: auth.user.userId } });
    if (Array.isArray(data)) {
      myClaims.value = data;
      claimEmptyText.value = data.length ? '' : '暂无认领记录';
    } else {
      claimEmptyText.value = '暂无认领记录';
    }
  } catch (error) {
    console.warn('Failed to load my claims', error);
    const message = extractErrorMessage(error);
    claimEmptyText.value = message ? `加载我的认领失败：${message}` : '加载我的认领失败，请稍后重试。';
  }
});

function formatStatus(row, column, cellValue) {
  return statusLabel(cellValue);
}
</script>

<style scoped>
.page {
  max-width: 960px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.user-alert {
  margin-bottom: 4px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
</style>
