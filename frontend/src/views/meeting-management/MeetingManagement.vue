<template>
  <div class="meeting-management">
    <div class="header">
      <el-input v-model="searchName" placeholder="会议名称" class="search-input"></el-input>
      <el-input v-model="searchOrganizer" placeholder="创建人" class="search-input"></el-input>
      <el-date-picker v-model="searchStartTime" type="datetime" placeholder="开始时间" class="search-input"></el-date-picker>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button type="primary" @click="goToAddMeeting">添加会议</el-button>
    </div>
    <el-table :data="meetings" style="width: 100%">
      <el-table-column prop="id" label="会议ID" width="80"></el-table-column>
      <el-table-column prop="name" label="会议名称"></el-table-column>
      <el-table-column prop="organizer" label="组织者"></el-table-column>
      <el-table-column prop="startTime" label="开始时间"></el-table-column>
      <el-table-column prop="endTime" label="结束时间"></el-table-column>
      <el-table-column prop="status" label="状态"></el-table-column>
      <el-table-column label="操作" width="180">
        <template #default="scope">
          <el-button @click="editMeeting(scope.row.id)" type="text">修改</el-button>
          <el-button @click="confirmDelete(scope.row.id)" type="text" class="delete-button">删除</el-button>
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
      searchName: '',
      searchOrganizer: '',
      searchStartTime: null,
      currentPage: 1,
      pageSize: 10,
      total: 0
    };
  },
  methods: {
    async fetchMeetings() {
      try {
        const response = await api.getAllMeetings();
        this.meetings = response;
      } catch (error) {
        console.error('Failed to fetch meetings:', error);
        this.$message.error('获取会议列表失败');
      }
    },
    async handleSearch() {
      try {
        const params = {
          name: this.searchName,
          organizer: this.searchOrganizer,
          startTime: this.searchStartTime ? this.searchStartTime.toISOString() : null
        };
        const response = await api.searchMeetings(params);
        this.meetings = response;
      } catch (error) {
        console.error('Failed to search meetings:', error);
        this.$message.error('搜索会议失败');
      }
    },
    async deleteMeeting(id) {
      try {
        await api.deleteMeeting(id);
        this.$message.success('删除成功');
        // 【修正】在这里添加 await，以确保刷新列表的错误也能被捕获
        await this.fetchMeetings();
      } catch (error) {
        // 现在这个 catch 块可以同时处理 deleteMeeting 和 fetchMeetings 的错误
        console.error('Failed to delete or refresh meetings:', error);
        this.$message.error('操作失败');
      }
    },
    confirmDelete(id) {
      this.$confirm('是否确认删除该会议?', '系统提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
        // 【优化】将回调改为 async 函数，以便使用 await
      }).then(async () => {
        await this.deleteMeeting(id);
      }).catch(() => {
        this.$message.info('已取消删除');
      });
    },
    goToAddMeeting() {
      this.$router.push({name: 'AddMeeting'});
    },
    editMeeting(id) {
      this.$router.push({name: 'EditMeeting', params: {id}});
    }
  },
  mounted() {
    this.fetchMeetings();
  }
};
</script>

<style scoped>
/* 样式保持不变 */
</style>