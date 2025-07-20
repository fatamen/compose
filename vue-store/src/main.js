// src/main.js

import { createApp } from 'vue'
import { createPinia } from 'pinia'  // 🔥 NEW: 導入 Pinia
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'
import App from './App.vue'

// 導入 router
import router from './router' // Vue 會自動找到 router/index.js

import "bootstrap/dist/css/bootstrap.min.css"
import "bootstrap"

// 🔥 NEW: 創建 Pinia 實例
const pinia = createPinia();
pinia.use(piniaPluginPersistedstate);

const app = createApp(App)

// 重要：必須在使用任何 store 之前安裝 Pinia
app.use(pinia)
app.use(router)

app.mount('#app')