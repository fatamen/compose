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
import { ref } from 'vue';
import TimeSettingManager from '../components/reservation/TimeSettingManager.vue';

// 測試用的餐廳ID
const storeId = ref(1);
</script>

<style scoped>
.time-setting-test {
    padding: 20px;
    background-color: #f8f9fa;
    min-height: 100vh;
}
</style>