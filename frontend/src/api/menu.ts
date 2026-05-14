import request from '@/utils/request'
import type { MenuItem, MenuTree } from '@/types/permission'

export const menuApi = {
  /**
   * 获取菜单详情
   */
  getMenuDetail: (id: number): Promise<ApiResponse<MenuItem>> => {
    return request.get(`/api/menus/${id}`)
  },

  /**
   * 创建菜单
   */
  createMenu: (data: Omit<MenuItem, 'id' | 'createTime' | 'updateTime'>): Promise<ApiResponse<MenuItem>> => {
    return request.post('/api/menus', data)
  },

  /**
   * 更新菜单
   */
  updateMenu: (id: number, data: Partial<MenuItem>): Promise<ApiResponse<MenuItem>> => {
    return request.put(`/api/menus/${id}`, data)
  },

  /**
   * 删除菜单
   */
  deleteMenu: (id: number): Promise<ApiResponse<void>> => {
    return request.delete(`/api/menus/${id}`)
  },

  /**
   * 获取完整的菜单树（用于管理页面）
   */
  getFullMenuTree: (): Promise<ApiResponse<MenuTree[]>> => {
    return request.get('/api/menus/full-tree')
  },

  /**
   * 菜单排序
   */
  sortMenus: (menuIds: number[]): Promise<ApiResponse<void>> => {
    return request.put('/api/menus/sort', { menuIds })
  }
}