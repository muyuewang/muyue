import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Form, Input, Button, message, Typography } from 'antd'
import { LockOutlined, UserOutlined, SafetyCertificateOutlined } from '@ant-design/icons'
import { login, getCaptcha } from '../api/auth'
import { setToken } from '../utils/request'

const { Title } = Typography

export default function Login() {
  const navigate = useNavigate()
  const [form] = Form.useForm()
  const [loading, setLoading] = useState(false)
  const [captcha, setCaptcha] = useState({ enabled: false, key: '', img: '' })

  const refreshCaptcha = () => {
    getCaptcha()
      .then((res) => {
        setCaptcha({
          enabled: !!res.data.captchaEnabled,
          key: res.data.key || '',
          img: res.data.img || ''
        })
      })
      .catch(() => {})
  }

  // 首次挂载拉取验证码
  React.useEffect(refreshCaptcha, [])

  const onFinish = (values) => {
    setLoading(true)
    const data = { username: values.username, password: values.password }
    if (captcha.enabled) {
      data.code = values.code
      data.uuid = captcha.key
    }
    login(data)
      .then((res) => {
        setToken(res.data.token)
        message.success('登录成功')
        window.location.href = '/index'
      })
      .catch((e) => {
        message.error(e.message)
        refreshCaptcha()
        form.setFieldValue('code', '')
      })
      .finally(() => setLoading(false))
  }

  return (
    <div
      style={{
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        background: 'linear-gradient(135deg, #1677ff15, #722ed115)'
      }}
    >
      <div
        style={{
          width: 400,
          padding: 36,
          background: '#fff',
          borderRadius: 12,
          boxShadow: '0 8px 40px rgba(0,0,0,0.08)'
        }}
      >
        <Title level={3} style={{ textAlign: 'center', marginBottom: 32 }}>
          沐月管理系统
        </Title>
        <Form form={form} onFinish={onFinish} size="large">
          <Form.Item name="username" rules={[{ required: true, message: '请输入账号' }]}>
            <Input prefix={<UserOutlined />} placeholder="账号" autoComplete="username" />
          </Form.Item>
          <Form.Item name="password" rules={[{ required: true, message: '请输入密码' }]}>
            <Input.Password prefix={<LockOutlined />} placeholder="密码" autoComplete="current-password" />
          </Form.Item>
          {captcha.enabled && (
            <Form.Item name="code" rules={[{ required: true, message: '请输入验证码' }]}>
              <div style={{ display: 'flex', gap: 12 }}>
                <Input
                  prefix={<SafetyCertificateOutlined />}
                  placeholder="验证码"
                  maxLength={6}
                />
                <img
                  src={captcha.img}
                  alt="验证码"
                  title="点击刷新"
                  onClick={refreshCaptcha}
                  style={{ height: 40, borderRadius: 6, cursor: 'pointer', border: '1px solid #eee' }}
                />
              </div>
            </Form.Item>
          )}
          <Form.Item>
            <Button type="primary" htmlType="submit" block loading={loading}>
              登 录
            </Button>
          </Form.Item>
        </Form>
      </div>
    </div>
  )
}
