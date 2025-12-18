import { createApp } from 'vue';
import ElementPlus from 'element-plus';
import 'element-plus/dist/index.css';
import { createPinia } from 'pinia';
import App from './App.vue';
import router from './router';
import { useAuthStore } from './stores/auth';

const app = createApp(App);
const pinia = createPinia();
const auth = useAuthStore(pinia);
auth.restoreFromCache();

app.use(pinia);
app.use(router);
app.use(ElementPlus);

app.mount('#app');
