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
// 导入你的 api 文件
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
    // 获取当前会议的详细信息
    async fetchMeetingData(id) {
      try {
        const data = await api.getMeetingById(id);
        this.meeting = data;
      } catch (error) {
        console.error('Failed to fetch meeting data:', error);
        this.$message.error('获取会议详情失败');
      }
    },
    // 提交更新
    async submitForm() {
      try {
        // 直接调用 api
        await api.updateMeeting(this.meeting.id, this.meeting);
        this.$message.success('会议更新成功');
        // 跳转回列表页
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
  mounted() {
    // 从路由参数中获取 id
    const meetingId = this.$route.params.id;
    if (meetingId) {
      this.fetchMeetingData(meetingId);
    }
  }
};
</script>