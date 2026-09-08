import React from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { Button, Dropdown, Tooltip } from 'antd'
import { CloseOutlined, ReloadOutlined, CloseCircleOutlined, MinusOutlined } from '@ant-design/icons'
import { tabsStore, closeTab, closeOthers, closeAll } from '../store/tabs'
import { emitPageRefresh } from '../utils/cacheBus'

/**
 * 已打开页面页签栏（对标 Vue 版 TagsView）：
 * 点击切换、× / 中键关闭当前、右键菜单（刷新 / 关闭 / 关闭其他 / 关闭所有）
 * 「刷新当前」仅重载该页面组件，不整页刷新（见 PageCache）
 */
export default function TabsView() {
  const { tabs } = tabsStore.use()
  const location = useLocation()
  const navigate = useNavigate()

  const onClose = (path) => {
    const next = closeTab(path)
    if (next && next !== location.pathname) navigate(next)
    else if (!next) navigate('/index')
  }

  const refreshTab = (path) => {
    emitPageRefresh(path)
    if (path !== location.pathname) navigate(path)
  }

  const menuItems = (path) => {
    const items = [
      { key: 'refresh', icon: <ReloadOutlined />, label: '刷新当前' }
    ]
    if (path !== '/index') {
      items.push({ key: 'close', icon: <CloseOutlined />, label: '关闭当前' })
    }
    items.push(
      { key: 'others', icon: <MinusOutlined />, label: '关闭其他' },
      { key: 'all', icon: <CloseCircleOutlined />, label: '关闭所有' }
    )
    return items
  }

  const onMenuClick = (path, key) => {
    if (key === 'refresh') {
      refreshTab(path)
    } else if (key === 'close') {
      onClose(path)
    } else if (key === 'others') {
      navigate(closeOthers(path))
    } else if (key === 'all') {
      navigate(closeAll())
    }
  }

  if (tabs.length <= 1) return null

  return (
    <div className="tabs-view">
      {tabs.map((tab) => {
        const active = location.pathname === tab.path
        return (
          <Dropdown
            key={tab.path}
            menu={{ items: menuItems(tab.path), onClick: ({ key }) => onMenuClick(tab.path, key) }}
            trigger={['contextMenu']}
          >
            <div
              className={'tab-item' + (active ? ' active' : '')}
              onClick={() => navigate(tab.path)}
              onAuxClick={(e) => {
                // 中键关闭（对齐 Vue TagsView @click.middle）
                if (e.button === 1) {
                  e.preventDefault()
                  if (tab.closable) onClose(tab.path)
                }
              }}
            >
              {active && <span className="dot" />}
              <span className="title">{tab.title}</span>
              {tab.closable && (
                <CloseOutlined
                  className="close"
                  onClick={(e) => {
                    e.stopPropagation()
                    onClose(tab.path)
                  }}
                />
              )}
            </div>
          </Dropdown>
        )
      })}
      <div className="tabs-actions">
        <Tooltip title="刷新当前">
          <Button size="small" type="text" icon={<ReloadOutlined />} onClick={() => refreshTab(location.pathname)} />
        </Tooltip>
        <Tooltip title="关闭所有">
          <Button size="small" type="text" icon={<CloseCircleOutlined />} onClick={() => navigate(closeAll())} />
        </Tooltip>
      </div>
    </div>
  )
}
