import api from '@/services/api.js'; // 請確保 api 服務的路徑正確

const state = {
    meetings: [],
    currentMeeting: null,
    loading: false
};

const mutations = {
    setMeetings(state, meetings) {
        state.meetings = Array.isArray(meetings) ? meetings : [];
    },
    setCurrentMeeting(state, meeting) {
        state.currentMeeting = meeting;
    },
    setLoading(state, isLoading) {
        state.loading = isLoading;
    }
};

const actions = {
    /**
     * 通用的獲取會議列表動作 (包含搜索功能)
     */
    async fetchMeetings({ commit }, params = {}) {
        commit('setLoading', true);
        try {
            // 【關鍵修正】API 返回的直接就是會議陣列
            const meetingsArray = await api.searchMeetings(params);
            // 直接將獲取到的陣列 commit 到 state
            commit('setMeetings', meetingsArray);
        } catch (error) {
            console.error('Failed to fetch meetings in Vuex:', error.message);
            commit('setMeetings', []);
        } finally {
            commit('setLoading', false);
        }
    },

    /**
     * 根據 ID 獲取單一會議詳情
     */
    async fetchMeetingById({ commit }, meetingId) {
        commit('setLoading', true);
        try {
            // 【關鍵修正】假設 getMeetingById 同樣是直接返回會議物件
            const meetingObject = await api.getMeetingById({ id: meetingId });
            commit('setCurrentMeeting', meetingObject);
        } catch (error) {
            console.error(`Failed to fetch meeting with id ${meetingId} in Vuex:`, error.message);
            commit('setCurrentMeeting', null);
        } finally {
            commit('setLoading', false);
        }
    },

    /**
     * 創建一個新會議
     */
    async createMeeting({ dispatch }, meetingPayload) {
        try {
            await api.createMeeting(meetingPayload);
            dispatch('fetchMeetings', {});
        } catch (error) {
            console.error('Failed to create meeting in Vuex:', error.message);
            throw error;
        }
    },

    /**
     * 更新一個已有的會議
     */
    async updateMeeting({ dispatch }, meetingPayload) {
        try {
            await api.updateMeeting(meetingPayload);
            dispatch('fetchMeetings', {});
        } catch (error) {
            console.error('Failed to update meeting in Vuex:', error.message);
            throw error;
        }
    },

    /**
     * 刪除一個會議
     */
    async deleteMeeting({ dispatch }, meetingId) {
        try {
            await api.deleteMeeting({ id: meetingId });
            dispatch('fetchMeetings', {});
        } catch (error) {
            console.error(`Failed to delete meeting with id ${meetingId} in Vuex:`, error.message);
            throw error;
        }
    },

    /**
     * 審批會議
     */
    async approveMeeting({ dispatch }, meetingId) {
        try {
            await api.approveMeeting({ id: meetingId });
            dispatch('fetchMeetings', {});
        } catch (error) {
            console.error(`Failed to approve meeting with id ${meetingId} in Vuex:`, error.message);
            throw error;
        }
    }
};

const getters = {
    allMeetings: state => state.meetings,
    currentMeeting: state => state.currentMeeting,
    isLoading: state => state.loading
};

export default {
    namespaced: true,
    state,
    mutations,
    actions,
    getters
};