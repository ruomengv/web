<template>
  <el-form :model="meeting" label-width="120px" @submit.native.prevent="submitForm">
    <el-form-item label="会议名称" required>
      <el-input v-model="meeting.name"></el-input>
    </el-form-item>

    <el-form-item label="会议类别">
      <el-select v-model="meeting.category" placeholder="请选择会议类别" clearable>
        <el-option label="技术分享" value="技术分享"></el-option>
        <el-option label="项目周会" value="项目周会"></el-option>
        <el-option label="部门例会" value="部门例会"></el-option>
        <el-option label="客户沟通" value="客户沟通"></el-option>
      </el-select>
    </el-form-item>

    <el-form-item label="开始时间" required>
      <el-date-picker v-model="meeting.startTime" type="datetime" placeholder="选择时间"></el-date-picker>
    </el-form-item>
    <el-form-item label="结束时间" required>
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
        status: 'planned',
        category: ''
      }
    };
  },
  methods: {
    async submitForm() {
      // 从 localStorage 获取用户信息
      const user = JSON.parse(localStorage.getItem('user') || '{}');

      // 检查用户是否存在以及用户名是否有效
      if (!user || !user.username) {
        this.$message.error('无法获取当前用户信息，请重新登录');
        return; // 中断提交
      }

      // 将当前用户名赋值给 meeting.organizer
      this.meeting.organizer = user.username;

      try {
        // 验证其他必填项
        if (!this.meeting.name || !this.meeting.startTime || !this.meeting.endTime) {
          this.$message.warning('请填写所有必填项');
          return;
        }

        // 使用更新后的 this.meeting 对象调用 api
        await api.createMeeting(this.meeting);
        this.$message.success('会议创建成功');

        // 跳转回列表页
        this.$router.push({ name: 'MeetingList' });
      } catch (error) {
        console.error('Failed to create meeting:', error);
        this.$message.error('创建会议失败: ' + (error.response?.data?.message || '未知错误'));
      }
    },
    cancel() {
      this.$router.push({ name: 'MeetingList' });
    }
  }
};
</script>

<style scoped>

</style>