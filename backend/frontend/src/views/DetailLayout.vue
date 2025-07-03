<!-- src/views/DetailLayout.vue -->
<template>
  <div class="detail-container">
    <!-- 返回按钮 -->
    <div class="back-button">
      <el-button type="primary" @click="goBack" circle>
        <el-icon><ArrowLeft /></el-icon>
      </el-button>
    </div>
    
    <!-- 主要内容区域 -->
    <div class="detail-content">
      <slot name="header"></slot>
      
      <div class="detail-body">
        <div class="main-content">
          <slot name="content"></slot>
        </div>
        
        <div class="sidebar">
          <slot name="sidebar"></slot>
        </div>
      </div>
    </div>
    
    <!-- 相关推荐 -->
    <div class="related-section">
      <slot name="related"></slot>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router';
import { ArrowLeft } from '@element-plus/icons-vue';

const router = useRouter();

const goBack = () => {
  router.go(-1);
};
</script>

<style scoped>
.detail-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  position: relative;
}

.back-button {
  position: absolute;
  top: 20px;
  left: 20px;
  z-index: 10;
}

.detail-content {
  margin-top: 50px;
}

.detail-body {
  display: flex;
  gap: 30px;
  margin-top: 30px;
}

.main-content {
  flex: 3;
}

.sidebar {
  flex: 1;
}

.related-section {
  margin-top: 60px;
}

@media (max-width: 768px) {
  .detail-body {
    flex-direction: column;
  }
  
  .sidebar {
    order: -1;
    margin-bottom: 30px;
  }
}
</style>