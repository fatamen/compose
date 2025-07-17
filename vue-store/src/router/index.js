import {
    createRouter,
    createWebHistory
} from 'vue-router';

// 為了讓路由設定更乾淨，我們在這裡導入頁面組件
import SellerLayout from '../layouts/SellerLayout.vue';
import MenuManagement from '../pages/MenuManagement.vue';
import OrderManagement from '../pages/OrderManager.vue';
import BusinessHoursManager from '../pages/BusinessHoursManager.vue';
import ReservationManagement from '../pages/ReservationManagement.vue';
import TimeSettingTest from '../views/TimeSettingTest.vue';

const routes = [{
    // 我們將根目錄暫時重導向到菜單管理頁面
    // 未來當您開發好 Dashboard，只需要把這行改成
    // component: () => import('../pages/SellerDashboard.vue') 即可
    path: '/',
    component: SellerLayout, // 所有 / 開頭的路由都先經過這個佈局組件
    redirect: '/menu',
    // 使用 children 來定義嵌套路由
    children: [{
            path: 'menu', // 注意：這裡沒有 /，代表是 /menu
            name: 'MenuManagement',
            component: MenuManagement
        },

        // 未來您可以繼續在這裡新增路由
        {
            path: 'orders', // 代表是 /orders
            name: 'OrderManager',
            component: OrderManagement
        },

        {
            path: 'hours', // 代表是 /hours
            name: 'BusinessHoursManager',
            component: BusinessHoursManager
        },
        {
            path: 'reservations', // 代表是 /reservations
            name: 'ReservationManagement',
            component: ReservationManagement
        },
        {
            path: 'time-setting-test', // 代表是 /time-setting-test
            name: 'TimeSettingTest',
            component: TimeSettingTest
        }
    ]
}, ];

const router = createRouter({
    history: createWebHistory(),
    routes, // 相當於 routes: routes
});

export default router;