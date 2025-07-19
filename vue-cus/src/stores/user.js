// src/stores/user.js
import {
    defineStore
} from 'pinia'
import {
    ref
} from 'vue'

export const useUserStore = defineStore('user', function () {
    // 這個命名方式vue import 時會被限制一定要使用 import {useUserStore} from '@/stores/user'

    // export default defineStore('user', function () {
    // 這種命名方式在vue import 時可以隨意命名且不用{} EX: import x from '@/stores/user'
    // 初始值從 localStorage 取，但後續都用 pinia 狀態！
    // const fullName = ref(localStorage.getItem('userFullName') || '');
    // const userId = ref(localStorage.getItem('userId') || '');
    // const email = ref(localStorage.getItem('userEmail') || '');
    // const token = ref(localStorage.getItem('token') || '');
    const fullName = ref('');
    const userId = ref('');
    const email = ref('');
    const token = ref('');
    const phone = ref('');
    const isLogin = ref(false);
    const verified = ref(false);

    function setFullName(name) {
        fullName.value = name;

    }

    function setUserId(id) { // <-- 新增這一行
        userId.value = id;

    }

    function setEmail(data) {
        email.value = data;

    }

    function setToken(data) {
        token.value = data;

    }

    function setPhone(data) { // <-- 新增這一行
        phone.value = data;

    }

    function setLogin(data) {
        isLogin.value = data;
    }

    function setVerified(data) {
        verified.value = data;
    }

    function loginSuccess(data) {
        fullName.value = data.fullName || '';
        userId.value = data.userId || '';
        email.value = data.email || '';
        token.value = data.token || ''; // 確保 token 也被設定
        phone.value = data.phone || '';
        isLogin.value = true;
        verified.value = data.verified || false;
    }

    function logout() {
        fullName.value = '';
        userId.value = '';
        email.value = '';
        token.value = '';
        phone.value = '';
        isLogin.value = false;
        verified.value = false;
        // Pinia persist 插件會自動清除 localStorage，無需手動 removeItem
    }

    return {
        fullName,
        setFullName,
        email,
        setEmail,
        token,
        setToken,
        userId,
        setUserId,
        phone,
        setPhone,
        isLogin,
        setLogin,
        verified,
        setVerified,
        logout,
        loginSuccess
    }
}, {
    // ✨ 新增這個配置物件來啟用持久化
    persist: {
        storage: localStorage, // 指定使用 sessionStorage
        // 或者使用 localStorage: storage: localStorage,
        // key: 'my-cart-data', // 可選：自訂儲存到 sessionStorage/localStorage 的鍵名，預設是 store 的 id ('cart')
    }
});