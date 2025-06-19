import { defineStore } from 'pinia'
import { ref } from 'vue'

export default defineStore('user', function() {
    const email = ref("");
    function setEmail(data) {
        email.value = data;
    }

    const token = ref("");
    function setToken(data) {
        token.value = data;
    }

    const isLogin = ref(false);
    function setLogin(data) {
        isLogin.value = data;
    }

    return {
        email, setEmail,
        token, setToken,
        isLogin, setLogin
    }
}, {
    persist: {
        storage: sessionStorage,
    }
});