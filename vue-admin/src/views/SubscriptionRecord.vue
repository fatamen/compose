<template>
    <h2>訂閱紀錄</h2>
    <div class ="subscription-record-wrapper">
    
        <!-- 表格卡片 -->
        <div class="table-card mb-4 p-4">

        <!-- 搜尋與下拉 -->
        <div class="filter-bar mb-4 d-flex flex-wrap align-items-center gap-2">
            <label class="form-label mb-0">搜尋：</label>
            <input
                type="text"
                class="form-control"
                style="width:200px"
                v-model="search"
                placeholder="搜尋用戶名稱..."
            >
            <label class="form-label mb-0 ms-2">訂閱方案：</label>
            <select class="form-select" style="width:150px" v-model="planName">
                <option value="">全部</option>
                <option v-for="name in uniquePlanNames" :key="name" :value="name">
                    {{ name }}
                </option>
            </select>
            <button class="btn btn-primary" @click="clearFilter">清除篩選</button>
        </div>
        <table class="table table-striped table-hover promotion-table">
            <thead>
                <tr>
                    <th>用戶名稱</th>
                    <th>訂閱方案</th>
                    <th>起訖</th>
                    <th>費用</th>
                    <th>到期時間</th>
                    <th>狀態</th>
                </tr>
            </thead>
            <tbody>
                <tr v-for="record in pagedSubscriptionRecords" :key="record.id">
                    <td>{{ record.userName }}</td>
                    <td>{{ record.subscriptionName }}</td>
                    <td>{{ record.dateStart }} ~ {{ record.dateEnd }}</td>
                    <td>{{ record.price }}</td>
                    <td>{{ record.dateEnd }}</td>
                    <td>{{ record.status }}</td>
                </tr>
                <tr v-if="pagedSubscriptionRecords.length === 0">
                    <td colspan="6" class="text-center">查無資料</td>
                </tr>
            </tbody>
        </table>
        </div>
        <!-- 分頁控制 -->
        <div class="pagination d-flex justify-content-end align-items-center pagebar-wrap">
            <button class="btn btn-outline-secondary me-2"
                :disabled="page === 1"
                @click="page > 1 && (page--)"
            >&lt; 上一頁</button>
            <nav>
                <ul class="pagination mb-0">
                    <li class="page-item disabled">
                        <span class="page-link">頁數：{{ page }} / {{ totalPages }}</span>
                    </li>
                </ul>
            </nav>
            <button class="btn btn-outline-secondary ms-2" :disabled="page === totalPages" @click="page < totalPages && (page++)">下一頁 &gt;</button>
            <div class="ms-3">
                <select class="form-select" v-model.number="pageSize" style="width:120px; min-width: 90px;">
                    <option v-for="s in pageSizes" :key="s" :value="s">{{ s }}/每頁</option>
                </select>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'

const search = ref('')
const planName = ref('')

const subscriptionRecords = ref([
    { id: 1, userName: 'Alice', subscriptionName: '月租', dateStart: '2024-07-01', dateEnd: '2024-07-31', price: 199, status: '啟用' },
    { id: 2, userName: 'Bob', subscriptionName: '季租', dateStart: '2024-04-01', dateEnd: '2024-06-30', price: 550, status: '停用' },
    { id: 3, userName: 'Cathy', subscriptionName: '年租', dateStart: '2024-01-01', dateEnd: '2024-12-31', price: 1800, status: '啟用' },
    { id: 4, userName: 'David', subscriptionName: '月租', dateStart: '2024-06-01', dateEnd: '2024-06-30', price: 199, status: '啟用' },
    { id: 5, userName: 'Eva', subscriptionName: '季租', dateStart: '2024-05-01', dateEnd: '2024-07-31', price: 550, status: '啟用' },
    { id: 6, userName: 'Frank', subscriptionName: '年租', dateStart: '2024-02-15', dateEnd: '2025-02-14', price: 1800, status: '啟用' },
    { id: 7, userName: 'Grace', subscriptionName: '月租', dateStart: '2024-05-15', dateEnd: '2024-06-14', price: 199, status: '停用' },
    { id: 8, userName: 'Henry', subscriptionName: '季租', dateStart: '2024-03-10', dateEnd: '2024-06-09', price: 550, status: '啟用' },
    { id: 9, userName: 'Ivy', subscriptionName: '年租', dateStart: '2023-09-01', dateEnd: '2024-08-31', price: 1800, status: '停用' },
    { id: 10, userName: 'Jack', subscriptionName: '月租', dateStart: '2024-04-20', dateEnd: '2024-05-19', price: 199, status: '啟用' },
    { id: 11, userName: 'Kate', subscriptionName: '月租', dateStart: '2024-03-01', dateEnd: '2024-03-31', price: 199, status: '啟用' },
    { id: 12, userName: 'Leo', subscriptionName: '季租', dateStart: '2024-06-01', dateEnd: '2024-08-31', price: 550, status: '啟用' },
])

// 取得訂閱紀錄
// const fetchRecords = async () => {
//     try {
//         // 後端回傳格式需有: id, userName, subscriptionName, price, status, dateStart, dateEnd
//         const res = await axios.get('http://localhost:8080/api/subscription/record')
//         subscriptionRecords.value = res.data
//     } catch (e) {
//         subscriptionRecords.value = []
//     }
// }

// 訂閱方案名稱下拉選單
const uniquePlanNames = computed(() => {
    return [...new Set(subscriptionRecords.value.map(rec => rec.subscriptionName))].filter(Boolean)
})

// 搜尋與方案過濾
function clearFilter() {
    search.value = ''
    planName.value = ''
}
const filteredSubscriptionRecords = computed(() => {
    return subscriptionRecords.value.filter(rec =>
        (search.value === '' || (rec.userName ?? '').includes(search.value)) &&
        (planName.value === '' || rec.subscriptionName === planName.value)
    )
})

// 分頁
const page = ref(1)
const pageSize = ref(5)
const pageSizes = [5, 10, 20]
const pagedSubscriptionRecords = computed(() => {
    const start = (page.value - 1) * pageSize.value
    const end = start + pageSize.value
    return filteredSubscriptionRecords.value.slice(start, end)
})

const totalPages = computed(() =>
    Math.max(1, Math.ceil(filteredSubscriptionRecords.value.length / pageSize.value))
)
watch(pageSize, () => { page.value = 1 })

</script>
