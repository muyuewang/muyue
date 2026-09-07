/**
 * 未读角标联动总线：
 * 公告页"读完一条/发布/删除"后调用 notify()，Navbar 收到后立即刷新角标。
 * 比纯轮询更即时，也避免为了联动在页面间传递回调。
 */
let listener = null

export function subscribeUnread(fn) {
  listener = fn
  return () => { if (listener === fn) listener = null }
}

export function notifyUnread() {
  if (listener) listener()
}
