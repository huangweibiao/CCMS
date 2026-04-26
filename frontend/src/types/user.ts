/**
 * 用户相关类型定义
 */

export interface LoginData {
  username: string
  password: string
}

export interface UserInfo {
  id: number
  userCode: string
  userName: string
  deptId: number
  deptName: string
  role: 'admin' | 'user' | 'finance' | 'approver'
  status: number
  mobile?: string
  email?: string
  createTime: string
}

export interface LoginResponse {
  token: string
  userInfo: UserInfo
}

export interface DeptInfo {
  id: number
  deptCode: string
  deptName: string
  parentId?: number
  leaderId?: number
  status: number
  children?: DeptInfo[]
}

export interface RoleInfo {
  id: number
  roleCode: string
  roleName: string
  status: number
}