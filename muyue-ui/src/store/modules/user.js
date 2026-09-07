import { defineStore } from 'pinia'
import { getToken, setToken, removeToken } from '@/utils/auth'
import { getInfo as getInfoApi, login as loginApi, logout as logoutApi } from '@/api/login'

const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken(),
    id: '',
    name: '',
    nickName: '',
    avatar: '',
    roles: [],
    permissions: []
  }),

  actions: {
    /** 登录 */
    login(userInfo) {
      const username = userInfo.username.trim()
      const password = userInfo.password
      const code = userInfo.code
      const uuid = userInfo.uuid
      return new Promise((resolve, reject) => {
        loginApi({ username, password, code, uuid })
          .then((res) => {
            setToken(res.data.token)
            this.token = res.data.token
            resolve()
          })
          .catch((error) => {
            reject(error)
          })
      })
    },

    /** 获取用户信息（角色 + 权限） */
    getInfo() {
      return new Promise((resolve, reject) => {
        getInfoApi()
          .then((res) => {
            const data = res.data
            const user = data.user
            this.id = user.userId
            this.name = user.userName
            this.nickName = user.nickName
            this.avatar = user.avatar || ''
            this.roles = data.roles && data.roles.length > 0 ? data.roles : ['ROLE_DEFAULT']
            this.permissions = data.permissions || []
            resolve(data)
          })
          .catch((error) => {
            reject(error)
          })
      })
    },

    /** 退出登录 */
    logOut() {
      return new Promise((resolve) => {
        logoutApi().catch(() => {})
        this.token = ''
        this.roles = []
        this.permissions = []
        removeToken()
        resolve()
      })
    }
  }
})

export default useUserStore
