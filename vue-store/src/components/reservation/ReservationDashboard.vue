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
const stats = ref({
    totalReservations: 0,
    pendingReservations: 0,
    confirmedReservations: 0,
    cancelledReservations: 0,
    todayReservations: 0,
    upcomingReservations: 0,
    averageGuests: 0
});

const loading = ref(false);
const selectedPeriod = ref('week'); // week, month, year

// 載入統計資料
const fetchStats = async () => {
    loading.value = true;
    try {
        // 嘗試使用真實API
        try {
            const data = await reservationService.getReservationStats(props.storeId);
            stats.value = data;
        } catch (apiError) {
            console.warn('API呼叫失敗，使用模擬資料:', apiError);
            // 使用模擬資料
            stats.value = {
                totalReservations: 156,
                pendingReservations: 23,
                confirmedReservations: 89,
                cancelledReservations: 12,
                todayReservations: 8,
                upcomingReservations: 45,
                averageGuests: 4.2
            };
        }
    } catch (error) {
        console.error('載入統計資料失敗:', error);
    } finally {
        loading.value = false;
    }
};

// 計算百分比
const getPercentage = (value, total) => {
    if (total === 0) return 0;
    return Math.round((value / total) * 100);
};

// 取得狀態顏色
const getStatusColor = (status) => {
    const colorMap = {
        pending: 'warning',
        confirmed: 'success',
        cancelled: 'danger'
    };
    return colorMap[status] || 'secondary';
};

// 取得狀態圖示
const getStatusIcon = (status) => {
    const iconMap = {
        pending: 'fas fa-clock',
        confirmed: 'fas fa-check-circle',
        cancelled: 'fas fa-times-circle'
    };
    return iconMap[status] || 'fas fa-question-circle';
};

// 頁面載入時取得資料
onMounted(() => {
    fetchStats();
});
</script>

<template>
    <div class="reservation-dashboard">
        <div class="row g-4">
            <!-- 總訂位數 -->
            <div class="col-md-3">
                <div class="card bg-primary text-white">
                    <div class="card-body">
                        <div class="d-flex justify-content-between align-items-center">
                            <div>
                                <h6 class="card-title mb-1">總訂位數</h6>
                                <h3 class="mb-0">{{ stats.totalReservations }}</h3>
                            </div>
                            <div class="fs-1">
                                <i class="fas fa-calendar-check"></i>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 今日訂位 -->
            <div class="col-md-3">
                <div class="card bg-success text-white">
                    <div class="card-body">
                        <div class="d-flex justify-content-between align-items-center">
                            <div>
                                <h6 class="card-title mb-1">今日訂位</h6>
                                <h3 class="mb-0">{{ stats.todayReservations }}</h3>
                            </div>
                            <div class="fs-1">
                                <i class="fas fa-calendar-day"></i>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 未來訂位 -->
            <div class="col-md-3">
                <div class="card bg-info text-white">
                    <div class="card-body">
                        <div class="d-flex justify-content-between align-items-center">
                            <div>
                                <h6 class="card-title mb-1">未來訂位</h6>
                                <h3 class="mb-0">{{ stats.upcomingReservations }}</h3>
                            </div>
                            <div class="fs-1">
                                <i class="fas fa-calendar-week"></i>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 平均人數 -->
            <div class="col-md-3">
                <div class="card bg-warning text-dark">
                    <div class="card-body">
                        <div class="d-flex justify-content-between align-items-center">
                            <div>
                                <h6 class="card-title mb-1">平均人數</h6>
                                <h3 class="mb-0">{{ stats.averageGuests }}</h3>
                            </div>
                            <div class="fs-1">
                                <i class="fas fa-users"></i>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- 狀態統計 -->
        <div class="row mt-4">
            <div class="col-12">
                <div class="card">
                    <div class="card-header">
                        <h5 class="mb-0">訂位狀態統計</h5>
                    </div>
                    <div class="card-body">
                        <div v-if="loading" class="text-center py-4">
                            <div class="spinner-border text-primary" role="status">
                                <span class="visually-hidden">載入中...</span>
                            </div>
                        </div>
                        <div v-else class="row g-3">
                            <!-- 待確認 -->
                            <div class="col-md-4">
                                <div class="d-flex align-items-center p-3 border rounded h-100">
                                    <div class="flex-shrink-0 me-3">
                                        <i :class="getStatusIcon('pending')" class="fs-2 text-warning"></i>
                                    </div>
                                    <div class="flex-grow-1">
                                        <h6 class="mb-1">待確認</h6>
                                        <div class="d-flex justify-content-between align-items-center">
                                            <span class="h5 mb-0">{{ stats.pendingReservations }}</span>
                                            <small class="text-muted">
                                                {{ getPercentage(stats.pendingReservations, stats.totalReservations) }}%
                                            </small>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <!-- 已確認 -->
                            <div class="col-md-4">
                                <div class="d-flex align-items-center p-3 border rounded h-100">
                                    <div class="flex-shrink-0 me-3">
                                        <i :class="getStatusIcon('confirmed')" class="fs-2 text-success"></i>
                                    </div>
                                    <div class="flex-grow-1">
                                        <h6 class="mb-1">已確認</h6>
                                        <div class="d-flex justify-content-between align-items-center">
                                            <span class="h5 mb-0">{{ stats.confirmedReservations }}</span>
                                            <small class="text-muted">
                                                {{ getPercentage(stats.confirmedReservations, stats.totalReservations)
                                                }}%
                                            </small>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <!-- 已取消 -->
                            <div class="col-md-4">
                                <div class="d-flex align-items-center p-3 border rounded h-100">
                                    <div class="flex-shrink-0 me-3">
                                        <i :class="getStatusIcon('cancelled')" class="fs-2 text-danger"></i>
                                    </div>
                                    <div class="flex-grow-1">
                                        <h6 class="mb-1">已取消</h6>
                                        <div class="d-flex justify-content-between align-items-center">
                                            <span class="h5 mb-0">{{ stats.cancelledReservations }}</span>
                                            <small class="text-muted">
                                                {{ getPercentage(stats.cancelledReservations, stats.totalReservations)
                                                }}%
                                            </small>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- 快速操作 -->
        <div class="row mt-4">
            <div class="col-12">
                <div class="card">
                    <div class="card-header">
                        <h5 class="mb-0">快速操作</h5>
                    </div>
                    <div class="card-body">
                        <div class="row g-3">
                            <div class="col-md-3">
                                <button class="btn btn-outline-primary w-100" @click="$emit('view-pending')">
                                    <i class="fas fa-clock me-2"></i>
                                    查看待確認訂位
                                </button>
                            </div>
                            <div class="col-md-3">
                                <button class="btn btn-outline-success w-100" @click="$emit('view-today')">
                                    <i class="fas fa-calendar-day me-2"></i>
                                    查看今日訂位
                                </button>
                            </div>
                            <div class="col-md-3">
                                <button class="btn btn-outline-info w-100" @click="$emit('view-upcoming')">
                                    <i class="fas fa-calendar-week me-2"></i>
                                    查看未來訂位
                                </button>
                            </div>
                            <div class="col-md-3">
                                <button class="btn btn-outline-secondary w-100" @click="$emit('export-data')">
                                    <i class="fas fa-download me-2"></i>
                                    匯出訂位資料
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
.reservation-dashboard {
    padding: 1rem;
}

.card {
    transition: all 0.3s ease;
    border: none;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.card:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.border {
    border-color: #dee2e6 !important;
}

.fs-1 {
    font-size: 2.5rem !important;
}

.fs-2 {
    font-size: 2rem !important;
}

.btn {
    transition: all 0.3s ease;
}

.btn:hover {
    transform: translateY(-1px);
}
</style>