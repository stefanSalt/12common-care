import { createRouter, createWebHistory } from 'vue-router'
import AdminLayout from '../layouts/AdminLayout.vue'
import HomeView from '../views/home/HomeView.vue'
import LoginView from '../views/login/LoginView.vue'
import { useUserStore } from '../stores/user'

declare module 'vue-router' {
  interface RouteMeta {
    public?: boolean
    requiresAuth?: boolean
  }
}

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      component: LoginView,
      meta: { public: true },
    },
    {
      path: '/',
      component: AdminLayout,
      meta: { requiresAuth: true },
      children: [
        {
          path: '',
          name: 'home',
          component: HomeView,
          meta: { requiresAuth: true },
        },
      ],
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: '/',
    },
  ],
})

router.beforeEach((to) => {
  const userStore = useUserStore()

  if (to.meta.public) {
    return true
  }

  if (to.meta.requiresAuth && !userStore.token) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }

  return true
})

export default router

