import { defineStore } from 'pinia'
import { getUnreadCount, listNotice } from '@/api/system/notice'

/**
 * 消息通知共享状态：
 * Navbar 铃铛与通知公告页共用，任何一端"读消息/发公告/删公告"
 * 调用 refresh() 即可让角标即时更新，无需刷新页面。
 */
const useNoticeStore = defineStore('notice', {
  state: () => ({
    unreadCount: 0,
    recentNotices: []
  }),
  actions: {
    /** 拉取未读数与最近公告（静默失败，不打扰用户） */
    refresh() {
      return Promise.allSettled([this.fetchUnread(), this.fetchRecent()])
    },
    async fetchUnread() {
      try {
        const res = await getUnreadCount()
        this.unreadCount = res.data || 0
      } catch (e) {
        /* 忽略：未登录或权限不足时静默 */
      }
    },
    async fetchRecent() {
      try {
        const res = await listNotice({ pageNum: 1, pageSize: 5 })
        this.recentNotices = res.rows || []
      } catch (e) {
        /* 忽略 */
      }
    }
  }
})

export default useNoticeStore
