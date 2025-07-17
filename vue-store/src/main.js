// src/main.js

import { createApp } from 'vue'
import App from './App.vue'

// 導入 router
import router from './router' // Vue 會自動找到 router/index.js

import "bootstrap/dist/css/bootstrap.min.css"
import "bootstrap"

const app = createApp(App)

// 在掛載之前，使用 router
app.use(router)

app.mount('#app')