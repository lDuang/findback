<template>
  <div class="page">
    <el-alert
      class="role-alert"
      :title="roleTitle"
      :description="roleDescription"
      type="info"
      show-icon
    />
    <el-card class="panel filters">
      <div class="filter-grid">
        <el-input v-model="search" placeholder="搜索物品名称、地点或描述" clearable />
        <el-select v-model="statusFilter" placeholder="按状态筛选" clearable filterable>
          <el-option v-for="option in statusOptions" :key="option.value" :label="option.label" :value="option.value" />
        </el-select>
        <el-select v-model="locationFilter" placeholder="按地点筛选" clearable filterable>
          <el-option v-for="option in locationOptions" :key="option" :label="option" :value="option" />
        </el-select>
        <el-button link type="primary" @click="resetFilters">清除筛选</el-button>
      </div>
    </el-card>
    <el-row :gutter="20">
      <el-col :span="16">
        <el-card class="panel" v-loading="loading">
          <template #header>
            <div class="card-header">
              <div>
                <p class="card-eyebrow">实时物品</p>
                <span class="card-title">物品列表</span>
              </div>
              <el-tag v-if="loadError" type="danger" effect="plain">加载出现问题</el-tag>
            </div>
          </template>
          <el-table
            :data="filteredItems"
            :empty-text="tableEmptyText"
            style="width: 100%"
            @row-click="goDetail"
            highlight-current-row
            border
          >
            <el-table-column prop="title" label="物品名称" :formatter="formatTitle" min-width="160" />
            <el-table-column prop="location" label="地点" :formatter="formatLocation" min-width="120" />
            <el-table-column prop="status" label="状态" width="140">
              <template #default="scope">
                <el-tag :type="statusColor(scope.row.status)" disable-transitions>{{ formatStatus(scope.row, null, scope.row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column
              prop="createdAt"
              label="创建时间"
              :formatter="formatCreatedAt"
              width="180"
            />
            <el-table-column prop="username" label="发布人" :formatter="formatOwner" min-width="140" />
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="panel">
          <template #header>
            <div class="card-header">
              <div>
                <p class="card-eyebrow">快速发布</p>
                <span class="card-title">创建物品</span>
              </div>
            </div>
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

<script lang="ts" setup>
import { computed, onMounted, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import client from '../api/client';
import ItemForm from '../components/ItemForm.vue';
import { statusLabel, statusTagType } from '../utils/status';
import { normalizeRole } from '../utils/auth';
import { extractErrorMessage } from '../utils/error';
import { formatDateTime, formatUserDisplay } from '../utils/format';
import { normalizeItem } from '../utils/normalizers';
import { useAuthStore } from '../stores/auth';
import type { LostItem } from '../types';

const router = useRouter();
const items = ref<LostItem[]>([]);
const search = ref('');
const loadHint = ref('正在加载物品，请稍候');
const loading = ref(false);
const loadError = ref(false);
const statusFilter = ref('');
const locationFilter = ref('');
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

const statusOptions = computed(() => [
  { value: 'OPEN', label: statusLabel('OPEN') },
  { value: 'CLAIMED', label: statusLabel('CLAIMED') },
  { value: 'RESOLVED', label: statusLabel('RESOLVED') },
  { value: 'CLOSED', label: statusLabel('CLOSED') }
]);

const locationOptions = computed(() =>
  Array.from(new Set(items.value.map((item) => item.location).filter(Boolean))).map((location) =>
    location as string
  )
);

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
  loadError.value = false;
  const params: Record<string, unknown> = {};
  if (isUser.value) {
    params.userId = auth.user?.userId;
  }
  try {
    const { data } = await client.get<LostItem[]>(
      '/items',
      Object.keys(params).length ? { params } : undefined
    );
    if (Array.isArray(data)) {
      items.value = data.map(normalizeItem);
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
    loadError.value = true;
    ElMessage.error(loadHint.value);
  } finally {
    loading.value = false;
  }
}

const filteredItems = computed(() =>
  items.value.filter((item) => {
    const query = search.value.trim().toLowerCase();
    if (
      query &&
      ![item.title, item.description, item.location, item.username, item.status]
        .map((field) => field?.toString?.().toLowerCase?.() || '')
        .some((field) => field.includes(query))
    ) {
      return false;
    }
    if (statusFilter.value && item.status !== statusFilter.value) return false;
    if (locationFilter.value && item.location !== locationFilter.value) return false;
    return true;
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

function goDetail(row: LostItem) {
  router.push({ name: 'item-detail', params: { id: row.id } });
}

function addItem(newItem: LostItem) {
  items.value.unshift(normalizeItem(newItem));
  ElMessage.success('物品已添加');
}

function resetFilters() {
  search.value = '';
  statusFilter.value = '';
  locationFilter.value = '';
}

function formatStatus(_row: LostItem, _column: unknown, cellValue: string) {
  return statusLabel(cellValue);
}

function statusColor(status?: string) {
  return statusTagType(status);
}

function formatLocation(_row: LostItem, _column: unknown, cellValue: string) {
  return cellValue || '未提供地点';
}

function formatTitle(_row: LostItem, _column: unknown, cellValue: string) {
  return cellValue || '未命名物品';
}

function formatOwner(row: LostItem) {
  return formatUserDisplay(row.username, row.userId);
}

function formatCreatedAt(_row: LostItem, _column: unknown, cellValue: string) {
  return formatDateTime(cellValue);
}
</script>

<style scoped>
.page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px 16px 48px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  background: radial-gradient(circle at 18% 20%, rgba(79, 70, 229, 0.05), transparent 32%),
    radial-gradient(circle at 82% 16%, rgba(16, 185, 129, 0.05), transparent 30%),
    #f9fafb;
}

.role-alert {
  margin-bottom: 16px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.card-eyebrow {
  margin: 0;
  font-size: 12px;
  letter-spacing: 0.04em;
  text-transform: uppercase;
  color: var(--el-text-color-secondary);
}

.card-title {
  font-weight: 700;
  font-size: 16px;
}

.panel {
  border-radius: 14px;
  box-shadow: 0 14px 32px -24px rgba(15, 23, 42, 0.35);
}

.filters {
  padding: 12px;
}

.filter-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 12px;
  align-items: center;
}

@media (max-width: 960px) {
  .page {
    padding: 16px 12px 36px;
  }
}
</style>
