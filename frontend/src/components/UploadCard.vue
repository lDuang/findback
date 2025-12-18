<template>
  <div class="upload-card">
    <div class="upload-actions">
      <el-upload
        class="upload-block"
        :http-request="handleUpload"
        :show-file-list="false"
        :accept="accept"
        :disabled="disabled || uploading"
      >
        <el-button :loading="uploading" type="primary" plain>{{ buttonLabel }}</el-button>
      </el-upload>
      <p v-if="tip" class="upload-tip">{{ tip }}</p>
    </div>
    <div class="upload-feedback">
      <el-progress
        v-if="uploading"
        :percentage="progress"
        :status="progressStatus"
        striped
        striped-flow
        :duration="20"
      />
      <el-alert
        v-else-if="errorMessage"
        type="error"
        show-icon
        :closable="false"
        :title="errorMessage"
        class="upload-alert"
      />
    </div>
    <div class="preview">
      <template v-if="modelValue">
        <el-image :src="modelValue" fit="cover" class="preview-image" />
        <div class="preview-meta">
          <p class="preview-title">已上传</p>
          <el-button link type="danger" size="small" @click="clear">移除</el-button>
        </div>
      </template>
      <div v-else class="placeholder">暂无文件</div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { computed, ref } from 'vue';
import { ElMessage, type UploadRequestOptions } from 'element-plus';
import client from '../api/client';
import { buildDisplayError } from '../utils/error';

const props = defineProps<{
  modelValue?: string;
  buttonLabel?: string;
  accept?: string;
  disabled?: boolean;
  tip?: string;
  action?: string;
}>();

const emit = defineEmits<{
  (event: 'update:modelValue', value: string): void;
  (event: 'uploaded', value: string): void;
}>();

const uploading = ref(false);
const progress = ref(0);
const errorMessage = ref('');

const uploadUrl = computed(() => props.action || '/files/upload');
const buttonLabel = computed(() => props.buttonLabel || '上传图片');
const accept = computed(() => props.accept || 'image/*');
const progressStatus = computed(() => (progress.value >= 100 ? 'success' : undefined));

async function handleUpload(options: UploadRequestOptions) {
  if (!options.file) return;
  uploading.value = true;
  errorMessage.value = '';
  progress.value = 0;
  try {
    const formData = new FormData();
    formData.append('file', options.file);
    const { data } = await client.post<{ url?: string; data?: { url?: string } }>(
      uploadUrl.value,
      formData,
      {
        headers: { 'Content-Type': 'multipart/form-data' },
        onUploadProgress: (event) => {
          if (event.total) {
            progress.value = Math.min(100, Math.round((event.loaded / event.total) * 100));
          }
        }
      }
    );
    const url = data?.url || data?.data?.url;
    if (!url) {
      throw new Error('上传成功但未返回文件地址');
    }
    emit('update:modelValue', url);
    emit('uploaded', url);
    ElMessage.success('上传成功');
    options.onSuccess?.(data);
    progress.value = 100;
  } catch (error) {
    const message = buildDisplayError('上传失败', error);
    errorMessage.value = message || '上传失败，请稍后重试。';
    options.onError?.(error as Parameters<NonNullable<UploadRequestOptions['onError']>>[0]);
  } finally {
    uploading.value = false;
    setTimeout(() => {
      progress.value = 0;
    }, 400);
  }
}

function clear() {
  emit('update:modelValue', '');
}
</script>

<style scoped>
.upload-card {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.upload-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.upload-block {
  display: inline-flex;
}

.upload-tip {
  margin: 0;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.upload-feedback {
  min-height: 32px;
}

.upload-alert {
  margin: 0;
}

.preview {
  display: flex;
  align-items: center;
  gap: 10px;
}

.preview-image {
  width: 120px;
  height: 120px;
  border-radius: 6px;
  overflow: hidden;
  object-fit: cover;
}

.preview-meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.preview-title {
  margin: 0;
  font-weight: 600;
}

.placeholder {
  color: var(--el-text-color-secondary);
}
</style>
