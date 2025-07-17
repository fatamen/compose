<template>
    <div class="time-setting-editor">
        <h3>餐廳預約時間設定</h3>

        <div class="form-group">
            <label>時間間隔（分鐘）</label>
            <InputNumber v-model="form.interval" :min="15" :max="120" step="15"
                :class="{ 'p-invalid': errors.interval }" />
            <small v-if="errors.interval" class="p-error">{{ errors.interval }}</small>
        </div>

        <div class="form-group">
            <label>預設用餐時間（分鐘）</label>
            <InputNumber v-model="form.mealTime" :min="30" :max="180" step="15"
                :class="{ 'p-invalid': errors.mealTime }" />
            <small v-if="errors.mealTime" class="p-error">{{ errors.mealTime }}</small>
        </div>

        <div class="button-group">
            <Button label="儲存設定" icon="pi pi-save" @click="submit" :loading="loading" />
            <Button label="重置" icon="pi pi-refresh" severity="secondary" @click="resetForm" />
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import InputNumber from 'primevue/inputnumber'
import Button from 'primevue/button'
import Swal from 'sweetalert2'

const props = defineProps({ restaurantId: Number })
const form = ref({ interval: 30, mealTime: 90 })
const errors = ref({})
const loading = ref(false)

// 驗證表單
const validateForm = () => {
    errors.value = {}

    if (!form.value.interval || form.value.interval < 15) {
        errors.value.interval = '時間間隔不可少於15分鐘'
    }

    if (!form.value.mealTime || form.value.mealTime < 30) {
        errors.value.mealTime = '用餐時間不可少於30分鐘'
    }

    if (form.value.mealTime > 180) {
        errors.value.mealTime = '用餐時間不可超過180分鐘'
    }

    return Object.keys(errors.value).length === 0
}

// 生成時段
const generateTimeSlots = async (storeId, interval) => {
    try {
        const response = await fetch(`/api/reservations/generate-time-slots`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                storeId: storeId,
                interval: interval
            })
        })

        if (!response.ok) {
            throw new Error('生成時段失敗')
        }

        return await response.json()
    } catch (error) {
        console.error('生成時段失敗:', error)
        throw error
    }
}

const fetchSetting = async () => {
    try {
        const res = await fetch(`/api/time-setting/${props.restaurantId}`)
        if (!res.ok) {
            throw new Error('載入設定失敗')
        }
        const data = await res.json()
        form.value = {
            interval: data.interval ?? 30,
            mealTime: data.mealTime ?? 90
        }
    } catch (error) {
        console.error('載入時段設定失敗', error)
        Swal.fire('錯誤', '無法載入設定資料', 'error')
    }
}

const resetForm = () => {
    form.value = { interval: 30, mealTime: 90 }
    errors.value = {}
}

const submit = async () => {
    // 驗證表單
    if (!validateForm()) {
        Swal.fire('驗證錯誤', '請檢查輸入的數值', 'error')
        return
    }

    loading.value = true

    try {
        const res = await fetch(`/api/time-setting/${props.restaurantId}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(form.value)
        })

        if (!res.ok) {
            throw new Error('儲存設定失敗')
        }

        const updated = await res.json()

        // 詢問是否同步更新時段
        const result = await Swal.fire({
            title: '設定已儲存',
            text: `時間間隔已設為 ${updated.interval} 分鐘，是否要同步重新生成預約時段？`,
            icon: 'success',
            showCancelButton: true,
            confirmButtonText: '是，重新生成',
            cancelButtonText: '否，稍後再說',
            reverseButtons: true
        })

        if (result.isConfirmed) {
            // 顯示載入中
            Swal.fire({
                title: '正在生成時段...',
                text: '請稍候，這可能需要一些時間',
                allowOutsideClick: false,
                didOpen: () => {
                    Swal.showLoading()
                }
            })

            // 生成時段
            await generateTimeSlots(props.restaurantId, form.value.interval)

            Swal.fire('完成', '時段已成功重新生成', 'success')
        }

    } catch (error) {
        console.error('儲存時段設定失敗', error)
        Swal.fire('錯誤', '儲存失敗，請稍後再試', 'error')
    } finally {
        loading.value = false
    }
}

onMounted(fetchSetting)
</script>

<style scoped>
.time-setting-editor {
    padding: 20px;
}

.form-group {
    margin-bottom: 20px;
}

.form-group label {
    display: block;
    margin-bottom: 8px;
    font-weight: 600;
    color: #333;
}

.button-group {
    display: flex;
    gap: 10px;
    margin-top: 20px;
}

.p-error {
    color: #dc3545;
    font-size: 0.875rem;
    margin-top: 4px;
    display: block;
}

.p-invalid {
    border-color: #dc3545 !important;
}
</style>