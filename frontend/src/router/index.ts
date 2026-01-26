import { defineComponent, h } from 'vue'
import { createRouter, createWebHistory } from 'vue-router'
import AdminLayout from '../layouts/AdminLayout.vue'
import FrontendLayout from '../layouts/FrontendLayout.vue'
import HomeView from '../views/home/HomeView.vue'
import LoginView from '../views/login/LoginView.vue'
import RegisterView from '../views/register/RegisterView.vue'
import FilesView from '../views/admin/FilesView.vue'
import AdminMessagesView from '../views/admin/MessagesView.vue'
import NotificationsView from '../views/admin/NotificationsView.vue'
import PermissionsView from '../views/admin/PermissionsView.vue'
import RolesView from '../views/admin/RolesView.vue'
import UsersView from '../views/admin/UsersView.vue'
import FrontMessagesView from '../views/message/MessagesView.vue'
import ProfileView from '../views/profile/ProfileView.vue'
import { useUserStore } from '../stores/user'

declare module 'vue-router' {
  interface RouteMeta {
    public?: boolean
    requiresAuth?: boolean
    permission?: string
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
      path: '/register',
      component: RegisterView,
      meta: { public: true },
    },
    {
      path: '/',
      component: FrontendLayout,
      meta: { public: true },
      children: [
        {
          path: '',
          name: 'front-home',
          component: defineComponent({
            name: 'FrontHomePlaceholder',
            setup() {
              return () => h('div', '前台占位页面')
            },
          }),
          meta: { public: true },
        },
      ],
    },
    {
      path: '/profile',
      component: FrontendLayout,
      meta: { requiresAuth: true },
      children: [
        {
          path: '',
          name: 'front-profile',
          component: ProfileView,
          meta: { requiresAuth: true },
        },
      ],
    },
    {
      path: '/messages',
      component: FrontendLayout,
      meta: { requiresAuth: true },
      children: [
        {
          path: '',
          name: 'front-messages',
          component: FrontMessagesView,
          meta: { requiresAuth: true },
        },
      ],
    },
    {
      path: '/admin',
      component: AdminLayout,
      meta: { requiresAuth: true },
      children: [
        {
          path: '',
          name: 'admin-home',
          component: HomeView,
          meta: { requiresAuth: true },
        },
        {
          path: 'users',
          name: 'admin-users',
          component: UsersView,
          meta: { requiresAuth: true, permission: 'user:list' },
        },
        {
          path: 'roles',
          name: 'admin-roles',
          component: RolesView,
          meta: { requiresAuth: true, permission: 'role:list' },
        },
        {
          path: 'permissions',
          name: 'admin-permissions',
          component: PermissionsView,
          meta: { requiresAuth: true, permission: 'permission:list' },
        },
        {
          path: 'files',
          name: 'admin-files',
          component: FilesView,
          meta: { requiresAuth: true },
        },
        {
          path: 'notifications',
          name: 'admin-notifications',
          component: NotificationsView,
          meta: { requiresAuth: true },
        },
        {
          path: 'messages',
          name: 'admin-messages',
          component: AdminMessagesView,
          meta: { requiresAuth: true, permission: 'message:list' },
        },
        {
          path: 'profile',
          name: 'admin-profile',
          component: ProfileView,
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
