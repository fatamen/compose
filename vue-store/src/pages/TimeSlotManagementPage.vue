<template>
    <div class="time-slot-management-page">
        <div class="container-fluid">
            <div class="row">
                <div class="col-12">
                    <div class="d-flex justify-content-between align-items-center mb-4">
                        <h4 class="mb-0">
                            <i class="fas fa-calendar-alt me-2"></i>
                            時段管理
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
                    <div class="time-slot-management">
                        <div class="card">
                            <div class="card-header">
                                <h5 class="card-title mb-0">
                                    <i class="fas fa-calendar-alt me-2"></i>
                                    時段管理
                                </h5>
                            </div>
                            <div class="card-body">
                                <div v-if="loading" class="text-center py-4">
                                    <div class="spinner-border text-primary" role="status">
                                        <span class="visually-hidden">載入中...</span>
                                    </div>
                                    <p class="mt-2 text-muted">載入時段設定中...</p>
                                </div>

                                <div v-else>
                                    <!-- 錯誤訊息 -->
                                    <div v-if="error" class="alert alert-danger">
                                        <i class="fas fa-exclamation-triangle me-2"></i>
                                        {{ error }}
                                    </div>

                                    <!-- 時段設定 -->
                                    <div class="row mb-4">
                                        <div class="col-md-6">
                                            <label for="interval" class="form-label">時段間隔（分鐘）</label>
                                            <select id="interval" v-model="timeSetting.interval" class="form-select"
                                                :disabled="saving">
                                                <option value="15">15 分鐘</option>
                                                <option value="30">30 分鐘</option>
                                                <option value="45">45 分鐘</option>
                                                <option value="60">60 分鐘</option>
                                                <option value="90">90 分鐘</option>
                                                <option value="120">120 分鐘</option>
                                            </select>
                                            <div class="form-text">
                                                設定每個時段的間隔時間，這會影響自動生成時段的密度
                                            </div>
                                        </div>
                                        <div class="col-md-6">
                                            <label for="mealTime" class="form-label">預設用餐時間（分鐘）</label>
                                            <select id="mealTime" v-model="timeSetting.mealTime" class="form-select"
                                                :disabled="saving">
                                                <option value="30">30 分鐘</option>
                                                <option value="60">60 分鐘</option>
                                                <option value="90">90 分鐘</option>
                                                <option value="120">120 分鐘</option>
                                                <option value="150">150 分鐘</option>
                                                <option value="180">180 分鐘</option>
                                            </select>
                                            <div class="form-text">
                                                設定顧客的預設用餐時間，用於計算桌位可用性
                                            </div>
                                        </div>
                                    </div>

                                    <!-- 時段預覽 -->
                                    <div class="mb-4">
                                        <h6 class="fw-bold mb-3">
                                            <i class="fas fa-eye me-2"></i>
                                            時段預覽
                                        </h6>
                                        <div class="row">
                                            <div class="col-md-6">
                                                <label for="previewTime" class="form-label">營業時間範圍</label>
                                                <div class="input-group">
                                                    <input type="time" id="previewStart" v-model="previewStart"
                                                        class="form-control" :disabled="saving">
                                                    <span class="input-group-text">至</span>
                                                    <input type="time" id="previewEnd" v-model="previewEnd"
                                                        class="form-control" :disabled="saving">
                                                </div>
                                            </div>
                                            <div class="col-md-6">
                                                <button @click="generatePreview" class="btn btn-outline-primary"
                                                    :disabled="saving || !previewStart || !previewEnd">
                                                    <i class="fas fa-eye me-2"></i>
                                                    生成預覽
                                                </button>
                                            </div>
                                        </div>
                                    </div>

                                    <!-- 預覽結果 -->
                                    <div v-if="previewSlots.length > 0" class="mb-4">
                                        <h6 class="fw-bold mb-3">預覽結果</h6>
                                        <div class="row">
                                            <div class="col-12">
                                                <div class="alert alert-success">
                                                    <i class="fas fa-check-circle me-2"></i>
                                                    根據目前設定，營業時間 {{ previewStart }} - {{ previewEnd }} 會產生以下時段：
                                                </div>
                                                <div class="time-slots-preview">
                                                    <div v-for="(slot, index) in previewSlots" :key="index"
                                                        class="time-slot-badge">
                                                        {{ slot.startTime }} - {{ slot.endTime }}
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>

                                    <!-- 操作按鈕 -->
                                    <div class="d-flex justify-content-between">
                                        <button @click="resetSettings" class="btn btn-outline-secondary"
                                            :disabled="saving">
                                            <i class="fas fa-arrow-clockwise me-2"></i>
                                            重置設定
                                        </button>
                                        <div>
                                            <button @click="generateTimeSlots" class="btn btn-warning me-2"
                                                :disabled="saving">
                                                <span v-if="saving" class="spinner-border spinner-border-sm me-2"
                                                    role="status"></span>
                                                <i v-else class="fas fa-magic me-2"></i>
                                                {{ saving ? '生成中...' : '重新生成時段' }}
                                            </button>
                                            <button @click="saveSettings" class="btn btn-primary" :disabled="saving">
                                                <span v-if="saving" class="spinner-border spinner-border-sm me-2"
                                                    role="status"></span>
                                                <i v-else class="fas fa-save me-2"></i>
                                                {{ saving ? '儲存中...' : '儲存設定' }}
                                            </button>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { computed } from 'vue';
import { useStore } from '@/composables/useStore.js';
import reservationService from '../services/reservationService.js';

// 使用 store composable
const { selectedStore } = useStore();

// 響應式資料
const loading = ref(false);
const saving = ref(false);
const error = ref(null);
const timeSetting = ref({
    interval: 30,
    mealTime: 90
});
const previewStart = ref('11:00');
const previewEnd = ref('22:00');
const previewSlots = ref([]);

// 驗證表單
const validateForm = () => {
    if (!timeSetting.value.interval || timeSetting.value.interval < 15) {
        error.value = '時間間隔不可少於15分鐘';
        return false;
    }

    if (!timeSetting.value.mealTime || timeSetting.value.mealTime < 30) {
        error.value = '用餐時間不可少於30分鐘';
        return false;
    }

    if (timeSetting.value.mealTime > 180) {
        error.value = '用餐時間不可超過180分鐘';
        return false;
    }

    error.value = null;
    return true;
};

// 生成時段
const generateTimeSlots = async () => {
    if (!validateForm()) {
        return;
    }

    if (!confirm('確定要重新生成時段嗎？這將會覆蓋現有的時段設定。')) {
        return;
    }

    saving.value = true;
    error.value = null;

    try {
        const response = await fetch(`/api/stores/${selectedStore.value}/timeslots/generate?daysAhead=30`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            }
        });

        if (!response.ok) {
            throw new Error('生成時段失敗');
        }

        alert('時段生成成功！');
    } catch (err) {
        console.error('生成時段失敗:', err);
        error.value = '生成時段失敗，請稍後再試';
    } finally {
        saving.value = false;
    }
};

// 載入時段設定
const loadTimeSetting = async () => {
    loading.value = true;
    error.value = null;

    try {
        const timeSettingService = reservationService.getTimeSettingService();
        const data = await timeSettingService.getTimeSetting(selectedStore.value);
        timeSetting.value = {
            interval: data.interval || 30,
            mealTime: data.mealTime || 90
        };
        console.log('成功載入時段設定:', data);
    } catch (err) {
        console.error('載入時段設定失敗:', err);
        error.value = '載入時段設定失敗，使用預設值';
        timeSetting.value = {
            interval: 30,
            mealTime: 90
        };
    } finally {
        loading.value = false;
    }
};

// 儲存時段設定
const saveSettings = async () => {
    if (!validateForm()) {
        return;
    }

    saving.value = true;
    error.value = null;

    try {
        const timeSettingService = reservationService.getTimeSettingService();
        await timeSettingService.updateTimeSetting(
            selectedStore.value,
            parseInt(timeSetting.value.interval),
            parseInt(timeSetting.value.mealTime)
        );

        alert('時段設定儲存成功！');
    } catch (err) {
        console.error('儲存時段設定失敗:', err);
        error.value = '儲存時段設定失敗，請稍後再試';
    } finally {
        saving.value = false;
    }
};

// 重置設定
const resetSettings = () => {
    if (confirm('確定要重置時段設定嗎？')) {
        timeSetting.value = {
            interval: 30,
            mealTime: 90
        };
        previewSlots.value = [];
        error.value = null;
    }
};

// 生成時段預覽
const generatePreview = () => {
    if (!previewStart.value || !previewEnd.value) {
        error.value = '請先設定營業時間範圍';
        return;
    }

    const startTime = new Date(`2000-01-01T${previewStart.value}`);
    const endTime = new Date(`2000-01-01T${previewEnd.value}`);
    const interval = parseInt(timeSetting.value.interval);

    if (startTime >= endTime) {
        error.value = '結束時間必須晚於開始時間';
        return;
    }

    const slots = [];
    let currentTime = new Date(startTime);

    while (currentTime < endTime) {
        const slotEnd = new Date(currentTime.getTime() + interval * 60000);

        if (slotEnd > endTime) break;

        slots.push({
            startTime: currentTime.toTimeString().slice(0, 5),
            endTime: slotEnd.toTimeString().slice(0, 5)
        });

        currentTime = slotEnd;
    }

    previewSlots.value = slots;
    error.value = null;
};

// 組件載入時執行
onMounted(() => {
    loadTimeSetting();
});
</script>

<style scoped>
.time-slot-management-page {
    padding: 20px;
    background-color: #f8f9fa;
    min-height: 100vh;
}

.time-slot-management {
    max-width: 800px;
    margin: 0 auto;
}

.time-slots-preview {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    margin-top: 10px;
}

.time-slot-badge {
    background-color: #e3f2fd;
    color: #1976d2;
    padding: 4px 12px;
    border-radius: 16px;
    font-size: 0.875rem;
    font-weight: 500;
    border: 1px solid #bbdefb;
}

.card-header {
    background-color: #f8f9fa;
    border-bottom: 1px solid #dee2e6;
}

.form-text {
    font-size: 0.875rem;
    color: #6c757d;
}

.alert {
    border-radius: 8px;
}

.btn {
    border-radius: 6px;
}

.form-select,
.form-control {
    border-radius: 6px;
}
</style>