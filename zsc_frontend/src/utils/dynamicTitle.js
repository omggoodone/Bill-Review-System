import useSettingsStore from '@/store/modules/settings'
import useUserStore from '@/store/modules/user'

/**
 * 动态修改标题，根据用户角色切换
 */
export function useDynamicTitle() {
  const settingsStore = useSettingsStore()
  const userStore = useUserStore()
  const roleTitle = userStore.roles.includes('admin') ? '票据管理后台' : '票据报销系统'

  if (settingsStore.dynamicTitle) {
    document.title = settingsStore.title + ' - ' + roleTitle
  } else {
    document.title = roleTitle
  }
}