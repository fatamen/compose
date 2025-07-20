<template>
    <div class="container py-5">
        <div class="text-center mb-4">
            <h3 class="d-inline-block">餐廳業主帳戶</h3>
        </div>

        <!-- 🔥 NEW: 載入中狀態 - 最小化加入 -->
        <div v-if="isLoading" class="text-center py-5">
            <div class="spinner-border text-warning" role="status">
                <span class="visually-hidden">載入中...</span>
            </div>
            <p class="mt-3">載入業主資料中...</p>
        </div>

        <div v-else class="mx-auto" style="max-width: 360px;">
            <div class="mb-3 text-center">
                <label class="form-label w-100 text-start">姓名</label>
                <input v-model="storeFullName" type="text" class="form-control rounded-pill" placeholder="請輸入姓名" />
            </div>
            <div class="mb-3 text-center">
                <label class="form-label w-100 text-start">手機號碼</label>
                <input v-model="phone" class="form-control rounded-pill" placeholder="0912345678" />
            </div>
            <div class="mb-2 text-center">
                <label class="form-label w-100 text-start">電子郵件</label>
                <input type="email" v-model="emailLocal" class="form-control rounded-pill" placeholder="請輸入 email" />
            </div>
            <div class="d-flex align-items-center justify-content-center mb-3">
                <i class="bi me-2"
                :class="isEmailVerified ? 'bi-check-circle-fill text-success' : 'bi-exclamation-circle-fill text-warning'"></i>
                <small class="text-secondary">
                    {{ isEmailVerified ? '已驗證' : '未驗證' }}
                </small>
            </div>
            <button type="button" class="btn btn-primary rounded-pill px-4 d-block mx-auto mb-2" :disabled="!isDirty || isSaving"
                @click="handleSave">
                <span v-if="isSaving" class="spinner-border spinner-border-sm me-2" role="status"></span>
                {{ isSaving ? '儲存中...' : '儲存' }}
            </button>
            <button type="button" class="btn btn-primary rounded-pill px-4 d-block mx-auto mt-2 mb-4" @click="goBack">
                返回
            </button>
        </div>
    </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from '@/composables/useStore.js' // 🔥 重新加入：為了清除 store 狀態
import axios from '@/plungins/axios.js'
import { useUserStore } from '@/stores/user.js'

const router = useRouter()
const userStore = useUserStore()

// 🔥 重新加入：為了監聽店家切換，清除舊的 store 狀態
const { selectedStore } = useStore()

// 🔥 NEW: 最小化加入載入和儲存狀態
const isLoading = ref(false)
const isSaving = ref(false)

// 🔥 組件內的業主資料狀態 - 不依賴 Pinia 的 setter 方法
const ownerData = reactive({
    fullName: '',
    phone: '',
    email: ''
})

// 🔥 修正：使用本地 reactive 狀態而不是 Pinia computed
const storeFullName = computed({
    get: () => ownerData.fullName,
    set: val => ownerData.fullName = val
})
const phone = computed({
    get: () => ownerData.phone,
    set: val => ownerData.phone = val
})
const emailLocal = computed({
    get: () => ownerData.email,
    set: val => ownerData.email = val
})

// 🔥 組員原始設計 - 保持不變
const initial = reactive({
    storeFullName: '',
    phone: '',
    email: '',
    isEmailVerified: false // 預設為未驗證
})

// 🔥 組員原始設計 - 完全保持不變
function goBack() {
    router.push('/store')
}

// 🔥 簡化的載入邏輯 - 移除不必要的 Pinia setter 呼叫
const loadOwnerData = async () => {
    const ownerId = localStorage.getItem('ownerId')
    console.log('[loadOwnerData] localStorage ownerId:', ownerId)
    if (!ownerId) {
        alert('未取得業者ID，請重新登入')
        return
    }
    
    try {
        isLoading.value = true
        
        // 1. 先抓 owner - 組員原始邏輯
        const res = await axios.get(`/api/owner/${ownerId}`)
        console.log('[loadOwnerData] axios.get /api/owner result:', res)
        const data = res.data.owner || res.data
        console.log('[loadOwnerData] owner data:', data)
        
        // 🔥 修正：直接設定本地狀態，不呼叫可能不存在的 Pinia setter
        ownerData.fullName = data.name || ''
        ownerData.phone = data.phone || ''
        ownerData.email = data.email || ''
        
        // 🔥 基本的 Pinia 狀態同步（只使用確定存在的方法）
        if (userStore.setOwnerId) {
            userStore.setOwnerId(ownerId + '')
        }
        
        // 🔥 修正：不要呼叫 fetchStoreProfile()！
        // 這會重新載入主要店家，覆蓋用戶當前選中的店家
        // await userStore.fetchStoreProfile?.()
        console.log('⚠️ [EditStoreUser] 跳過 fetchStoreProfile 以避免覆蓋當前選中的店家')
        
        // **這邊同步一次** - 組員原始註解和邏輯
        initial.storeFullName = data.name || ''
        initial.phone = data.phone || ''
        initial.email = data.email || ''
        initial.isEmailVerified = data.isVerify || false
        
        // debug: 初始同步值 - 組員原始 console.log
        console.log('[loadOwnerData] initial:', JSON.stringify(initial, null, 2))
        console.log('[loadOwnerData] ownerData:', JSON.stringify(ownerData, null, 2))
        
    } catch (e) {
        alert('取得帳戶資料失敗')
        console.error('[loadOwnerData] error:', e)
    } finally {
        isLoading.value = false
    }
}

// 🔥 組員原始設計 - 保持不變
onMounted(async () => {
    await loadOwnerData()
})

// 🔥 重要：監聽店家切換，清除舊的 store 狀態，確保 EditStore 能載入正確資料
watch(selectedStore, async (newStoreId, oldStoreId) => {
    if (newStoreId && newStoreId !== oldStoreId) {
        console.log(`🔄 [EditStoreUser] 店家切換: ${oldStoreId} → ${newStoreId}，清除舊的 store 狀態`)
        // 🔥 不重新載入業主資料，但清除 store 相關狀態，讓其他頁面能正確載入
        userStore.setStoreProfile({})
        console.log('✅ [EditStoreUser] 已清除 userStore.storeProfile')
    }
}, { immediate: false })

// 🔥 組員原始設計 - 使用本地狀態比較
const isDirty = computed(() => (
    ownerData.fullName !== initial.storeFullName ||
    ownerData.phone !== initial.phone ||
    ownerData.email !== initial.email
))

// 🔥 組員原始設計 - 使用本地狀態
const isEmailVerified = computed(() =>
    ownerData.email === initial.email && !!initial.isEmailVerified
)

// 🔥 簡化的儲存邏輯 - 移除不必要的 Pinia setter 呼叫
async function handleSave() {
    const ownerId = localStorage.getItem('ownerId')
    console.log('[handleSave] localStorage ownerId:', ownerId)
    if (!ownerId) {
        alert('未取得業者ID，請重新登入')
        return
    }
    
    try {
        isSaving.value = true
        
        // debug: 欲儲存資料 - 組員原始註解和邏輯
        console.log('[handleSave] 欲儲存 owner data:', {
            name: ownerData.fullName,
            phone: ownerData.phone,
            email: ownerData.email
        })
        
        const res = await axios.put(`/api/owner/${ownerId}`, {
            name: ownerData.fullName,
            phone: ownerData.phone,
            email: ownerData.email
        })
        console.log('[handleSave] axios.put response:', res)
        
        if (res.data.success) {
            // 🔥 修正：直接更新 initial 和本地狀態
            initial.storeFullName = ownerData.fullName
            initial.phone = ownerData.phone
            initial.email = ownerData.email
            
            // 🔥 同步到 localStorage（保持與其他組件的一致性）
            localStorage.setItem('storeFullName', ownerData.fullName)
            localStorage.setItem('storeEmail', ownerData.email)
            
            alert('儲存成功')
        } else {
            alert(res.data.message || '儲存失敗')
        }
    } catch (e) {
        alert('儲存失敗，請稍後再試')
        console.error('[handleSave] error:', e)
    } finally {
        isSaving.value = false
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

/* 🔥 NEW: 最小化加入載入狀態樣式 */
.spinner-border {
    border-width: 0.2em;
}

.spinner-border-sm {
    width: 1rem;
    height: 1rem;
    border-width: 0.1em;
}
</style>