export const storage = {
  getString(key: string): string | null {
    try {
      return localStorage.getItem(key)
    } catch {
      return null
    }
  },
  setString(key: string, value: string) {
    try {
      localStorage.setItem(key, value)
    } catch {
      // ignore
    }
  },
  getJson<T>(key: string): T | null {
    const raw = storage.getString(key)
    if (!raw) return null
    try {
      return JSON.parse(raw) as T
    } catch {
      return null
    }
  },
  setJson(key: string, value: unknown) {
    try {
      localStorage.setItem(key, JSON.stringify(value))
    } catch {
      // ignore
    }
  },
  remove(key: string) {
    try {
      localStorage.removeItem(key)
    } catch {
      // ignore
    }
  },
}

