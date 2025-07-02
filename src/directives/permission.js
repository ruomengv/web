import { getCurrentUser } from '@/utils/auth'

export default {
  mounted(el, binding) {
    const { value } = binding
    const user = getCurrentUser()
    
    if (!user) {
      el.style.display = 'none'
      return
    }
    
    let hasPermission = false
    const requiredPermissions = Array.isArray(value) ? value : [value]
    
    for (const permission of requiredPermissions) {
      if (permission === 'admin' && user.role === 0) {
        hasPermission = true
        break
      } else if (permission === 'company' && user.role === 1) {
        hasPermission = true
        break
      } else if (permission === 'creator') {
        const creatorId = binding.arg || binding.value?.creatorId
        if (user.uid == creatorId) {
          hasPermission = true
          break
        }
      }
    }
    
    if (!hasPermission) {
      el.style.display = 'none'
    }
  }
}