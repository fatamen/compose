<template>
    <header class="navbar">
        <a class="navbar-brand d-flex align-items-center gap-3" style="cursor: pointer" @click="$router.push('/search')">
        <img src="@/assets/logo.png" alt="Logo" height="80" />
        <span class="brand-title">金碗GoldenBowl</span>
        </a>
    </header>

    <div class="container text-center mt-5">
        <h2 class="mb-4">歡迎光臨！</h2>

        <!-- 🔥 主要起始按鈕 -->
        <div class="welcome-actions mb-4">
            <button @click="openLoginModal" class="btn btn-primary me-3">
                開始您的經營之旅
            </button>
        </div>

        <!-- 🔥 主要的選擇登入/註冊的 Modal -->
        <StoreLoginModal
            v-if="showLoginModal"
            :show="showLoginModal"
            @close="closeLoginModal"
            @login="onLogin"
            @register="onRegister"
        />

        <!-- 🔥 登入流程的 Modal 群組 -->
        <LoginEmailModal
            :show="step === 'loginEmail'"
            :error-msg="loginEmailError"
            @close="resetFlow"
            @back="goBackToLoginModal"
            @submit="handleLoginEmail"
        />
        <LoginPasswordModal
            :show="step === 'loginPassword'"
            :email="storeEmail"
            :error-msg="passwordErrorMsg"
            :loading="isLoading"
            @close="resetFlow"
            @forgot="step = 'forgotPassword'"
            @back="step = 'loginEmail'"
            @login="handlePasswordLogin"
        />
        <ForgotPasswordModal
            :show="step === 'forgotPassword'"
            :email="storeEmail"
            @close="resetFlow"
            @back="step = 'loginPassword'"
            @submit="handleForgotSubmit"
        />

        <!-- 🔥 註冊流程的 Modal 群組 -->
        <RegisterBusinessModal
            :show="step === 'registerBusiness'"
            @close="resetFlow"
            @back="goBackToLoginModal"
            @submit="handleRegisterBusiness"
        />
        <RegisterStoreInfoModal
            :show="step === 'registerStoreInfo'"
            :register-data="registerData"
            @close="resetFlow"
            @back="step = 'registerBusiness'"
            @submit="handleStoreInfo"
        />
        <VerifyAddressModal
            :show="step === 'verifyAddress'"
            :store-data="storeData"
            @close="resetFlow"
            @back="step = 'registerStoreInfo'"
            @submit="handleAddressVerification"
        />
        <VerifyPendingModal
            :show="step === 'verifyPending'"
            :complete-data="completeRegistrationData"
            @close="resetFlow"
            @submit="handleFinalSubmit"
        />
        <!-- 可以繼續添加其他註冊步驟的 Modal -->

        <!-- 重設密碼對話框 -->
        <ResetPasswordDialog
            v-if="showReset"
            @close="showReset = false"
            @submit="onResetPassword"
        />
    </div>

</template>

<script setup>
import { ref, onMounted, computed, watch, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import axios from '@/plungins/axios.js'
import { useUserStore } from '@/stores/user.js'
import { clearAllStoreState } from '@/composables/useStore.js' // 🔥 NEW: 清除 store 的資料

import StoreLoginModal from '@/components/Ivy/StoreLoginModal.vue'
import LoginEmailModal from '@/views/Ivy/LoginEmailModal.vue'
import LoginPasswordModal from '@/views/Ivy/LoginPasswordModal.vue'
import ForgotPasswordModal from '@/views/Ivy/ForgotPasswordModal.vue'
import ResetPasswordDialog from '@/views/Ivy/ResetPasswordDialog.vue'

// 🔥 NEW: 導入改造後的註冊 Modal 組件
import RegisterBusinessModal from '@/views/Ivy/RegisterBusinessModal.vue'
import RegisterStoreInfoModal from '@/views/Ivy/RegisterStoreInfoModal.vue'
import VerifyAddressModal from '@/views/Ivy/VerifyAddressModal.vue'
import VerifyPendingModal from '@/views/Ivy/VerifyPendingModal.vue'

const router = useRouter()
const route = useRoute()
const step = ref('')
const storeEmail = ref('')
const storePhone = ref('')
const showReset = ref(false)
const loginEmailError = ref('')
const showLoginModal = ref(false)
const isLoading = ref(false)
const passwordErrorMsg = ref('')

// 🔥 NEW: 註冊流程用的資料儲存
const registerData = ref({
    storeName: '',
    ownerFullName: '',
    ownerEmail: '',
    password: '',
    phone: '',
    ownerId: ''
})

// 🔥 NEW: 店家資料儲存
const storeData = ref({
    storeId: '',
    name: '',
    storeCategory: '',
    storeIntro: '',
    phone: '',
    photo: '',
    files: null
})

// 🔥 NEW: 地址資料儲存
const addressData = ref({
    storeId: '',
    address: '',
    city: '',
    district: '',
    zip: '',
    street: '',
    door: '',
    enAddress: '',
    lat: '',
    lng: '',
    mainAddress: ''
})

// 🔥 NEW: 完整註冊資料（供最後確認頁面使用）
const completeRegistrationData = computed(() => ({
    ...registerData.value,
    ...storeData.value,
    ...addressData.value,
    storeName: storeData.value.name || registerData.value.storeName,
    ownerName: registerData.value.ownerFullName,
    phone: storeData.value.phone || registerData.value.phone,
    address: addressData.value.mainAddress
}))

// Pinia狀態
const userStore = useUserStore()
const storeFullName = computed(() => userStore.ownerFullName)

// 進頁面就依據 query 狀態顯示登入彈窗
onMounted(() => {
    userStore.syncFromStorage()
    storeEmail.value = localStorage.getItem('storeEmail')
    storePhone.value = localStorage.getItem('storePhone') || ''
    if (route.query.login === '1') {
        showLoginModal.value = true
    }
})

// 監聽網址 query 變化決定彈窗狀態
watch(() => route.query.login, (val) => {
    showLoginModal.value = val === '1'
})

// 🔥 開啟主選擇 Modal
function openLoginModal() {
    showLoginModal.value = true
    router.replace({ path: '/home', query: { login: '1' } })
}

// 🔥 關閉並重設所有流程
function resetFlow() {
    showLoginModal.value = false
    step.value = ''
    loginEmailError.value = ''
    passwordErrorMsg.value = ''
    registerData.value = {
        storeName: '',
        ownerFullName: '',
        ownerEmail: '',
        password: '',
        phone: '',
        ownerId: ''
    }
    storeData.value = {
        storeId: '',
        name: '',
        storeCategory: '',
        storeIntro: '',
        phone: '',
        photo: '',
        files: null
    }
    addressData.value = {
        storeId: '',
        address: '',
        city: '',
        district: '',
        zip: '',
        street: '',
        door: '',
        enAddress: '',
        lat: '',
        lng: '',
        mainAddress: ''
    }
    router.replace({ path: '/home', query: {} })
}

function closeLoginModal() {
    resetFlow()
}

// 🔥 在 StoreLoginModal 中點選「登入」按鈕
function onLogin() {
    step.value = 'loginEmail'
    showLoginModal.value = false
}

// 🔥 在 StoreLoginModal 中點選「註冊」按鈕 - 改為顯示註冊 Modal
function onRegister() {
    step.value = 'registerBusiness'
    showLoginModal.value = false
}

// 🔥 處理註冊基本資料提交
async function handleRegisterBusiness(formData) {
    try {
        // 先檢查 email 是否已存在
        const checkRes = await axios.post('/api/owner/check-email', { email: formData.ownerEmail })
        if (checkRes.data.exists) {
            alert('此 Email 已註冊，請登入或用其他 Email')
            return
        }

        // 註冊 owner 帳號
        const regRes = await axios.post('/api/owner/register', {
            email: formData.ownerEmail,
            password: formData.password,
            name: formData.ownerFullName,
            phone: formData.phone
        })

        if (regRes.data.success) {
            // 儲存註冊資料，準備進入下一步
            registerData.value = {
                ...formData,
                ownerId: regRes.data.ownerId
            }
            
            // 進入店家資料設定步驟
            step.value = 'registerStoreInfo'
        } else {
            alert(regRes.data.message || '註冊失敗')
        }
    } catch (e) {
        alert('伺服器錯誤，請稍後再試')
    }
}

// 🔥 處理店家資料提交
function handleStoreInfo(submittedStoreData) {
    // 儲存店家資料
    storeData.value = submittedStoreData
    
    // 進入地址驗證步驟
    step.value = 'verifyAddress'
    
    console.log('進入地址驗證，店家資料：', submittedStoreData)
}

// 🔥 處理地址驗證提交
function handleAddressVerification(submittedAddressData) {
    // 儲存地址資料
    addressData.value = submittedAddressData
    
    // 進入最終等待審核步驟
    step.value = 'verifyPending'
    
    console.log('進入等待審核，完整註冊資料：', completeRegistrationData.value)
}

// 🔥 處理最終提交（從等待審核頁面返回首頁）
function handleFinalSubmit(status) {
    if (status === 'completed') {
        console.log('註冊流程完成，返回首頁')
        resetFlow()
        // 可選：顯示成功訊息
        // alert('註冊流程已完成，感謝您的申請！')
    }
}

// 登入 email 驗證
async function handleLoginEmail(email) {
    storeEmail.value = email
    loginEmailError.value = ''
    try {
        const res = await axios.post('/api/owner/check-email', { email })
        if (res.data.exists) {
            step.value = 'loginPassword'
        } else {
            loginEmailError.value = '查無此帳號，請確認Email或註冊新帳號'
        }
    } catch (e) {
        loginEmailError.value = '伺服器錯誤，請稍後再試'
    }
}

// 登入密碼
async function handlePasswordLogin({ email, password }) {
    isLoading.value = true
    passwordErrorMsg.value = ''

    try {
        const res = await axios.post('/api/owner/login', { email, password })

        if (res.data.success) {
            console.log('🔐 登入成功，開始設定新用戶資料')

            // 🔥 1. 完全清除舊的資料（重要！）
            console.log('🧹 清除舊用戶資料...')
            
            // 清除 useStore 全域狀態
            clearAllStoreState()
            
            // 清除 Pinia 狀態
            userStore.logoutAll()
            
            // 🔥 2. 等待兩個 tick 確保清除完成
            await nextTick()
            await nextTick()
            
            // 🔥 3. 設定新的用戶資料到 localStorage（先設定 localStorage）
            console.log('📝 設定新用戶資料...')
            localStorage.setItem('ownerId', res.data.ownerId)
            localStorage.setItem('storeFullName', res.data.name)
            localStorage.setItem('storeEmail', res.data.email)
            localStorage.setItem('storePhone', res.data.phone)
            
            // 🔥 4. 然後同步到 Pinia
            userStore.setOwnerId(res.data.ownerId)
            userStore.setOwnerFullName(res.data.name)
            userStore.setOwnerEmail(res.data.email)
            
            // 🔥 5. 強制重新同步所有狀態
            userStore.syncFromStorage()

            // 🔥 6. 觸發 useStore 重新載入資料
            setTimeout(() => {
                window.dispatchEvent(new CustomEvent('userLoggedIn', {
                    detail: { ownerId: res.data.ownerId }
                }))
            }, 100)
            
            // 🔥 7. 延遲載入店家資料，確保用戶資料已完全設定
            setTimeout(async () => {
                console.log('🏪 開始載入店家資料...')
                if (userStore.fetchStoreProfile) {
                    await userStore.fetchStoreProfile()
                }
                console.log('✅ 新用戶資料設定完成')
            }, 300)

            resetFlow()
            router.push('/store/menu')
        } else {
            passwordErrorMsg.value = res.data.message || '帳號或密碼錯誤'
        }
    } catch (e) {
        console.error('登入錯誤:', e)
        passwordErrorMsg.value = '伺服器錯誤，請稍後再試'
    } finally {
        isLoading.value = false
    }
}

// 忘記密碼
function handleForgotSubmit(email) {
    alert('已寄送重設密碼信到 ' + email)
    resetFlow()
}

function onResetPassword(newPwd) {
    showReset.value = false
    router.replace({ path: '/store' })
    userStore.syncFromStorage()
}

function goBackToLoginModal() {
    step.value = ''
    openLoginModal()
}
</script>

<style scoped>
.navbar {
    background-color: #ffba20;
    color: white;
    padding: 5px 10px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    position: sticky;
    top: 0;
    z-index: 1000;
}

.welcome-actions {
    margin: 2rem 0;
}

.welcome-actions .btn {
    padding: 0.75rem 2rem;
    font-size: 1.1rem;
    font-weight: 600;
    border-radius: 0.5rem;
}

</style>