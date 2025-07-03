<template>
  <div class="course-management">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="课程列表" name="list">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>课程管理</span>
              <el-button type="primary" @click="addCourse" v-permission="['admin', 'company']">
                添加课程
              </el-button>
            </div>
          </template>
          
          <div class="search-bar">
            <el-input 
              v-model="searchQuery" 
              placeholder="搜索课程名称/作者" 
              clearable
              @clear="fetchCourses"
              @keyup.enter="searchCourses"
            >
              <template #append>
                <el-button icon="Search" @click="searchCourses" />
              </template>
            </el-input>
          </div>
          
          <el-table :data="courses" border stripe v-loading="loading">
            <el-table-column label="标题" min-width="150">
              <template #default="{ row }">
                <router-link :to="{ name: 'CourseDetail', params: { id: row.id } }">
                  {{ row.title }}
                </router-link>
              </template>
            </el-table-column>
            <el-table-column prop="author" label="作者" width="120" />
            <el-table-column label="封面" width="100">
              <template #default="{ row }">
                <el-image 
                  :src="row.cover" 
                  style="width: 60px; height: 40px" 
                  fit="cover"
                  :preview-src-list="[row.cover]"
                  v-if="row.cover"
                >
                  <template #error>
                    <div class="image-error">
                      <el-icon><Picture /></el-icon>
                    </div>
                  </template>
                </el-image>
              </template>
            </el-table-column>
            <el-table-column label="排序" width="80">
              <template #default="{ row }">
                {{ row.sort }}
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="statusTagType(row.status)">
                  {{ statusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="创建时间" width="180">
              <template #default="{ row }">
                {{ formatDate(row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="220" fixed="right">
              <template #default="{ row }">
                <el-button 
                  size="small" 
                  @click="editCourse(row)"
                  v-permission="['admin', 'creator']" 
                  :permission-params="{ creatorId: row.creatorId }"
                >
                  编辑
                </el-button>
                <el-button 
                  size="small" 
                  type="danger" 
                  @click="deleteCourse(row.id)"
                  v-permission="['admin', 'creator']" 
                  :permission-params="{ creatorId: row.creatorId }"
                >
                  删除
                </el-button>
                <el-button 
                  v-if="row.status === 0 && user.role === 0" 
                  size="small" 
                  type="success" 
                  @click="approveCourse(row.id)"
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
              @current-change="fetchCourses"
              @size-change="handleSizeChange"
            />
          </div>
        </el-card>
      </el-tab-pane>

    </el-tabs>

    <!-- 添加/编辑课程对话框 -->
    <el-dialog v-model="showDialog" :title="dialogTitle" width="80%">
      <el-form :model="currentCourse" label-width="100px" ref="courseForm">
        <el-form-item label="课程标题" prop="title" required>
          <el-input v-model="currentCourse.title" />
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
              v-if="currentCourse.cover"
              :src="currentCourse.cover" 
              style="width: 200px; height: 120px; margin-top: 10px" 
              fit="cover"
            />
            <div v-else class="cover-placeholder">
              <el-icon><Picture /></el-icon>
              <span>无封面图片</span>
            </div>
          </div>
        </el-form-item>
        
        <el-form-item label="课程简介" prop="summary" required>
          <el-input v-model="currentCourse.summary" type="textarea" rows="3" />
        </el-form-item>
        <el-form-item label="课程排序" prop="sort" required>
          <el-input-number 
            v-model="currentCourse.sort" 
            :min="1" 
            :max="100"
          />
        </el-form-item>
        <el-form-item label="视频链接" prop="videoUrl" required>
          <el-input v-model="currentCourse.videoUrl" />
        </el-form-item>
        <el-form-item label="作者" prop="author" required>
          <el-input v-model="currentCourse.author" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="saveCourse">保存</el-button>
      </template>
    </el-dialog>
    
    <!-- 驳回原因对话框 -->
    <el-dialog v-model="rejectDialogVisible" title="驳回原因" width="500px">
      <el-form :model="rejectForm">
        <el-form-item>
          <el-input 
            v-model="rejectForm.reason" 
            type="textarea" 
            placeholder="请输入驳回原因"
            rows="4"
          ></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="rejectCourse">确定</el-button>
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
const courses = ref([])
const pendingCourses = ref([])
const currentCourse = ref({})
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
const courseForm = ref(null)
const rejectDialogVisible = ref(false)
const rejectForm = ref({ reason: '' })
const rejectingCourseId = ref(null)
const coverUrl = ref('')

const dialogTitle = computed(() => 
  currentCourse.value.id ? '编辑课程' : '添加课程'
)

onMounted(async () => {
  await fetchCourses()
})

const fetchCourses = async () => {
  try {
    loading.value = true
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      keyword: searchQuery.value || undefined
    }
    
    const res = await api.getCourses(params)
    courses.value = res.list || []
    total.value = res.total || 0
    
    if (courses.value.length === 0 && total.value > 0 && currentPage.value > 1) {
      currentPage.value = Math.max(1, Math.ceil(total.value / pageSize.value))
      await fetchCourses()
    }
  } catch (error) {
    console.error('获取课程列表失败:', error)
    ElMessage.error(`获取课程列表失败: ${error.message || '服务器错误'}`)
  } finally {
    loading.value = false
  }
}


const searchCourses = () => {
  currentPage.value = 1
  fetchCourses()
}

const handleSizeChange = (newSize) => {
  pageSize.value = newSize
  currentPage.value = 1
  fetchCourses()
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
  return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
}

const addCourse = () => {
  currentCourse.value = {
    title: '',
    cover: '',
    summary: '',
    sort: 1,
    videoUrl: '',
    author: user.nickname || user.username,
    status: user.role === 0 ? 1 : 0,
    creatorId: user.uid
  }
  coverUrl.value = ''
  showDialog.value = true
}

const editCourse = (course) => {
  currentCourse.value = { ...course }
  coverUrl.value = course.cover || ''
  showDialog.value = true
}

const deleteCourse = (id) => {
  ElMessageBox.confirm('确定要删除这个课程吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await api.deleteCourse(id)
      ElMessage.success('删除成功')
      await fetchCourses()
      if (user.role === 0) await fetchPendingCourses()
    } catch (error) {
      console.error('删除课程失败:', error)
      ElMessage.error('删除失败: ' + (error.message || '未知错误'))
    }
  }).catch(() => {})
}

const approveCourse = async (id) => {
  try {
    await api.approveCourse(id)
    ElMessage.success('审核通过')
    await fetchCourses()
    await fetchPendingCourses()
  } catch (error) {
    console.error('审核操作失败:', error)
    ElMessage.error('审核失败: ' + (error.message || '未知错误'))
  }
}


const rejectCourse = async () => {
  if (!rejectForm.value.reason.trim()) {
    ElMessage.warning('请输入驳回原因')
    return
  }
  
  try {
    await api.rejectCourse(rejectingCourseId.value, rejectForm.value.reason)
    ElMessage.warning('已驳回')
    rejectDialogVisible.value = false
    await fetchPendingCourses()
  } catch (error) {
    console.error('驳回操作失败:', error)
    ElMessage.error('驳回失败: ' + (error.message || '未知错误'))
  }
}

const saveCourse = async () => {
  try {
    await courseForm.value.validate()
    
    if (currentCourse.value.id) {
      await api.updateCourse(currentCourse.value)
    } else {
      await api.createCourse(currentCourse.value)
    }
    
    ElMessage.success('保存成功')
    showDialog.value = false
    await fetchCourses()
  } catch (error) {
    console.error('保存课程失败:', error)
    if (error.fields) {
      ElMessage.error('请填写必填字段')
    } else {
      ElMessage.error('保存失败: ' + (error.message || '未知错误'))
    }
  }
}

const setCoverFromUrl = () => {
  if (coverUrl.value) {
    currentCourse.value.cover = coverUrl.value
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

.image-error {
  width: 60px;
  height: 40px;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
  color: #c0c4cc;
}
</style>