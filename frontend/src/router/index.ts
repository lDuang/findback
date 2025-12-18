import { createRouter, createWebHistory, type RouteLocationNormalized } from 'vue-router';
import Home from '../views/Home.vue';
import ItemsList from '../views/ItemsList.vue';
import ItemDetail from '../views/ItemDetail.vue';
import Login from '../views/Login.vue';
import UserCenter from '../views/UserCenter.vue';
import AdminDashboard from '../views/AdminDashboard.vue';
import { useAuthStore } from '../stores/auth';
import { normalizeRole } from '../utils/auth';

type AppRouteMeta = RouteLocationNormalized['meta'] & {
  requiresAuth?: boolean;
  roles?: string[];
  title?: string;
};

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'home', component: Home, meta: { title: '首页' } },
    { path: '/items', name: 'items', component: ItemsList, meta: { title: '物品列表' } },
    { path: '/items/:id', name: 'item-detail', component: ItemDetail, props: true, meta: { title: '物品详情' } },
    { path: '/login', name: 'login', component: Login, meta: { title: '登录' } },
    {
      path: '/user',
      name: 'user',
      component: UserCenter,
      meta: { requiresAuth: true, title: '用户中心' }
    },
    {
      path: '/admin',
      name: 'admin',
      component: AdminDashboard,
      meta: { requiresAuth: true, roles: ['ADMIN'], title: '管理员面板' }
    }
  ]
});

router.beforeEach((to) => {
  const auth = useAuthStore();
  const meta = (to.meta || {}) as AppRouteMeta;
  const requiresAuth = meta.requiresAuth;
  const allowedRoles = meta.roles;
  const userRole = normalizeRole(auth.user?.role ?? '');

  if (requiresAuth && !auth.user) {
    return { name: 'login', query: { redirect: to.fullPath } };
  }

  if (allowedRoles && auth.user && (!userRole || !allowedRoles.includes(userRole))) {
    return { name: 'home' };
  }

  return true;
});

router.afterEach((to) => {
  const baseTitle = '失物招领系统';
  const meta = (to.meta || {}) as AppRouteMeta;
  const pageTitle = meta.title;
  document.title = pageTitle ? `${pageTitle} - ${baseTitle}` : baseTitle;
});

export default router;
