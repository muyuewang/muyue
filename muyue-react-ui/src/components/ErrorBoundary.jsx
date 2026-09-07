import React from 'react'
import { Button, Result, Typography } from 'antd'

const { Text, Paragraph } = Typography

/**
 * 页面级错误边界：
 * 某个页面崩溃时只替换内容区，侧边栏 / 顶栏不受影响；
 * 切换路由（resetKey 变化）自动清除错误状态。
 */
export default class ErrorBoundary extends React.Component {
  constructor(props) {
    super(props)
    this.state = { error: null }
  }

  static getDerivedStateFromError(error) {
    return { error }
  }

  componentDidCatch(error, info) {
    // eslint-disable-next-line no-console
    console.error('[PageError]', error, info)
  }

  componentDidUpdate(prevProps) {
    if (prevProps.resetKey !== this.props.resetKey && this.state.error) {
      // eslint-disable-next-line react/no-did-update-set-state
      this.setState({ error: null })
    }
  }

  render() {
    if (this.state.error) {
      return (
        <Result
          status="error"
          title="页面出错了"
          subTitle="该页面渲染发生异常，其他页面不受影响"
          extra={[
            <Button key="retry" type="primary" onClick={() => this.setState({ error: null })}>
              重试
            </Button>,
            <Button key="home" onClick={() => { window.location.href = '/index' }}>
              回首页
            </Button>
          ]}
        >
          <Paragraph>
            <Text type="secondary" code style={{ fontSize: 12 }}>
              {String((this.state.error && this.state.error.message) || this.state.error)}
            </Text>
          </Paragraph>
        </Result>
      )
    }
    return this.props.children
  }
}
