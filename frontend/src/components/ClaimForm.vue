<template>
  <el-form :model="form" label-width="120px" @submit.prevent>
    <el-form-item label="物品 ID">
      <el-input v-model="form.itemId" :disabled="isItemLocked" placeholder="请输入物品 ID" />
    </el-form-item>
    <el-form-item label="认领理由">
      <el-input v-model="form.reason" type="textarea" placeholder="请提供认领证明或描述" />
    </el-form-item>
    <el-form-item label="上传凭证">
      <div class="upload-wrapper">
        <el-upload
          class="upload-block"
          :http-request="handleUpload"
          :show-file-list="false"
          accept="image/*"
        >
          <el-button :loading="uploading">上传图片</el-button>
        </el-upload>
        <div v-if="form.evidenceUrl" class="preview">
          <el-image :src="form.evidenceUrl" fit="cover" style="width: 120px; height: 120px" />
          <span class="preview-text">已上传</span>
        </div>
        <div v-else class="preview placeholder">未上传凭证</div>
      </div>
    </el-form-item>
    <el-form-item>
      <el-button type="primary" :loading="loading" @click="submit">提交认领</el-button>
    </el-form-item>
  </el-form>
</template>

<script lang="ts" setup>
import { computed, reactive, ref, watch } from 'vue';
import { ElMessage, type UploadRequestOptions } from 'element-plus';
import client from '../api/client';
import { useAuthStore } from '../stores/auth';
import { buildDisplayError } from '../utils/error';
import { normalizeClaim } from '../utils/normalizers';
import type { Claim } from '../types';

const props = defineProps<{ itemId?: string | number }>();
const emit = defineEmits<{ (event: 'submitted', claim: Claim): void }>();
const auth = useAuthStore();
const loading = ref(false);
const form = reactive({
  itemId: props.itemId || '',
  reason: '',
  evidenceUrl: ''
});
const uploading = ref(false);

const isItemLocked = computed(() => !!props.itemId);

watch(
  () => props.itemId,
  (value) => {
    form.itemId = value || '';
  }
);

async function submit() {
  if (!form.itemId) {
    ElMessage.warning('请输入物品 ID');
    return;
  }
  if (!auth.user?.userId) {
    ElMessage.warning('请登录后提交认领');
    return;
  }

  const itemId = Number(form.itemId);
  if (Number.isNaN(itemId)) {
    ElMessage.warning('物品 ID 格式不正确');
    return;
  }

  loading.value = true;
  try {
    const payload = { itemId, reason: form.reason, evidenceUrl: form.evidenceUrl, userId: auth.user.userId };
    const { data } = await client.post<Claim>('/claims', payload);
    ElMessage.success('认领已提交');
    const normalized = normalizeClaim(
      data || {
        ...payload,
        id: Date.now().toString(),
        status: 'PENDING',
        username: auth.user.username,
        createdAt: new Date().toISOString()
      }
    );
    emit('submitted', normalized);
    form.itemId = props.itemId || '';
    form.reason = '';
    form.evidenceUrl = '';
  } catch (error) {
    console.error(error);
    const message = buildDisplayError('提交认领失败', error);
    ElMessage.error(message || '提交认领失败');
  } finally {
    loading.value = false;
  }
}

async function handleUpload(options: UploadRequestOptions) {
  if (!options.file) return;
  uploading.value = true;
  try {
    const formData = new FormData();
    formData.append('file', options.file);
    const { data } = await client.post<{ url: string }>('/files/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });
    form.evidenceUrl = data.url;
    ElMessage.success('凭证上传成功');
    options.onSuccess?.(data);
  } catch (error) {
    console.error(error);
    const message = buildDisplayError('凭证上传失败', error);
    ElMessage.error(message || '凭证上传失败');
    options.onError?.(error as Parameters<NonNullable<UploadRequestOptions['onError']>>[0]);
  } finally {
    uploading.value = false;
  }
}
</script>

<style scoped>
.upload-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
}
.upload-block {
  display: inline-flex;
}
.preview {
  display: flex;
  align-items: center;
  gap: 8px;
}
.preview.placeholder {
  color: var(--el-text-color-secondary);
}
.preview-text {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
</style>
