import React, { useRef, useState } from 'react'
import {
  PageContainer, ProTable, ModalForm,
  ProFormText, ProFormRadio, ProFormTextArea
} from '@ant-design/pro-components'
import { App, Button, Drawer, Popconfirm, Space, Tag } from 'antd'
import { PlusOutlined } from '@ant-design/icons'
import {
  listDictType, getDictType, addDictType, updateDictType, delDictType,
  listDictData, getDictData, addDictData, updateDictData, delDictData
} from '../../api/dict'
import Auth from '../../components/Auth'

export default function SystemDict() {
  const { message } = App.useApp()
  const typeActionRef = useRef()
  const dataActionRef = useRef()

  // ===== 类型表单 =====
  const [typeOpen, setTypeOpen] = useState(false)
  const [editType, setEditType] = useState(null)

  // ===== 字典数据抽屉 =====
  const [dataDrawer, setDataDrawer] = useState(null) // 当前字典类型行
  const [dataOpen, setDataOpen] = useState(false)
  const [dataFormOpen, setDataFormOpen] = useState(false)
  const [editData, setEditData] = useState(null)

  const typeColumns = [
    { title: '编号', dataIndex: 'dictId', width: 70 },
    { title: '字典名称', dataIndex: 'dictName', width: 160 },
    {
      title: '字典类型', dataIndex: 'dictType', width: 220,
      render: (v) => <a onClick={() => { setDataDrawer(v); setDataOpen(true) }}>{v}</a>
    },
    { title: '备注', dataIndex: 'remark', ellipsis: true },
    { title: '创建时间', dataIndex: 'createTime', width: 170 },
    {
      title: '操作', width: 130,
      render: (_, row) => (
        <Space>
          <Auth permi="system:dict:edit">
            <a onClick={async () => { const res = await getDictType(row.dictId); setEditType(res.data); setTypeOpen(true) }}>修改</a>
          </Auth>
          <Auth permi="system:dict:remove">
            <Popconfirm title="确认删除该字典（含数据）？" onConfirm={() =>
              delDictType(row.dictId).then(() => { message.success('删除成功'); typeActionRef.current?.reload() })
            }>
              <a style={{ color: 'red' }}>删除</a>
            </Popconfirm>
          </Auth>
        </Space>
      )
    }
  ]

  const dataColumns = [
    { title: '编号', dataIndex: 'dictCode', width: 70 },
    { title: '标签', dataIndex: 'dictLabel', width: 140 },
    { title: '键值', dataIndex: 'dictValue', width: 120 },
    { title: '排序', dataIndex: 'dictSort', width: 70, align: 'center' },
    {
      title: '状态', dataIndex: 'status', width: 90, align: 'center',
      render: (v) => <Tag color={v === '0' ? 'success' : 'default'}>{v === '0' ? '正常' : '停用'}</Tag>
    },
    { title: '备注', dataIndex: 'remark', ellipsis: true },
    {
      title: '操作', width: 130,
      render: (_, row) => (
        <Space>
          <Auth permi="system:dict:edit">
            <a onClick={async () => { const res = await getDictData(row.dictCode); setEditData(res.data); setDataFormOpen(true) }}>修改</a>
          </Auth>
          <Auth permi="system:dict:remove">
            <Popconfirm title="确认删除？" onConfirm={() =>
              delDictData(row.dictCode).then(() => { message.success('删除成功'); dataActionRef.current?.reload() })
            }>
              <a style={{ color: 'red' }}>删除</a>
            </Popconfirm>
          </Auth>
        </Space>
      )
    }
  ]

  return (
    <PageContainer>
      <ProTable
        rowKey="dictId"
        actionRef={typeActionRef}
        columns={typeColumns}
        cardBordered
        search={{ labelWidth: 'auto' }}
        pagination={{ defaultPageSize: 10 }}
        request={async (params) => {
          const { current, pageSize, ...rest } = params
          const res = await listDictType({ ...rest, pageNum: current, pageSize })
          return { data: res.rows, total: res.total, success: true }
        }}
        toolBarRender={() => [
          <Auth key="add" permi="system:dict:add">
            <a onClick={() => { setEditType(null); setTypeOpen(true) }}><PlusOutlined /> 新增字典类型</a>
          </Auth>
        ]}
      />

      {/* 字典类型表单 */}
      <ModalForm
        title={editType ? '修改字典类型' : '新增字典类型'}
        open={typeOpen}
        onOpenChange={setTypeOpen}
        width={520}
        modalProps={{ destroyOnClose: true }}
        key={typeOpen ? (editType ? 'e' + editType.dictId : 'a') : 'closed'}
        initialValues={editType || { status: '0' }}
        onFinish={async (values) => {
          if (editType) {
            await updateDictType({ dictId: editType.dictId, ...values })
            message.success('修改成功')
          } else {
            await addDictType(values)
            message.success('新增成功')
          }
          typeActionRef.current?.reload()
          return true
        }}
      >
        <ProFormText name="dictName" label="字典名称" rules={[{ required: true }]} />
        <ProFormText
          name="dictType" label="字典类型" disabled={!!editType}
          rules={[{ required: true }, { pattern: /^[a-zA-Z0-9_]+$/, message: '仅限字母数字与下划线' }]}
        />
        <ProFormRadio.Group
          name="status" label="状态"
          options={[{ label: '正常', value: '0' }, { label: '停用', value: '1' }]}
        />
        <ProFormTextArea name="remark" label="备注" />
      </ModalForm>

      {/* 字典数据抽屉 */}
      <Drawer
        title={`字典数据 - ${dataDrawer || ''}`}
        width={760}
        open={dataOpen}
        onClose={() => setDataOpen(false)}
        destroyOnClose
      >
        <ProTable
          rowKey="dictCode"
          actionRef={dataActionRef}
          columns={dataColumns}
          cardBordered
          search={false}
          pagination={{ defaultPageSize: 10 }}
          params={{ dictType: dataDrawer }}
          request={async (params) => {
            const { current, pageSize, ...rest } = params
            const res = await listDictData({ ...rest, pageNum: current, pageSize })
            return { data: res.rows, total: res.total, success: true }
          }}
          toolBarRender={() => [
            <Button key="add" size="small" onClick={() => { setEditData(null); setDataFormOpen(true) }}>
              <PlusOutlined /> 新增数据
            </Button>
          ]}
        />

        <ModalForm
          title={editData ? '修改字典数据' : '新增字典数据'}
          open={dataFormOpen}
          onOpenChange={setDataFormOpen}
          width={480}
          modalProps={{ destroyOnClose: true }}
          key={dataFormOpen ? (editData ? 'e' + editData.dictCode : 'a') : 'dclosed'}
          initialValues={editData || { status: '0', dictSort: 0 }}
          onFinish={async (values) => {
            const payload = { ...values, dictType: dataDrawer }
            if (editData) {
              await updateDictData({ dictCode: editData.dictCode, ...payload })
              message.success('修改成功')
            } else {
              await addDictData(payload)
              message.success('新增成功')
            }
            dataActionRef.current?.reload()
            return true
          }}
        >
          <ProFormText name="dictLabel" label="标签" rules={[{ required: true }]} />
          <ProFormText name="dictValue" label="键值" rules={[{ required: true }]} />
          <ProFormText name="dictSort" label="排序" rules={[{ required: true }]} />
          <ProFormRadio.Group
            name="status" label="状态"
            options={[{ label: '正常', value: '0' }, { label: '停用', value: '1' }]}
          />
          <ProFormTextArea name="remark" label="备注" />
        </ModalForm>
      </Drawer>
    </PageContainer>
  )
}
