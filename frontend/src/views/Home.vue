<template>
  <div class="page">
    <el-card>
      <h2>欢迎使用失物招领系统</h2>
      <p>在这里登记失物、提交认领、查看公告，基于 Vue 3 与 Element Plus 打造的便捷前端。</p>
      <el-space wrap>
        <el-button type="primary" @click="$router.push('/items')">浏览物品</el-button>
        <el-button @click="$router.push('/login')">登录</el-button>
      </el-space>
    </el-card>
  </div>
</template>

<style scoped>
.page {
  max-width: 960px;
  margin: 0 auto;
}
</style>
