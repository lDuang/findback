<template>
  <div class="page">
    <el-alert
      class="role-alert"
      :title="roleTitle"
      :description="roleDescription"
      type="info"
      show-icon
    />
    <el-row :gutter="20">
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>物品列表</span>
              <el-input v-model="search" placeholder="搜索物品名称、地点或描述" size="small" clearable style="width: 220px" />
            </div>
          </template>
          <el-table :data="filteredItems" :empty-text="tableEmptyText" style="width: 100%" @row-click="goDetail">
            <el-table-column prop="title" label="物品名称" :formatter="formatTitle" />
            <el-table-column prop="location" label="地点" :formatter="formatLocation" />
            <el-table-column prop="status" label="状态" :formatter="formatStatus" />
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header>
            <span>创建物品</span>
          </template>
          <item-form v-if="canCreate" @created="addItem" />
          <el-alert
            v-else
            title="当前为访客视角"
            description="登录后才可创建物品，并自动切换到个人数据视图。"
            type="warning"
            show-icon
          />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import client from '../api/client';
import ItemForm from '../components/ItemForm.vue';
import { statusLabel } from '../utils/status';
import { normalizeRole } from '../utils/auth';
import { extractErrorMessage } from '../utils/error';
import { useAuthStore } from '../stores/auth';

const router = useRouter();
const items = ref([]);
const search = ref('');
const loadHint = ref('正在加载物品，请稍候');
const loading = ref(false);
const auth = useAuthStore();

const normalizedRole = computed(() => normalizeRole(auth.user?.role));
const isAdmin = computed(() => normalizedRole.value === 'ADMIN');
const isUser = computed(() => normalizedRole.value === 'USER');
const canCreate = computed(() => !!auth.user);
const roleTitle = computed(() => {
  if (isAdmin.value) return '管理员模式：正在查看全量物品';
  if (isUser.value) return '普通用户模式：仅展示我的物品';
  return '访客模式：请登录以查看和管理个人物品';
});
const roleDescription = computed(() => {
  if (isAdmin.value) return '管理员可以浏览和维护所有用户的物品，并可在其他页面管理公告和认领。';
  if (isUser.value) return '列表已根据您的账号进行过滤，仅包含您自己创建的物品记录。';
  return '未登录状态下仅进行基础浏览，登录后即可按账号维度过滤并创建物品。';
});

onMounted(async () => {
  await loadItems();
});

watch(
  () => auth.user,
  async () => {
    await loadItems();
  },
  { deep: true }
);

async function loadItems() {
  loading.value = true;
  const params = {};
  if (isUser.value) {
    params.userId = auth.user.userId;
  }
  try {
    const { data } = await client.get('/items', Object.keys(params).length ? { params } : undefined);
    if (Array.isArray(data)) {
      items.value = data;
      if (!data.length) {
        loadHint.value = isAdmin.value ? '暂无物品，您可以创建或导入新的记录。' : '暂无物品，您可以先创建一条属于自己的记录。';
      } else {
        loadHint.value = '';
      }
    } else {
      loadHint.value = '暂无物品，您可以创建一条新的失物记录。';
    }
  } catch (error) {
    console.warn('Failed to load items', error);
    const message = extractErrorMessage(error);
    loadHint.value = message ? `物品列表加载失败：${message}` : '物品列表加载失败，请稍后重试。';
    ElMessage.error(loadHint.value);
  } finally {
    loading.value = false;
  }
}

const filteredItems = computed(() =>
  items.value.filter((item) => {
    const query = search.value.trim().toLowerCase();
    if (!query) return true;
    return [item.title, item.description, item.location]
      .map((field) => field?.toString?.().toLowerCase?.() || '')
      .some((field) => field.includes(query));
  })
);

const tableEmptyText = computed(() => {
  if (search.value && filteredItems.value.length === 0 && items.value.length) {
    return '没有匹配的物品';
  }
  if (!items.value.length) {
    return loadHint.value || '暂无物品';
  }
  return loading.value ? '正在加载物品，请稍候' : '暂无物品';
});

function goDetail(row) {
  router.push({ name: 'item-detail', params: { id: row.id } });
}

function addItem(newItem) {
  items.value.unshift(newItem);
  ElMessage.success('物品已添加');
}

function formatStatus(row, column, cellValue) {
  return statusLabel(cellValue);
}

function formatLocation(row, column, cellValue) {
  return cellValue || '未提供地点';
}

function formatTitle(row, column, cellValue) {
  return cellValue || '未命名物品';
}
</script>

<style scoped>
.page {
  max-width: 1200px;
  margin: 0 auto;
}

.role-alert {
  margin-bottom: 16px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
</style>
