<template>
    <div class="profile-bg">
        <div class="profile-card">
            <img src="https://cdn-icons-png.flaticon.com/512/2815/2815428.png" alt="icon" class="profile-img mb-2" />
            <h2 class="mb-1 fw-bold">讓我們開始吧！</h2>
            <div class="mb-3 text-secondary" style="font-size:15px;">
                首先，用 <span class="text-dark fw-bold">{{ email }}</span> 建立你的 金碗 帳號。
            </div>
            <form @submit.prevent="onSubmit">
                <div class="mb-3">
                    <input type="text" v-model="fullName" class="form-control custom-input" placeholder="姓名 *" required />
                </div>
                <div class="mb-3 position-relative">
                    <input :type="showPassword ? 'text' : 'password'" v-model="password"
                        class="form-control custom-input" placeholder="請設定密碼 *" required autocomplete="new-password" />
                    <button type="button" class="eye-btn" @click="togglePassword" tabindex="-1">
                        <svg v-if="!showPassword" width="22" height="22" fill="none" viewBox="0 0 24 24">
                            <path d="M1 12s4-7 11-7 11 7 11 7-4 7-11 7S1 12 1 12Z" stroke="#ffba20" stroke-width="2" />
                            <circle cx="12" cy="12" r="3" stroke="#ffba20" stroke-width="2" />
                        </svg>
                        <svg v-else width="22" height="22" fill="none" viewBox="0 0 24 24">
                            <path d="M17.94 17.94C16.14 19.25 14.12 20 12 20c-7 0-11-8-11-8a19.89 19.89 0 0 1 5.06-5.94M10.11 6.11A7.01 7.01 0 0 1 12 6c7 0 11 8 11 8a19.89 19.89 0 0 1-4.13 5.11M1 1l22 22"
                                stroke="#ffba20" stroke-width="2" stroke-linecap="round" />
                            <circle cx="12" cy="12" r="3" stroke="#ffba20" stroke-width="2" />
                        </svg>
                    </button>
                </div>
                <div class="mb-3 position-relative">
                    <input :type="showPassword2 ? 'text' : 'password'" v-model="password2"
                        class="form-control custom-input" placeholder="請再次輸入密碼 *" required autocomplete="new-password" />
                    <button type="button" class="eye-btn" @click="togglePassword2" tabindex="-1">
                        <svg v-if="!showPassword2" width="22" height="22" fill="none" viewBox="0 0 24 24">
                            <path d="M1 12s4-7 11-7 11 7 11 7-4 7-11 7S1 12 1 12Z" stroke="#ffba20" stroke-width="2" />
                            <circle cx="12" cy="12" r="3" stroke="#ffba20" stroke-width="2" />
                        </svg>
                        <svg v-else width="22" height="22" fill="none" viewBox="0 0 24 24">
                            <path d="M17.94 17.94C16.14 19.25 14.12 20 12 20c-7 0-11-8-11-8a19.89 19.89 0 0 1-4.13 5.11M10.11 6.11A7.01 7.01 0 0 1 12 6c7 0 11 8 11 8a19.89 19.89 0 0 1-4.13 5.11M1 1l22 22"
                                stroke="#ffba20" stroke-width="2" stroke-linecap="round" />
                            <circle cx="12" cy="12" r="3" stroke="#ffba20" stroke-width="2" />
                        </svg>
                    </button>
                </div>
                <div v-if="password2 && !isPwdMatch" class="text-danger mb-2" style="font-size:15px;">
                    兩次密碼不一致，請再檢查！
                </div>
                <!-- 密碼強度條&說明 -->
                <div class="d-flex align-items-center" style="margin-bottom:6px;">
                    <span style="font-size:16px;color:#888;">密碼強度</span>
                    <span
                        :style="{ color: strength.color, 'font-weight': 'bold', 'margin-left': '6px', 'font-size': '15px' }">
                        {{ strength.label }}
                    </span>
                </div>
                <div class="progress my-2" style="height: 6px; background: #eee;">
                    <div class="progress-bar" :style="{
                        width: `${strength.percent * 100}%`,
                        background: strength.color
                    }"></div>
                </div>
                <div class="text-start mb-2" style="font-size:15px;">
                    <span>密碼必須包含：</span>
                    <ul class="mb-1" style="list-style: none; padding-left: 0;">
                        <li class="require-item">
                            <span :class="{ 'check': hasLen }">
                                <svg v-if="hasLen" width="18" height="18" viewBox="0 0 24 24" fill="none">
                                    <path d="M6 12l4 4 8-8" stroke="#4caf50" stroke-width="2" stroke-linecap="round" />
                                </svg>
                                <svg v-else width="18" height="18" viewBox="0 0 24 24" fill="none">
                                    <circle cx="9" cy="12" r="1.5" fill="#bbb" />
                                    <circle cx="15" cy="12" r="1.5" fill="#bbb" />
                                </svg>
                            </span>
                            至少需要8個字
                        </li>
                        <li class="require-item">
                            <span :class="{ 'check': hasLower }">
                                <svg v-if="hasLower" width="18" height="18" viewBox="0 0 24 24" fill="none">
                                    <path d="M6 12l4 4 8-8" stroke="#4caf50" stroke-width="2" stroke-linecap="round" />
                                </svg>
                                <svg v-else width="18" height="18" viewBox="0 0 24 24" fill="none">
                                    <circle cx="9" cy="12" r="1.5" fill="#bbb" />
                                    <circle cx="15" cy="12" r="1.5" fill="#bbb" />
                                </svg>
                            </span>
                            至少需要一個小寫字母 (a-z)
                        </li>
                        <li class="require-item">
                            <span :class="{ 'check': hasNumber }">
                                <svg v-if="hasNumber" width="18" height="18" viewBox="0 0 24 24" fill="none">
                                    <path d="M6 12l4 4 8-8" stroke="#4caf50" stroke-width="2" stroke-linecap="round" />
                                </svg>
                                <svg v-else width="18" height="18" viewBox="0 0 24 24" fill="none">
                                    <circle cx="9" cy="12" r="1.5" fill="#bbb" />
                                    <circle cx="15" cy="12" r="1.5" fill="#bbb" />
                                </svg>
                            </span>
                            至少需要一個數字 (0-9)
                        </li>
                    </ul>
                </div>
                <button type="submit" class="btn btn-main w-100" :disabled="!canSubmit">建立帳戶</button>
            </form>
        </div>
    </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user.js'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const email = route.query.email || ''
const fullName = ref('')
const password = ref('')
const password2 = ref('')
const showPassword = ref(false)
const showPassword2 = ref(false)

// 密碼規則
const hasLen = computed(() => password.value.length >= 8)
const hasLower = computed(() => /[a-z]/.test(password.value))
const hasNumber = computed(() => /\d/.test(password.value))
const isPwdMatch = computed(() => password.value === password2.value)

// 密碼強度 & 標籤
const strength = computed(() => {
    if (hasLen.value && hasLower.value && hasNumber.value) return { label: '高', color: '#fbc02d', percent: 1 }
    if ((hasLen.value && hasLower.value) || (hasLen.value && hasNumber.value) || (hasLower.value && hasNumber.value))
        return { label: '中等', color: '#ffb300', percent: 0.7 }
    if (hasLen.value || hasLower.value || hasNumber.value)
        return { label: '弱', color: '#e57373', percent: 0.4 }
    return { label: '', color: '#eee', percent: 0 }
})

// 建立帳戶：條件全過才可送出
const canSubmit = computed(() =>
    fullName.value &&
    hasLen.value &&
    hasLower.value &&
    hasNumber.value &&
    isPwdMatch.value
)

function togglePassword() {
    showPassword.value = !showPassword.value
}
function togglePassword2() {
    showPassword2.value = !showPassword2.value
}

function onSubmit() {
    userStore.setFullName(fullName.value)
    userStore.setEmail(email)
    // 密碼不要存前端
    router.push('/')
}
</script>

<style scoped>
.profile-bg {
    min-height: 80vh;
    background: #fafafb;
    display: flex;
    justify-content: center;
    align-items: center;
}

.profile-card {
    background: #fff;
    border-radius: 18px;
    box-shadow: 0 2px 18px 4px rgba(0, 0, 0, 0.10);
    padding: 38px 32px 28px 32px;
    max-width: 430px;
    width: 100%;
    margin: 32px auto;
    text-align: center;
    position: relative;
}

.profile-img {
    width: 50px;
    height: 50px;
    margin-bottom: 6px;
}

.custom-input {
    border-radius: 10px;
    border: 2px solid #bbb;
    font-size: 17px;
    padding: 12px 13px;
    margin-bottom: 0;
}

.btn-main {
    background: #ffba20;
    color: #fff;
    font-weight: bold;
    font-size: 20px;
    height: 48px;
    border-radius: 12px;
    border: none;
    letter-spacing: 2px;
    transition: filter 0.15s;
    box-shadow: 0 2px 8px 1px #ffba200f;
}

.btn-main:disabled {
    background: #d5d5d5;
    color: #fff;
}

.btn-main:hover:enabled {
    filter: brightness(1.08);
    background: #ffba20;
}

.eye-btn {
    position: absolute;
    top: 12px;
    right: 16px;
    background: transparent;
    border: none;
    padding: 0;
    cursor: pointer;
    z-index: 10;
    outline: none;
    display: flex;
    align-items: center;
}

.require-item {
    display: flex;
    align-items: center;
    gap: 6px;
    margin: 2px 0 2px 0;
    color: #333;
    font-size: 16px;
}

.check {
    margin-right: 2px;
}

.progress {
    background: #eee;
}

.progress-bar {
    transition: width 0.25s;
    height: 100%;
    border-radius: 3px;
}
</style>
