<template>
  <div class="meeting-details-container" v-loading="loading">
    <el-page-header @back="goBack" content="会议详情"></el-page-header>

    <el-card class="details-card" v-if="meeting">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="会议ID">{{ meeting.id }}</el-descriptions-item>
        <el-descriptions-item label="会议状态">
          <el-tag :type="statusTagType(meeting.status)">{{ meeting.status }}</el-tag>
        </el-descriptions-item>

        <el-descriptions-item label="会议名称" :span="2">{{ meeting.name }}</el-descriptions-item>

        <el-descriptions-item label="组织者">{{ meeting.organizer }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{ formatDateTime(meeting.createTime) || 'N/A' }}
        </el-descriptions-item>

        <el-descriptions-item label="开始时间">{{ formatDateTime(meeting.startTime) }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ formatDateTime(meeting.endTime) }}</el-descriptions-item>

        <el-descriptions-item label="会议内容" :span="2">
          <div class="meeting-content">{{ meeting.content }}</div>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-empty v-if="error" :description="error"></el-empty>
  </div>
</template>

<script>
import api from '@/services/api.js';

export default {
  name: 'MeetingDetails',
  data() {
    return {
      meeting: null,
      loading: true,
      error: null,
    };
  },
  methods: {
    async fetchMeetingDetails() {
      // 从路由参数中获取会议ID
      const meetingId = this.$route.params.id;
      if (!meetingId) {
        this.error = '未提供有效的会议ID';
        this.loading = false;
        return;
      }

      this.loading = true;
      try {
        // 调用 api.js 中的 getMeetingById 方法
        this.meeting = await api.getMeetingById(meetingId);
      } catch (err) {
        console.error('Failed to fetch meeting details:', err);
        this.error = err.message || '获取会议详情失败';
      } finally {
        this.loading = false;
      }
    },
    goBack() {
      // 返回上一页，通常是会议列表
      this.$router.go(-1);
    },
    formatDateTime(dateTimeStr) {
      if (!dateTimeStr) return '';
      // 格式化为 YYYY-MM-DD HH:mm
      return dateTimeStr.replace('T', ' ').substring(0, 16);
    },
    statusTagType(status) {
      if (status === 'scheduled') return 'success';
      if (status === 'planned') return 'warning';
      return 'info';
    }
  },
  mounted() {
    this.fetchMeetingDetails();
  }
};
</script>

<style scoped>
.meeting-details-container {
  padding: 20px;
}
.details-card {
  margin-top: 20px;
}
.meeting-content {
  white-space: pre-wrap; /* 保留换行和空格 */
  line-height: 1.6;
}
</style>