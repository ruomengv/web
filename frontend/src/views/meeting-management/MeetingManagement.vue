<template>
  <div class="meeting-management">
    <div class="header">
      <div class="search-group">
        <el-input v-model="searchParams.name" placeholder="会议名称" class="search-input" clearable></el-input>
        <el-input v-model="searchParams.organizer" placeholder="创建人" class="search-input" clearable></el-input>

        <el-date-picker
            v-model="searchDateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            class="search-input"
            clearable>
        </el-date-picker>

        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </div>
      <div class="action-group">
        <el-button type="primary" icon="el-icon-plus" @click="goToAddMeeting">添加会议</el-button>
      </div>
    </div>

    <el-table :data="meetings" style="width: 100%" v-loading="loading">
      <el-table-column prop="id" label="会议ID" width="80"></el-table-column>
      <el-table-column prop="name" label="会议名称" show-overflow-tooltip></el-table-column>
      <el-table-column prop="organizer" label="组织者"></el-table-column>
      <el-table-column prop="startTime" label="开始时间" :formatter="formatTableDateTime"></el-table-column>
      <el-table-column prop="endTime" label="结束时间" :formatter="formatTableDateTime"></el-table-column>
      <el-table-column prop="status" label="状态"></el-table-column>
      <el-table-column label="操作" width="180">
        <template #default="scope">
          <el-button @click="editMeeting(scope.row.id)" type="primary" link>修改</el-button>
          <el-button @click="confirmDelete(scope.row.id)" type="danger" link>删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script>
import api from '@/services/api.js';

export default {
  name: 'MeetingManagement',
  data() {
    return {
      meetings: [],
      loading: false, // 【新增】加载状态，提升用户体验
      // 【修改】使用一个对象来聚合搜索参数
      searchParams: {
        name: '',
        organizer: ''
      },
      // 【修改】用于 el-date-picker 的 v-model
      searchDateRange: [],
    };
  },
  methods: {
    // 【优化】将列表获取逻辑统一，方便复用
    async fetchMeetings(params = {}) {
      this.loading = true;
      try {
        // 如果有查询参数，则调用搜索接口，否则调用获取全部列表的接口
        const response = Object.keys(params).length > 0
            ? await api.searchMeetings(params)
            : await api.getAllMeetings();
        this.meetings = response;
      } catch (error) {
        console.error('Failed to fetch meetings:', error);
        this.$message.error('获取会议列表失败');
      } finally {
        this.loading = false;
      }
    },
    handleSearch() {
      // 准备最终发送到后端的参数
      const params = {
        ...this.searchParams,
        // 【修改】处理日期范围
        searchStartDate: this.searchDateRange && this.searchDateRange[0]
            ? this.formatDateTimeForApi(this.searchDateRange[0])
            : null,
        searchEndDate: this.searchDateRange && this.searchDateRange[1]
            ? this.formatDateTimeForApi(this.searchDateRange[1])
            : null
      };

      // 过滤掉值为 null 或空字符串的参数
      const finalParams = Object.fromEntries(
          Object.entries(params).filter(([_, v]) => v != null && v !== '')
      );

      this.fetchMeetings(finalParams);
    },
    // 【新增】重置搜索条件
    resetSearch() {
      this.searchParams.name = '';
      this.searchParams.organizer = '';
      this.searchDateRange = [];
      this.fetchMeetings(); // 重置后加载全部数据
    },
    async deleteMeeting(id) {
      try {
        // 【修改】后端的 deleteMeeting 接口需要一个 JSON 对象作为 body
        await api.deleteMeeting({ id });
        this.$message.success('删除成功');
        this.fetchMeetings(); // 重新加载数据
      } catch (error) {
        console.error('Failed to delete meeting:', error);
        this.$message.error('删除失败');
      }
    },
    confirmDelete(id) {
      this.$confirm('是否确认删除该会议?', '系统提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.deleteMeeting(id);
      }).catch(() => {
        this.$message.info('已取消删除');
      });
    },
    goToAddMeeting() {
      this.$router.push({ name: 'AddMeeting' });
    },
    editMeeting(id) {
      this.$router.push({ name: 'EditMeeting', params: { id } });
    },
    //辅助函数，格式化日期以匹配后端 API 要求 (YYYY-MM-DDTHH:mm:ss)
    formatDateTimeForApi(date) {
      if (!date) return null;
      const pad = (num) => num.toString().padStart(2, '0');
      const year = date.getFullYear();
      const month = pad(date.getMonth() + 1);
      const day = pad(date.getDate());
      const hours = pad(date.getHours());
      const minutes = pad(date.getMinutes());
      const seconds = pad(date.getSeconds());
      return `${year}-${month}-${day}T${hours}:${minutes}:${seconds}`;
    },
    //辅助函数，用于在表格中格式化日期显示
    formatTableDateTime(row, column, cellValue) {
      if (!cellValue) return '';
      // 只取 YYYY-MM-DD HH:mm 部分
      return cellValue.replace('T', ' ').substring(0, 16);
    }
  },
  mounted() {
    this.fetchMeetings();
  }
};
</script>

<style scoped>
.meeting-management {
  padding: 20px;
}
.header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}
.search-group {
  display: flex;
  gap: 10px; /* 使用 gap 属性来创建间距 */
}
.search-input {
  width: 200px;
}
.action-group {
}
.delete-button {
  color: #F56C6C;
}
</style>