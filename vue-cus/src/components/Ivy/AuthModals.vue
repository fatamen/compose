<template>
 <!-- 註冊第一步 -->
    <RegisterModal
        :show="step === 'register'"
        @close="step = ''"
        @register="step = 'email'"
        @login="step = 'loginEmail'"
        @google-login="onGoogleLogin"
    />
    <!-- 第二步，Email 驗證步驟 -->
    <RegisterEmailModal
        :show="step === 'email'"
        @close="step = ''"
        @back="step = 'register'"
        @submit="handleRegisterEmail"
        />
    <!-- 第三步，驗證信 modal -->
    <VerifyEmailModal
        :show="step === 'verifyEmail'"
        :email="userStore.email"
        @close="step = ''"
        @back="step = 'email'"
        @send="handleSendVerification"  
        />
    <!-- 登入-1 Email Modal -->
    <LoginEmailModal
        :show="step === 'loginEmail'"
        @close="step = ''"
        @back="step = 'register'"
        @submit="handleLoginEmail"
        />
    <!-- 登入-2 密碼 -->
    <LoginPasswordModal
        :show="step === 'loginPassword'"
        :email="userStore.email"
        @close="step = ''"
        @back="step = 'loginEmail'"
        @login="handlePasswordLogin"
        @forgot="step = 'forgotPassword'"
    />
    <!-- 忘記密碼 -->
    <ForgotPasswordModal 
        :show="step === 'forgotPassword'" 
        :email="userStore.email" 
        @close="step = ''"
        @back="step = 'loginPassword'" 
        @submit="handleForgotSubmit" />
    <ForgotPasswordSentModal 
        :show="step === 'forgotSent'" 
        @close="step = ''" 
        @back="step = 'loginPassword'"
        @backToLogin="step = 'loginEmail'" />
    <ResetPasswordSentModal 
        :show="step === 'resetPasswordSent'" 
        @close="step = ''" 
        @back="step = 'loginPassword'" />
    <!-- or step = 'loginEmail' 依你的流程-->
    <ResetPasswordDialog 
        v-if="showReset" 
        @close="showReset = false" 
        @submit="onResetPassword" />
    <ResetSuccessModal 
        v-if="showSuccess"
        @close="closeSuccess"/>
</template>
<script setup>
import Swal from 'sweetalert2';
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter ,useRoute } from 'vue-router';
import { useUserStore }  from '@/stores/user.js'
import RegisterModal from '@/components/Ivy/RegisterModal.vue'
import LoginEmailModal from '@/views/Ivy/LoginEmailModal.vue'
import RegisterEmailModal from '@/views/Ivy/RegisterEmailModal.vue'
import VerifyEmailModal from '@/views/Ivy/VerifyEmailModal.vue'
import LoginPasswordModal from '@/views/Ivy/LoginPasswordModal.vue'
import ForgotPasswordModal from '@/views/Ivy/ForgotPasswordModal.vue'
import ForgotPasswordSentModal from '@/views/Ivy/ForgotPasswordSentModal.vue'
import ResetPasswordSentModal from '@/views/Ivy/ResetPasswordSentModal.vue'
import ResetPasswordDialog from '@/views/Ivy/ResetPasswordDialog.vue'
import ResetSuccessModal from '@/components/Ivy/ResetSuccessModal.vue'
//---------------------------------------------------------
const userStore = useUserStore();
const router = useRouter()
const route = useRoute()


const step = ref('')            // 控制哪個modal開
const showReset = ref(false)
const showSuccess = ref(false)

//第一步
function onGoogleLogin() {
    window.location.href = 'http://localhost:8080/oauth2/authorization/google'
}

// 第二步，假設Email已註冊，按繼續
function handleRegisterEmail(email) {
    userStore.setEmail(email)
    step.value = 'verifyEmail'
}
//第三步， 寄送驗證信
function handleSendVerification(email) {
    // 當你需要顯示提示時
    Swal.fire({
        icon: 'success', // 顯示成功圖示
        title: '驗證信已寄出！', // 標題
        // 使用 userStore.email 來獲取用戶的 Email
        text: `我們已將驗證信寄送到 ${userStore.email}，請前往您的信箱查看。`, // 內容文字
        confirmButtonText: '確定' // 確認按鈕文字
    });
    step.value = '';
    router.push({ path: '/register-profile', query: { email } })
}

// 登入-1 流程（Email 輸入完後處理）
function handleLoginEmail(email) {
    userStore.setEmail(email)
    step.value = 'loginPassword'
}
// 登入-2
// 這邊只接收子組件 emit 上來的完整資料（更推薦這種寫法，登入 API 在 LoginPasswordModal.vue 內處理）
function handlePasswordLogin({ userFullName: name, userEmail: email, userPhone: phone ,userId : id }) {
    // 寫入 localStorage
    userStore.loginSuccess({
        fullName: name,
        email: email,
        phone: phone,
        userId: id,
        // token: jwtToken
    })
    step.value = ''
    router.push('/search')
}

function handleForgotSubmit(email) {
    // alert('已寄送重設密碼信到 ' + email)
    // step.value = ''
    // router.push({
    //     path: '/resetPasswordEmail',
    //     query: (email)
    // })
    step.value = ''
    router.push('/') // 這裡**不要帶 query**
}

function onResetPassword() {
    showReset.value = false
    showSuccess.value = true // 顯示「密碼已更新」訊息
    // 清掉 query string，避免重複再次觸發
    router.replace({ path: '/', query: {} })
    userStore.syncFromStorage()
}

function closeSuccess() {
    showSuccess.value = false
    step.value = 'loginEmail' // 或你要打開登入頁
}

defineExpose({
    step
    // 如果你需要從外部直接觸發某些特定的流程，也可以暴露這些函數
    // openRegister: () => { step.value = 'register' },
    // openLogin: () => { step.value = 'loginEmail' }
})


</script>
<style >
    
</style>