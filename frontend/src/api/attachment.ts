import request from '@/utils/request'
import type { Attachment } from '@/types/system'

/**
 * 附件管理API
 */
export const attachmentApi = {
  /**
   * 获取附件列表
   */
  getAttachmentList: (params?: {
    businessType?: number
    businessId?: number
  }) => {
    return request.get.get<Attachment[]>('/system/attachments', { params })
  },

  /**
   * 根据ID获取附件信息
   */
  getAttachmentById: (attachmentId: number) => {
    return request.get.get<Attachment>(`/system/attachments/${attachmentId}`)
  },

  /**
   * 上传附件
   */
  uploadAttachment: (
    file: File,
    params?: {
      businessType?: number
      businessId?: number
      description?: string
    }
  ) => {
    const formData = new FormData()
    formData.append('file', file)
    if (params?.businessType !== undefined) {
      formData.append('businessType', params.businessType.toString())
    }
    if (params?.businessId !== undefined) {
      formData.append('businessId', params.businessId.toString())
    }
    if (params?.description) {
      formData.append('description', params.description)
    }

    return request.post.post<Attachment>('/system/attachments/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  /**
   * 删除附件（逻辑删除）
   */
  deleteAttachment: (attachmentId: number) => {
    return request.delete(`/system/attachments/${attachmentId}`)
  },

  /**
   * 更新附件信息
   */
  updateAttachment: (attachmentId: number, data: Partial<Attachment>) => {
    return request.put<Attachment>(`/system/attachments/${attachmentId}`, data)
  },

  /**
   * 根据业务类型和业务ID获取附件列表
   */
  getAttachmentsByBusiness: (businessType: number, businessId: number) => {
    return request.get.get<Attachment[]>(`/system/attachments/business/${businessType}/${businessId}`)
  },

  /**
   * 根据MD5查询附件（用于秒传）
   */
  getAttachmentsByMd5: (fileMd5: string) => {
    return request.get.get<Attachment[]>(`/system/attachments/md5/${fileMd5}`)
  }
}
