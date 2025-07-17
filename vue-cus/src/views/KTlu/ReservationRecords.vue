<template>
    <div class="reservation-records-container goldenbowl-restaurant-theme">
        <div class="header-section">
            <h2 class="page-title">
                <i class="pi pi-calendar"></i>
                我的訂位紀錄
            </h2>
            <div class="filter-section">
                <Select v-model="selectedStatus" :options="statusOptions" optionLabel="label" placeholder="篩選狀態"
                    class="status-filter" />
                <Button label="重新整理" icon="pi pi-refresh" @click="loadRecords" class="refresh-btn" :loading="loading" />
            </div>
        </div>

        <div v-if="loading" class="loading-state">
            <i class="pi pi-spinner pi-spin"></i>
            載入訂位紀錄中...
        </div>

        <div v-else-if="records.length === 0" class="empty-state">
            <i class="pi pi-calendar-times"></i>
            <h3>尚無訂位紀錄</h3>
            <p>您還沒有任何訂位紀錄</p>
            <Button label="立即預約" icon="pi pi-plus" @click="goToReservation" class="primary-btn" />
        </div>

        <div v-else class="records-section">
            <div class="records-grid">
                <div v-for="record in filteredRecords" :key="record.id" class="record-card"
                    :class="getStatusClass(record.status)">
                    <div class="record-header">
                        <div class="record-date">
                            <i class="pi pi-calendar"></i>
                            {{ formatDate(record.reservedDate) }}
                        </div>
                        <div class="record-time">
                            <i class="pi pi-clock"></i>
                            {{ formatTime(record.reservedTime) }}
                        </div>
                    </div>

                    <div class="record-details">
                        <div class="detail-item">
                            <span class="label">餐廳：</span>
                            <span class="value">{{ record.storeName || '未知餐廳' }}</span>
                        </div>
                        <div class="detail-item">
                            <span class="label">姓名：</span>
                            <span class="value">{{ record.userName || '未知用戶' }}</span>
                        </div>

                        <div class="detail-item">
                            <span class="label">人數：</span>
                            <span class="value">{{ record.guests }} 人</span>
                        </div>
                        <div class="detail-item">
                            <span class="label">狀態：</span>
                            <span class="status-badge" :class="getStatusBadgeClass(record.status)">
                                {{ getStatusText(record.status) }}
                            </span>
                        </div>
                        <div v-if="record.content" class="detail-item">
                            <span class="label">備註：</span>
                            <span class="value">{{ record.content }}</span>
                        </div>
                        <div class="detail-item">
                            <span class="label">用餐時長：</span>
                            <span class="value">{{ record.duration }} 分鐘</span>
                        </div>
                    </div>

                    <div class="record-actions">
                        <Button v-if="canEdit(record)" label="修改" icon="pi pi-pencil" @click="editRecord(record)"
                            class="edit-btn" />
                        <Button v-if="canCancel(record)" label="取消" icon="pi pi-times" @click="cancelRecord(record)"
                            class="cancel-btn" />
                        <Button v-if="canConfirm(record)" label="確認" icon="pi pi-check" @click="confirmRecord(record)"
                            class="confirm-btn" />
                        <span v-if="!canEdit(record) && !canCancel(record) && !canConfirm(record)"
                            class="text-muted">無法操作</span>
                    </div>
                </div>
            </div>
        </div>

        <!-- 編輯預約 Modal -->
        <Dialog v-model:visible="editModalVisible" modal header="修改預約" :style="{ width: '500px' }" class="edit-modal">
            <div v-if="editingRecord" class="edit-form">
                <div class="form-group">
                    <label>用餐人數</label>
                    <InputNumber v-model="editForm.guests" :min="1" :max="20" />
                </div>
                <div class="form-group">
                    <label>備註</label>
                    <Textarea v-model="editForm.content" rows="3" placeholder="請輸入備註..." />
                </div>
            </div>
            <template #footer>
                <Button label="取消" icon="pi pi-times" @click="editModalVisible = false" class="p-button-text" />
                <Button label="確認修改" icon="pi pi-check" @click="saveEdit" :loading="saving" />
            </template>
        </Dialog>

        <!-- 確認取消 Modal -->
        <Dialog v-model:visible="cancelModalVisible" modal header="確認取消預約" :style="{ width: '400px' }"
            class="cancel-modal">
            <p>確定要取消這個預約嗎？此操作無法復原。</p>
            <template #footer>
                <Button label="保留預約" icon="pi pi-times" @click="cancelModalVisible = false" class="p-button-text" />
                <Button label="確認取消" icon="pi pi-check" @click="confirmCancel" :loading="cancelling"
                    class="p-button-danger" />
            </template>
        </Dialog>
    </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import Swal from 'sweetalert2'
import Button from 'primevue/button'
import Select from 'primevue/select'
import Dialog from 'primevue/dialog'
import InputNumber from 'primevue/inputnumber'
import Textarea from 'primevue/textarea'
import '@/assets/css/restaurant-theme.css'

const router = useRouter()

// 響應式數據
const records = ref([])
const loading = ref(false)
const saving = ref(false)
const cancelling = ref(false)
const selectedStatus = ref(null)
const editModalVisible = ref(false)
const cancelModalVisible = ref(false)
const editingRecord = ref(null)
const cancellingRecord = ref(null)

// 編輯表單
const editForm = ref({
    guests: 0,
    content: ''
})

// 狀態選項
const statusOptions = ref([
    { value: null, label: '全部狀態' },
    { value: 'PENDING', label: '待確認' },
    { value: 'CONFIRMED', label: '已確認' },
    { value: 'CANCELLED', label: '已取消' },
    { value: 'COMPLETED', label: '已完成' }
])

// 計算屬性
const filteredRecords = computed(() => {
    if (!selectedStatus.value) {
        return records.value
    }
    return records.value.filter(record => record.status === selectedStatus.value)
})

// 載入預約紀錄
const loadRecords = async () => {
    loading.value = true
    try {
        const response = await fetch('/api/reservations/user/1')
        if (response.ok) {
            const data = await response.json()
            // 為每個預約添加餐廳名稱和用戶姓名
            const recordsWithDetails = await Promise.all(
                data.map(async (record) => {
                    const [storeName, userName] = await Promise.all([
                        getStoreName(record.storeId),
                        getUserName(record.userId)
                    ])
                    return {
                        ...record,
                        storeName: storeName,
                        userName: userName
                    }
                })
            )
            records.value = recordsWithDetails
        } else {
            throw new Error('載入失敗')
        }
    } catch (error) {
        console.error('載入預約紀錄失敗:', error)
        records.value = []
    } finally {
        loading.value = false
    }
}

// 根據餐廳 ID 獲取餐廳名稱
const getStoreName = async (storeId) => {
    try {
        const response = await fetch(`/api/stores/${storeId}`)
        if (response.ok) {
            const store = await response.json()
            return store.name || '未知餐廳'
        } else {
            return '未知餐廳'
        }
    } catch (error) {
        console.error('獲取餐廳名稱失敗:', error)
        return '未知餐廳'
    }
}

// 根據用戶 ID 獲取用戶姓名
const getUserName = async (userId) => {
    try {
        const response = await fetch(`/api/users/${userId}`)
        if (response.ok) {
            const user = await response.json()
            return user.name || '未知用戶'
        } else {
            return '未知用戶'
        }
    } catch (error) {
        console.error('獲取用戶姓名失敗:', error)
        // 如果 JSON 解析失敗，返回預設值
        return '未知用戶'
    }
}

// 格式化日期
const formatDate = (dateString) => {
    const date = new Date(dateString)
    return date.toLocaleDateString('zh-TW', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        weekday: 'long'
    })
}

// 格式化時間
const formatTime = (timeString) => {
    if (!timeString) return ''
    // 如果時間字串包含秒數，只取前5個字符 (HH:mm)
    if (timeString.length > 5) {
        return timeString.substring(0, 5)
    }
    return timeString
}

// 獲取狀態文字
const getStatusText = (status) => {
    const statusMap = {
        'PENDING': '待確認',
        'CONFIRMED': '已確認',
        'CANCELLED': '已取消',
        'COMPLETED': '已完成'
    }
    return statusMap[status] || status
}

// 獲取狀態樣式類別
const getStatusClass = (status) => {
    return `status-${status.toLowerCase()}`
}

// 獲取狀態徽章樣式類別
const getStatusBadgeClass = (status) => {
    return `badge-${status.toLowerCase()}`
}

// 檢查是否可以編輯
const canEdit = (record) => {
    const now = new Date()
    const reservedDate = new Date(record.reservedDate + 'T' + record.reservedTime)
    return reservedDate > now && record.status === 'CONFIRMED'
}

// 檢查是否可以取消
const canCancel = (record) => {
    const now = new Date()
    const reservedDate = new Date(record.reservedDate + 'T' + record.reservedTime)
    return reservedDate > now && ['PENDING', 'CONFIRMED'].includes(record.status)
}

// 檢查是否可以確認
const canConfirm = (record) => {
    return record.status === 'PENDING'
}

// 編輯預約
const editRecord = (record) => {
    editingRecord.value = record
    editForm.value = {
        guests: record.guests,
        content: record.content || ''
    }
    editModalVisible.value = true
}

// 保存編輯
const saveEdit = async () => {
    if (!editingRecord.value) return

    saving.value = true
    try {
        const response = await fetch(`/api/reservations/${editingRecord.value.id}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(editForm.value)
        })

        const result = await response.json()
        console.log('修改預約響應:', result)

        if (result.success) {
            await Swal.fire({
                title: '修改成功',
                text: '預約資訊已更新',
                icon: 'success'
            })

            // 更新本地數據
            const index = records.value.findIndex(r => r.id === editingRecord.value.id)
            if (index !== -1) {
                records.value[index] = { ...records.value[index], ...editForm.value }
            }

            // 只有在成功時才關閉表單
            editModalVisible.value = false
            editingRecord.value = null
        } else {
            // 失敗時不關閉表單，讓用戶可以修正
            throw new Error(result.errorMessage || '修改失敗')
        }
    } catch (error) {
        console.error('修改預約失敗:', error)
        await Swal.fire({
            title: '修改失敗',
            text: error.message || '請稍後再試',
            icon: 'error'
        })
        // 失敗時不關閉表單，讓用戶可以修正錯誤
    } finally {
        saving.value = false
    }
}

// 取消預約
const cancelRecord = (record) => {
    cancellingRecord.value = record
    cancelModalVisible.value = true
}

// 確認取消
const confirmCancel = async () => {
    if (!cancellingRecord.value) return

    cancelling.value = true
    try {
        const response = await fetch(`/api/reservations/${cancellingRecord.value.id}/status`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                status: 'CANCELLED'
            })
        })

        if (response.ok) {
            await Swal.fire({
                title: '取消成功',
                text: '預約已取消',
                icon: 'success'
            })

            // 更新本地數據
            const index = records.value.findIndex(r => r.id === cancellingRecord.value.id)
            if (index !== -1) {
                records.value[index].status = 'CANCELLED'
            }

            cancelModalVisible.value = false
        } else {
            throw new Error('取消失敗')
        }
    } catch (error) {
        console.error('取消預約失敗:', error)
        await Swal.fire({
            title: '取消失敗',
            text: '請稍後再試',
            icon: 'error'
        })
    } finally {
        cancelling.value = false
    }
}

// 確認預約
const confirmRecord = async (record) => {
    try {
        const response = await fetch(`/api/reservations/${record.id}/status`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                status: 'CONFIRMED'
            })
        })

        const result = await response.json()
        console.log('確認預約響應:', result)

        if (response.ok) {
            await Swal.fire({
                title: '確認成功',
                text: '預約已確認',
                icon: 'success'
            })

            // 更新本地數據
            const index = records.value.findIndex(r => r.id === record.id)
            if (index !== -1) {
                records.value[index].status = 'CONFIRMED'
            }
        } else {
            const errorMessage = result.errorMessage || `確認失敗: ${response.status}`
            throw new Error(errorMessage)
        }
    } catch (error) {
        console.error('確認預約失敗:', error)
        await Swal.fire({
            title: '確認失敗',
            text: error.message || '請稍後再試',
            icon: 'error'
        })
    }
}

// 跳轉到預約頁面
const goToReservation = () => {
    router.push('/restaurant/1') // 假設餐廳 ID 為 1
}

onMounted(() => {
    loadRecords()
})
</script>

<style scoped>
.reservation-records-container {
    padding: 20px;
    max-width: 1200px;
    margin: 0 auto;
}

.header-section {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 30px;
    padding: 20px;
    background: var(--restaurant-bg-light);
    border-radius: 12px;
    box-shadow: 0 2px 8px var(--restaurant-shadow-light);
}

.page-title {
    color: var(--restaurant-text-primary);
    margin: 0;
    font-size: 1.8rem;
    font-weight: 600;
}

.page-title i {
    margin-right: 10px;
    color: var(--restaurant-primary);
}

.filter-section {
    display: flex;
    gap: 15px;
    align-items: center;
}

.status-filter {
    min-width: 150px;
}

.refresh-btn {
    background: var(--restaurant-primary) !important;
    border: none !important;
    color: white !important;
}

.refresh-btn:hover {
    background: var(--restaurant-primary-hover) !important;
}

.loading-state,
.empty-state {
    text-align: center;
    padding: 60px 20px;
    color: var(--restaurant-text-secondary);
}

.empty-state i {
    font-size: 4rem;
    color: var(--restaurant-text-light);
    margin-bottom: 20px;
}

.empty-state h3 {
    color: var(--restaurant-text-primary);
    margin-bottom: 10px;
}

.primary-btn {
    background: var(--restaurant-primary) !important;
    border: none !important;
    color: white !important;
    margin-top: 20px;
}

.records-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
    gap: 20px;
}

.record-card {
    background: white;
    border-radius: 12px;
    padding: 20px;
    box-shadow: 0 4px 12px var(--restaurant-shadow-light);
    border: 2px solid var(--restaurant-border-light);
    transition: all 0.3s ease;
}

.record-card:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 24px var(--restaurant-shadow-medium);
}

.record-header {
    display: flex;
    justify-content: space-between;
    margin-bottom: 15px;
    padding-bottom: 15px;
    border-bottom: 1px solid var(--restaurant-border-light);
}

.record-date,
.record-time {
    display: flex;
    align-items: center;
    gap: 8px;
    font-weight: 600;
    color: var(--restaurant-text-primary);
}

.record-date i,
.record-time i {
    color: var(--restaurant-primary);
}

.record-details {
    margin-bottom: 20px;
}

.detail-item {
    display: flex;
    justify-content: space-between;
    margin-bottom: 8px;
    align-items: center;
}

.detail-item .label {
    font-weight: 500;
    color: var(--restaurant-text-secondary);
}

.detail-item .value {
    color: var(--restaurant-text-primary);
    font-weight: 500;
}

.status-badge {
    padding: 4px 12px;
    border-radius: 20px;
    font-size: 0.85rem;
    font-weight: 600;
}

.badge-pending {
    background: #fff3cd;
    color: #856404;
}

.badge-confirmed {
    background: #d1ecf1;
    color: #0c5460;
}

.badge-cancelled {
    background: #f8d7da;
    color: #721c24;
}

.badge-completed {
    background: #d4edda;
    color: #155724;
}

.record-actions {
    display: flex;
    gap: 10px;
    justify-content: flex-end;
}

.edit-btn {
    background: var(--restaurant-primary) !important;
    border: none !important;
    color: white !important;
    font-size: 0.85rem !important;
}

.cancel-btn {
    background: #dc3545 !important;
    border: none !important;
    color: white !important;
    font-size: 0.85rem !important;
}

.confirm-btn {
    background: #28a745 !important;
    border: none !important;
    color: white !important;
    font-size: 0.85rem !important;
}

.edit-form {
    padding: 20px 0;
}

.form-group {
    margin-bottom: 20px;
}

.form-group label {
    display: block;
    margin-bottom: 8px;
    font-weight: 600;
    color: var(--restaurant-text-primary);
}

/* 響應式設計 */
@media (max-width: 768px) {
    .header-section {
        flex-direction: column;
        gap: 15px;
        align-items: stretch;
    }

    .filter-section {
        justify-content: center;
    }

    .records-grid {
        grid-template-columns: 1fr;
    }

    .record-actions {
        flex-direction: column;
    }
}
</style>