import { ElNotification } from 'element-plus'

type WsMessage = { type?: string; data?: any }

let ws: WebSocket | null = null
let manualClose = false
let lastToken = ''
let reconnectTimer: number | null = null

function buildWsUrl(token: string) {
  const scheme = window.location.protocol === 'https:' ? 'wss' : 'ws'
  return `${scheme}://${window.location.host}/ws/notification?token=${encodeURIComponent(token)}`
}

function clearReconnectTimer() {
  if (reconnectTimer != null) {
    window.clearTimeout(reconnectTimer)
    reconnectTimer = null
  }
}

export function connectNotificationWs(token: string) {
  if (!token) return
  lastToken = token

  // Already connected/connecting.
  if (ws && (ws.readyState === WebSocket.OPEN || ws.readyState === WebSocket.CONNECTING)) {
    return
  }

  manualClose = false
  clearReconnectTimer()

  ws = new WebSocket(buildWsUrl(token))

  ws.onmessage = (ev) => {
    try {
      const msg = JSON.parse(String(ev.data)) as WsMessage
      if (msg.type === 'init' && Array.isArray(msg.data) && msg.data.length > 0) {
        ElNotification({
          title: '未读通知',
          message: `你有 ${msg.data.length} 条未读通知`,
          type: 'info',
          duration: 3000,
        })
        return
      }
      if (msg.type === 'notification' && msg.data) {
        ElNotification({
          title: msg.data.title ?? '通知',
          message: msg.data.content ?? '',
          type: 'success',
          duration: 5000,
        })
      }
    } catch {
      // ignore
    }
  }

  ws.onclose = () => {
    ws = null
    if (manualClose || !lastToken) return
    clearReconnectTimer()
    reconnectTimer = window.setTimeout(() => connectNotificationWs(lastToken), 2000)
  }

  ws.onerror = () => {
    // Let onclose handle reconnect.
  }
}

export function disconnectNotificationWs() {
  manualClose = true
  lastToken = ''
  clearReconnectTimer()
  if (ws) {
    try {
      ws.close()
    } catch {
      // ignore
    } finally {
      ws = null
    }
  }
}

