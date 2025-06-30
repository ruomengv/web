<!-- src/components/RelatedItems.vue -->
<template>
  <div class="related-items">
    <h2 class="section-title">{{ title }}</h2>
    <div class="items-container">
      <div 
        v-for="(item, index) in items" 
        :key="index" 
        class="item-card"
        @click="navigateTo(item.id)"
      >
        <div class="item-cover">
          <el-image :src="getCover(item)" fit="cover" />
        </div>
        <div class="item-content">
          <h3 class="item-title">{{ item.title }}</h3>
          <div class="item-meta">
            <span v-if="type === 'course'">
              <el-icon><VideoPlay /></el-icon> {{ formatDuration(item.duration) }}
            </span>
            <span v-if="type === 'news'">
              <el-icon><View /></el-icon> {{ item.views }} 阅读
            </span>
            <span>
              <el-icon><User /></el-icon> {{ item.author }}
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { VideoPlay, View, User } from '@element-plus/icons-vue';
import { useRouter } from 'vue-router';

const props = defineProps({
  title: String,
  items: Array,
  type: {
    type: String,
    default: 'course'
  }
});

const router = useRouter();

const getCover = (item) => {
  if (item.cover) return item.cover;
  return props.type === 'course' 
    ? 'https://via.placeholder.com/300x200?text=Course'
    : 'https://via.placeholder.com/300x200?text=News';
};

const formatDuration = (minutes) => {
  const hours = Math.floor(minutes / 60);
  const mins = minutes % 60;
  return hours > 0 ? `${hours}小时${mins}分钟` : `${mins}分钟`;
};

const navigateTo = (id) => {
  const routeName = props.type === 'course' ? 'CourseDetail' : 'NewsDetail';
  router.push({ name: routeName, params: { id } });
};
</script>

<style scoped>
/* 样式与之前提供的RelatedItems.vue相同 */
</style>