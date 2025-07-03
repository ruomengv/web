<!-- src/views/NewsDetail.vue -->
<template>
  <DetailLayout>
    <template #header>
      <div class="news-header">
        <h1 class="news-title">{{ news.title }}</h1>
        <div class="news-meta">
          <div class="author">
            <el-avatar :size="40" :src="authorAvatar" />
            <div>
              <div>{{ news.author }}</div>
              <div class="date">{{ formatDate(news.createTime) }}</div>
            </div>
          </div>
          <div class="stats">
            <span><el-icon><View /></el-icon> {{ views }} 阅读</span>
            <span><el-icon><ChatDotRound /></el-icon> {{ comments.length }} 评论</span>
            <span><el-icon><Star /></el-icon> {{ likes }} 点赞</span>
          </div>
        </div>
      </div>
    </template>
    
    <template #content>
      <div class="news-cover">
        <el-image 
          :src="news.cover" 
          fit="cover" 
          class="cover-image"
        />
        <div class="image-caption" v-if="news.summary">{{ news.summary }}</div>
      </div>
      
      <div class="news-content">
        <div v-html="news.content"></div>
      </div>
      
      <div class="news-tags">
        <el-tag 
          v-for="(tag, index) in tags" 
          :key="index" 
          type="info"
          size="large"
        >
          {{ tag }}
        </el-tag>
      </div>
      
      <div class="news-actions">
        <el-button :type="liked ? 'danger' : ''" @click="toggleLike">
          <el-icon><Star /></el-icon>
          {{ liked ? '已点赞' : '点赞' }} ({{ likes }})
        </el-button>
        <el-button @click="shareNews">
          <el-icon><Share /></el-icon>
          分享
        </el-button>
        <el-button @click="copyLink">
          <el-icon><Link /></el-icon>
          复制链接
        </el-button>
      </div>
      
      <div class="comments-section">
        <h2>评论 ({{ comments.length }})</h2>
        
        <div class="add-comment">
          <el-avatar :size="40" :src="userAvatar" />
          <el-input 
            v-model="newComment" 
            placeholder="写下你的评论..." 
            @keyup.enter="postComment"
          >
            <template #append>
              <el-button @click="postComment">发送</el-button>
            </template>
          </el-input>
        </div>
        
        <div class="comments-list">
          <div v-for="(comment, index) in comments" :key="index" class="comment">
            <el-avatar :size="40" :src="comment.avatar" />
            <div class="comment-content">
              <div class="comment-header">
                <strong>{{ comment.author }}</strong>
                <span class="date">{{ comment.date }}</span>
              </div>
              <div class="comment-text">{{ comment.text }}</div>
              <div class="comment-actions">
                <el-button text @click="toggleLikeComment(comment)">
                  <el-icon><Star /></el-icon>
                  {{ comment.likes }}
                </el-button>
                <el-button text @click="replyTo(comment)">回复</el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </template>
    
    <template #sidebar>
      <div class="author-card">
        <div class="author-header">
          <el-avatar :size="80" :src="authorAvatar" />
          <h3>{{ news.author }}</h3>
          <div class="author-title">高级编辑</div>
        </div>
        
        <div class="author-stats">
          <div class="stat-item">
            <div class="stat-value">42</div>
            <div class="stat-label">文章</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">1.2万</div>
            <div class="stat-label">粉丝</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">24.5万</div>
            <div class="stat-label">阅读</div>
          </div>
        </div>
        
        <el-button type="primary" plain class="follow-btn">
          <el-icon><Plus /></el-icon>
          关注作者
        </el-button>
        
        <div class="author-description">
          资深行业分析师，专注于技术创新和市场趋势研究，拥有10年行业经验。
        </div>
      </div>
      
      <div class="popular-news">
        <h3>热门动态</h3>
        <div v-for="(item, index) in popularNews" :key="index" class="popular-item">
          <div class="popular-index">{{ index + 1 }}</div>
          <div class="popular-content">
            <div class="popular-title">{{ item.title }}</div>
            <div class="popular-meta">
              <span>{{ item.author }}</span>
              <span>{{ item.views }}阅读</span>
            </div>
          </div>
        </div>
      </div>
    </template>
    
    <template #related>
      <RelatedItems title="相关动态" :items="relatedNews" type="news" />
    </template>
  </DetailLayout>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import DetailLayout from './DetailLayout.vue';
import RelatedItems from '@/components/RelatedItems.vue';
import { View, ChatDotRound, Star, Share, Link, Plus } from '@element-plus/icons-vue';
import api from '@/services/api';

const route = useRoute();
const newsId = route.params.id;

// 新闻数据
const news = ref({
  id: '',
  title: '',
  cover: '',
  summary: '',
  content: '',
  author: '',
  createTime: '',
});

const views = ref(3245);
const likes = ref(128);
const liked = ref(false);
const newComment = ref('');
const comments = ref([]);

// 模拟标签
const tags = ref(['行业趋势', '技术创新', '市场分析', '2025预测']);

// 模拟热门新闻
const popularNews = ref([
  { id: 101, title: '2025年科技行业五大趋势预测', author: '张分析师', views: 12450 },
  { id: 102, title: '人工智能在制造业的应用前景', author: '王研究员', views: 9870 },
  { id: 103, title: '区块链技术如何重塑金融行业', author: '李教授', views: 8450 },
  { id: 104, title: '可持续能源发展的最新突破', author: '赵工程师', views: 7620 },
]);

// 模拟相关新闻
const relatedNews = ref([
  { id: 2, title: '全球供应链变革对企业的影响', author: '陈观察员', views: 3240 },
  { id: 3, title: '数字化转型中的企业战略调整', author: '周顾问', views: 2870 },
  { id: 4, title: '后疫情时代的经济复苏路径', author: '吴分析师', views: 4120 },
]);

const authorAvatar = computed(() => {
  return `https://robohash.org/${news.value.author}?size=100x100`;
});

const userAvatar = computed(() => {
  return 'https://robohash.org/user-avatar?size=100x100';
});

const formatDate = (dateStr) => {
  if (!dateStr) return '未知';
  const date = new Date(dateStr);
  return date.toLocaleDateString();
};

const toggleLike = () => {
  liked.value = !liked.value;
  likes.value += liked.value ? 1 : -1;
  
  ElMessage({
    type: liked.value ? 'success' : 'info',
    message: liked.value ? '感谢您的点赞！' : '已取消点赞'
  });
};

const shareNews = () => {
  ElMessage({
    type: 'info',
    message: '分享功能即将上线，敬请期待！'
  });
};

const copyLink = () => {
  navigator.clipboard.writeText(window.location.href);
  ElMessage.success('链接已复制到剪贴板');
};

const postComment = () => {
  if (!newComment.value.trim()) {
    ElMessage.warning('请输入评论内容');
    return;
  }
  
  comments.value.unshift({
    id: Date.now(),
    author: '当前用户',
    avatar: userAvatar.value,
    text: newComment.value,
    date: '刚刚',
    likes: 0
  });
  
  newComment.value = '';
  ElMessage.success('评论已发布');
};

const toggleLikeComment = (comment) => {
  comment.likes += comment.liked ? -1 : 1;
  comment.liked = !comment.liked;
};

const replyTo = (comment) => {
  newComment.value = `@${comment.author} `;
  document.querySelector('.add-comment .el-input__inner').focus();
};

onMounted(async () => {
  try {
    // 从API获取新闻详情
    const res = await api.getNewsDetail(newsId);
    news.value = res;
    
    // 模拟加载评论
    setTimeout(() => {
      comments.value = [
        { 
          id: 1, 
          author: '行业观察者', 
          avatar: 'https://robohash.org/comment1', 
          text: '这篇文章非常有见地，特别是关于未来趋势的分析部分。', 
          date: '2小时前', 
          likes: 12 
        },
        { 
          id: 2, 
          author: '技术爱好者', 
          avatar: 'https://robohash.org/comment2', 
          text: '作者提出的创新点很有意思，期待看到更多实际应用案例。', 
          date: '3小时前', 
          likes: 8 
        }
      ];
    }, 500);
  } catch (error) {
    console.error('获取新闻详情失败:', error);
    ElMessage.error('获取新闻详情失败');
  }
});
</script>

<style scoped>
.news-header {
  margin-bottom: 30px;
}

.news-title {
  font-size: 2rem;
  margin-bottom: 20px;
  color: #1a1a1a;
  line-height: 1.4;
}

.news-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
  color: #666;
}

.author {
  display: flex;
  align-items: center;
  gap: 15px;
}

.author > div {
  line-height: 1.4;
}

.date {
  font-size: 0.9rem;
  color: #909399;
}

.stats {
  display: flex;
  gap: 20px;
  font-size: 0.95rem;
}

.stats span {
  display: flex;
  align-items: center;
  gap: 5px;
}

.news-cover {
  margin-bottom: 30px;
  border-radius: 8px;
  overflow: hidden;
  position: relative;
}

.cover-image {
  width: 100%;
  height: 400px;
  display: block;
}

.image-caption {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(0, 0, 0, 0.7);
  color: white;
  padding: 15px 20px;
  font-size: 0.95rem;
}

.news-content {
  line-height: 1.8;
  font-size: 1.05rem;
  color: #333;
  margin-bottom: 40px;
}

.news-content :deep(h2) {
  margin: 25px 0 15px;
  color: #1a1a1a;
  border-left: 4px solid #409EFF;
  padding-left: 12px;
}

.news-content :deep(p) {
  margin-bottom: 15px;
}

.news-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 30px;
}

.news-actions {
  display: flex;
  gap: 15px;
  margin-bottom: 40px;
}

.comments-section h2 {
  margin-bottom: 20px;
  color: #1a1a1a;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.add-comment {
  display: flex;
  gap: 15px;
  margin-bottom: 30px;
}

.add-comment .el-input {
  flex: 1;
}

.comments-list {
  display: flex;
  flex-direction: column;
  gap: 25px;
}

.comment {
  display: flex;
  gap: 15px;
}

.comment-content {
  flex: 1;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.comment-header .date {
  font-size: 0.85rem;
  color: #909399;
}

.comment-text {
  line-height: 1.6;
  margin-bottom: 10px;
}

.comment-actions {
  display: flex;
  gap: 15px;
}

.author-card {
  background-color: #f5f7fa;
  border-radius: 8px;
  padding: 25px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  margin-bottom: 30px;
}

.author-header {
  margin-bottom: 20px;
}

.author-header h3 {
  margin: 15px 0 5px;
}

.author-title {
  color: #909399;
  font-size: 0.9rem;
}

.author-stats {
  display: flex;
  justify-content: space-around;
  margin: 25px 0;
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 1.2rem;
  font-weight: bold;
  color: #409EFF;
}

.stat-label {
  font-size: 0.85rem;
  color: #909399;
}

.follow-btn {
  width: 100%;
  margin-bottom: 20px;
}

.author-description {
  font-size: 0.9rem;
  color: #606266;
  line-height: 1.6;
}

.popular-news h3 {
  margin-bottom: 15px;
  color: #1a1a1a;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.popular-item {
  display: flex;
  gap: 15px;
  padding: 15px 0;
  border-bottom: 1px solid #f0f0f0;
}

.popular-item:last-child {
  border-bottom: none;
}

.popular-index {
  width: 30px;
  height: 30px;
  background-color: #f5f7fa;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  flex-shrink: 0;
  margin-top: 5px;
}

.popular-item:nth-child(1) .popular-index {
  background-color: #f56c6c;
  color: white;
}

.popular-item:nth-child(2) .popular-index {
  background-color: #e6a23c;
  color: white;
}

.popular-item:nth-child(3) .popular-index {
  background-color: #409EFF;
  color: white;
}

.popular-content {
  flex: 1;
}

.popular-title {
  font-weight: 500;
  margin-bottom: 5px;
  line-height: 1.4;
  cursor: pointer;
  transition: color 0.3s;
}

.popular-title:hover {
  color: #409EFF;
}

.popular-meta {
  font-size: 0.85rem;
  color: #909399;
  display: flex;
  gap: 15px;
}
</style>