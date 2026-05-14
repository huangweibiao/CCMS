import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { MenuTree, PermissionState, UserPermission, PermissionResult, MenuItem } from '@/types/permission'
import { permissionApi } from '@/api/permission'
import { useUserStore } from './user'

export const usePermissionStore = defineStore('permission', () => {
  const userStore = useUserStore()
  
  // 状态定义
  const menuTree = ref<MenuTree[]>([])
  const permissionSet = ref<Set<string>>(new Set())
  const hasLoadedPermissions = ref(false)
  const menuCache = ref<Map<string, MenuItem>>(new Map())
  const permissionCache = ref<Map<string, PermissionResult>>(new Map())
  const isLoading = ref(false)
  const loadError = ref<string | null>(null)
  // 添加角色和权限数据的直接访问属性
  const roles = ref<string[]>([])
  const permissionCodes = ref<string[]>([])

  // 计算属性
  const flattedMenuList = computed(() => {
    const flatMenus: MenuItem[] = []
    
    const flattenMenu = (menus: MenuTree[]) => {
      menus.forEach(menu => {
        flatMenus.push({
          id: menu.id,
          parentId: menu.parentId,
          menuName: menu.menuName,
          menuCode: menu.menuCode,
          menuType: menu.menuType,
          path: menu.path,
          component: menu.component,
          icon: menu.icon,
          sort: menu.sort,
          status: menu.status,
          visible: menu.visible,
          perms: menu.perms,
          remark: menu.remark,
          createTime: menu.createTime,
          updateTime: menu.updateTime
        })
        
        if (menu.children && menu.children.length > 0) {
          flattenMenu(menu.children)
        }
      })
    }
    
    flattenMenu(menuTree.value)
    return flatMenus
  })

  const allMenuPaths = computed(() => {
    const paths = new Set<string>()
    const collectPaths = (menus: MenuTree[]) => {
      menus.forEach(menu => {
        if (menu.path) {
          paths.add(menu.path)
        }
        if (menu.children && menu.children.length > 0) {
          collectPaths(menu.children)
        }
      })
    }
    
    collectPaths(menuTree.value)
    return paths
  })

  const menuPermissions = computed(() => {
    const permissions = new Set<string>()
    const collectPerms = (menus: MenuTree[]) => {
      menus.forEach(menu => {
        if (menu.perms) {
          permissions.add(menu.perms)
        }
        if (menu.children && menu.children.length > 0) {
          collectPerms(menu.children)
        }
      })
    }
    
    collectPerms(menuTree.value)
    return permissions
  })



  const isSuperAdmin = computed(() => {
    return roles.value.includes('super_admin')
  })

  const isAdmin = computed(() => {
    return roles.value.includes('admin') || isSuperAdmin.value
  })

  // Actions
  
  /**
   * 加载用户权限数据
   */
  const loadUserPermissions = async (): Promise<void> => {
    if (!userStore.isAuthenticated) {
      throw new Error('未登录用户无法加载权限')
    }
    
    if (isLoading.value) return
    
    isLoading.value = true
    loadError.value = null
    
    try {
      // 并行加载菜单树和权限集合
      const [menuResponse, permissionResponse] = await Promise.all([
        permissionApi.getUserMenuTree(),
        permissionApi.getPermissionSet()
      ])
      
      // 更新状态
      menuTree.value = menuResponse.data || []
      permissionSet.value = new Set(permissionResponse.data || [])
      hasLoadedPermissions.value = true
      
      // 构建菜单缓存
      buildMenuCache()
      
    } catch (error) {
      loadError.value = error instanceof Error ? error.message : '权限加载失败'
      console.error('加载用户权限失败:', error)
      throw error
    } finally {
      isLoading.value = false
    }
  }

  /**
   * 构建菜单缓存
   */
  const buildMenuCache = (): void => {
    menuCache.value.clear()
    
    const addToCache = (menus: MenuTree[]) => {
      menus.forEach(menu => {
        menuCache.value.set(menu.menuCode, menu)
        if (menu.children && menu.children.length > 0) {
          addToCache(menu.children)
        }
      })
    }
    
    addToCache(menuTree.value)
  }

  /**
   * 检查是否有菜单权限
   */
  const hasMenuPermission = (menuCode: string): boolean => {
    if (!hasLoadedPermissions.value) return false
    return menuCache.value.has(menuCode)
  }

  /**
   * 检查是否有权限代码
   */
  const hasPermission = (permissionCode: string | string[], mode: 'any' | 'all' = 'any'): boolean => {
    if (!hasLoadedPermissions.value) return false
    
    // 管理员拥有所有权限
    if (userStore.isAdmin || userStore.isSuperAdmin) return true
    
    const checkSinglePermission = (code: string): boolean => {
      // 检查权限集合
      if (permissionSet.value.has('*') || permissionSet.value.has(code)) {
        return true
      }
      
      // 支持通配符权限检查
      for (const perm of permissionSet.value) {
        if (perm.endsWith(':*') && code.startsWith(perm.replace(':*', ''))) {
          return true
        }
      }
      
      return false
    }
    
    if (typeof permissionCode === 'string') {
      return checkSinglePermission(permissionCode)
    }
    
    // 数组权限检查
    if (mode === 'any') {
      return permissionCode.some(code => checkSinglePermission(code))
    } else {
      return permissionCode.every(code => checkSinglePermission(code))
    }
  }

  /**
   * 批量检查权限
   */
  const hasPermissions = (permissionCodes: string[]): boolean => {
    return permissionCodes.every(code => hasPermission(code))
  }

  /**
   * 检查是否有角色
   */
  const hasRole = (role: string | string[], mode: 'any' | 'all' = 'any'): boolean => {
    // 超级管理员拥有所有角色
    if (isSuperAdmin.value) return true
    
    if (typeof role === 'string') {
      return roles.value.includes(role)
    }
    
    // 数组角色检查
    if (mode === 'any') {
      return role.some(r => roles.value.includes(r))
    } else {
      return role.every(r => roles.value.includes(r))
    }
  }



  /**
   * 加载权限数据
   */
  const loadPermissions = async (): Promise<void> => {
    try {
      await loadUserPermissions()
      
      // 从用户store获取角色信息
      if (userStore.userInfo?.roles) {
        roles.value = userStore.userInfo.roles
      }
    } catch (error) {
      throw error
    }
  }

  /**
   * 更新权限数据
   */
  const updatePermissions = (newPermissions: string[]) => {
    permissionSet.value = new Set(newPermissions)
    permissionCodes.value = newPermissions
  }

  /**
   * 更新角色数据
   */
  const updateRoles = (newRoles: string[]) => {
    roles.value = newRoles
  }

  /**
   * 检查路径权限
   */
  const hasPathPermission = (path: string): boolean => {
    if (!hasLoadedPermissions.value) return false
    return allMenuPaths.value.has(path)
  }

  /**
   * 根据路径获取菜单
   */
  const getMenuByPath = (path: string): MenuItem | undefined => {
    if (!hasLoadedPermissions.value) return undefined
    
    const findMenuByPath = (menus: MenuTree[]): MenuItem | undefined => {
      for (const menu of menus) {
        if (menu.path === path) {
          return menu
        }
        if (menu.children && menu.children.length > 0) {
          const found = findMenuByPath(menu.children)
          if (found) return found
        }
      }
      return undefined
    }
    
    return findMenuByPath(menuTree.value)
  }

  /**
   * 根据菜单代码获取菜单
   */
  const getMenuByCode = (menuCode: string): MenuItem | undefined => {
    return menuCache.value.get(menuCode)
  }

  /**
   * 检查权限并缓存结果
   */
  const checkPermissionWithCache = async (permissionCode: string): Promise<boolean> => {
    if (!userStore.isAuthenticated) return false
    
    // 检查缓存
    const cachedResult = permissionCache.value.get(permissionCode)
    if (cachedResult) {
      return cachedResult.hasPermission
    }
    
    try {
      // 如果没有加载权限，先加载
      if (!hasLoadedPermissions.value) {
        await loadUserPermissions()
      }
      
      // 检查权限
      const hasPerm = hasPermission(permissionCode)
      
      // 缓存结果（5分钟有效期）
      permissionCache.value.set(permissionCode, {
        permissionCode,
        hasPermission: hasPerm
      })
      
      return hasPerm
    } catch (error) {
      console.error('权限检查失败:', error)
      return false
    }
  }

  /**
   * 清除权限缓存
   */
  const clearCache = (): void => {
    permissionCache.value.clear()
    menuCache.value.clear()
  }

  /**
   * 重置权限状态
   */
  const reset = (): void => {
    menuTree.value = []
    permissionSet.value = new Set()
    hasLoadedPermissions.value = false
    clearCache()
    isLoading.value = false
    loadError.value = null
  }

  return {
    // 状态
    menuTree,
    permissionSet,
    hasLoadedPermissions,
    isLoading,
    loadError,
    roles,
    permissionCodes,
    
    // 计算属性
    flattedMenuList,
    allMenuPaths,
    menuPermissions,
    isSuperAdmin,
    isAdmin,
    
    // actions
    loadUserPermissions,
    hasMenuPermission,
    hasPermission,
    hasPermissions,
    hasPathPermission,
    getMenuByPath,
    getMenuByCode,
    checkPermissionWithCache,
    clearCache,
    reset,
    hasRole,
    loadPermissions,
    updatePermissions,
    updateRoles
  }
})