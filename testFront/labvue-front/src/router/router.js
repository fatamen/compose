import { createRouter, createWebHistory } from 'vue-router'

import Home from "@/views/Home.vue"
import NotFound from "@/views/NotFound.vue"
import Forbidden from "@/views/Forbidden.vue"

import Login from '@/views/secure/Login.vue'
import Product from '@/views/pages/Product.vue'

const routes = [
    {
        path: "/",
        component: Home,
        name: "home-link"
    },
    {
        path: "/:pathMatch(.*)*",
        component: NotFound,
        name: "404-link"
    },
    {
        path: "/403",
        component: Forbidden,
        name: "403-link"
    },
    {
        path: "/secure/login",
        component: Login,
        name: "secure-login-link"
    },
    {
        path: "/pages/product",
        component: Product,
        name: "page-product-link"
    },
];

const router = createRouter({
    history: createWebHistory(),
    routes: routes
});

export default router;