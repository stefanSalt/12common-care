import { createRouter, createWebHistory } from 'vue-router'
import AdminLayout from '../layouts/AdminLayout.vue'
import FrontendLayout from '../layouts/FrontendLayout.vue'
import HomeView from '../views/home/HomeView.vue'
import FrontHomeView from '../views/home/FrontHomeView.vue'
import LoginView from '../views/login/LoginView.vue'
import RegisterView from '../views/register/RegisterView.vue'
import ActivitiesListView from '../views/activity/ActivitiesListView.vue'
import ActivityDetailView from '../views/activity/ActivityDetailView.vue'
import MySignupsView from '../views/activity/MySignupsView.vue'
import MyFavoritesView from '../views/activity/MyFavoritesView.vue'
import MyDonationsView from '../views/activity/MyDonationsView.vue'
import StoriesListView from '../views/story/StoriesListView.vue'
import StoryDetailView from '../views/story/StoryDetailView.vue'
import MyCommentsView from '../views/story/MyCommentsView.vue'
import CrowdfundingListView from '../views/crowdfunding/CrowdfundingListView.vue'
import CrowdfundingCreateView from '../views/crowdfunding/CrowdfundingCreateView.vue'
import CrowdfundingDetailView from '../views/crowdfunding/CrowdfundingDetailView.vue'
import MyCrowdfundingDonationsView from '../views/crowdfunding/MyCrowdfundingDonationsView.vue'
import MyCrowdfundingProjectsView from '../views/crowdfunding/MyCrowdfundingProjectsView.vue'
import FilesView from '../views/admin/FilesView.vue'
import BannersView from '../views/admin/BannersView.vue'
import ActivitiesView from '../views/admin/ActivitiesView.vue'
import StatsView from '../views/admin/StatsView.vue'
import StoriesView from '../views/admin/StoriesView.vue'
import CommentsView from '../views/admin/CommentsView.vue'
import CrowdfundingDonationsView from '../views/admin/CrowdfundingDonationsView.vue'
import CrowdfundingProjectsView from '../views/admin/CrowdfundingProjectsView.vue'
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
          component: FrontHomeView,
          meta: { public: true },
        },
      ],
    },
    {
      path: '/activities',
      component: FrontendLayout,
      meta: { public: true },
      children: [
        {
          path: '',
          name: 'front-activities',
          component: ActivitiesListView,
          meta: { public: true },
        },
        {
          path: ':id',
          name: 'front-activity-detail',
          component: ActivityDetailView,
          meta: { public: true },
        },
      ],
    },
    {
      path: '/stories',
      component: FrontendLayout,
      meta: { public: true },
      children: [
        {
          path: '',
          name: 'front-stories',
          component: StoriesListView,
          meta: { public: true },
        },
        {
          path: ':id',
          name: 'front-story-detail',
          component: StoryDetailView,
          meta: { public: true },
        },
      ],
    },
    {
      path: '/crowdfunding',
      component: FrontendLayout,
      meta: { public: true },
      children: [
        {
          path: '',
          name: 'front-crowdfunding',
          component: CrowdfundingListView,
          meta: { public: true },
        },
        {
          path: 'new',
          name: 'front-crowdfunding-new',
          component: CrowdfundingCreateView,
          meta: { requiresAuth: true, public: false },
        },
        {
          path: 'edit/:id',
          name: 'front-crowdfunding-edit',
          component: CrowdfundingCreateView,
          meta: { requiresAuth: true, public: false },
        },
        {
          path: ':id',
          name: 'front-crowdfunding-detail',
          component: CrowdfundingDetailView,
          meta: { public: true },
        },
      ],
    },
    {
      path: '/my',
      component: FrontendLayout,
      meta: { requiresAuth: true },
      children: [
        {
          path: 'signups',
          name: 'front-my-signups',
          component: MySignupsView,
          meta: { requiresAuth: true },
        },
        {
          path: 'favorites',
          name: 'front-my-favorites',
          component: MyFavoritesView,
          meta: { requiresAuth: true },
        },
        {
          path: 'donations',
          name: 'front-my-donations',
          component: MyDonationsView,
          meta: { requiresAuth: true },
        },
        {
          path: 'comments',
          name: 'front-my-comments',
          component: MyCommentsView,
          meta: { requiresAuth: true },
        },
        {
          path: 'crowdfunding-donations',
          name: 'front-my-crowdfunding-donations',
          component: MyCrowdfundingDonationsView,
          meta: { requiresAuth: true },
        },
        {
          path: 'crowdfunding-projects',
          name: 'front-my-crowdfunding-projects',
          component: MyCrowdfundingProjectsView,
          meta: { requiresAuth: true },
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
          path: 'banners',
          name: 'admin-banners',
          component: BannersView,
          meta: { requiresAuth: true, permission: 'banner:list' },
        },
        {
          path: 'activities',
          name: 'admin-activities',
          component: ActivitiesView,
          meta: { requiresAuth: true, permission: 'activity:list' },
        },
        {
          path: 'activity-signups',
          name: 'admin-activity-signups',
          component: ActivitiesView,
          meta: { requiresAuth: true, permission: 'activitySignup:list' },
        },
        {
          path: 'activity-donations',
          name: 'admin-activity-donations',
          component: ActivitiesView,
          meta: { requiresAuth: true, permission: 'activityDonation:list' },
        },
        {
          path: 'activity-favorites',
          name: 'admin-activity-favorites',
          component: ActivitiesView,
          meta: { requiresAuth: true, permission: 'activityFavorite:list' },
        },
        {
          path: 'stats',
          name: 'admin-stats',
          component: StatsView,
          meta: { requiresAuth: true, permission: 'stats:view' },
        },
        {
          path: 'stories',
          name: 'admin-stories',
          component: StoriesView,
          meta: { requiresAuth: true, permission: 'story:list' },
        },
        {
          path: 'comments',
          name: 'admin-comments',
          component: CommentsView,
          meta: { requiresAuth: true, permission: 'comment:list' },
        },
        {
          path: 'crowdfunding',
          name: 'admin-crowdfunding',
          component: CrowdfundingProjectsView,
          meta: { requiresAuth: true, permission: 'crowdfunding:list' },
        },
        {
          path: 'crowdfunding-donations',
          name: 'admin-crowdfunding-donations',
          component: CrowdfundingDonationsView,
          meta: { requiresAuth: true, permission: 'crowdfundingDonation:list' },
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

  if (to.path.startsWith('/admin')) {
    const isAdmin = (userStore.user?.roles ?? []).some((r) => r.code === 'admin')
    if (!isAdmin) {
      return { path: '/' }
    }
  }

  return true
})

export default router
