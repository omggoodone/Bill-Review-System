<template>
  <div class="topbar-wrapper">
    <el-menu
      class="topbar-menu"
      :ellipsis="false"
      :default-active="activeMenu"
      :active-text-color="theme"
      mode="horizontal"
    >
      <template v-for="menu in visibleMenus" :key="menu.path">
        <el-menu-item
          class="parent-item"
          :index="parentPath(menu)"
          @click="handleParentClick(menu)"
        >
          <svg-icon v-if="menu.meta && menu.meta.icon" :icon-class="menu.meta.icon" />
          <span>{{ menu.meta ? menu.meta.title : '' }}</span>
          <span v-if="menu.children && menu.children.length && !isExpanded(menu)" class="arrow-icon">▸</span>
        </el-menu-item>
        <template v-if="isExpanded(menu) && menu.children">
          <el-menu-item
            v-for="child in menu.children"
            :key="child.path"
            class="child-item"
            :index="childPath(menu, child)"
            @click="navigateTo(menu, child)"
          >
            <span>{{ child.meta ? child.meta.title : '' }}</span>
          </el-menu-item>
          <el-menu-item class="collapse-item" :index="parentPath(menu)" @click="collapseMenu(menu)">
            <span>◂ 收起</span>
          </el-menu-item>
        </template>
      </template>
    </el-menu>
  </div>
</template>

<script setup>
import useSettingsStore from '@/store/modules/settings'
import usePermissionStore from '@/store/modules/permission'

const route = useRoute()
const router = useRouter()
const settingsStore = useSettingsStore()
const permissionStore = usePermissionStore()

const theme = computed(() => settingsStore.theme)

const activeMenu = computed(() => {
  const { meta, path } = route
  if (meta.activeMenu) {
    return meta.activeMenu
  }
  return path
})

const visibleMenus = computed(() => {
  return permissionStore.sidebarRouters.filter(f => !f.hidden)
})

// 找到当前路由所属的父菜单
function findParentMenu(path) {
  for (const menu of visibleMenus.value) {
    const base = parentPath(menu)
    if (path === base || path.startsWith(base + '/')) {
      return menu
    }
  }
  return null
}

const expandedParentPath = ref('')

// 路由变化时自动展开对应父菜单
watch(() => route.path, (path) => {
  const parent = findParentMenu(path)
  if (parent) {
    expandedParentPath.value = parentPath(parent)
  }
}, { immediate: true })

function isExpanded(menu) {
  return expandedParentPath.value === parentPath(menu)
}

function handleParentClick(menu) {
  if (menu.children && menu.children.length) {
    if (expandedParentPath.value === parentPath(menu)) {
      // 已展开：导航到第一个子菜单
      const child = menu.children[0]
      router.push(childPath(menu, child))
    } else {
      // 展开并导航到第一个子菜单
      expandedParentPath.value = parentPath(menu)
      const child = menu.children[0]
      router.push(childPath(menu, child))
    }
  } else {
    router.push(parentPath(menu))
  }
}

function collapseMenu(menu) {
  expandedParentPath.value = ''
  router.push(parentPath(menu))
}

function parentPath(menu) {
  const p = menu.path
  return p.startsWith('/') ? p : '/' + p
}

function childPath(parent, child) {
  const base = parent.path.startsWith('/') ? parent.path : '/' + parent.path
  return base + '/' + child.path
}

function navigateTo(parent, child) {
  router.push(childPath(parent, child))
}
</script>

<style lang="scss">
.topbar-wrapper {
  flex: 1;
  min-width: 0;
}

.topbar-menu.el-menu--horizontal {
  border-bottom: none;

  > .el-menu-item {
    height: 50px;
    line-height: 50px;
    color: #303133;
    padding: 0 14px;
    margin: 0 4px;
    border-bottom: 2px solid transparent;
    flex-shrink: 0;

    &.is-active {
      border-bottom-color: v-bind(theme);
    }
  }

  > .parent-item {
    font-weight: 500;
  }

  > .child-item {
    background: #f5f7fa;
    padding: 0 16px;
    margin: 0 2px;
    font-size: 13px;

    &.is-active {
      background: #ecf5ff;
    }
  }

  > .collapse-item {
    color: #909399;
    padding: 0 12px;
    &:hover {
      color: v-bind(theme);
    }
  }
}

.topbar-menu .arrow-icon {
  font-size: 10px;
  margin-left: 2px;
  color: #c0c4cc;
}
</style>
