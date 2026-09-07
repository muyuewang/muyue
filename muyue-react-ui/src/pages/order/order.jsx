import React, { useRef, useState } from 'react'
import {
  PageContainer,
  ProTable,
  ProForm,
  ModalForm,
  ProFormText,
  ProFormSelect,
  ProFormRadio,
  ProFormTextArea,
  EditableProTable
} from '@ant-design/pro-components'
import { App, Button, Descriptions, Modal, Popconfirm, Space, Tag, Typography } from 'antd'
import { PlusOutlined } from '@ant-design/icons'
import { listOrder, getOrder, addOrder, updateOrder, delOrder } from '../../api/order'

const { Text } = Typography

const statusMap = {
  0: { text: '待付款', color: 'orange' },
  1: { text: '已付款', color: 'blue' },
  2: { text: '已发货', color: 'cyan' },
  3: { text: '已完成', color: 'green' },
  4: { text: '已取消', color: 'default' }
}
const payMap = { 0: '支付宝', 1: '微信', 2: '货到付款' }

export default function OrderOrder() {
  const { message } = App.useApp()
  const actionRef = useRef()
  const [formOpen, setFormOpen] = useState(false)
  const [editRow, setEditRow] = useState(null)
  const [detail, setDetail] = useState(null)

  const columns = [
    { title: '订单编号', dataIndex: 'orderNo', width: 180, ellipsis: true },
    { title: '买家', dataIndex: 'userName', width: 100 },
    {
      title: '订单总额',
      dataIndex: 'totalAmount',
      width: 110,
      align: 'right',
      render: (v) => <Text strong>￥{Number(v || 0).toFixed(2)}</Text>
    },
    {
      title: '状态',
      dataIndex: 'status',
      width: 90,
      align: 'center',
      render: (v) => <Tag color={statusMap[v]?.color}>{statusMap[v]?.text || v}</Tag>
    },
    { title: '支付方式', dataIndex: 'payType', width: 100, align: 'center', render: (v) => payMap[v] || v },
    { title: '收货人', dataIndex: 'receiver', width: 100, ellipsis: true },
    { title: '联系电话', dataIndex: 'phone', width: 130 },
    { title: '收货地址', dataIndex: 'address', ellipsis: true },
    { title: '下单时间', dataIndex: 'createTime', width: 170 },
    {
      title: '操作',
      width: 150,
      fixed: 'right',
      render: (_, row) => (
        <Space>
          <a onClick={() => getOrder(row.orderId).then((res) => setDetail(res.data))}>明细</a>
          <a onClick={() => { setEditRow(row); setFormOpen(true) }}>修改</a>
          <Popconfirm title="确认删除该订单（含明细）？" onConfirm={() =>
            delOrder(row.orderId).then(() => { message.success('删除成功'); actionRef.current?.reload() })
          }>
            <a style={{ color: 'red' }}>删除</a>
          </Popconfirm>
        </Space>
      )
    }
  ]

  return (
    <PageContainer>
      <ProTable
        rowKey="orderId"
        actionRef={actionRef}
        columns={columns}
        cardBordered
        scroll={{ x: 1300 }}
        pagination={{ defaultPageSize: 10 }}
        request={async (params) => {
          const { current, pageSize, ...rest } = params
          const res = await listOrder({ ...rest, pageNum: current, pageSize })
          return { data: res.rows, total: res.total, success: true }
        }}
        toolBarRender={() => [
          <Button key="add" type="primary" icon={<PlusOutlined />}
            onClick={() => { setEditRow(null); setFormOpen(true) }}>
            新增订单
          </Button>
        ]}
      />

      {/* 明细弹窗 */}
      <Modal
        open={!!detail}
        title={detail ? `订单明细 - ${detail.orderNo}` : ''}
        footer={null}
        width={720}
        onCancel={() => setDetail(null)}
      >
        {detail && (
          <>
            <Descriptions column={2} size="small" bordered>
              <Descriptions.Item label="买家">{detail.userName}</Descriptions.Item>
              <Descriptions.Item label="总额">￥{Number(detail.totalAmount).toFixed(2)}</Descriptions.Item>
              <Descriptions.Item label="状态">
                <Tag color={statusMap[detail.status]?.color}>{statusMap[detail.status]?.text}</Tag>
              </Descriptions.Item>
              <Descriptions.Item label="支付方式">{payMap[detail.payType]}</Descriptions.Item>
              <Descriptions.Item label="收货人">{detail.receiver}</Descriptions.Item>
              <Descriptions.Item label="电话">{detail.phone}</Descriptions.Item>
              <Descriptions.Item label="地址" span={2}>{detail.address}</Descriptions.Item>
            </Descriptions>
            <ProTable
              rowKey="itemId"
              search={false}
              options={false}
              pagination={false}
              style={{ marginTop: 16 }}
              columns={[
                { title: '商品', dataIndex: 'productName' },
                { title: '单价', dataIndex: 'price', align: 'right', render: (v) => `￥${Number(v).toFixed(2)}` },
                { title: '数量', dataIndex: 'quantity', align: 'center' },
                { title: '小计', dataIndex: 'totalPrice', align: 'right', render: (v) => `￥${Number(v).toFixed(2)}` }
              ]}
              request={async () => ({ data: detail.items || [], success: true })}
            />
          </>
        )}
      </Modal>

      <OrderForm
        open={formOpen}
        setOpen={setFormOpen}
        editRow={editRow}
        onDone={() => actionRef.current?.reload()}
      />
    </PageContainer>
  )
}

function OrderForm({ open, setOpen, editRow, onDone }) {
  const { message } = App.useApp()
  const [form] = ProForm.useForm()
  const [items, setItems] = useState([])

  React.useEffect(() => {
    if (open) {
      if (editRow) {
        getOrder(editRow.orderId).then((res) => {
          form.setFieldsValue(res.data)
          setItems(res.data.items || [])
        })
      } else {
        form.resetFields()
        form.setFieldsValue({ status: '0', payType: '0' })
        setItems([])
      }
    }
  }, [open])

  const total = items.reduce((s, it) => s + Number(it.price || 0) * Number(it.quantity || 0), 0)

  return (
    <ModalForm
      title={editRow ? '修改订单' : '新增订单'}
      open={open}
      form={form}
      width={860}
      modalProps={{ destroyOnClose: true }}
      onOpenChange={setOpen}
      onFinish={async (values) => {
        const payload = { ...values, items }
        if (editRow) {
          await updateOrder({ orderId: editRow.orderId, ...payload })
          message.success('修改成功')
        } else {
          await addOrder(payload)
          message.success('新增成功（订单号自动生成）')
        }
        onDone()
        return true
      }}
    >
      <ProFormText name="userName" label="买家" rules={[{ required: true }]} />
      <ProFormSelect
        name="status"
        label="状态"
        options={Object.entries(statusMap).map(([v, m]) => ({ label: m.text, value: v }))}
      />
      <ProFormSelect
        name="payType"
        label="支付方式"
        options={Object.entries(payMap).map(([v, t]) => ({ label: t, value: v }))}
      />
      <ProFormText name="receiver" label="收货人" rules={[{ required: true }]} />
      <ProFormText name="phone" label="联系电话" />
      <ProFormTextArea name="address" label="收货地址" fieldProps={{ rows: 2 }} />
      <ProFormTextArea name="remark" label="备注" />

      <div style={{ margin: '16px 0 8px', fontWeight: 600 }}>订单明细</div>
      <ProTable
        rowKey="_idx"
        search={false}
        options={false}
        pagination={false}
        toolBarRender={() => [
          <Button
            key="add"
            size="small"
            onClick={() => setItems((rows) => [...rows, { _idx: Date.now(), productName: '', price: 0, quantity: 1 }])}
          >
            加一行
          </Button>
        ]}
        columns={[
          {
            title: '商品名称',
            dataIndex: 'productName',
            render: (_, r) => (
              <input
                value={r.productName}
                placeholder="商品名称"
                onChange={(e) =>
                  setItems((rows) => rows.map((x) => (x._idx === r._idx ? { ...x, productName: e.target.value } : x)))
                }
                style={{ width: '100%' }}
              />
            )
          },
          {
            title: '单价',
            dataIndex: 'price',
            width: 140,
            render: (_, r) => (
              <input
                type="number"
                value={r.price}
                onChange={(e) =>
                  setItems((rows) => rows.map((x) => (x._idx === r._idx ? { ...x, price: e.target.value } : x)))
                }
                style={{ width: '100%' }}
              />
            )
          },
          {
            title: '数量',
            dataIndex: 'quantity',
            width: 100,
            render: (_, r) => (
              <input
                type="number"
                min={1}
                value={r.quantity}
                onChange={(e) =>
                  setItems((rows) => rows.map((x) => (x._idx === r._idx ? { ...x, quantity: e.target.value } : x)))
                }
                style={{ width: '100%' }}
              />
            )
          },
          { title: '小计', width: 120, render: (_, r) => `￥${(Number(r.price || 0) * Number(r.quantity || 0)).toFixed(2)}` },
          {
            title: '操作',
            width: 70,
            render: (_, r) => (
              <a style={{ color: 'red' }} onClick={() => setItems((rows) => rows.filter((x) => x._idx !== r._idx))}>
                删除
              </a>
            )
          }
        ]}
      />
      <div style={{ textAlign: 'right', padding: '12px 4px', fontWeight: 600 }}>
        合计：￥{total.toFixed(2)}（提交后端自动汇总）
      </div>
    </ModalForm>
  )
}
