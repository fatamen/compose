<template>
    <div class="container text-center mt-5">
        <h2 class="mb-4">歡迎光臨！</h2>
        <div class="d-grid gap-3 col-6 mx-auto">
            <!-- 沒有 firstName:顯示登入 -->
            <button v-if="!userFirstName" class="btn btn-primary btn-lg" @click="step = 'register'">登入</button>

            <!-- 有 firstName:顯示名字 -->
            <div v-else class="fs-4 dropdown-toggle" @click="toggleMenu">
                <i class="bi bi-person-circle me-1"></i>{{ userFirstName }}
            </div>

            <!-- 自訂下拉選單 -->
            <ul v-if="showMenu" class="custom-dropdown text-start">
                <li @click="goProfile">
                    <i class="bi bi-person me-2"></i>個人檔案
                </li>
                <li @click="logout">
                    <i class="bi bi-box-arrow-right me-2"></i>登出
                </li>
            </ul>
        </div>

        <!-- 註冊第一步 -->
        <RegisterModal :show="step === 'register'" @close="step = ''" @register="step = 'email'"
            @login="step = 'loginEmail'" />
        <!-- 第二步，Email 驗證步驟 -->
        <RegisterEmailModal :show="step === 'email'" @close="step = ''" @back="step = 'register'"
            @submit="handleRegisterEmail" />
        <!-- 第三步，驗證信 modal -->
        <VerifyEmailModal :show="step === 'verifyEmail'" :email="userEmail" @close="step = ''" @back="step = 'email'"
            @send="handleSendVerification" />
        <!-- 登入 Email Modal -->
        <LoginEmailModal :show="step === 'loginEmail'" @close="step = ''" @submit="Email"
            @register="step = 'register'" />
        <!-- 登入密碼 -->
        <LoginPasswordModal :show="step === 'loginPassword'" :email="userEmail" @close="step = ''"
            @forgot="step = 'forgotPassword'" @back="step = 'loginEmail'" @login="handlePasswordLogin" />
        <!-- 忘記密碼 -->
        <ForgotPasswordModal :show="step === 'forgotPassword'" :email="userEmail" @close="step = ''"
            @back="step = 'loginPassword'" @submit="handleForgotSubmit" />
        <ForgotPasswordSentModal :show="step === 'forgotSent'" @close="step = ''" @back="step = 'loginPassword'"
            @backToLogin="step = 'loginEmail'" />
        <ResetPasswordSentModal :show="step === 'resetPasswordSent'" @close="step = ''"
            @back="step = 'loginPassword'" />
        <!-- or step = 'loginEmail' 依你的流程-->
        <ResetPasswordDialog v-if="showReset" @close="showReset = false" @submit="onResetPassword" />
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import RegisterModal from '@/components/Ivy/RegisterModal.vue'
import LoginEmailModal from '@/views/Ivy/user/LoginEmailModal.vue'
import RegisterEmailModal from '@/views/Ivy/user/RegisterEmailModal.vue'
import VerifyEmailModal from '@/views/Ivy/user/VerifyEmailModal.vue'
import LoginPasswordModal from '@/views/Ivy/user/LoginPasswordModal.vue'
import ForgotPasswordModal from '@/views/Ivy/user/ForgotPasswordModal.vue'
import ForgotPasswordSentModal from '@/views/Ivy/user/ForgotPasswordSentModal.vue'
import ResetPasswordSentModal from '@/views/Ivy/user/ResetPasswordSentModal.vue'
import ResetPasswordDialog from '@/views/Ivy/user/ResetPasswordDialog.vue'

const router = useRouter()

const step = ref('')            // 控制哪個modal開
const userEmail = ref('')       // 存email
const userFirstName = ref('')   // 登入後要顯示的名字
const showMenu = ref(false)     // 顯示下拉選單
const showReset = ref(false)    // 顯示重設密碼 dialog

onMounted(() => {
    userFirstName.value = localStorage.getItem('userFirstName') || ''
})

function toggleMenu() {
    showMenu.value = !showMenu.value
}

function goProfile() {
    showMenu.value = false
    router.push('/profile')
}

function logout() {
    localStorage.removeItem('userFirstName')
    userFirstName.value = ''
    showMenu.value = false
}

// 假設Email已註冊，按繼續
function handleRegisterEmail(email) {
    userEmail.value = email
    step.value = 'verifyEmail'
}

// email 檢查流程（後端查詢）
// async function handleRegisterEmail(email) {
//     userEmail.value = email
//     // 這裡可改成你自己的 API 路徑
//     const res = await fetch('/api/check-email', {
//     method: 'POST',
//     headers: { 'Content-Type': 'application/json' },
//     body: JSON.stringify({ email })
//     }).then(r => r.json())

// 登入流程（Email 輸入完後處理）
function Email(email) {
    userEmail.value = email;
    // 這裡你未來會用 fetch 查詢後端
    // 這裡直接假設有註冊
    const exists = true; // 你之後用API結果

    if (exists) {
        step.value = 'loginPassword'
    } else {
        alert('此 email 尚未註冊，請先註冊');
        step.value = 'register'
    }
}

// 寄送驗證信
function handleSendVerification() {
    // 可加發信 API
    alert(`已寄出驗證信到 ${userEmail.value}`)
    step.value = ''
    router.push('/verify-pending')
}

function handlePasswordLogin({ email, password }) {
    alert(`登入成功：${email} / ${password}`)
    step.value = ''
}

function handleForgotSubmit(email) {
    alert('已寄送重設密碼信到 ' + email)
    step.value = ''
    router.push({
        path: '/resetPasswordEmail',
        query: (email)
    })
}

function handleCompleteRegistration(firstName) {
    localStorage.setItem('userFirstName', firstName)
    router.push('/')
    userFirstName.value = firstName
}

function onResetPassword(newPwd) {
    showReset.value = false
    // 假設 API call 完成後
    router.replace({ path: '/', })
    // 再重新讀一次名字（通常你會從後端拿回名字再存）
    userFirstName.value = localStorage.getItem('userFirstName') || ''
}
</script>

<style scoped>
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
</style>