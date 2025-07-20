<template>
    <div class="time-setting-test">
        <div class="container-fluid">
            <div class="row">
                <div class="col-12">
                    <div class="d-flex justify-content-between align-items-center mb-4">
                        <h4 class="mb-0">
                            <i class="fas fa-cog me-2"></i>
                            時段設定測試頁面
                        </h4>
                        <div class="d-flex gap-2">
                            <button class="btn btn-outline-secondary" @click="$router.go(-1)">
                                <i class="fas fa-arrow-left me-2"></i>
                                返回
                            </button>
                        </div>
                    </div>
                </div>
            </div>

            <div class="row">
                <div class="col-12">
                    <TimeSettingManager :storeId="storeId" />
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import TimeSettingManager from '../components/reservation/TimeSettingManager.vue';

// 從 localStorage 讀取 storeId
const storeId = ref(null);

onMounted(() => {
    const savedStoreId = localStorage.getItem('storeId');
    if (savedStoreId) {
        storeId.value = parseInt(savedStoreId);
        console.log('從 localStorage 讀取到 storeId:', storeId.value);
    } else {
        console.warn('localStorage 中沒有 storeId，使用預設值 1');
        storeId.value = 1;
    }
});
</script>

<style scoped>
.time-setting-test {
    padding: 20px;
    background-color: #f8f9fa;
    min-height: 100vh;
}
</style>