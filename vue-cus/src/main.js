import { createApp } from 'vue'
import App from './App.vue'
import router from './router/router.js'

import { createPinia } from 'pinia'
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'
import 'bootstrap/dist/js/bootstrap.bundle.min.js';
import 'bootstrap/dist/css/bootstrap.min.css';
import 'bootstrap-icons/font/bootstrap-icons.css';
import '@fortawesome/fontawesome-free/css/all.min.css';

// PrimeVue for stores
import PrimeVue from 'primevue/config'
import InputText from 'primevue/inputtext'
import Button from 'primevue/button'
import Dropdown from 'primevue/dropdown'
import Message from 'primevue/message'
import DatePicker from 'primevue/datepicker'
import Tooltip from 'primevue/tooltip';

import Aura from '@primevue/themes/aura'
import 'primeicons/primeicons.css'
// PrimeVue for stores

const pinia = createPinia();
pinia.use(piniaPluginPersistedstate);

createApp(App)
    .use(router)
    .use(pinia)
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
    .directive('tooltip', Tooltip)
    .mount('#app')