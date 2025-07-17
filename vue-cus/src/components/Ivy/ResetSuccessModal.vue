<template>
    <div class="modal-bg">
        <div class="modal-dialog modal-dialog-centered">
            <div class="modal-content p-4 text-center">
                <div class="modal-header border-0 pb-0 justify-content-end">
                    <button type="button" class="btn-close custom-close" @click="$emit('close')"></button>
                </div>
                <div class="modal-body pt-0 d-flex flex-column align-items-center">
                    <!-- 綠色勾勾 SVG -->
                    <svg viewBox="0 0 64 64" width="58" height="58" fill="none" class="mb-3">
                    <circle cx="32" cy="32" r="32" fill="#48C774"/>
                    <path d="M18 33L28.5 44L46 23" stroke="#fff" stroke-width="6" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    <div class="fw-bold mb-2 main-title">密碼已更新成功！</div>
                    <div class="sub-title mb-3">請使用新密碼重新登入</div>
                    <button class="btn btn-main w-100" @click="$emit('close')">確定</button>
                </div>
            </div>
        </div>
    </div>
</template>
    
    <script setup>
    defineProps(['show']);
    defineEmits(['close']);
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
        margin: 0 auto;
    }
    .modal-content {
        background: #fff !important;
        border-radius: 22px;
        box-shadow: 0 2px 24px 4px rgba(0, 0, 0, 0.11);
        border: none;
        padding: 2.2rem 2rem 2rem 2rem;
        position: relative;
    }
    .btn-close, .custom-close {
        position: absolute;
        top: 14px;
        right: 14px;
        background: #fff;
        border: none;
        border-radius: 50%;
        box-shadow: 0 2px 8px 1px rgba(0,0,0,0.10);
        width: 36px;
        height: 36px;
        padding: 0;
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        z-index: 10;
        transition: background 0.15s;
        font-size: 1.2rem;
    }
    .main-title {
        font-size: 1.7rem;
        font-weight: bold;
        color: #222;
    }
    .sub-title {
        color: #888;
        font-size: 1.1rem;
        font-weight: 400;
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
        filter: brightness(1.12);
        background: #f1cd78;
    }
    </style>