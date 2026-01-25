export type LongLike = string | number

export type PageResult<T> = {
  records: T[]
  total: LongLike
  current: LongLike
  size: LongLike
}

