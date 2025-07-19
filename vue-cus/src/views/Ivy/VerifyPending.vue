<template>
    <div class="text-center mt-5">
        <h2>驗證中，請稍候...</h2>
    </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from '@/plungins/axios'
import Swal from 'sweetalert2'

const route = useRoute()
const router = useRouter()

    onMounted(async () => {
        console.log('VerifyPending mounted')
    const token = route.query.token
    const email = route.query.email
    console.log('token:', token, 'email:', email)

    if (!token || !email) {
        Swal.fire({
        icon: 'error',
        title: '驗證資訊缺失',
        text: '請重新操作',
    }).then(() => {
        router.push('/')
        return
    })
    }

    try {
        const res = await axios.get('/api/verify-email', {
        params: { token, email }
        })
        await Swal.fire({
        icon: 'success',
        title: '驗證成功！',
        text: res.data || '你的帳號已啟用，歡迎使用！',
        confirmButtonText: '前往首頁'
    })
        router.push('/')             // ← 跳轉首頁
    } catch (err) {
        await Swal.fire({
        icon: 'error',
        title: '驗證失敗',
        text: err.response?.data || '連結錯誤或已失效，請重新發送驗證信。',
        confirmButtonText: '回到驗證頁'
    })
        router.push('/')
    }
})
    </script>

<style scoped>
.btn-main {
    background: #ffba20;
    color: #fff;
    font-weight: bold;
    font-size: 20px;
    height: 48px;
    border-radius: 24px;
    border: none;
    letter-spacing: 2px;
    transition: filter 0.15s;
    box-shadow: 0 2px 8px 1px #ffba200f;
}
.btn-main:hover {
    filter: brightness(1.08);
    background: #f1cd78;
}
</style>
