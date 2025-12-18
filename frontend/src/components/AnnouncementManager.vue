<template>
  <el-card>
    <template #header>
      <div class="card-header">
        <span>公告管理</span>
        <el-button type="primary" size="small" @click="addAnnouncement">新增公告</el-button>
      </div>
    </template>
    <el-table :data="announcements" style="width: 100%">
      <el-table-column prop="title" label="标题" />
      <el-table-column prop="content" label="内容" />
      <el-table-column prop="createdAt" label="创建时间" :formatter="formatCreatedAt" width="180" />
      <el-table-column label="操作" width="120">
        <template #default="scope">
          <el-button type="text" size="small" @click="remove(scope.$index)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script lang="ts" setup>
import { onMounted, reactive } from 'vue';
import { ElMessageBox, ElMessage } from 'element-plus';
import client from '../api/client';
import { buildDisplayError } from '../utils/error';
import { formatDateTime } from '../utils/format';
import { normalizeAnnouncement } from '../utils/normalizers';
import type { Announcement } from '../types';

const announcements = reactive<Announcement[]>([]);

onMounted(async () => {
  try {
    const { data } = await client.get<Announcement[]>('/announcements');
    if (Array.isArray(data)) {
      announcements.splice(0, announcements.length, ...data.map(normalizeAnnouncement));
      return;
    }
  } catch (error) {
    console.warn('Falling back to local announcements', error);
    const message = buildDisplayError('公告列表加载失败', error);
    ElMessage.warning((message || '公告列表加载失败') + '，已使用本地占位数据。');
  }
  announcements.splice(0, announcements.length, {
    id: 'local-welcome',
    title: '欢迎使用',
    content: '请清晰描述失物信息。',
    createdAt: new Date().toISOString()
  });
});

async function addAnnouncement() {
  try {
    const { value } = await ElMessageBox.prompt('请输入公告内容', '创建公告', {
      confirmButtonText: '创建',
      cancelButtonText: '取消'
    });
    const announcement: Announcement = {
      title: '公告',
      content: value,
      id: Date.now(),
      createdAt: new Date().toISOString()
    };
    try {
      const { data } = await client.post<Announcement>('/announcements', announcement);
      announcements.push(
        data || {
          ...announcement,
          id: Date.now()
        }
      );
    } catch (error) {
      console.warn('Fallback to local announcement', error);
      announcements.push({ ...announcement, id: Date.now() });
      const message = buildDisplayError('创建公告未同步到服务器', error);
      ElMessage.warning(message || '创建公告未同步到服务器，已在本地展示。');
    }
    ElMessage.success('公告已添加');
  } catch (error) {
    // cancelled
  }
}

async function remove(index: number) {
  const item = announcements[index];
  await ElMessageBox.confirm(`删除公告“${item.title}”？`, '确认操作', {
    type: 'warning'
  });
  announcements.splice(index, 1);
  try {
    await client.delete(`/announcements/${item.id || index}`);
  } catch (error) {
    console.warn('Deletion not persisted', error);
    const message = buildDisplayError('删除公告未同步到服务器', error);
    ElMessage.warning(message || '删除公告未同步到服务器。');
  }
  ElMessage.success('已删除');
}

function formatCreatedAt(_row: Announcement, _column: unknown, value?: string) {
  return formatDateTime(value);
}
</script>

<style scoped>
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
</style>
