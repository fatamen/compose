import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap/dist/js/bootstrap.bundle.min.js'
import 'bootstrap-icons/font/bootstrap-icons.css'


import '@/assets/styles/common.css'
import PrimeVue from 'primevue/config'
import InputText from 'primevue/inputtext'
import Button from 'primevue/button'
import Dropdown from 'primevue/dropdown'
import Message from 'primevue/message'
import DatePicker from 'primevue/datepicker'

import Aura from '@primevue/themes/aura'
import 'primeicons/primeicons.css'

createApp(App)
.use(router)
.use(PrimeVue, {
    theme: {
        preset: Aura
    }
})
.component('InputText', InputText)
.component('Button', Button)
.component('Dropdown', Dropdown)
.component('Message', Message)
.component('DatePicker', DatePicker)
.mount('#app')
