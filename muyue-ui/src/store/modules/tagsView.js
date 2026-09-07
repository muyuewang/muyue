import { defineStore } from 'pinia'

const useTagsViewStore = defineStore('tagsView', {
  state: () => ({
    visitedViews: [],
    cachedViews: []
  }),

  actions: {
    /** 添加一个路由视图 */
    addView(view) {
      this.addVisitedView(view)
      this.addCachedView(view)
    },

    /** 添加到已访问列表（affix 排在最前） */
    addVisitedView(view) {
      if (this.visitedViews.some((v) => v.path === view.path)) return
      if (view.meta && view.meta.affix) {
        this.visitedViews.unshift(Object.assign({}, view))
      } else {
        this.visitedViews.push(Object.assign({}, view))
      }
    },

    /** 添加到 keep-alive 缓存列表 */
    addCachedView(view) {
      if (this.cachedViews.includes(view.name)) return
      if (!view.meta || !view.meta.noCache) {
        this.cachedViews.push(view.name)
      }
    },

    /** 删除指定视图 */
    delView(view) {
      return new Promise((resolve) => {
        this.delVisitedView(view)
        this.delCachedView(view)
        resolve({
          visitedViews: [...this.visitedViews],
          cachedViews: [...this.cachedViews]
        })
      })
    },

    delVisitedView(view) {
      for (const [i, v] of this.visitedViews.entries()) {
        if (v.path === view.path) {
          this.visitedViews.splice(i, 1)
          break
        }
      }
    },

    delCachedView(view) {
      const index = this.cachedViews.indexOf(view.name)
      if (index > -1) {
        this.cachedViews.splice(index, 1)
      }
    },

    /** 删除其他视图 */
    delOthersViews(view) {
      return new Promise((resolve) => {
        this.delOthersVisitedViews(view)
        this.delOthersCachedViews(view)
        resolve({
          visitedViews: [...this.visitedViews],
          cachedViews: [...this.cachedViews]
        })
      })
    },

    delOthersVisitedViews(view) {
      this.visitedViews = this.visitedViews.filter((v) => {
        return v.meta && v.meta.affix || v.path === view.path
      })
    },

    delOthersCachedViews(view) {
      this.cachedViews = this.cachedViews.filter((name) => {
        const found = this.visitedViews.find((v) => v.name === name)
        return found && (found.meta && found.meta.affix || found.path === view.path)
      })
    },

    /** 删除所有视图 */
    delAllViews(view) {
      return new Promise((resolve) => {
        this.delAllVisitedViews(view)
        this.delAllCachedViews(view)
        resolve({
          visitedViews: [...this.visitedViews],
          cachedViews: [...this.cachedViews]
        })
      })
    },

    delAllVisitedViews() {
      const affixTags = this.visitedViews.filter((v) => v.meta && v.meta.affix)
      this.visitedViews = affixTags
    },

    delAllCachedViews() {
      const affixNames = this.visitedViews
        .filter((v) => v.meta && v.meta.affix)
        .map((v) => v.name)
      this.cachedViews = affixNames
    },

    /** 更新当前标签（比如路由参数变化） */
    updateVisitedView(view) {
      for (let v of this.visitedViews) {
        if (v.path === view.path) {
          v = Object.assign(v, view)
          break
        }
      }
    }
  }
})

export default useTagsViewStore
