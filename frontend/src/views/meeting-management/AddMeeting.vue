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
  name: 'AddMeeting',
  data() {
    return {
      meeting: {
        name: '',
        organizer: '',
        startTime: '',
        endTime: '',
        content: '',
        status: 'planned'
      }
    };
  },
  methods: {
    async submitForm() {
      try {
        // 直接调用 api
        await api.createMeeting(this.meeting);
        this.$message.success('会议创建成功');
        // 跳转回列表页
        this.$router.push({ name: 'MeetingList' });
      } catch (error) {
        console.error('Failed to create meeting:', error);
        this.$message.error('创建会议失败');
      }
    },
    cancel() {
      this.$router.push({ name: 'MeetingList' });
    }
  }
};
</script>