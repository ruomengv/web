import axios from 'axios'

const api = axios.create({
  baseURL: 'http://localhost:8081/api',
  timeout: 10000,
  withCredentials: true,

  paramsSerializer: {
    serialize: (params) => {
      const validParams = Object.entries(params).reduce((acc, [key, value]) => {
        if (value !== undefined && value !== null && value !== '') {
          acc[key] = value
        }
        return acc
      }, {})

      return new URLSearchParams(validParams).toString()
    }
  }
})

// 请求拦截器
api.interceptors.request.use(config => {
  const user = JSON.parse(localStorage.getItem('user') || '{}')

  config.withCredentials = true;

  if (user && user.uid) {
    config.headers['X-User-Id'] = user.uid
    if (user.sessionId) {
      config.headers['X-Session-Id'] = user.sessionId
    }
  }
  return config
})

// 响应拦截器
api.interceptors.response.use(
    response => response.data,
    error => {
      console.error('API Error:', error)
      if (error.response) {
        return Promise.reject({
          status: error.response.status,
          message: error.response.data?.message || error.response.data || '请求失败',
          data: error.response.data
        })
      }
      return Promise.reject({ message: '网络错误或服务器无响应' })
    }
)

export default {
  // 认证相关
  login(data) {
    return api.post('/auth/login', data)
  },
  register(data) {
    return api.post('/auth/register', data)
  },
  getCaptcha() {
    return api.get('/auth/captcha', { responseType: 'blob' })
  },

  // 用户相关
  getProfile() {
    return api.get('/user/profile')
  },
  updateProfile(data) {
    return api.put('/user/profile', data)
  },
  changePassword(data) {
    return api.put('/user/password', data)
  },
  getUsers(params) {
    return api.get('/user/list', {
      params,
      validateStatus: (status) => status < 500
    })
  },
  createUser(data) {
    return api.post('/user', data)
  },
  updateUser(data) {
    return api.put(`/user/admin/${data.uid}`, data)
  },
  deleteUser(uid) {
    return api.delete(`/user/admin/${uid}`)
  },
  updateUserStatus(uid, status) {
    return api.put(`/user/admin/status/${uid}`, { status })
  },

  // 动态相关
  getNews(params) {
    return api.get('/news', {
      params,
      validateStatus: (status) => status < 500
    })
  },
  createNews(data) {
    return api.post('/news', data)
  },
  updateNews(data) {
    return api.put(`/news/${data.id}`, data)
  },
  deleteNews(id) {
    return api.delete(`/news/${id}`)
  },
  getPendingNews(params) {
    return api.get('/admin/news/audits', { params })
  },
  approveNews(id) {
    return api.put(`/admin/news/${id}/approve`)
  },
  rejectNews(id, reason) {
    return api.put(`/admin/news/${id}/reject`, { reason })
  },

  // 课程相关
  getCourses(params) {
    return api.get('/courses', { params })
  },
  createCourse(data) {
    return api.post('/courses', data)
  },
  updateCourse(data) {
    return api.put(`/courses/${data.id}`, data)
  },
  deleteCourse(id) {
    return api.delete(`/courses/${id}`)
  },
  getPendingCourses(params) {
    return api.get('/admin/courses/audits', { params })
  },
  approveCourse(id) {
    return api.put(`/admin/courses/${id}/approve`)
  },
  rejectCourse(id, reason) {
    return api.put(`/admin/courses/${id}/reject`, { reason })
  },

  // 会议相关
  getAllMeetings() {
    return api.get('/meetings/listAll');
  },
  getMeetingById(id) {
    return api.post('/meetings/getMeetingById', { id });
  },
  createMeeting(meetingData) {
    return api.post('/meetings/addMeeting', meetingData);
  },
  updateMeeting(id, meetingData) {
    // 将 id 和 meetingData 合并为一个对象发送
    const payload = {
      id: id,
      ...meetingData
    };
    return api.post('/meetings/updateMeeting', payload);
  },
  deleteMeeting(id) {
    return api.post('/meetings/deleteMeeting', { id });
  },
  searchMeetings(params) {
    return api.post('/meetings/searchMeetings', params);
  },
  approveMeeting(id) {
    return api.post('/meetings/approve', { id });
  },

  // 文件上传
  uploadFile(file) {
    const formData = new FormData()
    formData.append('file', file)

    return api.post('/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  }
}