<template>
  <el-form :model="meeting" label-width="120px" @submit.native.prevent="submitForm">
    <el-form-item label="会议名称">
      <el-input v-model="meeting.name"></el-input>
    </el-form-item>
    <el-form-item label="创建人">
      <el-input v-model="meeting.organizer"></el-input>
    </el-form-item>
    <el-form-item label="开始时间">
      <el-date-picker v-model="meeting.startTime" type="datetime" placeholder="选择时间"></el-date-picker>
    </el-form-item>
    <el-form-item label="结束时间">
      <el-date-picker v-model="meeting.endTime" type="datetime" placeholder="选择时间"></el-date-picker>
    </el-form-item>
    <el-form-item label="会议内容">
      <el-input type="textarea" v-model="meeting.content"></el-input>
    </el-form-item>
    <el-form-item>
      <el-button type="primary" @click="submitForm">确定</el-button>
      <el-button @click="cancel">取消</el-button>
    </el-form-item>
  </el-form>
</template>

<script>
import api from '@/services/api.js';

export default {
  name: 'EditMeeting',
  data() {
    return {
      meeting: {
        id: '',
        name: '',
        organizer: '',
        startTime: '',
        endTime: '',
        content: '',
        status: ''
      }
    };
  },
  methods: {
    async fetchMeetingData(id) {
      try {
        const data = await api.getMeetingById(id);
        this.meeting = data;
      } catch (error) {
        console.error('Failed to fetch meeting data:', error);
        this.$message.error('获取会议详情失败');
      }
    },
    async submitForm() {
      try {
        await api.updateMeeting(this.meeting.id, this.meeting);
        this.$message.success('会议更新成功');
        this.$router.push({ name: 'MeetingList' });
      } catch (error) {
        console.error('Failed to update meeting:', error);
        this.$message.error('更新会议失败');
      }
    },
    cancel() {
      this.$router.push({ name: 'MeetingList' });
    }
  },
  // 【优化】将 mounted 声明为 async 函数
  async mounted() {
    const meetingId = this.$route.params.id;
    if (meetingId) {
      // 【优化】添加 await，使异步流程更清晰
      await this.fetchMeetingData(meetingId);
    }
  }
};
</script>