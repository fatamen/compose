<script setup>
import { ref, computed, onMounted } from 'vue';
import reservationService from '../../services/reservationService.js';

// Props
const props = defineProps({
    storeId: {
        type: Number,
        required: true
    }
});

// 響應式資料
const timeSlots = ref([]);
const loading = ref(false);
const selectedDate = ref('');
const showAddModal = ref(false);
const showEditModal = ref(false);
const showCustomGenerateModal = ref(false);
const showTimeSettingModal = ref(false);
const selectedTimeSlot = ref(null);

// 時段設定資料
const timeSetting = ref({
    interval: 30,
    mealTime: 120
});

// 新增時段表單
const newTimeSlot = ref({
    day: '',
    startTime: '',
    endTime: '',
    isActive: true
});

// 編輯時段表單
const editTimeSlot = ref({
    id: null,
    startTime: '',
    endTime: '',
    isActive: true
});

// 載入時段設定
const loadTimeSetting = async () => {
    try {
        const timeSettingService = reservationService.getTimeSettingService();
        const data = await timeSettingService.getTimeSetting(props.storeId);
        timeSetting.value = {
            interval: data.interval || 30,
            mealTime: data.mealTime || 120
        };
    } catch (error) {
        console.error('載入時段設定失敗:', error);
        // 使用預設值
        timeSetting.value = {
            interval: 30,
            mealTime: 120
        };
    }
};

// 載入時段資料
const fetchTimeSlots = async (date = null) => {
    loading.value = true;
    try {
        const timeSlotService = reservationService.getTimeSlotService();
        const data = await timeSlotService.getTimeSlots(props.storeId, date);
        timeSlots.value = data;
    } catch (error) {
        console.error('載入時段資料失敗:', error);
        // 使用模擬資料
        timeSlots.value = [
            {
                id: 1,
                storeId: props.storeId,
                day: '2024-01-15',
                startTime: '11:00',
                endTime: '14:00',
                isActive: true
            },
            {
                id: 2,
                storeId: props.storeId,
                day: '2024-01-15',
                startTime: '17:00',
                endTime: '21:00',
                isActive: true
            },
            {
                id: 3,
                storeId: props.storeId,
                day: '2024-01-16',
                startTime: '11:00',
                endTime: '14:00',
                isActive: false
            }
        ];
    } finally {
        loading.value = false;
    }
};

// 按日期分組的時段
const timeSlotsByDate = computed(() => {
    const grouped = {};
    timeSlots.value.forEach(slot => {
        if (!grouped[slot.day]) {
            grouped[slot.day] = [];
        }
        grouped[slot.day].push(slot);
    });
    return grouped;
});

// 開啟新增時段模態框
const openAddModal = () => {
    newTimeSlot.value = {
        day: selectedDate.value || new Date().toISOString().split('T')[0],
        startTime: '',
        endTime: '',
        isActive: true
    };
    showAddModal.value = true;
};

// 開啟編輯時段模態框
const openEditModal = (timeSlot) => {
    selectedTimeSlot.value = timeSlot;
    editTimeSlot.value = {
        id: timeSlot.id,
        startTime: timeSlot.startTime,
        endTime: timeSlot.endTime,
        isActive: timeSlot.isActive
    };
    showEditModal.value = true;
};

// 新增時段
const addTimeSlot = async () => {
    try {
        const timeSlotService = reservationService.getTimeSlotService();
        await timeSlotService.addTimeSlot(
            props.storeId,
            newTimeSlot.value.day,
            newTimeSlot.value.startTime,
            newTimeSlot.value.endTime,
            newTimeSlot.value.isActive
        );

        showAddModal.value = false;
        await fetchTimeSlots(selectedDate.value);
        alert('時段新增成功！');
    } catch (error) {
        console.error('新增時段失敗:', error);
        alert('新增時段失敗，請稍後再試');
    }
};

// 更新時段
const updateTimeSlot = async () => {
    try {
        const timeSlotService = reservationService.getTimeSlotService();
        await timeSlotService.updateTimeSlot(
            props.storeId,
            editTimeSlot.value.id,
            {
                startTime: editTimeSlot.value.startTime,
                endTime: editTimeSlot.value.endTime,
                isActive: editTimeSlot.value.isActive
            }
        );

        showEditModal.value = false;
        await fetchTimeSlots(selectedDate.value);
        alert('時段更新成功！');
    } catch (error) {
        console.error('更新時段失敗:', error);
        alert('更新時段失敗，請稍後再試');
    }
};

// 刪除時段
const deleteTimeSlot = async (timeSlotId) => {
    if (!confirm('確定要刪除這個時段嗎？')) {
        return;
    }

    try {
        const timeSlotService = reservationService.getTimeSlotService();
        await timeSlotService.deleteTimeSlot(props.storeId, timeSlotId);
        await fetchTimeSlots(selectedDate.value);
        alert('時段刪除成功！');
    } catch (error) {
        console.error('刪除時段失敗:', error);
        alert('刪除時段失敗，請稍後再試');
    }
};

// 生成時段資料
const generateTimeSlots = async () => {
    const days = prompt('請輸入要生成的天數（預設30天）:', '30');
    if (!days || isNaN(days)) return;

    try {
        const timeSlotService = reservationService.getTimeSlotService();
        await timeSlotService.generateTimeSlots(props.storeId, parseInt(days));
        await fetchTimeSlots(selectedDate.value);
        alert(`成功生成${days}天的時段資料！`);
    } catch (error) {
        console.error('生成時段失敗:', error);
        alert('生成時段失敗，請稍後再試');
    }
};

// 切換時段啟用狀態
const toggleTimeSlotStatus = async (timeSlot) => {
    try {
        const timeSlotService = reservationService.getTimeSlotService();
        await timeSlotService.updateTimeSlot(
            props.storeId,
            timeSlot.id,
            { isActive: !timeSlot.isActive }
        );
        await fetchTimeSlots(selectedDate.value);
    } catch (error) {
        console.error('切換時段狀態失敗:', error);
        alert('切換時段狀態失敗，請稍後再試');
    }
};

// 日期變更時重新載入資料
const onDateChange = () => {
    fetchTimeSlots(selectedDate.value);
};

// 格式化完整日期
const formatFullDate = (dateString) => {
    const date = new Date(dateString);
    const year = date.getFullYear();
    const month = date.getMonth() + 1;
    const day = date.getDate();
    const days = ['日', '一', '二', '三', '四', '五', '六'];
    const dayName = days[date.getDay()];
    return `${year}年${month}月${day}日 (週${dayName})`;
};

// 格式化時間，移除秒數
const formatTime = (timeString) => {
    if (!timeString) return '';
    // 如果是 "20:00:00" 格式，只取前5個字符 "20:00"
    if (timeString.length >= 8) {
        return timeString.substring(0, 5);
    }
    return timeString;
};

// 儲存時段設定
const saveTimeSetting = async () => {
    try {
        const timeSettingService = reservationService.getTimeSettingService();
        await timeSettingService.updateTimeSetting(
            props.storeId,
            parseInt(timeSetting.value.interval),
            parseInt(timeSetting.value.mealTime)
        );
        alert('時段設定儲存成功！');
        showTimeSettingModal.value = false;
    } catch (error) {
        console.error('儲存時段設定失敗:', error);
        alert('儲存時段設定失敗，請稍後再試');
    }
};

// 頁面載入時取得資料
onMounted(() => {
    selectedDate.value = new Date().toISOString().split('T')[0];
    fetchTimeSlots();
    loadTimeSetting();
});
</script>

<template>
    <div class="time-slot-manager">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <h5 class="mb-0">時段管理</h5>
            <div class="d-flex gap-2">
                <input type="date" class="form-control" v-model="selectedDate" @change="onDateChange">
                <button class="btn btn-primary" @click="openAddModal">
                    <i class="fas fa-plus me-1"></i> 新增時段
                </button>
                <button class="btn btn-outline-secondary" @click="generateTimeSlots">
                    <i class="fas fa-magic me-1"></i> 生成時段
                </button>
                <button class="btn btn-outline-info" @click="showTimeSettingModal = true">
                    <i class="fas fa-cog me-1"></i> 時段設定
                </button>
            </div>
        </div>

        <div v-if="loading" class="text-center py-4">
            <div class="spinner-border text-primary" role="status">
                <span class="visually-hidden">載入中...</span>
            </div>
            <p class="mt-2">載入時段資料中...</p>
        </div>

        <div v-else-if="Object.keys(timeSlotsByDate).length === 0" class="text-center py-4">
            <i class="fas fa-clock fa-3x text-muted mb-3"></i>
            <h6 class="text-muted">沒有時段資料</h6>
            <p class="text-muted">請新增時段或使用生成功能</p>
        </div>

        <div v-else>
            <!-- 日期選擇器 -->
            <div class="date-selector mb-4">
                <div class="row">
                    <div class="col-md-4">
                        <label class="form-label fw-bold">選擇日期</label>
                        <input type="date" class="form-control" v-model="selectedDate" @change="onDateChange">
                    </div>
                    <div class="col-md-8 d-flex align-items-end">
                        <div class="d-flex gap-2">
                            <button class="btn btn-primary" @click="openAddModal">
                                <i class="fas fa-plus me-1"></i> 新增時段
                            </button>
                            <button class="btn btn-outline-secondary" @click="generateTimeSlots">
                                <i class="fas fa-magic me-1"></i> 生成時段
                            </button>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 時段列表 -->
            <div v-if="selectedDate && timeSlotsByDate[selectedDate]" class="time-slots-container">
                <div class="d-flex justify-content-between align-items-center mb-3">
                    <h6 class="text-primary mb-0">
                        {{ formatFullDate(selectedDate) }} 的時段
                    </h6>
                    <span class="badge bg-primary">{{ timeSlotsByDate[selectedDate].length }} 個時段</span>
                </div>

                <div class="time-slots-vertical-scroll">
                    <div class="time-slots-vertical-wrapper">
                        <div v-for="slot in timeSlotsByDate[selectedDate]" :key="slot.id" class="time-slot-item">
                            <div class="card"
                                :class="{ 'border-success': slot.isActive, 'border-secondary': !slot.isActive }">
                                <div class="card-body">
                                    <div class="d-flex justify-content-between align-items-start mb-2">
                                        <h6 class="card-title mb-0">
                                            {{ formatTime(slot.startTime) }} - {{ formatTime(slot.endTime) }}
                                        </h6>
                                        <div class="form-check form-switch">
                                            <input class="form-check-input" type="checkbox" :checked="slot.isActive"
                                                @change="toggleTimeSlotStatus(slot)">
                                        </div>
                                    </div>
                                    <div class="d-flex gap-2">
                                        <button class="btn btn-sm btn-outline-primary" @click="openEditModal(slot)">
                                            <i class="fas fa-edit"></i>
                                        </button>
                                        <button class="btn btn-sm btn-outline-danger" @click="deleteTimeSlot(slot.id)">
                                            <i class="fas fa-trash"></i>
                                        </button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div v-else-if="selectedDate" class="text-center py-4">
                <i class="fas fa-calendar-times fa-3x text-muted mb-3"></i>
                <h6 class="text-muted">{{ formatFullDate(selectedDate) }} 沒有時段資料</h6>
                <p class="text-muted">請為此日期新增時段</p>
            </div>
        </div>

        <!-- 新增時段模態框 -->
        <div class="modal fade" :class="{ show: showAddModal }" :style="{ display: showAddModal ? 'block' : 'none' }"
            tabindex="-1">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title">新增時段</h5>
                        <button type="button" class="btn-close" @click="showAddModal = false"></button>
                    </div>
                    <div class="modal-body">
                        <div class="mb-3">
                            <label class="form-label">日期</label>
                            <input type="date" class="form-control" v-model="newTimeSlot.day" required>
                        </div>
                        <div class="row">
                            <div class="col-6">
                                <label class="form-label">開始時間</label>
                                <input type="time" class="form-control" v-model="newTimeSlot.startTime" required>
                            </div>
                            <div class="col-6">
                                <label class="form-label">結束時間</label>
                                <input type="time" class="form-control" v-model="newTimeSlot.endTime" required>
                            </div>
                        </div>
                        <div class="form-check mt-3">
                            <input class="form-check-input" type="checkbox" v-model="newTimeSlot.isActive"
                                id="newActive">
                            <label class="form-check-label" for="newActive">
                                啟用此時段
                            </label>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" @click="showAddModal = false">取消</button>
                        <button type="button" class="btn btn-primary" @click="addTimeSlot">新增</button>
                    </div>
                </div>
            </div>
        </div>

        <!-- 編輯時段模態框 -->
        <div class="modal fade" :class="{ show: showEditModal }" :style="{ display: showEditModal ? 'block' : 'none' }"
            tabindex="-1">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title">編輯時段</h5>
                        <button type="button" class="btn-close" @click="showEditModal = false"></button>
                    </div>
                    <div class="modal-body">
                        <div class="row">
                            <div class="col-6">
                                <label class="form-label">開始時間</label>
                                <input type="time" class="form-control" v-model="editTimeSlot.startTime" required>
                            </div>
                            <div class="col-6">
                                <label class="form-label">結束時間</label>
                                <input type="time" class="form-control" v-model="editTimeSlot.endTime" required>
                            </div>
                        </div>
                        <div class="form-check mt-3">
                            <input class="form-check-input" type="checkbox" v-model="editTimeSlot.isActive"
                                id="editActive">
                            <label class="form-check-label" for="editActive">
                                啟用此時段
                            </label>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" @click="showEditModal = false">取消</button>
                        <button type="button" class="btn btn-primary" @click="updateTimeSlot">更新</button>
                    </div>
                </div>
            </div>
        </div>

        <!-- 模態框背景 -->
        <div v-if="showAddModal || showEditModal" class="modal-backdrop fade show"></div>

        <!-- 時段設定模態框 -->
        <div class="modal fade" :class="{ show: showTimeSettingModal }"
            :style="{ display: showTimeSettingModal ? 'block' : 'none' }" tabindex="-1">
            <div class="modal-dialog modal-lg">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title">
                            <i class="fas fa-cog me-2"></i>
                            時段設定管理
                        </h5>
                        <button type="button" class="btn-close" @click="showTimeSettingModal = false"></button>
                    </div>
                    <div class="modal-body">
                        <!-- 時段間隔設定 -->
                        <div class="mb-4">
                            <h6 class="fw-bold mb-3">
                                <i class="fas fa-clock me-2"></i>
                                時段間隔設定
                            </h6>
                            <div class="row">
                                <div class="col-md-6">
                                    <label for="interval" class="form-label">時段間隔（分鐘）</label>
                                    <select id="interval" v-model="timeSetting.interval" class="form-select">
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
                                    <div class="alert alert-info">
                                        <i class="fas fa-info-circle me-2"></i>
                                        <strong>目前設定：</strong> {{ timeSetting.interval }} 分鐘間隔
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- 用餐時間設定 -->
                        <div class="mb-4">
                            <h6 class="fw-bold mb-3">
                                <i class="fas fa-people me-2"></i>
                                用餐時間設定
                            </h6>
                            <div class="row">
                                <div class="col-md-6">
                                    <label for="mealTime" class="form-label">預設用餐時間（分鐘）</label>
                                    <select id="mealTime" v-model="timeSetting.mealTime" class="form-select">
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
                                <div class="col-md-6">
                                    <div class="alert alert-info">
                                        <i class="fas fa-info-circle me-2"></i>
                                        <strong>目前設定：</strong> {{ timeSetting.mealTime }} 分鐘用餐時間
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- 設定說明 -->
                        <div class="alert alert-warning">
                            <i class="fas fa-exclamation-triangle me-2"></i>
                            <strong>注意：</strong> 修改時段設定後，需要重新生成時段才能套用新的設定。
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary"
                            @click="showTimeSettingModal = false">取消</button>
                        <button type="button" class="btn btn-primary" @click="saveTimeSetting">
                            <i class="fas fa-save me-2"></i>
                            儲存設定
                        </button>
                    </div>
                </div>
            </div>
        </div>

        <!-- 模態框背景 -->
        <div v-if="showTimeSettingModal" class="modal-backdrop fade show"></div>
    </div>
</template>

<style scoped>
.time-slot-manager {
    padding: 1rem;
}

.card {
    transition: all 0.3s ease;
}

.card:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.form-check-input:checked {
    background-color: #198754;
    border-color: #198754;
}

.modal {
    background-color: rgba(0, 0, 0, 0.5);
}

.modal.show {
    display: block;
}

/* 日期選擇器樣式 */
.date-selector {
    background: #f8f9fa;
    border-radius: 0.5rem;
    padding: 1.5rem;
    margin-bottom: 1.5rem;
}

/* 時段列表垂直滾輪樣式 */
.time-slots-container {
    background: white;
    border-radius: 0.5rem;
    padding: 1.5rem;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.time-slots-vertical-scroll {
    max-height: 500px;
    overflow-y: auto;
    scrollbar-width: thin;
    scrollbar-color: #c1c1c1 #f1f1f1;
}

.time-slots-vertical-scroll::-webkit-scrollbar {
    width: 8px;
}

.time-slots-vertical-scroll::-webkit-scrollbar-track {
    background: #f1f1f1;
    border-radius: 4px;
}

.time-slots-vertical-scroll::-webkit-scrollbar-thumb {
    background: #c1c1c1;
    border-radius: 4px;
}

.time-slots-vertical-scroll::-webkit-scrollbar-thumb:hover {
    background: #a8a8a8;
}

.time-slots-vertical-wrapper {
    display: flex;
    flex-direction: column;
    gap: 1rem;
    padding: 0.5rem 0;
}

.time-slot-item {
    width: 100%;
}

.time-slot-item .card {
    transition: all 0.3s ease;
    border: 1px solid #dee2e6;
}

.time-slot-item .card:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.time-slot-item .card.border-success {
    border-color: #198754 !important;
    background-color: rgba(25, 135, 84, 0.05);
}

.time-slot-item .card.border-secondary {
    border-color: #6c757d !important;
    background-color: rgba(108, 117, 125, 0.05);
}

/* 響應式設計 */
@media (max-width: 768px) {
    .time-slots-vertical-scroll {
        max-height: 400px;
    }
}

@media (max-width: 576px) {
    .time-slots-vertical-scroll {
        max-height: 350px;
    }

    .date-selector {
        padding: 1rem;
    }
}
</style>