import type { App, DirectiveBinding } from 'vue'
import { useUserStore } from '../stores/user'

function hasPermission(value: unknown, permissions: string[]): boolean {
  if (!value) return true
  if (typeof value === 'string') return permissions.includes(value)
  if (Array.isArray(value)) return value.every((p) => typeof p === 'string' && permissions.includes(p))
  return false
}

export function setupPermissionDirective(app: App) {
  app.directive('permission', {
    mounted(el: HTMLElement, binding: DirectiveBinding) {
      const userStore = useUserStore()
      if (!hasPermission(binding.value, userStore.permissions)) {
        el.parentNode?.removeChild(el)
      }
    },
  })
}

