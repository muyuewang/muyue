import React, { useEffect, useState } from 'react'
import {
  PageContainer, ProCard, ModalForm,
  ProFormText, ProFormTextArea
} from '@ant-design/pro-components'
import { App, Button, Card, Descriptions, Form, Input, message } from 'antd'
import { getMailConfig, saveMailConfig, sendMail } from '../../api/tool'

export default function ToolMail() {
  const [config, setConfig] = useState(null)
  const [configOpen, setConfigOpen] = useState(false)
  const [sending, setSending] = useState(false)
  const [sendForm] = Form.useForm()
  const { message } = App.useApp()

  const load = () => getMailConfig().then((res) => setConfig(res.data))
  useEffect(load, [])

  return (
    <PageContainer>
      <ProCard split="vertical">
        <ProCard colSpan={{ xs: 24, md: '38%' }} title="SMTP 配置" extra={
          <a onClick={() => setConfigOpen(true)}>修改配置</a>
        }>
          {config && (
            <Descriptions column={1} size="small">
              <Descriptions.Item label="SMTP 服务器">{config.host || '-'}</Descriptions.Item>
              <Descriptions.Item label="端口">{config.port || '-'}</Descriptions.Item>
              <Descriptions.Item label="发件人">{config.from || config.username || '-'}</Descriptions.Item>
              <Descriptions.Item label="用户名">{config.username || '-'}</Descriptions.Item>
              <Descriptions.Item label="SSL">{config.sslEnabled ? '开启' : '关闭'}</Descriptions.Item>
            </Descriptions>
          )}
        </ProCard>

        <ProCard colSpan={{ xs: 24, md: '62%' }} title="发送测试邮件">
          <Form
            form={sendForm}
            layout="vertical"
            style={{ maxWidth: 560 }}
            onFinish={async (values) => {
              setSending(true)
              try {
                await sendMail(values)
                message.success('发送成功')
                sendForm.resetFields()
              } catch (e) {
                message.error(e.message)
              } finally {
                setSending(false)
              }
            }}
          >
            <Form.Item name="to" label="收件人" rules={[{ required: true }, { type: 'email' }]}>
              <Input placeholder="receiver@example.com" />
            </Form.Item>
            <Form.Item name="subject" label="主题" rules={[{ required: true }]}>
              <Input />
            </Form.Item>
            <Form.Item name="content" label="正文（支持 HTML 富文本）">
              <Input.TextArea rows={8} />
            </Form.Item>
            <Button type="primary" htmlType="submit" loading={sending}>发 送</Button>
          </Form>
        </ProCard>
      </ProCard>

      <ModalForm
        title="修改 SMTP 配置"
        open={configOpen}
        onOpenChange={setConfigOpen}
        width={520}
        modalProps={{ destroyOnClose: true }}
        initialValues={config}
        key={configOpen ? 'cfg' : 'cfgClosed'}
        onFinish={async (values) => {
          await saveMailConfig(values)
          message.success('保存成功')
          load()
          return true
        }}
      >
        <ProFormText name="host" label="SMTP 服务器" rules={[{ required: true }]} />
        <ProFormText name="port" label="端口" rules={[{ required: true }]} />
        <ProFormText name="username" label="用户名" rules={[{ required: true }]} />
        <ProFormText.Password name="password" label="密码 / 授权码" />
        <ProFormText name="from" label="发件人地址" />
      </ModalForm>
    </PageContainer>
  )
}
