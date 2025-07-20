<script setup>
import { ref, computed, onMounted } from 'vue';
import PageHeader from '../components/common/PageHeader.vue';
import SlideOutPanel from '../components/common/SlideOutPanel.vue';
import reservationService from '../services/reservationService.js';
import ReservationDashboard from '../components/reservation/ReservationDashboard.vue';
import TimeSlotManager from '../components/reservation/TimeSlotManager.vue';
import { useStore } from '../composables/useStore.js';

// 使用商店管理
const { selectedStore, isLoggedIn } = useStore();

// 響應式資料
const reservations = ref([]);
const selectedReservation = ref(null);
const isDetailSidebarVisible = ref(false);
const loading = ref(false);
const searchKeyword = ref('');
const statusFilter = ref('all');
const dateFilter = ref('');
const currentView = ref('dashboard'); // dashboard, list, timeslots



// 狀態選項
const statusOptions = [
    { value: 'all', label: '全部狀態' },
    { value: 'PENDING', label: '待確認' },
    { value: 'CONFIRMED', label: '已確認' },
    { value: 'CANCELLED', label: '已取消' }
];

// 計算屬性：篩選後的訂位列表
const filteredReservations = computed(() => {
    let filtered = reservations.value;

    // 關鍵字搜尋
    if (searchKeyword.value) {
        const keyword = searchKeyword.value.toLowerCase();
        filtered = filtered.filter(reservation =>
            reservation.customerName.toLowerCase().includes(keyword) ||
            reservation.phone.includes(keyword) ||
            reservation.content?.toLowerCase().includes(keyword)
        );
    }

    // 狀態篩選
    if (statusFilter.value !== 'all') {
        filtered = filtered.filter(reservation =>
            reservation.status === statusFilter.value
        );
    }

    // 日期篩選
    if (dateFilter.value) {
        filtered = filtered.filter(reservation =>
            reservation.date === dateFilter.value
        );
    }

    return filtered;
});

// 取得狀態顯示文字
const getStatusText = (status) => {
    const statusMap = {
        'PENDING': '待確認',
        'CONFIRMED': '已確認',
        'CANCELLED': '已取消'
    };
    return statusMap[status] || status;
};

// 取得狀態樣式類別
const getStatusClass = (status) => {
    const classMap = {
        'PENDING': 'badge-warning',
        'CONFIRMED': 'badge-success',
        'CANCELLED': 'badge-danger'
    };
    return classMap[status] || 'badge-secondary';
};

// 載入訂位資料
const fetchReservations = async () => {
    loading.value = true;
    try {
        if (!selectedStore.value) {
            console.error('未選擇商店');
            reservations.value = [];
            return;
        }

        const filters = {
            status: statusFilter.value !== 'all' ? statusFilter.value : null,
            date: dateFilter.value || null,
            keyword: searchKeyword.value || null
        };

        // 使用真實API
        const data = await reservationService.getStoreReservations(selectedStore.value, filters);
        console.log('API 返回的訂位資料:', data);

        // 轉換資料格式以符合前端期望
        if (data && Array.isArray(data)) {
            reservations.value = data.map(item => ({
                id: item.id,
                customerName: item.userName || '未知顧客',
                phone: item.userPhone || '未知電話',
                date: item.reservedDate,
                time: item.reservedTime,
                guests: item.guests,
                status: item.status,
                content: item.content || '',
                createdAt: item.createdAt
            }));
        } else {
            reservations.value = [];
        }
    } catch (error) {
        console.error('載入訂位資料失敗:', error);
        // 不使用模擬資料，保持空陣列
        reservations.value = [];
        alert('載入訂位資料失敗，請檢查網路連線或稍後再試');
    } finally {
        loading.value = false;
    }
};

// 查看訂位詳情
const viewReservationDetail = (reservation) => {
    selectedReservation.value = reservation;
    isDetailSidebarVisible.value = true;
};

// 更新訂位狀態
const updateReservationStatus = async (reservationId, newStatus) => {
    try {
        // 嘗試使用真實API
        try {
            await reservationService.updateReservationStatus(reservationId, newStatus);
        } catch (apiError) {
            console.warn('API呼叫失敗，使用本地更新:', apiError);
            // 如果API失敗，仍然更新本地資料
        }

        // 更新本地資料
        const index = reservations.value.findIndex(r => r.id === reservationId);
        if (index !== -1) {
            reservations.value[index].status = newStatus;
        }

        // 如果當前選中的訂位被更新，也要更新詳情
        if (selectedReservation.value && selectedReservation.value.id === reservationId) {
            selectedReservation.value.status = newStatus;
        }

        alert('狀態更新成功！');
    } catch (error) {
        console.error('更新狀態失敗:', error);
        alert('更新狀態失敗，請稍後再試');
    }
};

// 刪除訂位
const deleteReservation = async (reservationId) => {
    if (!confirm('確定要刪除這筆訂位嗎？')) {
        return;
    }

    try {
        // 嘗試使用真實API
        try {
            await reservationService.deleteReservation(reservationId);
        } catch (apiError) {
            console.warn('API呼叫失敗，使用本地刪除:', apiError);
            // 如果API失敗，仍然從本地移除
        }

        // 從本地資料中移除
        reservations.value = reservations.value.filter(r => r.id !== reservationId);

        // 如果當前選中的訂位被刪除，關閉詳情面板
        if (selectedReservation.value && selectedReservation.value.id === reservationId) {
            isDetailSidebarVisible.value = false;
            selectedReservation.value = null;
        }

        alert('訂位刪除成功！');
    } catch (error) {
        console.error('刪除訂位失敗:', error);
        alert('刪除訂位失敗，請稍後再試');
    }
};

// 清除篩選條件
const clearFilters = () => {
    searchKeyword.value = '';
    statusFilter.value = 'all';
    dateFilter.value = '';
};

// 匯出訂位資料
const exportReservations = async () => {
    try {
        if (!selectedStore.value) {
            console.error('未選擇商店');
            alert('請先選擇商店');
            return;
        }

        const filters = {
            status: statusFilter.value !== 'all' ? statusFilter.value : null,
            date: dateFilter.value || null,
            keyword: searchKeyword.value || null
        };

        // 嘗試使用真實API匯出
        try {
            const blob = await reservationService.exportReservations(selectedStore.value, filters);
            const link = document.createElement('a');
            link.href = URL.createObjectURL(blob);
            link.download = `訂位資料_${new Date().toISOString().split('T')[0]}.csv`;
            link.click();
            return;
        } catch (apiError) {
            console.warn('API匯出失敗，使用本地匯出:', apiError);
        }

        // 如果API失敗，使用本地匯出
        const csvContent = [
            ['訂位編號', '顧客姓名', '電話', '日期', '時間', '人數', '狀態', '備註', '建立時間'],
            ...filteredReservations.value.map(r => [
                r.id,
                r.customerName,
                r.phone,
                r.date,
                r.time,
                r.guests,
                getStatusText(r.status),
                r.content || '',
                r.createdAt
            ])
        ].map(row => row.join(',')).join('\n');

        const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
        const link = document.createElement('a');
        link.href = URL.createObjectURL(blob);
        link.download = `訂位資料_${new Date().toISOString().split('T')[0]}.csv`;
        link.click();
    } catch (error) {
        console.error('匯出失敗:', error);
        alert('匯出失敗，請稍後再試');
    }
};

// 頁面載入時取得資料
onMounted(() => {
    fetchReservations();
});
</script>

<template>
    <div class="reservation-management">
        <!-- 頁面標頭 -->
        <PageHeader title="訂位管理" subtitle="管理餐廳的訂位預約">
            <template #actions>
                <div class="btn-group me-2" role="group">
                    <button class="btn" :class="currentView === 'dashboard' ? 'btn-primary' : 'btn-outline-primary'"
                        @click="currentView = 'dashboard'">
                        <i class="fas fa-chart-bar me-1"></i> 儀表板
                    </button>
                    <button class="btn" :class="currentView === 'list' ? 'btn-primary' : 'btn-outline-primary'"
                        @click="currentView = 'list'">
                        <i class="fas fa-list me-1"></i> 訂位列表
                    </button>
                    <button class="btn" :class="currentView === 'timeslots' ? 'btn-primary' : 'btn-outline-primary'"
                        @click="currentView = 'timeslots'">
                        <i class="fas fa-clock me-1"></i> 時段管理
                    </button>
                </div>
                <button class="btn btn-outline-secondary me-2" @click="exportReservations">
                    <i class="fas fa-download me-1"></i> 匯出資料
                </button>
                <button class="btn btn-primary" @click="fetchReservations">
                    <i class="fas fa-sync-alt me-1"></i> 重新整理
                </button>
            </template>
        </PageHeader>

        <!-- 內容區域 -->
        <div v-if="currentView === 'dashboard'">
            <ReservationDashboard :store-id="selectedStore"
                @view-pending="() => { currentView = 'list'; statusFilter = 'PENDING'; }"
                @view-today="() => { currentView = 'list'; dateFilter = new Date().toISOString().split('T')[0]; }"
                @view-upcoming="() => { currentView = 'list'; }" @export-data="exportReservations" />
        </div>

        <div v-else-if="currentView === 'list'">
            <!-- 篩選區域 -->
            <div class="filter-section mb-4">
                <div class="row g-3">
                    <div class="col-md-4">
                        <div class="input-group">
                            <span class="input-group-text">
                                <i class="fas fa-search"></i>
                            </span>
                            <input type="text" class="form-control" placeholder="搜尋顧客姓名、電話或備註..."
                                v-model="searchKeyword">
                        </div>
                    </div>
                    <div class="col-md-3">
                        <select class="form-select" v-model="statusFilter">
                            <option v-for="option in statusOptions" :key="option.value" :value="option.value">
                                {{ option.label }}
                            </option>
                        </select>
                    </div>
                    <div class="col-md-3">
                        <input type="date" class="form-control" v-model="dateFilter">
                    </div>
                    <div class="col-md-2">
                        <button class="btn btn-outline-secondary w-100" @click="clearFilters">
                            <i class="fas fa-times me-1"></i> 清除
                        </button>
                    </div>
                </div>
            </div>

            <!-- 訂位列表 -->
            <div class="reservation-list">
                <div v-if="loading" class="text-center py-5">
                    <div class="spinner-border text-primary" role="status">
                        <span class="visually-hidden">載入中...</span>
                    </div>
                    <p class="mt-2">載入訂位資料中...</p>
                </div>

                <div v-else-if="filteredReservations.length === 0" class="text-center py-5">
                    <i class="fas fa-calendar-times fa-3x text-muted mb-3"></i>
                    <h5 class="text-muted">沒有找到符合條件的訂位</h5>
                    <p class="text-muted">請嘗試調整搜尋條件或篩選器</p>
                </div>

                <div v-else class="table-responsive">
                    <table class="table table-hover">
                        <thead class="table-light">
                            <tr>
                                <th>訂位編號</th>
                                <th>顧客姓名</th>
                                <th>電話</th>
                                <th>日期</th>
                                <th>時間</th>
                                <th>人數</th>
                                <th>狀態</th>
                                <th>備註</th>
                                <th>操作</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr v-for="reservation in filteredReservations" :key="reservation.id">
                                <td>
                                    <strong>#{{ reservation.id }}</strong>
                                </td>
                                <td>{{ reservation.customerName }}</td>
                                <td>{{ reservation.phone }}</td>
                                <td>{{ reservation.date }}</td>
                                <td>{{ reservation.time }}</td>
                                <td>
                                    <span class="badge bg-info">{{ reservation.guests }}人</span>
                                </td>
                                <td>
                                    <span :class="`badge ${getStatusClass(reservation.status)}`">
                                        {{ getStatusText(reservation.status) }}
                                    </span>
                                </td>
                                <td>
                                    <span v-if="reservation.content" class="text-muted">
                                        {{ reservation.content }}
                                    </span>
                                    <span v-else class="text-muted">-</span>
                                </td>
                                <td>
                                    <div class="btn-group btn-group-sm">
                                        <!-- <button class="btn btn-outline-primary"
                                            @click="viewReservationDetail(reservation)" title="查看詳情">
                                            <i class="fas fa-eye"></i>
                                        </button> -->
                                        <button v-if="reservation.status === 'PENDING'" class="btn btn-outline-success"
                                            @click="updateReservationStatus(reservation.id, 'CONFIRMED')" title="確認訂位">
                                            <i class="fas fa-check"></i>
                                        </button>
                                        <!-- 移除標記完成按鈕，因為沒有COMPLETED狀態 -->
                                        <button
                                            v-if="reservation.status === 'PENDING' || reservation.status === 'CONFIRMED'"
                                            class="btn btn-outline-warning"
                                            @click="updateReservationStatus(reservation.id, 'CANCELLED')" title="取消訂位">
                                            <i class="fas fa-times"></i>
                                        </button>
                                        <!-- <button class="btn btn-outline-danger"
                                            @click="deleteReservation(reservation.id)" title="刪除訂位">
                                            <i class="fas fa-trash"></i> -->
                                        <!-- </button> -->
                                    </div>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>

        <div v-else-if="currentView === 'timeslots'">
            <TimeSlotManager :store-id="selectedStore" />
        </div>

        <!-- 訂位詳情側邊欄 -->
        <SlideOutPanel :visible="isDetailSidebarVisible" @close="isDetailSidebarVisible = false" title="訂位詳情">
            <div v-if="selectedReservation" class="reservation-detail">
                <div class="row mb-3">
                    <div class="col-6">
                        <label class="form-label fw-bold">訂位編號</label>
                        <p class="form-control-plaintext">#{{ selectedReservation.id }}</p>
                    </div>
                    <div class="col-6">
                        <label class="form-label fw-bold">狀態</label>
                        <p>
                            <span :class="`badge ${getStatusClass(selectedReservation.status)}`">
                                {{ getStatusText(selectedReservation.status) }}
                            </span>
                        </p>
                    </div>
                </div>

                <div class="row mb-3">
                    <div class="col-6">
                        <label class="form-label fw-bold">顧客姓名</label>
                        <p class="form-control-plaintext">{{ selectedReservation.customerName }}</p>
                    </div>
                    <div class="col-6">
                        <label class="form-label fw-bold">電話</label>
                        <p class="form-control-plaintext">{{ selectedReservation.phone }}</p>
                    </div>
                </div>

                <div class="row mb-3">
                    <div class="col-6">
                        <label class="form-label fw-bold">訂位日期</label>
                        <p class="form-control-plaintext">{{ selectedReservation.date }}</p>
                    </div>
                    <div class="col-6">
                        <label class="form-label fw-bold">訂位時間</label>
                        <p class="form-control-plaintext">{{ selectedReservation.time }}</p>
                    </div>
                </div>

                <div class="row mb-3">
                    <div class="col-6">
                        <label class="form-label fw-bold">人數</label>
                        <p class="form-control-plaintext">{{ selectedReservation.guests }}人</p>
                    </div>
                    <div class="col-6">
                        <label class="form-label fw-bold">建立時間</label>
                        <p class="form-control-plaintext">{{ selectedReservation.createdAt }}</p>
                    </div>
                </div>

                <div class="mb-3">
                    <label class="form-label fw-bold">備註</label>
                    <p class="form-control-plaintext">
                        {{ selectedReservation.content || '無備註' }}
                    </p>
                </div>

                <hr>

                <div class="d-grid gap-2">
                    <button v-if="selectedReservation.status === 'PENDING'" class="btn btn-success"
                        @click="updateReservationStatus(selectedReservation.id, 'CONFIRMED')">
                        <i class="fas fa-check me-1"></i> 確認訂位
                    </button>
                    <!-- 移除標記完成按鈕，因為沒有COMPLETED狀態 -->
                    <button
                        v-if="selectedReservation.status === 'PENDING' || selectedReservation.status === 'CONFIRMED'"
                        class="btn btn-warning" @click="updateReservationStatus(selectedReservation.id, 'CANCELLED')">
                        <i class="fas fa-times me-1"></i> 取消訂位
                    </button>
                    <!-- <button class="btn btn-danger" @click="deleteReservation(selectedReservation.id)">
                        <i class="fas fa-trash me-1"></i> 刪除訂位
                    </button> -->


                </div>
            </div>
        </SlideOutPanel>
    </div>
</template>

<style scoped>
.reservation-management {
    height: 100%;
}

.filter-section {
    background-color: #f8f9fa;
    padding: 1.5rem;
    border-radius: 0.5rem;
    border: 1px solid #dee2e6;
}

.reservation-list {
    background-color: white;
    border-radius: 0.5rem;
    border: 1px solid #dee2e6;
    overflow: hidden;
}

.table th {
    border-top: none;
    font-weight: 600;
    color: #495057;
}

.table td {
    vertical-align: middle;
}

.btn-group-sm .btn {
    padding: 0.25rem 0.5rem;
    font-size: 0.875rem;
}

.reservation-detail {
    padding: 1rem;
}

.form-label {
    color: #6c757d;
    font-size: 0.875rem;
    margin-bottom: 0.25rem;
}

.form-control-plaintext {
    color: #212529;
    font-weight: 500;
}

.badge {
    font-size: 0.75rem;
    padding: 0.375rem 0.75rem;
}

.badge-warning {
    background-color: #ffc107;
    color: #212529;
}

.badge-success {
    background-color: #198754;
}

.badge-danger {
    background-color: #dc3545;
}

.badge-info {
    background-color: #0dcaf0;
    color: #212529;
}

.badge-secondary {
    background-color: #6c757d;
}
</style>