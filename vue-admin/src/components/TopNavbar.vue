<template>
    <header class="navbar">
    <a class="navbar-brand d-flex align-items-center gap-3" style="cursor: pointer" @click="$router.push('/search')">
        <img src="@/assets/logo.png" alt="Logo" height="80" />
        <span class="brand-title">網站後台管理系統</span>
    </a>
        <div class="d-flex align-items-center ms-auto gap-3">
            <!-- 功能選單 -->
            <div class="dropdown me-2">
                <button
                    class="dropdown-toggle action-plain-btn"
                    type="button"
                    id="quickActionsDropdown"
                    data-bs-toggle="dropdown"
                    aria-expanded="false"
                >
                    📂
                </button>
            <ul class="dropdown-menu dropdown-menu-end" aria-labelledby="quickActionsDropdown">
                <li><router-link class="dropdown-item" to="/promotion">行銷活動</router-link></li>
                <li><router-link class="dropdown-item" to="/product">我的商品</router-link></li>
                <li><router-link class="dropdown-item" to="/coupon">我的領率</router-link></li>
                <li><router-link class="dropdown-item" to="/advertise">廣告</router-link></li>
                <li><router-link class="dropdown-item" to="/feedback">意見反饋</router-link></li>
                <li><router-link class="dropdown-item" to="/expert">專家諮詢</router-link></li>
            </ul>
            </div>
            <span class="text-white fw-semibold">
            {{ isLoggedIn ? userName : '使用者，您好！' }}
            </span>
            <!-- 純 Vue 控 dropdown -->
            <div ref="iconDropdownRef" class="position-relative">
            <i
                class="bi bi-person-circle text-white"
                style="font-size: 2rem; cursor:pointer"
                @click="onUserIconClick"
            ></i>
            <ul
                v-if="isLoggedIn && showDropdown"
                class="dropdown-menu dropdown-menu-end show"
                style="position: absolute; right: 0; top: 110%;"
            >
                <li>
                <a class="dropdown-item" href="#" @click.prevent="logout">登出</a>
                </li>
            </ul>
            </div>
        </div>
        <Login v-if="showLogin" @close="showLogin = false" @login-success="onLoginSuccess" />
    </header>
    </template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import Login from '../views/Login.vue'

const isLoggedIn = ref(false)
const showLogin = ref(false)
const userName = ref('')
const showDropdown = ref(false)
const iconDropdownRef = ref(null)

function onLoginSuccess({ name }) {
    isLoggedIn.value = true
    userName.value = name
    showLogin.value = false
}
function logout() {
    isLoggedIn.value = false
    userName.value = ''
    showDropdown.value = false
}
function onUserIconClick() {
    if (!isLoggedIn.value) {
        showLogin.value = true
    } else {
        showDropdown.value = !showDropdown.value
    }
}

// --- 這裡就是放你的外部點擊收起 dropdown ---
function handleOutsideClick(e) {
    if (
        iconDropdownRef.value &&
        !iconDropdownRef.value.contains(e.target)
    ) {
    showDropdown.value = false
    }
}

onMounted(() => {
    document.addEventListener('click', handleOutsideClick)
})
onBeforeUnmount(() => {
    document.removeEventListener('click', handleOutsideClick)
})
// -----------------------------------------
</script>

    <style scoped>
    .brand-title {
    color: #5c3203;
    font-weight: bold;
    font-size: 1.5rem;
    }
    .action-plain-btn {
    background: transparent;
    border: none;
    outline: none;
    box-shadow: none;
    font-size: 1.25rem;
    padding: 4px 8px;
    cursor: pointer;
    }
    .action-plain-btn:active,
    .action-plain-btn:focus {
    background: transparent;
    border: none;
    box-shadow: none;
    }
    .navbar {
    background-color: #ffba20;
    color: white;
    padding: 15px 20px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    position: sticky;
    top: 0;
    z-index: 3000;
}

.navbar .logo {
    font-size: 26px;
    font-weight: bold;
}

.navbar .nav-links {
    display: flex;
    align-items: center;
    gap: 20px;
}

.navbar .nav-links a {
    display: block;
    color: white;
    font-size: 18px;
    margin: 10px 0;
    width: 100%;
    text-align: left;
    text-decoration: none;
    font-size: 16px;
}
</style>
