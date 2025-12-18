<template>
  <el-card>
    <template #header>
      <div class="card-header">
        <div>
          <p class="eyebrow">Banner 管理</p>
          <span class="title">轮播图</span>
        </div>
        <el-button type="primary" size="small" @click="resetForm">新增 Banner</el-button>
      </div>
    </template>

    <el-form :model="form" label-width="120px" class="banner-form" @submit.prevent>
      <el-form-item label="标题">
        <el-input v-model="form.title" placeholder="请输入 Banner 标题" />
      </el-form-item>
      <el-form-item label="跳转链接">
        <el-input v-model="form.link" placeholder="例如：https://example.com" />
      </el-form-item>
      <el-form-item label="图片">
        <upload-card v-model="form.imageUrl" button-label="上传 Banner" tip="建议尺寸 1200x400" />
      </el-form-item>
      <el-form-item label="描述">
        <el-input v-model="form.description" type="textarea" placeholder="可选的描述信息" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="saving" @click="submit">{{ submitLabel }}</el-button>
        <el-button v-if="form.id" @click="resetForm">取消编辑</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="banners" style="width: 100%; margin-top: 16px" :empty-text="bannerEmptyText">
      <el-table-column prop="title" label="标题" min-width="160" />
      <el-table-column prop="imageUrl" label="图片" width="160">
        <template #default="scope">
          <el-image v-if="scope.row.imageUrl" :src="scope.row.imageUrl" fit="cover" class="thumb" />
          <span v-else class="muted">未上传</span>
        </template>
      </el-table-column>
      <el-table-column prop="link" label="链接">
        <template #default="scope">
          <a v-if="scope.row.link" :href="scope.row.link" target="_blank" rel="noreferrer" class="link">{{ scope.row.link }}</a>
          <span v-else class="muted">无</span>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" :formatter="formatCreatedAt" width="180" />
      <el-table-column label="操作" width="160">
        <template #default="scope">
          <el-button link size="small" type="primary" @click="edit(scope.row)">编辑</el-button>
          <el-popconfirm title="确认删除此 Banner 吗？" @confirm="remove(scope.row)">
            <template #reference>
              <el-button link size="small" type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import client from '../api/client';
import UploadCard from './UploadCard.vue';
import { buildDisplayError } from '../utils/error';
import { formatDateTime } from '../utils/format';
import { normalizeBanner } from '../utils/normalizers';
import type { Banner } from '../types';

const banners = ref<Banner[]>([]);
const bannerEmptyText = ref('正在加载 Banner...');
const form = reactive<Banner>({
  id: '',
  title: '',
  imageUrl: '',
  link: '',
  description: ''
});
const saving = ref(false);

const submitLabel = computed(() => (form.id ? '保存修改' : '创建 Banner'));

onMounted(async () => {
  await loadBanners();
});

async function loadBanners() {
  try {
    const { data } = await client.get<Banner[]>('/banners');
    if (Array.isArray(data)) {
      banners.value = data.map(normalizeBanner).filter((banner) => banner.imageUrl);
      bannerEmptyText.value = banners.value.length ? '' : '暂无 Banner，请先上传。';
    }
  } catch (error) {
    console.warn('Failed to load banners', error);
    const message = buildDisplayError('加载 Banner 失败', error);
    bannerEmptyText.value = message || '加载 Banner 失败';
  }
}

async function submit() {
  if (!form.title || !form.imageUrl) {
    ElMessage.warning('请填写标题并上传图片');
    return;
  }
  saving.value = true;
  try {
    const payload = { title: form.title, imageUrl: form.imageUrl, link: form.link, description: form.description };
    if (form.id) {
      const { data } = await client.put<Banner>(`/banners/${form.id}`, payload);
      const normalized = normalizeBanner({ ...form, ...data });
      updateLocalBanner(normalized);
      ElMessage.success('Banner 已更新');
    } else {
      const { data } = await client.post<Banner>('/banners', payload);
      const normalized = normalizeBanner(data || { ...payload, id: Date.now() });
      banners.value.unshift(normalized);
      ElMessage.success('Banner 已创建');
    }
    resetForm();
  } catch (error) {
    console.warn('Failed to save banner', error);
    const message = buildDisplayError('保存 Banner 失败', error);
    ElMessage.error(message || '保存 Banner 失败');
  } finally {
    saving.value = false;
  }
}

function edit(banner: Banner) {
  Object.assign(form, banner);
}

async function remove(banner: Banner) {
  const original = [...banners.value];
  banners.value = banners.value.filter((item) => item.id !== banner.id);
  try {
    await client.delete(`/banners/${banner.id}`);
    ElMessage.success('已删除 Banner');
    bannerEmptyText.value = banners.value.length ? '' : '暂无 Banner，请先上传。';
  } catch (error) {
    banners.value = original;
    console.warn('Failed to delete banner', error);
    const message = buildDisplayError('删除 Banner 失败', error);
    ElMessage.error(message || '删除 Banner 失败');
  }
}

function resetForm() {
  form.id = '';
  form.title = '';
  form.imageUrl = '';
  form.link = '';
  form.description = '';
}

function updateLocalBanner(updated: Banner) {
  const index = banners.value.findIndex((item) => `${item.id}` === `${updated.id}`);
  if (index >= 0) {
    banners.value.splice(index, 1, updated);
  } else {
    banners.value.unshift(updated);
  }
}

function formatCreatedAt(_row: Banner, _column: unknown, value?: string) {
  return formatDateTime(value);
}
</script>

<style scoped>
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.title {
  font-weight: 700;
  font-size: 16px;
}

.eyebrow {
  margin: 0;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  letter-spacing: 0.04em;
}

.banner-form {
  margin-bottom: 12px;
}

.thumb {
  width: 120px;
  height: 72px;
  object-fit: cover;
  border-radius: 6px;
}

.muted {
  color: var(--el-text-color-secondary);
}

.link {
  color: var(--el-color-primary);
}
</style>
