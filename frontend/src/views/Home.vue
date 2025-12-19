<template>
  <div class="home page-shell">
    <section class="hero">
      <div class="hero__content">
        <p class="eyebrow">失物招领 · 校园/社区互助</p>
        <h1>一站式失物招领平台</h1>
        <p class="lede">
          轻松发布失物信息、浏览待认领物品、跟踪认领进度。支持移动端访问，随时随地都能找到回家的路。
        </p>
        <div class="actions">
          <el-button type="primary" size="large" @click="goToItems">浏览失物/招领</el-button>
          <el-button type="success" plain size="large" @click="goToCreate">发布失物</el-button>
        </div>
        <div class="chips">
          <span class="chip">即时上传图片</span>
          <span class="chip">进度可视化</span>
          <span class="chip">支持管理员审核</span>
        </div>
      </div>
      <div class="hero__panel">
        <div class="panel-head">
          <span class="dot dot--success"></span>
          <span class="dot dot--warning"></span>
          <span class="dot dot--info"></span>
        </div>
        <div class="panel-body">
          <div class="stat">
            <p class="stat-label">今日新增</p>
            <p class="stat-value">18</p>
          </div>
          <div class="stat">
            <p class="stat-label">已归还</p>
            <p class="stat-value">132</p>
          </div>
          <div class="stat">
            <p class="stat-label">处理中</p>
            <p class="stat-value">47</p>
          </div>
        </div>
      </div>
    </section>

    <section class="banner-strip" v-loading="bannersLoading">
      <div class="card-header">
        <div>
          <p class="card-eyebrow">轮播图</p>
          <span class="card-title">最新横幅</span>
        </div>
        <el-button link type="primary" @click="goToItems">查看物品</el-button>
      </div>
      <div class="banner-surface" v-if="banners.length">
        <el-carousel height="220px" arrow="always" indicator-position="outside">
          <el-carousel-item v-for="banner in banners" :key="banner.id">
            <div class="banner-slide" :style="{ backgroundImage: banner.imageUrl ? `url(${banner.imageUrl})` : '' }">
              <div class="banner-overlay">
                <div class="banner-meta">
                  <p class="card-eyebrow">{{ formatCreatedAt(banner.createdAt) || '最新' }}</p>
                  <h3>{{ banner.title }}</h3>
                  <p class="banner-desc">{{ banner.description || '点击查看详情' }}</p>
                </div>
                <el-button v-if="banner.link" type="primary" size="small" @click="openLink(banner.link)">
                  立即查看
                </el-button>
              </div>
            </div>
          </el-carousel-item>
        </el-carousel>
      </div>
      <el-empty v-else :description="bannerHint" />
    </section>

    <section class="updates">
      <el-card class="announcement-card" v-loading="announcementLoading">
        <template #header>
          <div class="card-header">
            <div>
              <p class="card-eyebrow">公告栏</p>
              <span class="card-title">最新公告</span>
            </div>
            <el-button link type="primary" @click="goToItems">前往列表</el-button>
          </div>
        </template>
        <el-timeline v-if="announcements.length">
          <el-timeline-item
            v-for="announcement in announcements"
            :key="announcement.id"
            type="primary"
            :timestamp="formatCreatedAt(announcement.createdAt)"
          >
            <div class="announcement">
              <h4>{{ announcement.title }}</h4>
              <p>{{ announcement.content || '暂无内容' }}</p>
            </div>
          </el-timeline-item>
        </el-timeline>
        <el-empty v-else :description="announcementHint" />
      </el-card>
    </section>

    <section class="feature-grid">
      <el-card v-for="feature in featureCards" :key="feature.title" shadow="hover" class="feature">
        <div class="feature__icon">{{ feature.icon }}</div>
        <h3>{{ feature.title }}</h3>
        <p class="feature__desc">{{ feature.description }}</p>
        <el-button type="primary" link @click="feature.onClick">{{ feature.action }}</el-button>
      </el-card>
    </section>

    <section class="flow">
      <div class="flow__header">
        <h2>快速了解流程</h2>
        <p>从发布到认领，每一步都有清晰的指引与可视化状态。</p>
      </div>
      <div class="flow__grid">
        <el-card class="flow__card">
          <h4 id="quick-create">发布失物</h4>
          <p>填写名称、地点与描述，一键上传图片。发布后可在「物品列表」中随时编辑。</p>
          <el-alert
            type="success"
            show-icon
            title="提示：支持图片上传"
            description="上传后即可在详情页展示，方便他人确认。"
          />
        </el-card>
        <el-card class="flow__card">
          <h4>浏览与公告</h4>
          <p>按地点或状态筛选，公告板实时同步管理员发布的提醒，重要信息不错过。</p>
          <el-alert
            type="info"
            show-icon
            title="公告示例"
            description="礼堂拾获雨伞 3 把，请到服务台登记领取。"
          />
        </el-card>
        <el-card class="flow__card">
          <h4>认领进度</h4>
          <p>提交认领凭证后，可在详情页查看时间线，管理员与发布者都能透明处理。</p>
          <el-alert
            type="warning"
            show-icon
            title="状态可视化"
            description="待认领、认领中、已处理实时更新。"
          />
        </el-card>
      </div>
    </section>
  </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import client from '../api/client';
import { extractErrorMessage } from '../utils/error';
import { formatDateTime } from '../utils/format';
import { normalizeAnnouncement, normalizeBanner } from '../utils/normalizers';
import type { Announcement, Banner } from '../types';

interface FeatureCard {
  title: string;
  description: string;
  action: string;
  icon: string;
  onClick: () => void;
}

const router = useRouter();
const banners = ref<Banner[]>([]);
const bannerHint = ref('正在加载 Banner...');
const bannersLoading = ref(false);
const announcements = ref<Announcement[]>([]);
const announcementHint = ref('正在加载公告...');
const announcementLoading = ref(false);

onMounted(() => {
  loadBanners();
  loadAnnouncements();
});

const goToItems = () => router.push('/items');
const goToCreate = () => {
  const target = document.getElementById('quick-create');
  if (target) {
    target.scrollIntoView({ behavior: 'smooth', block: 'start' });
  } else {
    goToItems();
  }
};
const goToLogin = () => router.push('/login');

const featureCards = computed<FeatureCard[]>(() => [
  {
    title: '快速发布',
    description: '2 步完成失物登记，支持图片上传与地点标记。',
    action: '去发布',
    icon: '🧭',
    onClick: goToCreate
  },
  {
    title: '浏览与筛选',
    description: '按关键字、地点或状态浏览待认领物品。',
    action: '查看列表',
    icon: '🔍',
    onClick: goToItems
  },
  {
    title: '公告提醒',
    description: '管理员发布集中提醒，重要信息实时同步。',
    action: '前往登录',
    icon: '📣',
    onClick: goToLogin
  }
]);

async function loadBanners() {
  bannersLoading.value = true;
  try {
    const { data } = await client.get<Banner[]>('/banners');
    if (Array.isArray(data)) {
      banners.value = data.map(normalizeBanner).filter((banner) => banner.imageUrl);
      bannerHint.value = banners.value.length ? '' : '暂无轮播图';
    }
  } catch (error) {
    const message = extractErrorMessage(error);
    bannerHint.value = message ? `Banner 加载失败：${message}` : 'Banner 加载失败';
  } finally {
    bannersLoading.value = false;
  }
}

async function loadAnnouncements() {
  announcementLoading.value = true;
  try {
    const { data } = await client.get<Announcement[]>('/announcements');
    if (Array.isArray(data)) {
      announcements.value = data.map(normalizeAnnouncement);
      announcementHint.value = announcements.value.length ? '' : '暂无公告';
    }
  } catch (error) {
    const message = extractErrorMessage(error);
    announcementHint.value = message ? `公告加载失败：${message}` : '公告加载失败';
  } finally {
    announcementLoading.value = false;
  }
}

function openLink(link: string) {
  window.open(link, '_blank');
}

function formatCreatedAt(value?: string) {
  return formatDateTime(value);
}
</script>

<style scoped>
.home {
  display: flex;
  flex-direction: column;
  gap: 28px;
}

.hero {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 24px;
  align-items: center;
  background: #ffffff;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 16px;
  padding: 28px;
  box-shadow: var(--el-box-shadow-light);
}

.hero__content h1 {
  margin: 8px 0 12px;
  font-size: 32px;
  line-height: 1.3;
}

.hero__content .lede {
  color: var(--el-text-color-secondary);
  margin-bottom: 16px;
  max-width: 620px;
}

.actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}

.chips {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.chip {
  background: #fff;
  border: 1px solid var(--el-border-color-lighter);
  color: var(--el-text-color-regular);
  padding: 6px 12px;
  border-radius: 999px;
  font-size: 13px;
  box-shadow: var(--el-box-shadow-lighter);
}

.eyebrow {
  font-size: 13px;
  font-weight: 600;
  color: #4f46e5;
  letter-spacing: 0.04em;
  text-transform: uppercase;
}

.hero__panel {
  background: #0f172a;
  color: #e2e8f0;
  border-radius: 14px;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.08), 0 16px 30px -18px rgba(15, 23, 42, 0.8);
}

.panel-head {
  display: flex;
  gap: 8px;
  align-items: center;
}

.dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  display: inline-block;
}

.dot--success {
  background: #22c55e;
}

.dot--warning {
  background: #f59e0b;
}

.dot--info {
  background: #3b82f6;
}

.panel-body {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 12px;
}

.stat {
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 10px;
  padding: 12px;
}

.stat-label {
  margin: 0;
  color: #cbd5e1;
  font-size: 13px;
}

.stat-value {
  margin: 4px 0 0;
  font-size: 22px;
  font-weight: 700;
  color: #f8fafc;
}

.feature-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 16px;
}

.updates {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 16px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
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

.announcement-card {
  border-radius: 14px;
  box-shadow: var(--el-box-shadow-lighter);
}

.banner-strip {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 16px;
  background: #ffffff;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 14px;
  box-shadow: var(--el-box-shadow-lighter);
}

.banner-surface {
  border-radius: 12px;
  overflow: hidden;
}

.banner-slide {
  position: relative;
  height: 100%;
  border-radius: 12px;
  overflow: hidden;
  background-size: cover;
  background-position: center;
  display: flex;
  align-items: stretch;
  padding: 0;
  background-color: #0f172a;
}

.banner-overlay {
  background: linear-gradient(90deg, rgba(15, 23, 42, 0.92), rgba(15, 23, 42, 0.6));
  color: #fff;
  padding: 16px 18px;
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.banner-meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.banner-overlay h3 {
  margin: 0 0 4px;
}

.banner-desc {
  margin: 0 0 8px;
  color: rgba(255, 255, 255, 0.8);
}

.announcement h4 {
  margin: 0 0 4px;
}

.announcement p {
  margin: 0;
  color: var(--el-text-color-secondary);
}

.feature {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-height: 180px;
}

.feature__icon {
  font-size: 24px;
}

.feature__desc {
  flex: 1;
  color: var(--el-text-color-secondary);
  margin: 0;
}

.flow {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.flow__header h2 {
  margin: 0;
}

.flow__header p {
  margin: 4px 0 0;
  color: var(--el-text-color-secondary);
}

.flow__grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 16px;
}

.flow__card h4 {
  margin: 0 0 6px;
}

.flow__card p {
  margin: 0 0 12px;
  color: var(--el-text-color-secondary);
}

@media (max-width: 640px) {
  .home {
    padding: 24px 16px 48px;
  }

  .hero__content h1 {
    font-size: 26px;
  }

  .actions {
    width: 100%;
  }

  .hero__panel {
    order: -1;
  }
}
</style>
