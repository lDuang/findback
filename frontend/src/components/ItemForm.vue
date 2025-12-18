<template>
  <el-form :model="form" label-width="120px" @submit.prevent>
    <el-form-item label="物品名称">
      <el-input v-model="form.title" placeholder="请输入物品名称" />
    </el-form-item>
    <el-form-item label="描述">
      <el-input v-model="form.description" type="textarea" placeholder="请填写简要描述" />
    </el-form-item>
    <el-form-item label="状态">
      <el-select v-model="form.status" placeholder="请选择状态">
        <el-option label="待认领" value="OPEN" />
        <el-option label="认领中" value="CLAIMED" />
        <el-option label="已处理" value="RESOLVED" />
      </el-select>
    </el-form-item>
    <el-form-item label="地点（可选）">
      <el-input v-model="form.location" placeholder="请输入发现地点" />
    </el-form-item>
    <el-form-item label="物品图片">
      <div class="upload-wrapper">
        <el-upload
          class="upload-block"
          :http-request="handleUpload"
          :show-file-list="false"
          accept="image/*"
        >
          <el-button :loading="uploading">上传图片</el-button>
        </el-upload>
        <div v-if="form.imageUrl" class="preview">
          <el-image :src="form.imageUrl" fit="cover" style="width: 120px; height: 120px" />
          <span class="preview-text">已上传</span>
        </div>
        <div v-else class="preview placeholder">暂无图片</div>
      </div>
    </el-form-item>
    <el-form-item>
      <el-button type="primary" :loading="loading" @click="submit">{{ submitLabel }}</el-button>
    </el-form-item>
  </el-form>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue';
import { ElMessage } from 'element-plus';
import client from '../api/client';
import { useAuthStore } from '../stores/auth';
import { buildDisplayError } from '../utils/error';

const props = defineProps({
  item: {
    type: Object,
    default: null
  }
});
const emit = defineEmits(['created']);
const auth = useAuthStore();
const loading = ref(false);
const uploading = ref(false);
const form = reactive(buildInitialForm());

const isEditing = computed(() => !!props.item?.id);
const submitLabel = computed(() => (isEditing.value ? '保存修改' : '创建物品'));

watch(
  () => props.item,
  (value) => {
    const next = buildInitialForm(value);
    Object.assign(form, next);
  },
  { deep: true }
);

async function submit() {
  if (!form.title) {
    ElMessage.warning('请输入物品名称');
    return;
  }
  if (!auth.user?.userId) {
    ElMessage.warning('请登录后创建物品');
    return;
  }
  loading.value = true;
  try {
    const payload = { ...form };
    const { data } = isEditing.value
      ? await client.put(`/items/${props.item.id}`, payload)
      : await client.post('/items', payload);
    const savedItem = data || { ...payload, id: props.item?.id || Date.now().toString() };
    ElMessage.success(isEditing.value ? '物品已更新' : '物品已创建');
    emit('created', savedItem);
    Object.assign(form, isEditing.value ? buildInitialForm(savedItem) : buildInitialForm());
  } catch (error) {
    console.error(error);
    const message = buildDisplayError(isEditing.value ? '更新物品失败' : '创建物品失败', error);
    ElMessage.error(message || (isEditing.value ? '更新物品失败' : '创建物品失败'));
  } finally {
    loading.value = false;
  }
}

function buildInitialForm(item) {
  return {
    title: item?.title || '',
    description: item?.description || '',
    status: item?.status || 'OPEN',
    location: item?.location || '',
    imageUrl: item?.imageUrl || ''
  };
}

async function handleUpload(options) {
  if (!options.file) {
    return;
  }
  uploading.value = true;
  try {
    const formData = new FormData();
    formData.append('file', options.file);
    const { data } = await client.post('/files/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });
    form.imageUrl = data.url;
    ElMessage.success('图片上传成功');
    options.onSuccess?.(data, options.file);
  } catch (error) {
    console.error(error);
    const message = buildDisplayError('图片上传失败', error);
    ElMessage.error(message || '图片上传失败');
    options.onError?.(error);
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
