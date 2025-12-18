import { describe, expect, it, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import { createTestingPinia } from '@pinia/testing';
import { createRouter, createWebHistory } from 'vue-router';
import ElementPlus from 'element-plus';
import App from '../App.vue';
import { useAuthStore } from '../stores/auth';
import type { AuthUser } from '../types';

function createRouterForTest() {
  return createRouter({
    history: createWebHistory(),
    routes: [
      { path: '/', component: { template: '<div>home</div>' } },
      { path: '/items', component: { template: '<div>items</div>' } },
      { path: '/login', component: { template: '<div>login</div>' } },
      { path: '/user', component: { template: '<div>user</div>' } },
      { path: '/admin', component: { template: '<div>admin</div>' } }
    ]
  });
}

describe('App navigation menu', () => {
  it('removes login link after user signs in', async () => {
    const router = createRouterForTest();
    router.push('/');
    await router.isReady();

    const pinia = createTestingPinia({
      stubActions: false,
      createSpy: vi.fn,
      initialState: {
        auth: {
          isRestoring: false,
          user: null
        }
      }
    });

    const wrapper = mount(App, {
      global: {
        plugins: [pinia, router, ElementPlus]
      }
    });

    const auth = useAuthStore();

    expect(wrapper.text()).toContain('登录');

    const user: AuthUser = { userId: 1, username: '测试用户', role: 'USER' };
    auth.user = user;
    await wrapper.vm.$nextTick();

    expect(wrapper.text()).not.toContain('登录');
    expect(wrapper.text()).toContain('用户中心');
  });
});
