<template>
  <div class="news-management">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="新闻列表" name="list">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>新闻管理</span>
              <el-button type="primary" @click="addNews">添加新闻</el-button>
            </div>
          </template>
          
          <div class="search-bar">
            <el-input 
              v-model="searchQuery" 
              placeholder="搜索标题/作者/简介" 
              clearable
              @clear="fetchNews"
              @keyup.enter="searchNews"
            >
              <template #append>
                <el-button icon="Search" @click="searchNews" />
              </template>
            </el-input>
          </div>
          
          <el-table :data="newsList" border v-loading="loading">
            <el-table-column label="标题">
              <template #default="{ row }">
                <router-link :to="{ name: 'NewsDetail', params: { id: row.id } }">
                  {{ row.title }}
                </router-link>
              </template>
            </el-table-column>
            <el-table-column prop="author" label="作者" />
            <el-table-column label="封面">
              <template #default="{ row }">
                <el-image 
                  :src="row.cover" 
                  style="width: 60px; height: 40px" 
                  fit="cover" 
                  v-if="row.cover"
                />
              </template>
            </el-table-column>
            <el-table-column label="状态">
              <template #default="{ row }">
                <el-tag :type="statusTagType(row.status)">
                  {{ statusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="发布时间">
              <template #default="{ row }">
                {{ formatDate(row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="220">
              <template #default="{ row }">
                <el-button size="small" @click="editNews(row)">编辑</el-button>
                <el-button size="small" type="danger" @click="deleteNews(row.id)">删除</el-button>
                <el-button 
                  v-if="row.status === 0 && user.role === 0" 
                  size="small" 
                  type="success" 
                  @click="approveNews(row.id)"
                >
                  通过
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          
          <div class="pagination">
            <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :total="total"
              :page-sizes="[5, 10, 20, 50]"
              layout="total, sizes, prev, pager, next, jumper"
              @current-change="fetchNews"
              @size-change="handleSizeChange"
            />
          </div>
        </el-card>
      </el-tab-pane>

    </el-tabs>

    <!-- 添加/编辑新闻对话框 -->
    <el-dialog v-model="showDialog" :title="dialogTitle" width="80%">
      <el-form :model="currentNews" label-width="100px" ref="newsForm">
        <el-form-item label="新闻标题" prop="title" required>
          <el-input v-model="currentNews.title" />
        </el-form-item>
        
        <el-form-item label="封面图片" prop="cover">
          <div class="cover-options">
            <div class="url-option">
              <el-input 
                v-model="coverUrl" 
                placeholder="输入图片URL" 
                clearable
                @keyup.enter="setCoverFromUrl"
              >
                <template #append>
                  <el-button @click="setCoverFromUrl">应用</el-button>
                </template>
              </el-input>
            </div>
            
            
          </div>
          
          <div class="cover-preview">
            <el-image 
              v-if="currentNews.cover"
              :src="currentNews.cover" 
              style="width: 200px; height: 120px; margin-top: 10px" 
              fit="cover"
            />
            <div v-else class="cover-placeholder">
              <el-icon><Picture /></el-icon>
              <span>无封面图片</span>
            </div>
          </div>
        </el-form-item>
        
        <el-form-item label="内容摘要" prop="summary" required>
          <el-input v-model="currentNews.summary" type="textarea" rows="3" />
        </el-form-item>
        <el-form-item label="详细内容" prop="content" required>
          <el-input v-model="currentNews.content" type="textarea" rows="8" />
        </el-form-item>
        <el-form-item label="作者" prop="author" required>
          <el-input v-model="currentNews.author" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="saveNews">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import api from '@/services/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Picture } from '@element-plus/icons-vue'

const user = JSON.parse(localStorage.getItem('user') || '{}') 
const newsList = ref([])
const pendingNews = ref([])
const currentNews = ref({})
const showDialog = ref(false)
const activeTab = ref('list')
const searchQuery = ref('')
const currentPage = ref(1)
const auditPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const auditTotal = ref(0)
const loading = ref(false)
const auditLoading = ref(false)
const newsForm = ref(null)
const coverUrl = ref('')

const dialogTitle = computed(() => 
  currentNews.value.id ? '编辑新闻' : '添加新闻'
)

onMounted(async () => {
  await fetchNews()

})

const fetchNews = async () => {
  try {
    loading.value = true
    const params = {
      page: Math.max(1, parseInt(currentPage.value)),
      size: Math.max(1, parseInt(pageSize.value)),
      query: searchQuery.value?.trim() || undefined,
      author: user.role === 1 ? user.nickname : undefined
    }

    const res = await api.getNews(params)
    console.log(res)
    newsList.value = res.list || []
    total.value = res.total || 0
  } catch (error) {
    console.error('获取新闻列表失败:', error)
    ElMessage.error(`获取新闻列表失败: ${error.message || '参数错误'}`)
  } finally {
    loading.value = false
  }
}



const searchNews = () => {
  currentPage.value = 1
  fetchNews()
}

const handleSizeChange = (newSize) => {
  pageSize.value = newSize
  currentPage.value = 1
  fetchNews()
}

const handleAuditSizeChange = (newSize) => {
  pageSize.value = newSize
  auditPage.value = 1
  fetchPendingNews()
}

const statusTagType = (status) => {
  switch (status) {
    case 0: return 'warning'
    case 1: return 'success'
    case 2: return 'danger'
    default: return 'info'
  }
}

const statusText = (status) => {
  switch (status) {
    case 0: return '待审核'
    case 1: return '已发布'
    case 2: return '已驳回'
    default: return '未知'
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`
}

const addNews = () => {
  currentNews.value = {
    title: '',
    cover: '',
    summary: '',
    content: '',
    author: user.nickname || user.username,
    status: user.role === 0 ? 1 : 0
  }
  coverUrl.value = ''
  showDialog.value = true
}

const editNews = (news) => {
  currentNews.value = { ...news }
  coverUrl.value = news.cover || ''
  showDialog.value = true
}

const deleteNews = (id) => {
  ElMessageBox.confirm('确定要删除这条新闻吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await api.deleteNews(id)
      ElMessage.success('删除成功')
      await fetchNews()
    } catch (error) {
      console.error('删除新闻失败:', error)
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

const approveNews = async (id) => {
    await api.approveNews(id)
    ElMessage.success('审核通过')
    await fetchNews()
    await fetchPendingNews()

}

const rejectNews = async (id) => {
  try {
    await api.rejectNews(id, '内容不符合要求')
    ElMessage.warning('已驳回')
    await fetchPendingNews()
  } catch (error) {
    console.error('驳回操作失败:', error)
    ElMessage.error('驳回失败')
  }
}

const saveNews = async () => {
  try {
    await newsForm.value.validate()
    
    if (currentNews.value.id) {
      await api.updateNews(currentNews.value)
    } else {
      await api.createNews(currentNews.value)
    }
    
    ElMessage.success('保存成功')
    showDialog.value = false
    await fetchNews()
  } catch (error) {
    console.error('保存新闻失败:', error)
    if (error.errors) {
      ElMessage.error('请填写必填字段')
    } else {
      ElMessage.error('保存失败: ' + (error.response?.data?.message || '未知错误'))
    }
  }
}

const setCoverFromUrl = () => {
  if (coverUrl.value) {
    currentNews.value.cover = coverUrl.value
    ElMessage.success('封面URL已应用')
  } else {
    ElMessage.warning('请输入有效的图片URL')
  }
}
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.search-bar {
  margin-bottom: 20px;
  width: 300px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.cover-options {
  display: flex;
  gap: 15px;
  margin-bottom: 10px;
}

.url-option {
  flex: 1;
}

.upload-option {
  flex-shrink: 0;
}

.cover-preview {
  margin-top: 10px;
}

.cover-placeholder {
  width: 200px;
  height: 120px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
  border: 1px dashed #dcdfe6;
  border-radius: 4px;
  color: #909399;
  font-size: 14px;
}

.cover-placeholder .el-icon {
  font-size: 40px;
  margin-bottom: 10px;
}
</style>