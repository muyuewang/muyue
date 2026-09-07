import { defineStore } from 'pinia'

const useAppStore = defineStore('app', {
  state: () => ({
    sidebar: {
      opened: localStorage.getItem('muyue_sidebar_status') !== 'closed'
    }
  }),

  actions: {
    toggleSideBar() {
      this.sidebar.opened = !this.sidebar.opened
      localStorage.setItem('muyue_sidebar_status', this.sidebar.opened ? 'opened' : 'closed')
    },
    closeSideBar() {
      this.sidebar.opened = false
      localStorage.setItem('muyue_sidebar_status', 'closed')
    }
  }
})

export default useAppStore
