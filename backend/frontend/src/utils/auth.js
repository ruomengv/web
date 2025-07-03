export function getCurrentUser() {
  const userData = localStorage.getItem('user')
  return userData ? JSON.parse(userData) : null
}

export function isAdmin() {
  const user = getCurrentUser()
  return user && user.role === 0
}

export function isCompanyUser() {
  const user = getCurrentUser()
  return user && user.role === 1
}