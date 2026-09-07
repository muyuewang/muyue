/**
 * 为查询参数追加时间范围条件
 *
 * @param {Object} params 查询参数
 * @param {Array} dateRange 时间范围 [begin, end]
 * @param {String} propName 字段名前缀，不传则使用 beginTime/endTime
 */
export function addDateRange(params, dateRange, propName) {
  const search = { ...params }
  if (propName) {
    search[propName + 'BeginTime'] = undefined
    search[propName + 'EndTime'] = undefined
  } else {
    search.beginTime = undefined
    search.endTime = undefined
  }
  if (dateRange && dateRange.length === 2) {
    if (propName) {
      search[propName + 'BeginTime'] = dateRange[0]
      search[propName + 'EndTime'] = dateRange[1]
    } else {
      search.beginTime = dateRange[0]
      search.endTime = dateRange[1]
    }
  }
  return search
}

/**
 * 通用字典标签转换
 */
export function selectDictLabel(options, value) {
  const item = (options || []).find((option) => String(option.value) === String(value))
  return item ? item.label : String(value)
}
