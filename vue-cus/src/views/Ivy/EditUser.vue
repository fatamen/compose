<template>
    <!-- 有改過 -->
    <div class="container py-5">
        <div class="text-center mb-4">
            <h3 class="d-inline-block">我的帳戶</h3>
            <i class="bi bi-info-circle text-secondary ms-2"></i>
        </div>
        <div class="mx-auto" style="max-width: 360px;">
            <!-- 姓名 -->
            <div class="mb-3 text-center">
                <label class="form-label w-100 text-start">姓名</label>
                <input v-model="fullName" type="text" class="form-control rounded-pill" placeholder="請輸入姓名" />
            </div>
            <!-- 手機號碼 -->
            <div class="mb-3 text-center">
                <label class="form-label w-100 text-start">手機號碼</label>
                <input v-model="phone" class="form-control rounded-pill" placeholder="0912345678" />
            </div>
            <!-- 電子郵件 + 驗證 -->
            <div class="mb-2 text-center">
                <label class="form-label w-100 text-start">電子郵件</label>
                <div class="input-group align-items-center">
                    <input
                        type="email"
                        v-model="emailLocal"
                        @change="onEmailChange"
                        :disabled="emailChecking"
                        class="form-control rounded-pill"
                        placeholder="請輸入 email"
                    />
                    <button
                        v-if="!isVerified && emailLocal"
                        class="btn btn-outline-warning ms-2"
                        type="button"
                        @click="handleResend"
                        :disabled="emailChecking || resendLoading"
                    >
                        {{ resendLoading ? "發送中..." : "重新驗證" }}
                    </button>
                </div>
            </div>
            <div class="d-flex align-items-center justify-content-center mb-3">
                <i class="bi me-2"
                    :class="isVerified ? 'bi-check-circle-fill text-success' : 'bi-exclamation-circle-fill text-warning'"></i>
                <small class="text-secondary">
                    {{ isVerified ? '已驗證' : '未驗證' }}
                    <span v-if="emailChecking" class="ms-2 text-primary">檢查中...</span>
                </small>
            </div>
            <button
                type="button"
                class="btn btn-primary rounded-pill px-4 d-block mx-auto mb-2"
                :disabled="!isDirty || saveLoading"
                @click="handleSave"
            >
                {{ saveLoading ? "儲存中..." : "儲存" }}
            </button>
            <button
                type="button"
                class="btn btn-primary rounded-pill px-4 d-block mx-auto mt-2 mb-4"
                @click="goBack"
            >
                返回
            </button>
        </div>
    </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from '@/plungins/axios.js'
import { useUserStore } from '@/stores/user.js'
import Swal from 'sweetalert2'
    const router = useRouter()
    function goBack() {
        router.back()
    }

    const userStore = useUserStore()

    // loading 狀態
    const emailChecking = ref(false)
    const saveLoading = ref(false)
    const resendLoading = ref(false)

    // 直接用 Pinia 狀態
    const fullName = computed({
    get: () => userStore.fullName,
    set: v => userStore.setFullName(v)
    })
    const phone = computed({
    get: () => userStore.phone,
    set: v => userStore.setPhone(v)
    })
    const emailLocal = computed({
    get: () => userStore.email,
    set: v => userStore.setEmail(v)
    })
    const isVerified = computed(() => userStore.verified)
    const userId = computed(() => userStore.userId)

    // 初始值記錄（比較用）
    const initial = ref({})

    // 初始載入
    onMounted(async () => {
    
    // 拉一次後端最新資料
    if (userStore.email) {
        try {
        const res = await axios.get('/api/users/profile', { params: { email: userStore.email } })
        const user = res.data
        console.log(user)
        if (user) {
            userStore.setUserId(user.id)
            userStore.setFullName(user.name || '')
            userStore.setPhone(user.phone || '')
            userStore.setEmail(user.email || '')
            userStore.setVerified(!!user.isVerify)
            // 紀錄初始值（for isDirty）
            initial.value = {
            fullName: user.name || '',
            phone: user.phone || '',
            email: user.email || ''
            }
        }
        } catch {
        // 清空
        userStore.setUserId('')
        userStore.setFullName('')
        userStore.setPhone('')
        userStore.setVerified(false)
        userStore.setEmail('')
        initial.value = { fullName: '', phone: '', email: userStore.email }
        }
    }
    })

    // email 異動即查驗
    async function onEmailChange() {
    const email = emailLocal.value?.trim()
    if (!email) {
        userStore.setVerified(false)
        return
    }
    emailChecking.value = true
    try {
        const res = await axios.get('/api/users/profile', { params: { email } })
        const user = res.data
        userStore.setVerified(!!user?.isVerify)
    } catch {
        userStore.setVerified(false)
    } finally {
        emailChecking.value = false
    }
    }

    // 檢查是否有異動
    const isDirty = computed(() => {
    return (
        fullName.value !== (initial.value.fullName || '') ||
        phone.value !== (initial.value.phone || '') ||
        emailLocal.value !== (initial.value.email || '')
    )
    })

    // 儲存
    async function handleSave() {
    if (!userId.value) {
        Swal.fire({
            icon: 'error', // 顯示錯誤圖示
            title: '更新失敗', // 標題
            text: '找不到對應的會員 ID，因此無法更新資料。', // 內容文字
            confirmButtonText: '確定' // 確認按鈕文字
        });
        return
    }
    saveLoading.value = true
    try {
        const updateUser = {
        id: userId.value,
        name: fullName.value.trim(),
        phone: phone.value,
        email: emailLocal.value
        }
        const res = await axios.put(`/api/users/${userId.value}`, updateUser)
        const user = res.data
        userStore.setFullName(user.name || '')
        userStore.setPhone(user.phone || '')
        userStore.setEmail(user.email || '')
        // 不同步驗證狀態（保持 onEmailChange 統一來源）
        // 更新初始值
        initial.value = {
        fullName: user.name || '',
        phone: user.phone || '',
        email: user.email || ''
        }
        Swal.fire({
            icon: 'success', // 顯示成功圖示 (綠色勾勾)
            title: '更新成功！', // 標題
            text: '您的資料已成功更新。', // 內容文字
            showConfirmButton: false, // 不顯示確認按鈕
            timer: 1500, // 1.5 秒後自動關閉
            timerProgressBar: true // 顯示進度條
        });
    } catch {
        Swal.fire({
            icon: 'error', // 顯示錯誤圖示
            title: '更新失敗', // 標題
            text: '資料更新時發生錯誤，請稍後再試。', // 內容文字
            confirmButtonText: '確定' // 確認按鈕文字
        });
    } finally {
        saveLoading.value = false
    }
    }
</script>

<style scoped>
.form-control.rounded-pill {
    border-radius: 50px;
}

.btn.rounded-pill {
    border-radius: 50px;
}

.btn.btn-primary {
    background-color: #ffba20;
    border-color: #ffba20;
    color: #fff;
}

.btn.btn-primary:disabled {
    background-color: #d5d5d5;
    border-color: #d5d5d5;
    color: #fff;
}

.btn.btn-primary:hover:not(:disabled) {
    background-color: #f1cd78;
    border-color: #f1cd78;
}
</style>