import useDictStore from '@/store/modules/dict'
import { getDicts } from '@/api/system/dict/data'

/**
 * 获取字典数据
 */
export function useDict(...args) {
  const res = ref({})
  return (() => {
    args.forEach((dictType, index) => {
      res.value[dictType] = []
      const dicts = useDictStore().getDict(dictType)
      if (dicts) {
        res.value[dictType] = dicts
      } else {
        getDicts(dictType).then(resp => {
          res.value[dictType] = resp.data.map(p => {
            // 如果 DB 没配颜色，按字典类型 + 值自动匹配
            const statusColors = { '0': 'info', '1': 'warning', '2': 'success', '3': 'danger' }
            const enableColors  = { '0': 'success', '1': 'warning' }
            const autoColor = dictType === 'biz_bill_status' ? statusColors : enableColors
            return {
              label: p.dictLabel,
              value: p.dictValue,
              elTagType: p.listClass || autoColor[p.dictValue] || '',
              elTagClass: p.cssClass || ''
            }
          })
          useDictStore().setDict(dictType, res.value[dictType])
        })
      }
    })
    return toRefs(res.value)
  })()
}