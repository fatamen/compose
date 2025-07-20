<template>
    <div class="modal-bg" v-if="show">
        <div class="modal-dialog modal-dialog-centered">
            <div class="modal-content">
                <!-- 🔥 這個頁面不需要返回和關閉按鈕，因為流程已完成 -->
                <div class="modal-body text-center py-5">
                    <!-- 成功圖示 -->
                    <div class="success-icon mb-4">
                        <svg width="80" height="80" fill="none" viewBox="0 0 24 24">
                            <circle cx="12" cy="12" r="10" stroke="#4caf50" stroke-width="2" fill="#f8fff8"/>
                            <path d="8 12l2 2 4-4" stroke="#4caf50" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/>
                        </svg>
                    </div>
                    
                    <h2 class="mb-4 fw-bold text-dark">註冊資料待審核</h2>
                    
                    <div class="mb-4 text-secondary" style="font-size: 16px; line-height: 1.6;">
                        嗨，感謝您的註冊！<br>
                        我們已收到您的商家資料，系統管理員將在 1-3 個工作天內完成審核。<br>
                        審核通過後，我們會透過 Email 通知您。
                    </div>
                    
                    <!-- 註冊資料摘要（可選） -->
                    <div v-if="completeData" class="registration-summary mb-4 p-3 bg-light rounded">
                        <h6 class="fw-bold mb-2">註冊資料摘要</h6>
                        <div class="text-start small">
                            <div><strong>餐廳名稱：</strong>{{ completeData.storeName || '未提供' }}</div>
                            <div><strong>負責人：</strong>{{ completeData.ownerName || '未提供' }}</div>
                            <div><strong>聯絡電話：</strong>{{ completeData.phone || '未提供' }}</div>
                            <div><strong>商家地址：</strong>{{ completeData.address || '未提供' }}</div>
                        </div>
                    </div>
                    
                    <hr class="w-100 my-4" />
                    
                    <button class="btn btn-main px-5 py-3" @click="goHome">
                        返回首頁
                    </button>
                    
                    <div class="mt-3">
                        <small class="text-muted">
                            如有任何問題，請聯繫客服：support@example.com
                        </small>
                    </div>
                    
                    <div v-if="errorMsg" class="text-danger mt-3">{{ errorMsg }}</div>
                    <div v-if="successMsg" class="text-success mt-3">{{ successMsg }}</div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({ 
    show: Boolean,
    completeData: Object  // 包含所有註冊完成的資料摘要
})

const emit = defineEmits(['close', 'submit'])

const errorMsg = ref('')
const successMsg = ref('')

// 整理註冊資料摘要
const registrationSummary = computed(() => {
    if (!props.completeData) return null
    
    return {
        storeName: props.completeData.storeName || props.completeData.name,
        ownerName: props.completeData.ownerFullName || props.completeData.ownerName,
        phone: props.completeData.phone,
        address: props.completeData.mainAddress || props.completeData.address
    }
})

function goHome() {
    // 清除所有註冊相關的暫存資料
    localStorage.removeItem('registerStoreName')
    localStorage.removeItem('registerStoreCategory')
    localStorage.removeItem('registerPhone')
    localStorage.removeItem('registerStoreIntro')
    localStorage.removeItem('registerStoreId')
    
    // 發送完成事件給父組件
    emit('submit', 'completed')
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
    max-width: 500px;
    width: 95vw;
    margin: 0 auto;
}

.modal-content {
    background: #fff !important;
    border-radius: 20px;
    box-shadow: 0 2px 24px 4px rgba(0, 0, 0, 0.10);
    border: none;
    padding: 2rem;
}

.success-icon {
    display: flex;
    justify-content: center;
    align-items: center;
}

.registration-summary {
    border: 1px solid #e9ecef;
    font-size: 14px;
}

.btn-main {
    background: #ffba20;
    color: #fff;
    font-weight: bold;
    font-size: 18px;
    border-radius: 24px;
    border: none;
    letter-spacing: 1px;
    transition: filter 0.15s;
    box-shadow: 0 2px 8px 1px #ffba200f;
}

.btn-main:hover {
    filter: brightness(1.08);
    background: #ffba20;
}

.text-danger {
    color: #dc3545;
    font-size: 14px;
}

.text-success {
    color: #198754;
    font-size: 14px;
}

.bg-light {
    background-color: #f8f9fa !important;
}
</style>