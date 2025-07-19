<template>
    <div class="modal-bg" v-if="show">
        <div class="modal-dialog modal-dialog-centered">
            <div class="modal-content p-4">
                <div class="modal-header border-0 pb-0 justify-content-between">
                    <button class="btn nav-btn" @click="emit('back') ">
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
                    <div class="fw-bold verify-title mb-1">驗證你的<span class="highlight">email</span>以開始使用</div>
                    <div class="verify-desc mb-4">這有助我們預防詐騙，並保護你的個人資料安全</div>
                    <button
                        class="btn btn-main w-100"
                        :disabled="loading"
                        @click="sendVerifyEmail"
                    >
                        {{ loading ? '寄送中...' : '發送驗證信' }}
                    </button>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref } from 'vue'
import axios from '@/plungins/axios.js'
import Swal from 'sweetalert2'

const props = defineProps({ show: Boolean, email: String })
const emit = defineEmits(['close', 'send', 'back'])

const loading = ref(false)

async function sendVerifyEmail() {
    if (!props.email) {
        Swal.fire({
            icon: 'warning', // 顯示警告圖示 (黃色三角形)
            title: '請注意', // 標題
            text: '請輸入您的 Email 地址。', // 內容文字
            confirmButtonText: '確定' // 確認按鈕文字
        });
        return
    }
    loading.value = true
    try {
    // 建議用 URLSearchParams，因為後端是 @RequestParam String email
        const params = new URLSearchParams();
        params.append('email', props.email.trim());
        await axios.post('/api/send-verify-email', params);
        Swal.fire({
            icon: 'success', // 顯示成功圖示 (綠色勾勾)
            title: '驗證信已寄出！', // 標題
            text: '請前往您的信箱查收驗證信，以完成後續步驟。', // 內容文字
            confirmButtonText: '確定' // 確認按鈕文字
        });
        emit('send')
    } catch (e) {
        Swal.fire({
            icon: 'error', // 顯示錯誤圖示
            title: '寄送失敗', // 標題
            text: '郵件寄送時發生問題，請稍後再試一次。', // 內容文字
            confirmButtonText: '確定' // 確認按鈕文字
        });
    } finally {
        loading.value = false
    }
}
</script>


<style scoped>
/* 參照前面 modal 樣式 */
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
width: auto;
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

.nav-btn {
background: none;
border: none;
padding: 0;
margin-left: -5px;
margin-top: -5px;
box-shadow: none;
outline: none;
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

.email-img {
width: 62px;
height: 62px;
object-fit: contain;
margin-bottom: 8px;
}

.verify-title {
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

.verify-desc {
color: #999;
font-size: 1.1rem;
font-weight: 400;
margin-bottom: 1.2rem;
}

.btn-main {
background: #ffba20;
color: #fff;
font-weight: bold;
font-size: 20px;
height: 48px;
border-radius: 10px;
border: none;
margin-top: 8px;
letter-spacing: 2px;
transition: filter 0.15s;
box-shadow: 0 2px 8px 1px #ffba200f;
}

.btn-main:hover {
filter: brightness(1.12);
background: #f1cd78;
}
</style>
