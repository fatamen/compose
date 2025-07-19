import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '@/layouts/MainLayout.vue'
import Home from '@/views/Home.vue'
import Account from '@/views/Account.vue'
import Restaurant from '@/views/Restaurant.vue'
import Review from '@/views/Review.vue'
// import Discount from '@/views/Discount.vue'
import SubscriptionPlan from '@/views/SubscriptionPlan.vue'
import SubscriptionRecord from '@/views/SubscriptionRecord.vue'
import RestaurantTag from '@/views/RestaurantTag.vue'
import FoodTag from '@/views/FoodTag.vue'
import Recommendation from '@/views/Recommendation.vue'
import Login from '@/views/Login.vue'
import PromotionList from '@/pages/Yifan/PromotionList.vue'
import ReportType from "@/views/ReportType.vue"
// ...其餘頁面

const routes = [
    {
        path: '/',
        component: MainLayout,
        children: [
            { path: '', component: Home },
            { path: 'account', component: Account },
            { path: 'restaurant', component: Restaurant },
            { path: 'review', component: Review },
            // { path: 'discount', component: Discount },
            { path: 'promotionList', component: PromotionList },
            { path: 'subscription/plan', component: SubscriptionPlan },
            { path: 'subscription/record', component: SubscriptionRecord },
            { path: 'restaurantTag', component: RestaurantTag },
            { path: 'foodTag', component: FoodTag },
            { path: 'recommendation', component: Recommendation },
            { path: 'reportType', component: ReportType },
            { path: 'login', component: Login },
            // ...其餘子路由
        ]
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})
export default router
