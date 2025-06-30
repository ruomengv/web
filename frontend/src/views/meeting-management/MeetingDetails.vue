<template>
  <div class="meeting-details-container" v-loading="loading">
    <el-page-header @back="goBack" content="会议详情"></el-page-header>

    <el-card class="details-card" v-if="meeting">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="会议ID">{{ meeting.id }}</el-descriptions-item>
        <el-descriptions-item label="会议类别">{{ meeting.category || '未分类' }}</el-descriptions-item>

        <el-descriptions-item label="会议名称" :span="2">{{ meeting.name }}</el-descriptions-item>

        <el-descriptions-item label="会议状态">
          <el-tag :type="statusTagType(meeting.status)">{{ meeting.status }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="组织者">{{ meeting.organizer }}</el-descriptions-item>

        <el-descriptions-item label="开始时间">{{ formatDateTime(meeting.startTime) }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ formatDateTime(meeting.endTime) }}</el-descriptions-item>

        <el-descriptions-item label="会议内容" :span="2">
          <div class="meeting-content">{{ meeting.content }}</div>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-empty v-if="!meeting && !loading" description="未找到会议信息"></el-empty>
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
    };
  },

  methods: {
    fetchMeetingDetails() {
      // 【追蹤點 7】
      console.log("【追蹤點 7】: fetchMeetingDetails - 方法開始執行");

      const meetingId = this.$route.params.id;
      if (!meetingId) {
        this.$message.error('未提供有效的会议ID');
        this.loading = false;
        return;
      }

      this.loading = true;

      // 這裡我們仍然使用 .then() 的寫法，因為它更穩健
      api.getMeetingById({ id: meetingId })
          .then(meetingData => {
            this.meeting = meetingData;
          })
          .catch(err => {
            console.error("API 請求失敗:", err);
            this.meeting = null;
            this.$message.error('获取会议详情失败');
          })
          .finally(() => {
            this.loading = false;
          });
    },
    goBack() {
      this.$router.go(-1);
    },
    formatDateTime(dateTimeStr) {
      if (!dateTimeStr) return '';
      return dateTimeStr.replace('T', ' ').substring(0, 16);
    },
    statusTagType(status) {
      if (status === 'scheduled') return 'success';
      if (status === 'planned') return 'warning';
      if (status === 'completed') return 'info';
      if (status === 'cancelled') return 'danger';
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
  white-space: pre-wrap;
  line-height: 1.6;
}
</style>