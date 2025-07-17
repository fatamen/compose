<template>
    <h2>訂閱方案</h2>
    <div class="subscription-plan-wrapper">

        <!-- 表格卡片 -->
        <div class="table-card mb-4 p-4">
        <!-- 搜尋與下拉 -->
        <div class="filter-bar mb-4 d-flex flex-wrap align-items-center gap-2">
            <label class="form-label mb-0">搜尋：</label>
            <input 
                type="text"
                class="form-control"
                style="width:200px"
                v-model="search" placeholder="搜尋訂閱方案..."
            >
            <label class="form-label mb-0 ms-2">方案類型：</label>
            <select class="form-select" style="width:150px" v-model="type">
                <option value="">全部</option>
                <option value="月租">月租</option>
                <option value="季租">季租</option>
                <option value="年租">年租</option>
            </select>
            <button class="btn btn-primary" @click="clearFilter">清除篩選</button>
        </div>

            <table class="table table-striped table-hover promotion-table">
                <thead>
                    <tr>
                        <th>訂閱方案</th>
                        <th>訂閱期限</th>
                        <th>類型</th>
                        <th>費用</th>
                        <th>狀態</th>
                        <th>操作</th>
                    </tr>
                </thead>
                <tbody>
                    <!-- 資料列 -->
                    <tr v-for="plan in pagedSubscriptionPlans" :key="plan.id">
                        <template v-if="editingId === plan.id">
                            <td><input class="form-control form-control-sm" style="width:160px" v-model="editRow.subscriptionName"></td>
                            <td><input class="form-control form-control-sm" v-model="editRow.deadline"></td>
                            <td>
                                <select class="form-select form-select-sm" v-model="editRow.type">
                                    <option value="月租">月租</option>
                                    <option value="季租">季租</option>
                                    <option value="年租">年租</option>
                                </select>
                            </td>
                            <td><input type="number" class="form-control form-control-sm" v-model="editRow.price"></td>
                            <td>
                                <select class="form-select form-select-sm" v-model="editRow.status">
                                    <option value="啟用">啟用</option>
                                    <option value="停用">停用</option>
                                </select>
                            </td>
                            <td>
                                <button class="btn btn-success btn-sm me-2" @click="saveEdit(plan.id)">儲存</button>
                                <button class="btn btn-secondary btn-sm" @click="cancelEdit">取消</button>
                            </td>
                        </template>
                        <template v-else>
                            <td>{{ plan.subscriptionName }}</td>
                            <td>{{ plan.deadline }}</td>
                            <td>{{ plan.type }}</td>
                            <td>{{ plan.price }}</td>
                            <td>
                                <span :class="{
                                    'badge bg-success': plan.status === '啟用',
                                    'badge bg-secondary': plan.status === '停用'
                                }">{{ plan.status }}</span>
                            </td>
                            <td>
                                <button class="btn btn-sm btn-edit me-1" @click="editPlan(plan)">修改</button>
                                <button class="btn btn-sm btn-delete" @click="deletePlan(plan)">刪除</button>
                            </td>
                        </template>
                    </tr>
                    <!-- 新增行 -->
                    <tr>
                        <td><input type="text" class="form-control form-control-sm" v-model="newRow.subscriptionName" placeholder="請輸入訂閱方案"></td>
                        <td><input type="text" class="form-control form-control-sm" v-model="newRow.deadline" placeholder="請輸入訂閱期限"></td>
                        <td>
                            <select class="form-select form-select-sm" v-model="newRow.type">
                                <option value="">請選擇類型</option>
                                <option value="月租">月租</option>
                                <option value="季租">季租</option>
                                <option value="年租">年租</option>
                            </select>
                        </td>
                        <td><input type="number" min="0" class="form-control form-control-sm" v-model="newRow.price" placeholder="請輸入費用"></td>
                        <td>
                            <select class="form-select form-select-sm" v-model="newRow.status">
                                <option value="">請選擇狀態</option>
                                <option value="啟用">啟用</option>
                                <option value="停用">停用</option>
                            </select>
                        </td>
                        <td>
                            <button class="btn btn-add btn-sm me-2" @click="addPlan">➕ 新增</button>
                            <button class="btn btn-cancel btn-sm" @click="clearNewRow">✖️ 取消</button>
                        </td>
                    </tr>
                    <tr v-if="pagedSubscriptionPlans.length === 0">
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
                    <li class="page-item disabled"><span class="page-link">頁數：{{ page }} / {{ totalPages }}</span></li>
                </ul>
            </nav>
            <button class="btn btn-outline-secondary ms-2"
                :disabled="page === totalPages"
                @click="page < totalPages && (page++)"
            >下一頁 &gt;</button>
            <div class="ms-3">
                <select class="form-select" v-model.number="pageSize" style="width:120px; min-width: 90px;">
                    <option v-for="s in pageSizes" :key="s" :value="s">{{ s }}/每頁</option>
                </select>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
// import axios from 'axios'

const subscriptionPlans = ref([
    { id: 1, subscriptionName: '月租標準', deadline: '30天', type: '月租', price: 199, status: '啟用' },
    { id: 2, subscriptionName: '季租超值', deadline: '90天', type: '季租', price: 550, status: '啟用' },
    { id: 3, subscriptionName: '年租尊享', deadline: '365天', type: '年租', price: 1800, status: '停用' },
])
const search = ref('')
const type = ref('')

// 編輯相關
const editingId = ref(null)
const editRow = ref({})

// 新增行欄位
const newRow = ref({
    subscriptionName: '',
    deadline: '',
    type: '',
    price: '',
    status: ''
})
function clearNewRow() {
    newRow.value = { subscriptionName: '', deadline: '', type: '', price: '', status: '' }
}
function addPlan() {
    if (!newRow.value.subscriptionName || !newRow.value.deadline || !newRow.value.type || !newRow.value.price || !newRow.value.status) {
        alert('請完整填寫欄位')
        return
    }
    const nextId = subscriptionPlans.value.length > 0
        ? Math.max(...subscriptionPlans.value.map(r => Number(r.id) || 0)) + 1
        : 1
    subscriptionPlans.value.push({
        id: nextId,
        ...newRow.value
    })
    clearNewRow()
}
function clearFilter() {
    search.value = ''
    type.value = ''
}
function editPlan(plan) {
    editingId.value = plan.id
    editRow.value = { ...plan }
}
function saveEdit(id) {
    const idx = subscriptionPlans.value.findIndex(r => r.id === id)
    if (idx > -1) {
        subscriptionPlans.value[idx] = { ...subscriptionPlans.value[idx], ...editRow.value }
    }
    editingId.value = null
}
function cancelEdit() {
    editingId.value = null
}
function deletePlan(plan) {
    if (confirm(`確定要刪除訂閱方案（ID: ${plan.id}）嗎？`)) {
        subscriptionPlans.value = subscriptionPlans.value.filter(p => p.id !== plan.id)
    }
}

// 分頁
const page = ref(1)
const pageSize = ref(5)
const pageSizes = [5, 10, 20]
const filteredSubscriptionPlans = computed(() => {
    return subscriptionPlans.value.filter(p =>
        (search.value === '' || p.subscriptionName?.includes(search.value)) &&
        (type.value === '' || p.type === type.value)
    )
})
const pagedSubscriptionPlans = computed(() => {
    const start = (page.value - 1) * pageSize.value
    const end = start + pageSize.value
    return filteredSubscriptionPlans.value.slice(start, end)
})
const totalPages = computed(() =>
    Math.max(1, Math.ceil(filteredSubscriptionPlans.value.length / pageSize.value))
)
watch(pageSize, () => { page.value = 1 })
</script>