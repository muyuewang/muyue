import React, { useEffect, useState } from 'react'
import { PageContainer, ProCard } from '@ant-design/pro-components'
import { App, Avatar, Card, Descriptions, Form, Input, Radio, Button, Tag } from 'antd'
import { UserOutlined } from '@ant-design/icons'
import { getProfile, updateProfile, updatePwd } from '../api/tool'

export default function Profile() {
  const { message } = App.useApp()
  const [data, setData] = useState(null)
  const [infoForm] = Form.useForm()
  const [pwdForm] = Form.useForm()

  const load = () => getProfile().then((res) => {
    setData(res.data)
    infoForm.setFieldsValue(res.data.user || {})
  })
  useEffect(load, [])

  if (!data) return <PageContainer><Card loading /></PageContainer>

  return (
    <PageContainer>
      <ProCard split="vertical">
        <ProCard colSpan={{ xs: 24, md: '38%' }} title="个人信息">
          <div style={{ textAlign: 'center', marginBottom: 16 }}>
            <Avatar size={80} src={data.user?.avatar || undefined} icon={<UserOutlined />} />
          </div>
          <Descriptions column={1} size="small">
            <Descriptions.Item label="账号">{data.user?.userName}</Descriptions.Item>
            <Descriptions.Item label="昵称">{data.user?.nickName}</Descriptions.Item>
            <Descriptions.Item label="角色">
              {(data.roleGroup || '').split(',').filter(Boolean).map((r) => <Tag key={r} color="blue">{r}</Tag>)}
            </Descriptions.Item>
            <Descriptions.Item label="手机号">{data.user?.phonenumber || '-'}</Descriptions.Item>
            <Descriptions.Item label="邮箱">{data.user?.email || '-'}</Descriptions.Item>
            <Descriptions.Item label="创建时间">{data.user?.createTime}</Descriptions.Item>
          </Descriptions>
        </ProCard>

        <ProCard colSpan={{ xs: 24, md: '62%' }}>
          <Card type="inner" title="修改资料" style={{ marginBottom: 16 }}>
            <Form form={infoForm} layout="vertical" style={{ maxWidth: 480 }}
              onFinish={async (values) => {
                await updateProfile(values)
                message.success('保存成功')
                load()
              }}>
              <Form.Item name="nickName" label="昵称" rules={[{ required: true }]}>
                <Input />
              </Form.Item>
              <Form.Item name="phonenumber" label="手机号">
                <Input />
              </Form.Item>
              <Form.Item name="email" label="邮箱">
                <Input />
              </Form.Item>
              <Form.Item name="sex" label="性别">
                <Radio.Group options={[{ label: '男', value: '0' }, { label: '女', value: '1' }]} />
              </Form.Item>
              <Button type="primary" htmlType="submit">保存</Button>
            </Form>
          </Card>

          <Card type="inner" title="修改密码">
            <Form form={pwdForm} layout="vertical" style={{ maxWidth: 480 }}
              onFinish={async (values) => {
                await updatePwd({ oldPassword: values.oldPassword, newPassword: values.newPassword })
                message.success('密码修改成功，下次登录请使用新密码')
                pwdForm.resetFields()
              }}>
              <Form.Item name="oldPassword" label="旧密码" rules={[{ required: true }]}>
                <Input.Password />
              </Form.Item>
              <Form.Item name="newPassword" label="新密码" rules={[{ required: true }, { min: 5, message: '至少 5 位' }]}>
                <Input.Password />
              </Form.Item>
              <Form.Item
                name="confirmPassword" label="确认新密码"
                dependencies={['newPassword']}
                rules={[
                  { required: true },
                  ({ getFieldValue }) => ({
                    validator(_, value) {
                      if (!value || value === getFieldValue('newPassword')) return Promise.resolve()
                      return Promise.reject(new Error('两次输入的密码不一致'))
                    }
                  })
                ]}
              >
                <Input.Password />
              </Form.Item>
              <Button type="primary" htmlType="submit">修改密码</Button>
            </Form>
          </Card>
        </ProCard>
      </ProCard>
    </PageContainer>
  )
}
