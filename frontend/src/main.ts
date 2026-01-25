import { createApp, watch } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './style.css'
import App from './App.vue'
import router from './router'
import { setupPermissionDirective } from './directives/permission'
import { useUserStore } from './stores/user'
import { connectNotificationWs, disconnectNotificationWs } from './utils/notificationWs'

const app = createApp(App)

const pinia = createPinia()
app.use(pinia)
app.use(router)
app.use(ElementPlus)

setupPermissionDirective(app)

// 4B: connect WS right after login (as long as token exists), even if user stays on the front layout.
const userStore = useUserStore(pinia)
watch(
  () => userStore.token,
  (token) => {
    if (token) connectNotificationWs(token)
    else disconnectNotificationWs()
  },
  { immediate: true },
)

app.mount('#app')
