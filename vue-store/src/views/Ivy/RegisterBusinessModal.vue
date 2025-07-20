<template>
    <div class="modal-bg" v-if="show">
        <div class="modal-dialog modal-dialog-centered">
            <div class="modal-content">
                <div class="modal-header border-0 pb-0 justify-content-between">
                    <button class="btn nav-btn" @click="emit('back')">
                        <svg width="28" height="28" fill="none" viewBox="0 0 24 24">
                            <path d="M15 6l-6 6 6 6" stroke="#222" stroke-width="2" stroke-linecap="round"
                                stroke-linejoin="round" />
                        </svg>
                    </button>
                    <button type="button" class="btn-close custom-close" @click="emit('close')"></button>
                </div>
                
                <div class="modal-body">
                    <h4 class="mb-4 fw-bold text-dark text-center" style="font-size:22px;">
                        準備好拓展您的業務了嗎？
                    </h4>
                    
                    <form @submit.prevent="onSubmit">
                        <!-- 餐廳名稱 -->
                        <div class="mb-3 position-relative">
                            <input type="text" class="form-control form-input" v-model="storeName" placeholder="餐廳或商店名稱 *" />
                        </div>
                        <!-- 負責人姓名 -->
                        <div class="mb-3 position-relative">
                            <input type="text" class="form-control form-input" v-model="ownerFullName" placeholder="負責人姓名 *" />
                        </div>
                        <!-- 電子郵件 -->
                        <div class="mb-3">
                            <input type="email" class="form-control form-input" v-model="ownerEmail" placeholder="電子郵件 *" />
                        </div>
                        <!-- 設定密碼 -->
                        <div class="mb-3 position-relative">
                            <input :type="showPwd1 ? 'text' : 'password'" class="form-control form-input" v-model="password"
                                placeholder="請設定密碼 *" required autocomplete="new-password" />
                            <button type="button" class="eye-btn" @click="showPwd1 = !showPwd1" tabindex="-1">
                                <svg v-if="!showPwd1" width="22" height="22" fill="none" viewBox="0 0 24 24">
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
                        <!-- 再次確認密碼 -->
                        <div class="mb-3 position-relative">
                            <input :type="showPwd2 ? 'text' : 'password'" class="form-control form-input"
                                v-model="passwordConfirm" placeholder="請再次確認密碼 *" required autocomplete="new-password" />
                            <button type="button" class="eye-btn" @click="showPwd2 = !showPwd2" tabindex="-1">
                                <svg v-if="!showPwd2" width="22" height="22" fill="none" viewBox="0 0 24 24">
                                    <path d="M1 12s4-7 11-7 11 7 11 7-4 7-11 7S1 12 1 12Z" stroke="#ffba20" stroke-width="2" />
                                    <circle cx="12" cy="12" r="3" stroke="#ffba20" stroke-width="2" />
                                </svg>
                                <svg v-else width="22" height="22" fill="none" viewBox="0 0 24 24">
                                    <path d="M17.94 17.94C16.14 19.25 14.12 20 12 20c-7 0-11-8-11-8a19.89 19.89 0 0 1-4.13 5.11M10.11 6.11A7.01 7.01 0 0 1 12 6c7 0 11 8 11 8a19.89 19.89 0 0 1-4.13 5.11M1 1l22 22"
                                    stroke="#ffba20" stroke-width="2" stroke-linecap="round" />
                                    <circle cx="12" cy="12" r="3" stroke="#ffba20" stroke-width="2" />
                                </svg>
                            </button>
                            <div v-if="passwordConfirm && !isPwdMatch" class="text-danger mb-2" style="font-size:15px;">
                                兩次輸入的密碼不一致，請再檢查！
                            </div>
                        </div>
                        
                        <!-- 密碼強度條&說明 -->
                        <div class="d-flex align-items-center" style="margin-bottom:6px;">
                            <span style="font-size:16px;color:#888;">密碼強度</span>
                            <span :style="{ color: pwdStrength.color, 'font-weight': 'bold', 'margin-left': '6px', 'font-size': '15px' }">
                                {{ pwdStrength.label }}
                            </span>
                        </div>
                        <div class="progress my-2" style="height: 6px; background: #eee;">
                            <div class="progress-bar" :style="{
                                width: `${pwdStrength.percent * 100}%`,
                                background: pwdStrength.color
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
                        <!-- 電話 -->
                        <div class="mb-4 d-flex align-items-center">
                            <div class="flag-box d-flex align-items-center px-2 me-2">
                                <img src="https://flagcdn.com/h20/tw.png" alt="台灣" style="width:22px; height:16px" />
                                <span class="ms-1">+886</span>
                            </div>
                            <input type="tel" class="form-control form-input" v-model="phone" pattern="[0-9]{9,10}"
                                placeholder="聯絡電話 *" style="flex:1" />
                        </div>
                        
                        <div v-if="error" class="text-danger mb-3">{{ error }}</div>
                        
                        <button type="submit" class="btn btn-main w-100" style="font-size:20px;" :disabled="!canSubmit">
                            開始
                        </button>
                    </form>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({ 
    show: Boolean
})

const emit = defineEmits(['close', 'submit', 'back'])

const storeName = ref('')
const ownerFullName = ref('')
const ownerEmail = ref('')
const password = ref('')
const passwordConfirm = ref('')
const phone = ref('')
const error = ref('')
const showPwd1 = ref(false)
const showPwd2 = ref(false)

// 密碼規則驗證
const hasLen = computed(() => password.value.length >= 8)
const hasLower = computed(() => /[a-z]/.test(password.value))
const hasNumber = computed(() => /\d/.test(password.value))
const isPwdMatch = computed(() => password.value === passwordConfirm.value)
const pwdStrength = computed(() => {
    if (hasLen.value && hasLower.value && hasNumber.value)
        return { label: '高', color: '#fbc02d', percent: 1 }
    if ((hasLen.value && hasLower.value) || (hasLen.value && hasNumber.value) || (hasLower.value && hasNumber.value))
        return { label: '中等', color: '#ffb300', percent: 0.7 }
    if (hasLen.value || hasLower.value || hasNumber.value)
        return { label: '弱', color: '#e57373', percent: 0.4 }
    return { label: '', color: '#eee', percent: 0 }
})

const canSubmit = computed(() =>
    storeName.value &&
    ownerFullName.value &&
    ownerEmail.value &&
    phone.value &&
    hasLen.value &&
    hasLower.value &&
    hasNumber.value &&
    isPwdMatch.value &&
    !error.value
)

function onSubmit() {
    if (!canSubmit.value) return
    
    // 將資料傳給父組件處理
    emit('submit', {
        storeName: storeName.value,
        ownerFullName: ownerFullName.value,
        ownerEmail: ownerEmail.value,
        password: password.value,
        phone: phone.value
    })
}
</script>

<style scoped>
.modal-bg {
    position: fixed;
    inset: 0;
    background: rgba(0, 0, 0, 0.08);
    z-index: 9999;
    display: flex;
    align-items: center;
    justify-content: center;
}

.modal-dialog {
    max-width: 450px;
    width: 95vw;
    margin: 0 auto;
    max-height: 90vh;
    overflow-y: auto;
}

.modal-content {
    background: #fff !important;
    border-radius: 20px;
    box-shadow: 0 2px 24px 4px rgba(0, 0, 0, 0.10);
    border: none;
    padding: 1.5rem;

    width: 100%;
    max-width: 600px;
    max-height: 90vh;
    overflow-y: auto;
}

.nav-btn {
    background: none;
    border: none;
    padding: 0;
    margin-left: -5px;
    margin-top: -5px;
    box-shadow: none;
    outline: none;
}

.form-input {
    border-radius: 10px;
    border: 2px solid #ddd;
    font-size: 16px;
    padding: 13px 13px;
    margin-bottom: 0;
}

.form-input:focus {
    border-color: #ffba20;
    box-shadow: 0 0 0 1px #ffba2021;
}

.flag-box {
    background: #f7f7f7;
    border-radius: 7px;
    border: 1px solid #eee;
    height: 46px;
}

.eye-btn {
    position: absolute;
    right: 10px;
    top: 50%;
    transform: translateY(-50%);
    background: none;
    border: none;
    padding: 0;
    cursor: pointer;
}

.btn-main {
    background: #ffba20;
    color: #fff;
    font-weight: bold;
    font-size: 20px;
    height: 48px;
    border-radius: 10px;
    border: none;
    letter-spacing: 2px;
    transition: filter 0.15s;
    box-shadow: 0 2px 8px 1px #ffba200f;
}

.btn-main:hover {
    filter: brightness(1.08);
    background: #ffba20;
}

.btn-main:disabled {
    opacity: 0.6;
    cursor: not-allowed;
}

.text-danger {
    color: #dc3545;
    font-size: 14px;
}

.require-item {
    display: flex;
    align-items: center;
    margin-bottom: 4px;
    font-size: 14px;
    color: #666;
}

.require-item span {
    margin-right: 8px;
    display: flex;
    align-items: center;
}
</style>