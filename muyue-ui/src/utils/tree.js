/**
 * 将平铺数据构建成树结构（与后端返回结构保持一致）
 *
 * @param {Array} data 数据源
 * @param {String} id 主键字段
 * @param {String} parentId 父节点字段
 * @param {String} children 子节点字段
 */
export function handleTree(data, id = 'id', parentId = 'parentId', children = 'children') {
  if (!Array.isArray(data)) {
    return []
  }
  const childrenListMap = {}
  const nodeIds = {}
  const tree = []

  for (const d of data) {
    const parentIdValue = d[parentId]
    if (!childrenListMap[parentIdValue]) {
      childrenListMap[parentIdValue] = []
    }
    childrenListMap[parentIdValue].push(d)
    nodeIds[d[id]] = d
  }

  for (const d of data) {
    const parentIdValue = d[parentId]
    if (!nodeIds[parentIdValue]) {
      tree.push(d)
    }
  }

  const adaptToChildrenList = (o) => {
    if (childrenListMap[o[id]] !== undefined) {
      o[children] = childrenListMap[o[id]]
    }
    if (o[children]) {
      for (const c of o[children]) {
        adaptToChildrenList(c)
      }
    }
  }

  for (const t of tree) {
    adaptToChildrenList(t)
  }
  return tree
}

/**
 * 构建 el-tree-select / el-tree 使用的数据结构
 */
export function buildTreeData(data, idKey, labelKey) {
  if (!Array.isArray(data)) {
    return []
  }
  const map = {}
  const tree = []
  for (const item of data) {
    map[item[idKey]] = { id: item[idKey], label: item[labelKey], children: [] }
  }
  for (const item of data) {
    const node = map[item[idKey]]
    const parent = item.parentId !== undefined ? map[item.parentId] : undefined
    if (parent) {
      parent.children.push(node)
    } else {
      tree.push(node)
    }
  }
  const clean = (nodes) => {
    for (const node of nodes) {
      if (node && node.children && node.children.length === 0) {
        delete node.children
      } else if (node && node.children) {
        clean(node.children)
      }
    }
  }
  clean(tree)
  return tree
}
