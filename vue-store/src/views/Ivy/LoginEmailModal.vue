<template>
    <div class="modal-bg" v-if="show">
        <div class="modal-dialog modal-dialog-centered">
            <div class="modal-content p-4">
                <div class="modal-header border-0 pb-0 justify-content-between">
                    <button class="btn nav-btn" @click="emit('back')">
                        <svg width="28" height="28" fill="none" viewBox="0 0 24 24">
                            <path d="M15 6l-6 6 6 6" stroke="#222" stroke-width="2" stroke-linecap="round"
                                stroke-linejoin="round" />
                        </svg>
                    </button>
                    <button type="button" class="btn-close custom-close" @click="emit('close')"></button>
                </div>
                <div class="modal-body d-flex flex-column align-items-center pt-0">
                    <img src="https://cdn-icons-png.flaticon.com/512/561/561127.png" alt="mail"
                        class="email-img mb-3">
                    <div class="fw-bold title mb-1">你的<span class="highlight">email</span>是？</div>
                    <div class="desc mb-4">請輸入登入 Email</div>
                    <form class="w-100" @submit.prevent="onSubmit">
                        <div class="mb-3 text-start w-100">
                            <label class="form-label label-strong">電子郵件</label>
                            <input type="email" v-model="email" class="form-control custom-input" required
                                placeholder="請輸入 email">
                        </div>
                        <div v-if="errorMsg" class="text-danger mb-2">{{ errorMsg }}</div>
                        <button type="submit" class="btn btn-main w-100" :disabled="loading">
                            <span v-if="loading">檢查中...</span>
                            <span v-else>繼續</span>
                        </button>
                    </form>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref } from 'vue'
import axios from '@/plungins/axios.js'
import { useUserStore } from '@/stores/user.js'

const props = defineProps({ 
    show: Boolean,
    errorMsg: String
})

const emit = defineEmits(['close', 'submit', 'back'])

const userStore = useUserStore()
const email = ref(userStore.ownerEmail || '')
const errorMsg = ref('')
const loading = ref(false)

async function onSubmit() {
    errorMsg.value = ''
    if (!email.value) {
        errorMsg.value = '請輸入 Email'
        return
    }
    loading.value = true
    try {
        // 用你專案 axios 路徑
        const res = await axios.post('/api/owner/check-email', { email: email.value })
        if (res.data.exists) {
            userStore.setOwnerEmail(email.value) // 記下這次 email，方便後續步驟
            emit('submit', email.value) // 通知父元件可以進入密碼步驟
        } else {
            errorMsg.value = '查無此帳號，請確認Email或註冊新帳號'
        }
    } catch (e) {
        errorMsg.value = '伺服器錯誤，請稍後再試'
    } finally {
        loading.value = false
    }
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
    width: 400px;
    /* margin: 0 200px; */
}

.modal-content {
    background: #fff !important;
    border-radius: 20px;
    box-shadow: 0 2px 24px 4px rgba(0, 0, 0, 0.10);
    border: none;
    padding: 2.2rem 2rem 2rem 2rem;
    position: relative;
}

.close-btn {
    position: absolute;
    top: 14px;
    right: 14px;
    background: #fff;
    border: none;
    border-radius: 50%;
    box-shadow: 0 2px 8px 1px rgba(0, 0, 0, 0.10);
    width: 40px;
    height: 40px;
    padding: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    z-index: 10;
    transition: background 0.15s;
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

.email-img {
    width: 62px;
    height: 62px;
    object-fit: contain;
    margin-bottom: 8px;
}

.title {
    font-size: 2rem;
    font-weight: bold;
    color: #222;
    margin-bottom: 4px;
}

.highlight {
    color: #222;
    font-weight: 900;
    font-size: 2rem;
    font-family: inherit;
    letter-spacing: 0.5px;
}

.desc {
    color: #999;
    font-size: 1.1rem;
    font-weight: 400;
    margin-bottom: 1.2rem;
}

.label-strong {
    font-weight: bold;
    color: #222;
    font-size: 1.08rem;
    margin-bottom: 2px;
}

.custom-input {
    font-size: 1.1rem;
    border: 2px solid #222;
    border-radius: 8px;
    height: 46px;
    padding: 7px 12px;
    margin-top: 5px;
    background: #fff;
    box-shadow: none;
}

.custom-input:focus {
    border-color: #ffba20;
    box-shadow: 0 0 0 1px #ffba2021;
}

.btn-main {
    background: #ffba20;
    color: #fff;
    font-weight: bold;
    font-size: 20px;
    height: 48px;
    border-radius: 10px;
    border: none;
    margin-top: 18px;
    letter-spacing: 2px;
    transition: filter 0.15s;
    box-shadow: 0 2px 8px 1px #ffba200f;
}

.btn-main:hover {
    filter: brightness(1.08);
    background: #ffba20;
}
</style>
