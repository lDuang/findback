<template>
  <div class="page">
    <el-empty v-if="!displayItem" :description="loadHint" />
    <el-row v-else :gutter="16">
      <el-col :md="16" :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <div>
                <p class="card-eyebrow">认领进展</p>
                <span class="card-title">{{ displayItem.title }}</span>
              </div>
              <el-tag :type="displayStatusType">{{ displayStatus }}</el-tag>
            </div>
          </template>
          <div class="timeline-wrap">
            <h4>认领时间线</h4>
            <el-timeline v-if="sortedClaims.length">
              <el-timeline-item
                v-for="claim in sortedClaims"
                :key="claim.id"
                :type="statusColor(claim.status)"
                :timestamp="claimTimestamp(claim)"
              >
                <div class="claim-content">
                  <div class="claim-meta">
                    <el-tag size="small" :type="statusColor(claim.status)" disable-transitions>{{ claimStatusLabel(claim.status) }}</el-tag>
                    <span class="claim-user">{{ displayOwner(claim) }}</span>
                  </div>
                  <p>{{ claim.reason || '暂无详情' }}</p>
                  <div v-if="claim.evidenceUrl" class="claim-proof">
                    <el-image :src="claim.evidenceUrl" fit="cover" style="width: 120px; height: 120px" />
                    <span>认领凭证</span>
                  </div>
                </div>
              </el-timeline-item>
            </el-timeline>
            <p v-else class="muted">{{ claimsHint }}</p>
          </div>
          <el-divider />
          <div class="claim-form-block">
            <h4>提交认领</h4>
            <el-alert
              class="claim-alert"
              :title="claimAlertTitle"
              :description="claimAlertDescription"
              type="info"
              show-icon
            />
            <claim-form :item-id="displayItem?.id" @submitted="addClaim" />
          </div>
        </el-card>
      </el-col>
      <el-col :md="8" :span="8">
        <el-card class="side-card">
          <template #header>
            <div class="card-header">
              <span>物品信息</span>
              <el-tag :type="displayStatusType" effect="plain">{{ displayStatus }}</el-tag>
            </div>
          </template>
          <div class="item-visual">
            <el-image v-if="displayItem.imageUrl" :src="displayItem.imageUrl" fit="cover" class="item-image" />
            <div v-else class="placeholder">暂无图片</div>
          </div>
          <div class="item-meta">
            <p><strong>发布人：</strong> {{ displayOwner(displayItem) }}</p>
            <p><strong>创建时间：</strong> {{ formatCreatedAt(displayItem.createdAt) }}</p>
            <p><strong>地点：</strong> {{ displayItem.location }}</p>
            <p><strong>描述：</strong> {{ displayItem.description }}</p>
          </div>
          <div v-if="canUpdateStatus" class="status-actions">
            <p class="status-label">更新状态</p>
            <el-select v-model="statusValue" placeholder="请选择状态" size="small">
              <el-option v-for="option in statusOptions" :key="option.value" :label="option.label" :value="option.value" />
            </el-select>
            <el-button type="primary" size="small" :loading="statusUpdating" @click="updateStatus">保存</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import client from '../api/client';
import ClaimForm from '../components/ClaimForm.vue';
import { useAuthStore } from '../stores/auth';
import { statusLabel, statusTagType } from '../utils/status';
import { normalizeRole } from '../utils/auth';
import { extractErrorMessage } from '../utils/error';
import { formatDateTime, formatUserDisplay } from '../utils/format';
import { normalizeClaim, normalizeItem } from '../utils/normalizers';
import type { Claim, LostItem } from '../types';

const route = useRoute();
const item = ref<LostItem | null>(null);
const claims = ref<Claim[]>([]);
const auth = useAuthStore();
const loadHint = ref('正在加载物品详情，请稍候');
const claimsHint = ref('暂无认领记录');
const statusValue = ref('');
const statusUpdating = ref(false);
const normalizedRole = computed(() => normalizeRole(auth.user?.role));
const isAdmin = computed(() => normalizedRole.value === 'ADMIN');
const isItemOwner = computed(
  () => item.value && auth.user?.userId && `${item.value.userId}` === `${auth.user.userId}`
);
const canUpdateStatus = computed(() => isAdmin.value || isItemOwner.value);
const statusOptions = [
  { value: 'OPEN', label: statusLabel('OPEN') },
  { value: 'CLAIMED', label: statusLabel('CLAIMED') },
  { value: 'RESOLVED', label: statusLabel('RESOLVED') },
  { value: 'CLOSED', label: statusLabel('CLOSED') }
];
const claimAlertTitle = computed(() => {
  if (isAdmin.value) return '管理员视角：可查看该物品的全部认领';
  if (isItemOwner.value) return '物品发布者视角：可查看并管理该物品的全部认领';
  if (auth.user?.userId) return '普通用户视角：仅展示您提交的认领';
  return '访客视角：请登录后查看与您相关的认领记录';
});
const claimAlertDescription = computed(() => {
  if (isAdmin.value) return '列表将展示所有用户的认领记录，便于统一审批。';
  if (isItemOwner.value) return '作为物品所有者，您可以看到全部认领并决定处理结果。';
  if (auth.user?.userId) return '为了保护隐私，仅展示您自己提交的认领状态。';
  return '当前未登录，仅可查看物品信息，认领记录需要登录后才能加载。';
});

onMounted(async () => {
  const id = route.params.id as string;
  try {
    const { data } = await client.get<LostItem>(`/items/${id}`);
    item.value = data ? normalizeItem(data) : null;
    if (!item.value) {
      loadHint.value = '未找到对应的物品详情。';
    } else {
      loadHint.value = '';
      statusValue.value = item.value.status;
    }
  } catch (error) {
    console.warn('Failed to load item detail', error);
    item.value = null;
    const message = extractErrorMessage(error);
    loadHint.value = message ? `物品详情加载失败：${message}` : '物品详情加载失败，请稍后重试。';
  }

  if (!auth.user?.userId) {
    claimsHint.value = '请登录后查看您的认领记录。';
    return;
  }

  try {
    const { data } = await client.get<Claim[]>(`/items/${id}/claims`);
    if (Array.isArray(data)) {
      const normalizedClaims = data.map(normalizeClaim).sort(sortByCreatedAtDesc);
      const shouldShowAll = isAdmin.value || isItemOwner.value;
      claims.value = shouldShowAll ? normalizedClaims : filterMyClaims(normalizedClaims);
      claimsHint.value = claims.value.length ? '' : '暂无认领记录';
    }
  } catch (error) {
    console.warn('Failed to load claims', error);
    claims.value = [];
    const message = extractErrorMessage(error);
    claimsHint.value = message ? `认领记录加载失败：${message}` : '认领记录加载失败，请稍后重试。';
  }
});

watch(
  () => item.value?.status,
  (value) => {
    if (value) statusValue.value = value;
  }
);

function addClaim(claim: Claim) {
  const normalized = normalizeClaim(claim);
  if (isMyClaim(normalized)) {
    claims.value.unshift(normalized);
  }
  ElMessage.success('认领已提交');
}

function filterMyClaims(list: Claim[]) {
  const currentUserId = auth.user?.userId;
  if (!currentUserId) return [];
  return list.filter((claim) => `${claim.userId}` === `${currentUserId}`);
}

function isMyClaim(claim?: Claim | null) {
  const currentUserId = auth.user?.userId;
  if (!claim || !currentUserId) return false;
  return `${claim.userId}` === `${currentUserId}`;
}

function statusColor(status?: string) {
  return statusTagType(status);
}

function claimStatusLabel(status?: string) {
  return statusLabel(status);
}

const sortedClaims = computed(() => [...claims.value].sort(sortByCreatedAtDesc));

const displayStatus = computed(() => statusLabel(item.value?.status));
const displayStatusType = computed(() => statusColor(item.value?.status));

const displayItem = computed(() => {
  if (!item.value) return null;
  return {
    ...item.value,
    title: item.value.title || '未命名物品',
    location: item.value.location || '未提供地点',
    description: item.value.description || '暂无描述'
  };
});

function displayOwner(row?: { username?: string; userId?: string | number }) {
  return formatUserDisplay(row?.username, row?.userId as string | number);
}

function claimTimestamp(claim: Claim) {
  const created = formatDateTime(claim.createdAt);
  return created !== '—' ? created : claimStatusLabel(claim.status);
}

function formatCreatedAt(value?: string) {
  return formatDateTime(value);
}

function sortByCreatedAtDesc(a: Claim, b: Claim) {
  const aTime = new Date(a.createdAt || 0).getTime();
  const bTime = new Date(b.createdAt || 0).getTime();
  return bTime - aTime;
}

async function updateStatus() {
  if (!displayItem.value || !statusValue.value) {
    ElMessage.warning('请选择要更新的状态');
    return;
  }
  const previous = item.value?.status;
  statusUpdating.value = true;
  try {
    const payload = { status: statusValue.value };
    const { data } = await client.put<LostItem>(`/items/${displayItem.value.id}/status`, payload);
    const normalized = normalizeItem({ ...displayItem.value, ...data });
    item.value = normalized;
    statusValue.value = normalized.status;
    ElMessage.success('状态已更新');
  } catch (error) {
    try {
      const { data } = await client.put<LostItem>(`/items/${displayItem.value.id}`, {
        status: statusValue.value
      });
      const normalized = normalizeItem({ ...displayItem.value, ...data });
      item.value = normalized;
      statusValue.value = normalized.status;
      ElMessage.success('状态已更新');
    } catch (fallbackError) {
      console.warn('Failed to update status', fallbackError);
      if (previous && item.value) {
        item.value.status = previous;
        statusValue.value = previous;
      }
      const message = extractErrorMessage(fallbackError);
      ElMessage.error(message ? `更新状态失败：${message}` : '更新状态失败');
    }
  } finally {
    statusUpdating.value = false;
  }
}
</script>

<style scoped>
.page {
  max-width: 960px;
  margin: 0 auto;
  padding: 24px 16px 48px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  background: radial-gradient(circle at 18% 18%, rgba(79, 70, 229, 0.05), transparent 32%),
    radial-gradient(circle at 80% 12%, rgba(16, 185, 129, 0.05), transparent 30%),
    #f9fafb;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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
  font-size: 18px;
}

.claim-alert {
  margin-bottom: 12px;
}

.timeline-wrap {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.claim-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.claim-meta {
  display: flex;
  align-items: center;
  gap: 8px;
}

.claim-user {
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.claim-proof {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--el-text-color-secondary);
}

.muted {
  color: var(--el-text-color-secondary);
  margin: 0;
}

.side-card {
  position: sticky;
  top: 16px;
}

.item-visual {
  border: 1px dashed var(--el-border-color);
  border-radius: 12px;
  padding: 8px;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 8px;
}

.item-image {
  width: 100%;
  max-height: 260px;
  object-fit: cover;
  border-radius: 8px;
}

.item-meta {
  display: flex;
  flex-direction: column;
  gap: 6px;
  color: var(--el-text-color-regular);
}

.status-actions {
  margin-top: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.status-label {
  margin: 0;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.claim-form-block {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.placeholder {
  color: var(--el-text-color-secondary);
  padding: 24px 0;
}
</style>
