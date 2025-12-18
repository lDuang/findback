<template>
  <div class="page">
    <el-card v-if="displayItem">
      <template #header>
        <div class="card-header">
          <span>{{ displayItem.title }}</span>
          <el-tag :type="displayStatusType">{{ displayStatus }}</el-tag>
        </div>
      </template>
      <p><strong>地点：</strong> {{ displayItem.location }}</p>
      <p><strong>描述：</strong> {{ displayItem.description }}</p>
      <div v-if="displayItem.imageUrl" class="image-row">
        <strong>图片：</strong>
        <el-image :src="displayItem.imageUrl" fit="cover" style="width: 200px; height: 200px" />
      </div>
    </el-card>
    <el-empty v-else :description="loadHint" />

    <el-card style="margin-top: 16px">
      <template #header>
        <span>提交认领</span>
      </template>
      <el-alert
        class="claim-alert"
        :title="claimAlertTitle"
        :description="claimAlertDescription"
        type="info"
        show-icon
      />
      <claim-form :item-id="displayItem?.id" @submitted="addClaim" />
      <el-divider />
      <h4>我的认领</h4>
      <el-timeline v-if="claims.length">
        <el-timeline-item
          v-for="claim in claims"
          :key="claim.id"
          :type="statusColor(claim.status)"
          :timestamp="claimStatusLabel(claim.status)"
        >
          <div class="claim-content">
            <p>{{ claim.reason || '暂无详情' }}</p>
            <div v-if="claim.evidenceUrl" class="claim-proof">
              <el-image :src="claim.evidenceUrl" fit="cover" style="width: 120px; height: 120px" />
              <span>认领凭证</span>
            </div>
          </div>
        </el-timeline-item>
      </el-timeline>
      <p v-else class="muted">{{ claimsHint }}</p>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import client from '../api/client';
import ClaimForm from '../components/ClaimForm.vue';
import { useAuthStore } from '../stores/auth';
import { statusLabel, statusTagType } from '../utils/status';
import { normalizeRole } from '../utils/auth';
import { extractErrorMessage } from '../utils/error';

const route = useRoute();
const item = ref(null);
const claims = ref([]);
const auth = useAuthStore();
const loadHint = ref('正在加载物品详情，请稍候');
const claimsHint = ref('暂无认领记录');
const normalizedRole = computed(() => normalizeRole(auth.user?.role));
const isAdmin = computed(() => normalizedRole.value === 'ADMIN');
const isItemOwner = computed(
  () => item.value && auth.user?.userId && `${item.value.userId}` === `${auth.user.userId}`
);
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
  const id = route.params.id;
  try {
    const { data } = await client.get(`/items/${id}`);
    item.value = data || null;
    if (!item.value) {
      loadHint.value = '未找到对应的物品详情。';
    } else {
      loadHint.value = '';
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
    const { data } = await client.get(`/items/${id}/claims`);
    if (Array.isArray(data)) {
      const shouldShowAll = isAdmin.value || isItemOwner.value;
      claims.value = shouldShowAll ? data : filterMyClaims(data);
      claimsHint.value = claims.value.length ? '' : '暂无认领记录';
    }
  } catch (error) {
    console.warn('Failed to load claims', error);
    claims.value = [];
    const message = extractErrorMessage(error);
    claimsHint.value = message ? `认领记录加载失败：${message}` : '认领记录加载失败，请稍后重试。';
  }
});

function addClaim(claim) {
  if (isMyClaim(claim)) {
    claims.value.unshift(claim);
  }
  ElMessage.success('认领已提交');
}

function filterMyClaims(list) {
  if (!auth.user?.userId) return [];
  return list.filter((claim) => `${claim.userId}` === `${auth.user.userId}`);
}

function isMyClaim(claim) {
  if (!claim || !auth.user?.userId) return false;
  return `${claim.userId}` === `${auth.user.userId}`;
}

function statusColor(status) {
  return statusTagType(status);
}

function claimStatusLabel(status) {
  return statusLabel(status);
}

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
</script>

<style scoped>
.page {
  max-width: 960px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.image-row {
  display: flex;
  align-items: center;
  gap: 12px;
}
.claim-alert {
  margin-bottom: 12px;
}
.claim-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
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
</style>
