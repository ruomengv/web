<template>

  <div class="meeting-management">

    <div class="header">

      <div class="search-group">

        <el-input v-model="searchParams.name" placeholder="会议名称" class="search-input" clearable></el-input>

        <el-input v-model="searchParams.organizer" placeholder="创建人" class="search-input" clearable></el-input>



        <el-select v-model="searchParams.category" placeholder="会议类别" class="search-input" clearable>

          <el-option label="技术分享" value="技术分享"></el-option>

          <el-option label="项目周会" value="项目周会"></el-option>

          <el-option label="部门例会" value="部门例会"></el-option>

          <el-option label="客户沟通" value="客户沟通"></el-option>

        </el-select>



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



      <el-table-column prop="category" label="类别" width="120"></el-table-column>

      <el-table-column prop="organizer" label="组织者" width="120"></el-table-column>

      <el-table-column prop="startTime" label="开始时间" width="180" :formatter="formatTableDateTime"></el-table-column>

      <el-table-column prop="endTime" label="结束时间" width="180" :formatter="formatTableDateTime"></el-table-column>

      <el-table-column prop="status" label="状态" width="100"></el-table-column>



      <el-table-column label="操作" width="220" fixed="right">

        <template #default="scope">

          <el-button

              v-if="isAdmin && scope.row.status === 'planned'"

              @click="handleApprove(scope.row.id)"

              type="success"

              link>

            审批通过

          </el-button>



          <el-button

              v-if="isAdmin || currentUsername === scope.row.organizer"

              @click="editMeeting(scope.row.id)"

              type="primary"

              link>

            修改

          </el-button>



          <el-button

              v-if="isAdmin || currentUsername === scope.row.organizer"

              @click="confirmDelete(scope.row.id)"

              type="danger"

              link>

            删除

          </el-button>

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

        organizer: '',

        category: '' // 新增 category 搜索参数

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

    },

    currentUsername() {

      const userJson = localStorage.getItem('user');

      if (!userJson) return null;

      const user = JSON.parse(userJson);

      return user ? user.username : null;

    }

  },

  methods: {

    async fetchMeetings(params = {}) {

      this.loading = true;

      try {

        const response = await api.searchMeetings(params);

        this.meetings = response; // 假设API返回 { code, data, msg }

      } catch (error) {

        console.error('Failed to fetch meetings:', error);

        this.$message.error('获取会议列表失败');

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

      this.searchParams.category = ''; // 重置 category

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

          await api.approveMeeting({ id });

          this.$message.success('审批成功！');

          this.fetchMeetings(this.searchParams); // 重新加载当前搜索条件下的列表

        } catch (error) {

          console.error('Failed to approve meeting:', error);

          this.$message.error('审批失败');

        }

      }).catch(() => {

        this.$message.info('已取消操作');

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

        this.fetchMeetings(this.searchParams); // 重新加载当前搜索条件下的列表

      } catch (error) {

        console.error('Failed to delete meeting:', error);

        this.$message.error('删除失败');

      }

    },

    goToAddMeeting() {

      this.$router.push({ name: 'AddMeeting' });

    },

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

  align-items: center; /* 垂直居中对齐 */

}

.search-input {

  width: 180px; /* 稍微调整宽度以容纳更多筛选器 */

}

.action-group {

  margin-left: auto;

  padding-left: 20px;

}

</style>