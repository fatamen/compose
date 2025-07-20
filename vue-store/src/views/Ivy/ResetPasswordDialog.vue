<template>
    <div class="modal-backdrop">
        <div class="modal-content">
            <button class="close-btn" @click="emitClose">×</button>
            <h3 class="modal-title">請重新設定密碼</h3>
            <div class="mb-2">
                <label class="label">請輸入新密碼</label>
                <div class="input-group">
                    <input :type="showPwd1 ? 'text' : 'password'" v-model="password" class="input" placeholder="新密碼" />
                    <button class="eye-btn" @click="showPwd1 = !showPwd1" type="button" tabindex="-1">
                        <!-- 眼睛 icon 開關 -->
                        <svg v-if="!showPwd1" width="22" height="22" fill="none" viewBox="0 0 24 24">
                            <path d="M1 12s4-7 11-7 11 7 11 7-4 7-11 7S1 12 1 12Z" stroke="#ffba20" stroke-width="2" />
                            <circle cx="12" cy="12" r="3" stroke="#ffba20" stroke-width="2" />
                        </svg>
                        <svg v-else width="22" height="22" fill="none" viewBox="0 0 24 24">
                            <path
                                d="M17.94 17.94C16.14 19.25 14.12 20 12 20c-7 0-11-8-11-8a19.89 19.89 0 0 1 5.06-5.94M10.11 6.11A7.01 7.01 0 0 1 12 6c7 0 11 8 11 8a19.89 19.89 0 0 1-4.13 5.11M1 1l22 22"
                                stroke="#ffba20" stroke-width="2" stroke-linecap="round" />
                            <circle cx="12" cy="12" r="3" stroke="#ffba20" stroke-width="2" />
                        </svg>
                    </button>
                </div>
            </div>
            <div class="mb-2">
                <label class="label">請再次重新輸入新密碼</label>
                <div class="input-group">
                    <input :type="showPwd2 ? 'text' : 'password'" v-model="password2" class="input"
                        placeholder="再次輸入新密碼" />
                    <button class="eye-btn" @click="showPwd2 = !showPwd2" type="button" tabindex="-1">
                        <svg v-if="!showPwd2" width="22" height="22" fill="none" viewBox="0 0 24 24">
                            <path d="M1 12s4-7 11-7 11 7 11 7-4 7-11 7S1 12 1 12Z" stroke="#ffba20" stroke-width="2" />
                            <circle cx="12" cy="12" r="3" stroke="#ffba20" stroke-width="2" />
                        </svg>
                        <svg v-else width="22" height="22" fill="none" viewBox="0 0 24 24">
                            <path
                                d="M17.94 17.94C16.14 19.25 14.12 20 12 20c-7 0-11-8-11-8a19.89 19.89 0 0 1 5.06-5.94M10.11 6.11A7.01 7.01 0 0 1 12 6c7 0 11 8 11 8a19.89 19.89 0 0 1-4.13 5.11M1 1l22 22"
                                stroke="#ffba20" stroke-width="2" stroke-linecap="round" />
                            <circle cx="12" cy="12" r="3" stroke="#ffba20" stroke-width="2" />
                        </svg>
                    </button>
                </div>
            </div>
            <div v-if="error" class="error mb-2" style="color:#ffba20">{{ error }}</div>
            <button class="btn-main w-100 mt-2" @click="submit">送出</button>
        </div>
    </div>
</template>

<script setup>
import { ref } from 'vue'

const emit = defineEmits(['close', 'submit'])

const password = ref('')
const password2 = ref('')
const showPwd1 = ref(false)
const showPwd2 = ref(false)
const error = ref('')

function emitClose() {
    emit('close')
}

function submit() {
    error.value = ''
    if (!password.value || !password2.value) {
        error.value = '請填寫所有欄位'
        return
    }
    if (password.value.length < 6) {
        error.value = '密碼至少需 6 字'
        return
    }
    if (password.value !== password2.value) {
        error.value = '兩次輸入的密碼不一致'
        return
    }
    // 這裡應呼叫後端 API
    emit('submit', password.value)
}
</script>

<style scoped>
.modal-backdrop {
    position: fixed;
    inset: 0;
    background: rgba(0, 0, 0, 0.13);
    z-index: 9999;
    display: flex;
    align-items: center;
    justify-content: center;
}

.modal-content {
    background: #fff;
    border-radius: 20px;
    box-shadow: 0 2px 24px 4px rgba(0, 0, 0, 0.10);
    padding: 2.2rem 2rem 2rem 2rem;
    max-width: 400px;
    width: 100%;
    position: relative;
    text-align: left;
}

.close-btn {
    position: absolute;
    top: 14px;
    right: 14px;
    background: #fff;
    border: none;
    border-radius: 50%;
    box-shadow: 0 2px 8px 1px rgba(0, 0, 0, 0.10);
    width: 34px;
    height: 34px;
    font-size: 1.5rem;
    color: #aaa;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
}

.input-group {
    display: flex;
    align-items: center;
    border-radius: 8px;
    background: #f3f3f5;
    margin-bottom: 0;
}

.input {
    flex: 1;
    border: none;
    background: transparent;
    font-size: 1.1rem;
    padding: 0.6rem 0.7rem;
    outline: none;
}

.eye-btn {
    background: none;
    border: none;
    margin-right: 5px;
    cursor: pointer;
    padding: 0 6px;
}

.btn-main {
    background: #ffba20;
    color: #fff;
    font-weight: bold;
    font-size: 18px;
    border-radius: 10px;
    border: none;
    height: 46px;
    transition: filter 0.15s;
    box-shadow: 0 2px 8px 1px #ffba200f;
    margin-top: 10px;
}

.btn-main:hover {
    filter: brightness(1.08);
    background: #ffba20;
}

.label {
    font-size: 15px;
    margin-bottom: 2px;
    color: #222;
}

.error {
    color: #ffba20;
    font-size: 15px;
    margin-bottom: 5px;
}
</style>
