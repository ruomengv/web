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

      <el-table-column label="会议名称" prop="name" show-overflow-tooltip>
        <template #default="scope">
          <el-button type="primary" link @click="goToDetails(scope.row.id)">
            {{ scope.row.name }}
          </el-button>
        </template>
      </el-table-column>

      <el-table-column prop="organizer" label="组织者"></el-table-column>
      <el-table-column prop="startTime" label="开始时间" :formatter="formatTableDateTime"></el-table-column>
      <el-table-column prop="endTime" label="结束时间" :formatter="formatTableDateTime"></el-table-column>
      <el-table-column prop="status" label="状态"></el-table-column>
      <el-table-column label="操作" width="220">
        <template #default="scope">
          <el-button
              v-if="isAdmin && scope.row.status === 'planned'"
              @click="handleApprove(scope.row.id)"
              type="success"
              link>
            审批通过
          </el-button>

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
      loading: false,
      searchParams: {
        name: '',
        organizer: ''
      },
      searchDateRange: [],
    };
  },
  computed: {
    isAdmin() {
      const userJson = localStorage.getItem('user');
      if (!userJson) return false;
      const user = JSON.parse(userJson);
      return user && user.role === 0;
    }
  },
  methods: {
    async fetchMeetings(params = {}) {
      this.loading = true;
      try {
        const response = Object.keys(params).length > 0
            ? await api.searchMeetings(params)
            : await api.getAllMeetings();
        this.meetings = response;
      } catch (error) {
        console.error('Failed to fetch meetings:', error);
        this.$message.error(error.message || '获取会议列表失败');
      } finally {
        this.loading = false;
      }
    },
    handleSearch() {
      const params = {
        ...this.searchParams,
        searchStartDate: this.searchDateRange && this.searchDateRange[0]
            ? this.formatDateTimeForApi(this.searchDateRange[0])
            : null,
        searchEndDate: this.searchDateRange && this.searchDateRange[1]
            ? this.formatDateTimeForApi(this.searchDateRange[1])
            : null
      };
      const finalParams = Object.fromEntries(
          Object.entries(params).filter(([_, v]) => v != null && v !== '')
      );
      this.fetchMeetings(finalParams);
    },
    resetSearch() {
      this.searchParams.name = '';
      this.searchParams.organizer = '';
      this.searchDateRange = [];
      this.fetchMeetings();
    },
    async handleApprove(id) {
      this.$confirm('是否确认通过此会议审批？(状态将从 planned 变为 scheduled)', '审批确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          this.loading = true;
          await api.approveMeeting(id);
          this.$message.success('审批成功！');
          await this.fetchMeetings();
        } catch (error) {
          console.error('Failed to approve meeting:', error);
          this.$message.error(error.message || '审批失败');
        } finally {
          this.loading = false;
        }
      }).catch(() => {
        this.message.info('已取消操作');
      });
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
    async deleteMeeting(id) {
      try {
        await api.deleteMeeting({ id });
        this.$message.success('删除成功');
        await this.fetchMeetings();
      } catch (error) {
        console.error('Failed to delete meeting:', error);
        this.$message.error(error.message || '删除失败');
      }
    },
    goToAddMeeting() {
      this.$router.push({ name: 'AddMeeting' });
    },
    /**
     * 【新增】跳转到会议详情页的方法
     */
    goToDetails(id) {
      this.$router.push({ name: 'MeetingDetails', params: { id } });
    },
    editMeeting(id) {
      this.$router.push({ name: 'EditMeeting', params: { id } });
    },
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
    formatTableDateTime(row, column, cellValue) {
      if (!cellValue) return '';
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
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
}
.search-group {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}
.search-input {
  width: 200px;
}
.action-group {
  margin-left: auto;
  padding-left: 20px;
}
</style>