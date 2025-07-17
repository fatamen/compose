<template>
    <!-- 登入步驟 -->
    <div class="user-dropdown-container">
        <a href="#" class="user-link" @click.prevent="toggleDropdown">
            {{ cUser }}
        </a>
        <div class="dropdown-menu" v-if="showDropdown">
            <ul>
                <li @click="openRegisterModal">會員*</li>
                <!-- <li @click="toStore">餐廳方*</li>
                <li @click="navigateTo('admin')">管理者*</li>
                <li @click="navigateTo('Search')">Search*</li> -->
                <li v-if="userStore.isLogin" class="login-item" @click="logout">登出</li>
            </ul>
        </div>
    </div>

    <AuthModals ref="authModalsRef" />
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter ,useRoute } from 'vue-router';
import AuthModals from '@/components/Ivy/AuthModals.vue' // 導入新的 AuthModals 組件
//---------------------------------------------------------
import { useUserStore }  from '@/stores/user.js'
const userStore = useUserStore();
//-----Ivy----------------------

const step = ref('')            // 控制哪個modal開
const authModalsRef = ref(null) // AuthModals 的 ref
const showDropdown = ref(false) //選單的出現
const router = useRouter()
const route = useRoute()
//會員登入
const openRegisterModal = () => {
  showDropdown.value = false // 關閉下拉選單
  if (authModalsRef.value) {
    authModalsRef.value.step = 'register' // 直接修改 AuthModals 內部的 step
  }
}
//到店家頁面
function toStore() { 
    router.push('/store') }

// 導航到對應頁面
const navigateTo = (path) => {
    router.push(`/${path}`);
    showDropdown.value = false;
    
};

function logout() {
    userStore.logout()
    showDropdown.value = false  // 只留這行就好
}


const toggleDropdown = () => {
    showDropdown.value = !showDropdown.value;
};


// cUser 直接用 computed，跟著 userFullName 和 isLoggedIn 動態變動
const cUser = computed(() =>
    // isLoggedIn.value
    // ? (userFullName.value ? userFullName.value : "目前使用者*")
    // : "請選擇登入身分"
    userStore.isLogin?(userStore.fullName? userStore.fullName : "目前使用者*") 
    :"請登入"
)

// // 監聽 isLoggedIn 變化，動態更新 cUser
// watch(userStore.isLogin, (newValue) => {
//     cUser.value = newValue ? "目前使用者*" : "請選擇登入身分";
// });

// 點擊外部關閉下拉選單
const handleClickOutside = (event) => {
    if (!event.target.closest('.user-dropdown-container')) {
        showDropdown.value = false;
    }
};

onMounted(() => {
    // 1. 點擊外部關閉下拉選單
    document.addEventListener('click', handleClickOutside)

    // 2. 偵測 query string 觸發重設密碼 dialog
    if (route.query.reset === '1' && route.query.email && route.query.token) {
        
        userStore.setEmail(route.query.email)
        // 可選：清除 query 以免重複
        // router.replace({ path: '/', query: {} })
    }
})

onUnmounted(() => {
    document.removeEventListener('click', handleClickOutside);
});
</script>

<style scoped>
/*--------- Ivy -------------*/
.user-dropdown {
    cursor: pointer;
    user-select: none;
    position: relative;
    user-select: none;
}

.custom-dropdown {
    display: block;
    position: absolute;
    top: 22%;
    left: 50%;
    transform: translateX(-50%);
    background: #fff;
    border: 1px solid #ddd;
    border-radius: 8px;
    padding: 4px 0;
    min-width: 160px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    z-index: 1000;
}

.custom-dropdown li {
    list-style: none;
    padding: 8px 16px;
    cursor: pointer;
    display: flex;
    align-items: center;
    font-size: 0.95rem;
    color: #333;
}

.custom-dropdown li:hover {
    background: #f5f5f5;
}

.custom-dropdown li i {
    font-size: 1.1rem;
    color: #666;
}

/*--------------------------*/

.user-dropdown-container {
    position: relative;
    display: inline-block;
}

.user-link {
    color: white;
    text-decoration: none;
    font-size: 16px;
    padding: 8px 12px;
    cursor: pointer;
}

.user-link:hover {
    background-color: rgba(255, 255, 255, 0.1);
    border-radius: 4px;
}

.dropdown-menu {
    position: absolute;
    top: 100%;
    right: 0;
    background-color: white;
    border: 1px solid #ddd;
    border-radius: 4px;
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
    z-index: 1000;
    /* 確保 z-index 足夠高 */
    min-width: 160px;
    margin-top: 5px;
    display: block;
    /* 添加此行，確保顯示 */
}

.dropdown-menu ul {
    list-style: none;
    padding: 0;
    margin: 0;
}



.dropdown-menu li {
    padding: 12px 16px;
    font-size: 14px;
    color: #333;
    cursor: pointer;
    transition: background-color 0.2s;
}

.dropdown-menu li:hover {
    background-color: #f5f5f5;
}
.login-item {
    border-top: 1px solid #ddd;
    color: #ffba20 !important;
}

</style>
