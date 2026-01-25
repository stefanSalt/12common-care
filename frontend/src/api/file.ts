import request, { type ApiResult } from '../utils/request'
import type { LongLike, PageResult } from './types'

export type FileVisibility = 'PUBLIC' | 'PRIVATE'

export type FileInfoDto = {
  id: string
  originalName: string
  size: LongLike
  contentType?: string
  visibility: FileVisibility
}

export async function listMyFiles(current = 1, size = 10) {
  const res = await request.get<ApiResult<PageResult<FileInfoDto>>>('/files', { params: { current, size } })
  return res.data.data
}

export async function uploadFile(file: File, visibility?: FileVisibility) {
  const form = new FormData()
  form.append('file', file)
  const res = await request.post<ApiResult<FileInfoDto>>('/files/upload', form, {
    params: visibility ? { visibility } : undefined,
    headers: { 'Content-Type': 'multipart/form-data' },
  })
  return res.data.data
}

export async function deleteFile(id: string) {
  const res = await request.delete<ApiResult<null>>(`/files/${id}`)
  return res.data.data
}

export async function downloadFileBlob(id: string) {
  const res = await request.get(`/files/${id}/download`, { responseType: 'blob' })
  return res.data as Blob
}

