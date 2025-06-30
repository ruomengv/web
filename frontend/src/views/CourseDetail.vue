<!-- src/views/CourseDetail.vue -->
<template>
  <DetailLayout>
    <template #header>
      <div class="course-header">
        <h1 class="course-title">{{ course.title }}</h1>
        <div class="course-meta">
          <div class="author">
            <el-avatar :size="40" :src="authorAvatar" />
            <span>{{ course.author }}</span>
          </div>
          <div class="stats">
            <span><el-icon><View /></el-icon> {{ views }} 次学习</span>
            <span><el-icon><Clock /></el-icon> {{ duration }} 分钟</span>
            <span><el-icon><Star /></el-icon> {{ course.sort }}</span>
          </div>
        </div>
      </div>
    </template>
    
    <template #content>
      <div class="course-video">
        <video-player :src="course.videoUrl" />
      </div>
      
      <div class="course-description">
        <h2>课程简介</h2>
        <p>{{ course.summary }}</p>
      </div>
      
      <div class="course-content">
        <h2>课程大纲</h2>
        <el-collapse v-model="activeSections">
          <el-collapse-item 
            v-for="(section, index) in syllabus" 
            :key="index" 
            :title="section.title"
            :name="index"
          >
            <div class="section-content">
              <div 
                v-for="(item, i) in section.items" 
                :key="i" 
                class="lesson-item"
                :class="{ 'free': item.free }"
              >
                <el-icon><VideoPlay /></el-icon>
                <span>{{ item.title }}</span>
                <span class="duration">{{ item.duration }}分钟</span>
              </div>
            </div>
          </el-collapse-item>
        </el-collapse>
      </div>
    </template>
    
    <template #sidebar>
      <div class="course-actions">
        <el-button type="primary" size="large" @click="startLearning">
          <el-icon><VideoPlay /></el-icon>
          开始学习
        </el-button>
        
        <div class="secondary-actions">
          <el-button :type="isFavorite ? 'warning' : 'default'" @click="toggleFavorite">
            <el-icon><Star /></el-icon>
            {{ isFavorite ? '已收藏' : '收藏课程' }}
          </el-button>
          <el-button @click="shareCourse">
            <el-icon><Share /></el-icon>
            分享
          </el-button>
        </div>
        
        <div class="course-info">
          <h3>课程信息</h3>
          <ul>
            <li><strong>创建时间：</strong> {{ formatDate(course.createTime) }}</li>
            <li><strong>最近更新：</strong> {{ formatDate(course.updateTime) }}</li>
            <li><strong>难度级别：</strong> 
              <el-rate v-model="difficulty" disabled />
            </li>
            <li><strong>适合人群：</strong> {{ course.target }}</li>
          </ul>
        </div>
      </div>
    </template>
    
    <template #related>
      <RelatedItems title="相关课程" :items="relatedCourses" type="course" />
    </template>
  </DetailLayout>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import DetailLayout from './DetailLayout.vue';
import RelatedItems from '@/components/RelatedItems.vue';
import { View, Clock, Star, VideoPlay, Share } from '@element-plus/icons-vue';
import api from '@/services/api';

const route = useRoute();
const courseId = route.params.id;

// 课程数据
const course = ref({
  id: '',
  title: '',
  cover: '',
  summary: '',
  sort: 0,
  videoUrl: '',
  author: '',
  createTime: '',
  updateTime: '',
  target: '所有对课程感兴趣的学习者'
});

const views = ref(1250);
const duration = ref(180);
const isFavorite = ref(false);
const difficulty = ref(3);
const activeSections = ref([0]);

// 模拟课程大纲数据
const syllabus = ref([
  {
    title: '第一章：入门基础',
    items: [
      { title: '课程介绍', duration: 10, free: true },
      { title: '环境搭建', duration: 20, free: true },
      { title: '基础知识讲解', duration: 30, free: false }
    ]
  },
  {
    title: '第二章：核心概念',
    items: [
      { title: '核心概念解析', duration: 25, free: false },
      { title: '实战案例演示', duration: 45, free: false }
    ]
  },
  {
    title: '第三章：高级应用',
    items: [
      { title: '高级技巧讲解', duration: 35, free: false },
      { title: '项目实战', duration: 60, free: false }
    ]
  }
]);

// 模拟相关课程
const relatedCourses = ref([
  { id: 2, title: '进阶应用开发', author: '张老师', duration: 240, views: 890 },
  { id: 3, title: '实战项目演练', author: '王教授', duration: 320, views: 1200 },
  { id: 4, title: '基础概念精讲', author: '李老师', duration: 180, views: 2100 }
]);

const authorAvatar = computed(() => {
  return `https://robohash.org/${course.value.author}?size=100x100`;
});

const formatDate = (dateStr) => {
  if (!dateStr) return '未知';
  const date = new Date(dateStr);
  return date.toLocaleDateString();
};

const toggleFavorite = () => {
  isFavorite.value = !isFavorite.value;
  ElMessage({
    type: isFavorite.value ? 'success' : 'info',
    message: isFavorite.value ? '已添加到收藏' : '已从收藏移除'
  });
};

const shareCourse = () => {
  ElMessage({
    type: 'info',
    message: '分享功能即将上线，敬请期待！'
  });
};

const startLearning = () => {
  // 实际应用中跳转到第一课
  ElMessage({
    type: 'success',
    message: '开始学习课程！'
  });
};

onMounted(async () => {
  try {
    // 从API获取课程详情
    const res = await api.getCourseDetail(courseId);
    course.value = res;
  } catch (error) {
    console.error('获取课程详情失败:', error);
    ElMessage.error('获取课程详情失败');
  }
});
</script>

<style scoped>
.course-header {
  text-align: center;
  margin-bottom: 30px;
}

.course-title {
  font-size: 2.2rem;
  margin-bottom: 20px;
  color: #1a1a1a;
}

.course-meta {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 40px;
  flex-wrap: wrap;
  color: #666;
  font-size: 0.95rem;
}

.author {
  display: flex;
  align-items: center;
  gap: 10px;
}

.stats {
  display: flex;
  gap: 20px;
}

.stats span {
  display: flex;
  align-items: center;
  gap: 5px;
}

.course-video {
  margin-bottom: 30px;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.course-description {
  margin-bottom: 40px;
  line-height: 1.8;
}

.course-description h2 {
  margin-bottom: 15px;
  color: #1a1a1a;
  border-left: 4px solid #409EFF;
  padding-left: 12px;
}

.course-content h2 {
  margin-bottom: 20px;
  color: #1a1a1a;
}

.section-content {
  padding: 10px 0;
}

.lesson-item {
  display: flex;
  align-items: center;
  padding: 10px 15px;
  border-radius: 6px;
  margin-bottom: 8px;
  background-color: #f9f9f9;
  transition: all 0.3s;
}

.lesson-item:hover {
  background-color: #f0f7ff;
  transform: translateX(5px);
}

.lesson-item.free {
  background-color: #f0f9eb;
}

.lesson-item .el-icon {
  margin-right: 10px;
  color: #409EFF;
}

.lesson-item.free .el-icon {
  color: #67C23A;
}

.lesson-item .duration {
  margin-left: auto;
  color: #909399;
}

.course-actions {
  background-color: #f5f7fa;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.course-actions .el-button {
  width: 100%;
  margin-bottom: 15px;
}

.secondary-actions {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.secondary-actions .el-button {
  flex: 1;
}

.course-info {
  border-top: 1px solid #ebeef5;
  padding-top: 20px;
}

.course-info h3 {
  margin-bottom: 15px;
  color: #1a1a1a;
}

.course-info ul {
  list-style: none;
  padding: 0;
}

.course-info li {
  margin-bottom: 12px;
  display: flex;
}

.course-info li strong {
  min-width: 80px;
  display: inline-block;
  color: #606266;
}
</style>