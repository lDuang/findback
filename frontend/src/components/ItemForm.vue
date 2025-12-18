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
      <upload-card v-model="form.imageUrl" tip="支持 jpg、png 等常见图片格式" />
    </el-form-item>
    <el-form-item>
      <el-button type="primary" :loading="loading" @click="submit">{{ submitLabel }}</el-button>
    </el-form-item>
  </el-form>
</template>

<script lang="ts" setup>
import { computed, reactive, ref, watch } from 'vue';
import { ElMessage } from 'element-plus';
import client from '../api/client';
import UploadCard from './UploadCard.vue';
import { useAuthStore } from '../stores/auth';
import { buildDisplayError } from '../utils/error';
import { normalizeItem } from '../utils/normalizers';
import type { LostItem } from '../types';

const props = defineProps<{ item?: LostItem | null }>();
const emit = defineEmits<{ (event: 'created', item: LostItem): void }>();
const auth = useAuthStore();
const loading = ref(false);
const form = reactive(buildInitialForm());

const isEditing = computed(() => !!props.item?.id);
const submitLabel = computed(() => (isEditing.value ? '保存修改' : '创建物品'));

watch(
  () => props.item,
  (value) => {
    const next = buildInitialForm(value || undefined);
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
    const payload = { ...form, userId: auth.user.userId, username: auth.user.username };
    const { data } = isEditing.value
      ? await client.put<LostItem>(`/items/${props.item?.id}`, payload)
      : await client.post<LostItem>('/items', payload);
    const savedItem: LostItem = normalizeItem(
      data || {
        ...payload,
        id: props.item?.id || Date.now().toString(),
        userId: auth.user?.userId ?? '',
        username: auth.user?.username ?? '',
        createdAt: new Date().toISOString()
      }
    );
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

function buildInitialForm(item?: Partial<LostItem>) {
  const normalized = item ? normalizeItem(item) : null;
  return {
    title: normalized?.title || '',
    description: normalized?.description || '',
    status: normalized?.status || 'OPEN',
    location: normalized?.location || '',
    imageUrl: normalized?.imageUrl || ''
  };
}

</script>

<style scoped></style>
