<template>
    <!-- 有改過 -->
    <div class="modal-bg" v-if="show">
        <div class="modal-dialog modal-dialog-centered">
            <div class="modal-content p-4">
                <div class="modal-header border-0 pb-0 justify-content-between">
                    <button class="btn nav-btn" @click="goemit('back')">
                        <svg width="28" height="28" fill="none" viewBox="0 0 24 24">
                            <path d="M15 6l-6 6 6 6" stroke="#222" stroke-width="2" stroke-linecap="round"
                                stroke-linejoin="round" />
                        </svg>
                    </button>
                    <button type="button" class="btn-close custom-close" @click="goemit('close')"></button>
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
                        <button type="submit" class="btn btn-main w-100">繼續</button>
                    </form>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { computed } from 'vue'
import { useUserStore } from '@/stores/user.js'
import axios from '@/plungins/axios.js'
import Swal from 'sweetalert2'

const props = defineProps({ show: Boolean })
const emit = defineEmits(['submit', 'close', 'back'])

const userStore = useUserStore()
// email 欄位直接和 Pinia 同步
const email = computed({
    get: () => userStore.email,
    set: v => userStore.setEmail(v)
})

const goemit = (e) => {
    userStore.setEmail('')
    emit(e)
}

async function onSubmit() {
    if (!email.value) {
        Swal.fire({
            icon: 'warning', // 顯示警告圖示
            title: '請注意！', // 標題
            text: '請輸入 email', // 內容文字
            confirmButtonText: '確定' // 確認按鈕的文字
        });
        return
    }
    try {
        const res = await axios.post('/api/users/check-email-exists', { email: email.value })
        if (res.data.exists) {
            emit('submit', email.value) // 將 email 傳到父元件
        } else {
            Swal.fire({
                icon: 'error', // 因為是「尚未註冊」，帶有錯誤或需要注意的性質，所以用 error 或 warning 都可以
                title: '帳戶不存在', // 或 '註冊提醒', '帳戶不存在'
                text: '此 Email 尚未註冊，請先註冊。',
                confirmButtonText: '前往註冊' // 提供明確的下一步操作
            });
            
            // emit('register') // 如果要跳註冊可以放開
        }
    } catch (e) {
         Swal.fire({
            icon: 'error',
            title: '操作失敗',
            text: '檢查 Email 時發生錯誤，請稍後再試。',
            confirmButtonText: '確定'
        });
        console.error(e)
    }
}
</script>

<style scoped>
.modal-bg {
position: fixed;
inset: 0;
background: rgba(0, 0, 0, 0.08);
z-index: 900;
display: flex;
align-items: center;
justify-content: center;
}

.modal-dialog {
width: 400px;
margin: 0 auto;
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
filter: brightness(1.12);
background: #f1cd78;
}
</style>
