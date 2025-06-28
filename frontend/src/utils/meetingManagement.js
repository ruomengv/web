import api from '@/services/api.js'; // <-- 请确保这里的路径是正确的

const state = {
    meetings: [],
    currentMeeting: null
};

const mutations = {
    setMeetings(state, meetings) {
        state.meetings = meetings;
    },
    setCurrentMeeting(state, meeting) {
        state.currentMeeting = meeting;
    }
};

const actions = {
    async fetchMeetings({ commit }) {
        try {
            // 直接调用 api 模块的方法
            const meetingsData = await api.getAllMeetings();
            commit('setMeetings', meetingsData);
        } catch (error) {
            // 4. 错误已被 api.js 拦截器打印，这里可以处理UI反馈，例如弹窗提示
            console.error('Failed to fetch meetings in Vuex:', error.message);
        }
    },
    async searchMeetings({ commit }, params) {
        try {
            // 确保传递给API的数据是干净的
            const formattedParams = {
                name: params.name,
                organizer: params.organizer,
                startTime: params.startTime,
            };
            const meetingsData = await api.searchMeetings(formattedParams);
            commit('setMeetings', meetingsData);
        } catch (error) {
            console.error('Failed to search meetings in Vuex:', error.message);
        }
    },
    async fetchMeetingById({ commit }, id) {
        try {
            const meetingData = await api.getMeetingById(id);
            commit('setCurrentMeeting', meetingData);
        } catch (error) {
            console.error('Failed to fetch meeting in Vuex:', error.message);
        }
    },
    async createMeeting({ dispatch }, meeting) {
        try {
            const formattedMeeting = {
                name: meeting.name,
                organizer: meeting.organizer,
                startTime: meeting.startTime,
                endTime: meeting.endTime,
                content: meeting.content,
                status: 'Scheduled' // 确保状态为 'Scheduled'
            };
            // 调用 api.createMeeting，它会返回创建成功后的数据（根据后端实现）
            await api.createMeeting(formattedMeeting);
            // 操作成功后重新拉取列表
            dispatch('fetchMeetings');
        } catch (error) {
            console.error('Failed to create meeting in Vuex:', error.message);
        }
    },
    async editMeeting({ dispatch }, meeting) {
        try {
            const { id, ...meetingData } = meeting;
            const formattedMeeting = {
                name: meetingData.name,
                organizer: meetingData.organizer,
                startTime: meetingData.startTime,
                endTime: meetingData.endTime,
                content: meetingData.content,
                status: 'Scheduled' // 确保状态为 'Scheduled'
            };
            await api.updateMeeting(id, formattedMeeting);
            dispatch('fetchMeetings');
        } catch (error) {
            console.error('Failed to update meeting in Vuex:', error.message);
        }
    },
    async deleteMeeting({ dispatch }, id) {
        try {
            await api.deleteMeeting(id);
            dispatch('fetchMeetings');
        } catch (error) {
            console.error('Failed to delete meeting in Vuex:', error.message);
        }
    }
};

const getters = {
    allMeetings: state => state.meetings,
    currentMeeting: state => state.currentMeeting
};

export default {
    namespaced: true,
    state,
    mutations,
    actions,
    getters
};