import React from 'react'
import { Empty, Result } from 'antd'
import { useLocation } from 'react-router-dom'

export default function Placeholder() {
  const location = useLocation()
  return (
    <Result
      status="info"
      title="页面建设中"
      subTitle={`路径「${location.pathname}」对应的页面尚未在 React 版中实现，组件注册表中登记后即可启用`}
      extra={<Empty description={false} />}
    />
  )
}
