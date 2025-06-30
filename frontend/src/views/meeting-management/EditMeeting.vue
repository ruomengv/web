<template>
  <el-form :model="meeting" label-width="120px" @submit.native.prevent="submitForm">
    <el-form-item label="会议名称">
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

    <el-form-item label="创建人">
      <el-input v-model="meeting.organizer" :disabled="!isAdmin"></el-input>
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
        status: '',
        category: ''
      }
    };
  },
  // 【核心改動】新增 computed 區塊，並加入 isAdmin 判斷邏輯
  computed: {
    isAdmin() {
      // 這段邏輯直接從 MeetingManagement.vue 複製而來
      const userJson = localStorage.getItem('user');
      if (!userJson) return false;
      const user = JSON.parse(userJson);
      // 假設 role 為 0 的是超級管理員
      return user && user.role === 0;
    }
  },
  methods: {
    fetchMeetingData(id) {
      api.getMeetingById({ id })
          .then(meetingData => {
            if (meetingData) {
              this.meeting = meetingData;
            } else {
              this.$message.error('未找到該會議的詳細資訊');
            }
          })
          .catch(error => {
            console.error('Failed to fetch meeting data:', error);
            this.$message.error('獲取會議詳情失敗');
          });
    },
    async submitForm() {
      try {
        await api.updateMeeting(this.meeting);
        this.$message.success('會議更新成功');
        this.$router.push({ name: 'MeetingList' });
      } catch (error) {
        console.error('Failed to update meeting:', error);
        this.$message.error('更新會議失敗');
      }
    },
    cancel() {
      this.$router.push({ name: 'MeetingList' });
    }
  },
  mounted() {
    const meetingId = this.$route.params.id;
    if (meetingId) {
      this.fetchMeetingData(meetingId);
    }
  }
};
</script>