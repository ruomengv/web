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
            <el-table-column prop="title" label="标题" />
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
      
      <el-tab-pane label="待审核新闻" name="audit" v-if="user.role === 0">
        <el-card v-loading="auditLoading">
          <el-table :data="pendingNews" border>
            <el-table-column prop="title" label="标题" />
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
            <el-table-column label="操作" width="180">
              <template #default="{ row }">
                <el-button size="small" type="success" @click="approveNews(row.id)">通过</el-button>
                <el-button size="small" type="warning" @click="rejectNews(row.id)">驳回</el-button>
              </template>
            </el-table-column>
          </el-table>
          
          <div class="pagination">
            <el-pagination
              v-model:current-page="auditPage"
              v-model:page-size="pageSize"
              :total="auditTotal"
              :page-sizes="[5, 10, 20, 50]"
              layout="total, sizes, prev, pager, next, jumper"
              @current-change="fetchPendingNews"
              @size-change="handleAuditSizeChange"
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
          <el-upload 
            action="/api/upload" 
            :show-file-list="false"
            :before-upload="beforeCoverUpload"
            :http-request="uploadCover"
          >
            <el-button type="primary">上传封面</el-button>
            <div v-if="currentNews.cover" class="cover-preview">
              <el-image 
                :src="currentNews.cover" 
                style="width: 200px; height: 120px; margin-top: 10px" 
                fit="cover"
              />
            </div>
          </el-upload>
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

// 获取用户信息
const user = JSON.parse(localStorage.getItem('user') || '{}') 

// 数据状态
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

// 计算属性
const dialogTitle = computed(() => 
  currentNews.value.id ? '编辑新闻' : '添加新闻'
)

// 生命周期钩子
onMounted(async () => {
  await fetchNews()
  if (user.role === 0) {
    await fetchPendingNews()
  }
})

// 获取新闻列表（修正参数处理）
const fetchNews = async () => {
  try {
    loading.value = true
    
    // 确保参数合法
    const params = {
      page: Math.max(1, parseInt(currentPage.value)),
      size: Math.max(1, parseInt(pageSize.value)),
      query: searchQuery.value?.trim() || undefined,
      author: user.role === 1 ? user.nickname : undefined
    }

    const res = await api.getNews(params)
    newsList.value = res.list || []
    total.value = res.total || 0
  } catch (error) {
    console.error('获取新闻列表失败:', error)
    ElMessage.error(`获取新闻列表失败: ${error.message || '参数错误'}`)
  } finally {
    loading.value = false
  }
}

// 获取待审核新闻
const fetchPendingNews = async () => {
  try {
    auditLoading.value = true
    
    const params = {
      page: Math.max(1, parseInt(auditPage.value)),
      size: Math.max(1, parseInt(pageSize.value))
    }
    
    const res = await api.getPendingNews(params)
    pendingNews.value = res.list || []
    auditTotal.value = res.total || 0
  } catch (error) {
    console.error('获取待审核新闻失败:', error)
    ElMessage.error('获取待审核新闻失败')
  } finally {
    auditLoading.value = false
  }
}

// 搜索新闻
const searchNews = () => {
  currentPage.value = 1
  fetchNews()
}

// 分页大小变化处理
const handleSizeChange = (newSize) => {
  pageSize.value = newSize
  currentPage.value = 1
  fetchNews()
}

// 审核分页大小变化处理
const handleAuditSizeChange = (newSize) => {
  pageSize.value = newSize
  auditPage.value = 1
  fetchPendingNews()
}

// 状态标签样式
const statusTagType = (status) => {
  switch (status) {
    case 0: return 'warning'
    case 1: return 'success'
    case 2: return 'danger'
    default: return 'info'
  }
}

// 状态文本
const statusText = (status) => {
  switch (status) {
    case 0: return '待审核'
    case 1: return '已发布'
    case 2: return '已驳回'
    default: return '未知'
  }
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`
}

// 添加新闻
const addNews = () => {
  currentNews.value = {
    title: '',
    cover: '',
    summary: '',
    content: '',
    author: user.nickname || user.username,
    status: user.role === 0 ? 1 : 0
  }
  showDialog.value = true
}

// 编辑新闻
const editNews = (news) => {
  currentNews.value = { ...news }
  showDialog.value = true
}

// 删除新闻
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

// 审核通过
const approveNews = async (id) => {
  try {
    await api.approveNews(id)
    ElMessage.success('审核通过')
    await fetchNews()
    await fetchPendingNews()
  } catch (error) {
    console.error('审核操作失败:', error)
    ElMessage.error('审核失败')
  }
}

// 驳回新闻
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

// 保存新闻
const saveNews = async () => {
  try {
    // 验证表单
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

// 封面上传前验证
const beforeCoverUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传图片格式文件!')
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过5MB!')
  }
  
  return isImage && isLt5M
}

// 上传封面
const uploadCover = async (options) => {
  try {
    const formData = new FormData()
    formData.append('file', options.file)
    
    // 使用正确的URL
    const res = await api.uploadFile(formData)
    
    // 处理响应
    if (res && res.data && res.data.url) {
      currentNews.value.cover = res.data.url
      ElMessage.success('封面上传成功')
    } else {
      ElMessage.error('封面上传失败：未返回有效URL')
    }
  } catch (error) {
    console.error('封面上传失败:', error)
    
    let errorMsg = '封面上传失败：'
    if (error.response) {
      if (error.response.status === 404) {
        errorMsg += '上传接口不存在 (404)'
      } else {
        errorMsg += `服务器错误 (${error.response.status})`
      }
    } else if (error.request) {
      errorMsg += '服务器无响应'
    } else {
      errorMsg += error.message || '未知错误'
    }
    
    ElMessage.error(errorMsg)
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

.cover-preview {
  margin-top: 10px;
}
</style>