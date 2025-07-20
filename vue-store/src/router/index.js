import {
    createRouter,
    createWebHistory
} from 'vue-router';

// 為了讓路由設定更乾淨，我們在這裡導入頁面組件
import StoreHome from '../views/Ivy/StoreHome.vue';
import SellerLayout from '../layouts/SellerLayout.vue';
import MenuManagement from '../pages/MenuManagement.vue';
import EditStore from '../pages/EditStore.vue';
import EditStoreUser from '../pages/EditStoreUser.vue';

import OrderManagement from '../pages/OrderManager.vue';
import BusinessHoursManager from '../pages/BusinessHoursManager.vue';
import ReservationManagement from '../pages/ReservationManagement.vue';
import TimeSettingTest from '../views/TimeSettingTest.vue';
import TimeSlotManagementPage from '../pages/TimeSlotManagementPage.vue';

// 檢查業者登入狀態的函數
function checkOwnerLogin() {
    const ownerId = localStorage.getItem('ownerId')
    const ownerEmail = localStorage.getItem('storeEmail')
    const ownerFullName = localStorage.getItem('storeFullName')

    return {
        isLoggedIn: !!ownerId,
        ownerId,
        ownerEmail,
        ownerFullName
    }
}

// 取得餐廳資料的函數
function getStoreData() {
    const storeId = localStorage.getItem('storeId')
    const storeProfile = localStorage.getItem('storeProfile')

    return {
        storeId,
        storeProfile: storeProfile ? JSON.parse(storeProfile) : null
    }
}

// 臨時測試用：設定假登入資料 (使用實際 SQL 資料)
function setTempLoginData(ownerNum = 1) {
    const tempData = {
        1: {
            ownerId: '1', // 🔥 修正：使用數字 ID
            ownerFullName: '李俊傑',
            ownerEmail: 'john.lee@example.com',
            ownerPhone: '0918765432'
        },
        2: {
            ownerId: '2',
            ownerFullName: '吳雅雯',
            ownerEmail: 'mary.wu@example.com',
            ownerPhone: '0981234567'
        },
        3: {
            ownerId: '3',
            ownerFullName: '張偉倫',
            ownerEmail: 'peter.chang@example.com',
            ownerPhone: '0927654321'
        },
        4: {
            ownerId: '4',
            ownerFullName: '陳麗華',
            ownerEmail: 'susan.chen@example.com',
            ownerPhone: '0972345678'
        },
        5: {
            ownerId: '5',
            ownerFullName: '林志明',
            ownerEmail: 'tom.lin@example.com',
            ownerPhone: '0936543210'
        }
    }

    const data = tempData[ownerNum]
    if (data) {
        // 🔥 NEW: 只設定基本的 owner 資料，讓系統從 API 載入 store 資料
        localStorage.setItem('ownerId', data.ownerId)
        localStorage.setItem('storeFullName', data.ownerFullName)
        localStorage.setItem('storeEmail', data.ownerEmail)

        // 清除之前可能存在的 store 相關資料，讓系統重新載入
        localStorage.removeItem('storeId')
        localStorage.removeItem('storeProfile')

        //console.log(`✅ 已設定臨時登入資料 - ${data.ownerFullName} (owner ID: ${data.ownerId})`)
        //console.log('🔄 請重新整理頁面來載入該 owner 的店家資料')
    }
}

// 清除登入資料
function clearLoginData() {
    localStorage.removeItem('ownerId')
    localStorage.removeItem('storeFullName')
    localStorage.removeItem('storeEmail')
    localStorage.removeItem('storeId')
    localStorage.removeItem('storeProfile')
    //console.log('🗑️ 已清除登入資料')
}

// 把這些函數掛到 window 上，方便在 //console 中測試
window.setTempLogin = setTempLoginData
window.clearLogin = clearLoginData

const routes = [{
        // 根目錄重導向邏輯：已登入→管理後台，未登入→首頁
        path: '/',
        redirect: () => {
            const ownerStatus = checkOwnerLogin()
            return ownerStatus.isLoggedIn ? '/store' : '/home'
        }
    },

    // 🔥 首頁路由 - StoreHome.vue (歡迎光臨頁面)
    {
        path: '/home', // 建議改為 /home 比較貼切
        name: 'StoreHome',
        component: StoreHome, // 🔥 使用已 import 的組件
        meta: {
            requiresOwnerAuth: false
        }
    },

    // 🔥 (備用)保留 /login 作為別名，指向同一個組件
    {
        path: '/login',
        redirect: '/home' // 重導向到首頁
    },

    // 🔥 NEW: 註冊流程相關路由 (無需登入權限)
    {
        path: '/register',
        redirect: '/home' // 直接重導向到首頁
    },

    // 🔥 管理後台路由 (需要登入權限)
    {
        path: '/store',
        component: SellerLayout, // 所有 / 開頭的路由都先經過這個佈局組件
        redirect: '/store/menu', // 預設進入菜單管理
        meta: {
            requiresOwnerAuth: true
        }, // 需要業者登入權限
        // 使用 children 來定義嵌套路由
        children: [{
                path: 'menu', // 注意：這裡沒有 /，代表是 /menu
                name: 'MenuManagement',

                component: MenuManagement,
                meta: {
                    requiresOwnerAuth: true
                }
            },

            // 未來您可以繼續在這裡新增路由

            {
                path: 'edit-owner', // 代表是 /store/edit-owner
                name: 'EditStoreUser',
                component: EditStoreUser,
                meta: {
                    requiresOwnerAuth: true
                }
            },
            {
                path: 'edit-store', // 代表是 /store/edit-store
                name: 'EditStore',
                component: EditStore,
                meta: {
                    requiresOwnerAuth: true
                }
            },
            {
                path: 'orders', // 代表是 /store/orders
                name: 'OrderManager',
                component: OrderManagement,
                meta: {
                    requiresOwnerAuth: true
                }
            },

            {
                path: 'hours', // 代表是 /store/hours
                name: 'BusinessHoursManager',
                component: BusinessHoursManager,
                meta: {
                    requiresOwnerAuth: true
                }
            },

            {
                path: 'reservations', // 代表是 /store/reservations
                name: 'ReservationManagement',
                component: ReservationManagement,
                meta: {
                    requiresOwnerAuth: true
                }
            },

            {
                path: 'time-setting-test', // 代表是 /store/time-setting-test
                name: 'TimeSettingTest',
                component: TimeSettingTest,
                meta: {
                    requiresOwnerAuth: true
                }
            },

            {
                path: 'timeslots', // 代表是 /store/timeslots
                name: 'TimeSlotManagement',
                component: TimeSlotManagementPage,
                meta: {
                    requiresOwnerAuth: true
                }
            }

        ]
    },

    // 🔥 404 處理：根據登入狀態重導向
    {
        path: '/:pathMatch(.*)*',
        redirect: () => {
            const ownerStatus = checkOwnerLogin()
            return ownerStatus.isLoggedIn ? '/store' : '/home'
        }
    }
    // {
    //     // 如果訪問不存在的路徑，重導向到 /store
    //     path: '/:pathMatch(.*)*',
    //     redirect: '/store'
    // }
];

const router = createRouter({
    history: createWebHistory(),
    routes, // 相當於 routes: routes
});

// 路由守衛：檢查業者登入狀態
router.beforeEach((to, from, next) => {
    // 檢查是否需要業者登入權限
    const requiresOwnerAuth = to.matched.some(record => record.meta.requiresOwnerAuth)

    if (requiresOwnerAuth) {
        const ownerStatus = checkOwnerLogin()

        if (!ownerStatus.isLoggedIn) {
            //console.log('⚠️ 未登入狀態')
            //console.log('💡 請在 //console 中執行以下指令來測試登入：')
            //console.log('   setTempLogin(1)  // 李俊傑 - 李俊傑的餐廳')
            //console.log('   setTempLogin(2)  // 吳雅雯 - 雅雯美食館')
            //console.log('   setTempLogin(3)  // 張偉倫 - 偉倫小廚 (未驗證email)')
            //console.log('   setTempLogin(4)  // 陳麗華 - 麗華風味餐廳')
            //console.log('   setTempLogin(5)  // 林志明 - 志明經典餐廳')
            //console.log('   clearLogin()     // 清除登入資料')

            // 🔥 正式環境：未登入時跳轉到註冊頁
            next('/register')
            return

            // 🔥 測試環境：註解掉上面的 next('/register')，取消註解下面的程式碼，可暫時阻止進入，但不跳轉
            // alert('請先設定登入資料！請查看 //console 說明。')
            // return
        }

        // 登入成功，顯示登入狀態
        //console.log('✅ 業者登入狀態:', ownerStatus)

        // 顯示餐廳資料
        const storeData = getStoreData()
        //console.log('🏪 餐廳資料:', storeData)
    }

    next()
})

export default router;