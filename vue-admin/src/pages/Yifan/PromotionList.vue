<template>
    <h2>優惠活動</h2>
    <div v-if="sidebarOpen" class="overlay" @click="toggleSidebar"></div>
    <main class="admin-main p-4">
        <div class="table-card mb-4">
        <div class="filter-bar mb-4 d-flex flex-wrap align-items-center gap-2">
            <div>搜尋：</div>
            <input v-model="keyword" placeholder="搜尋活動標題或優惠碼..." class="form-control-sm w-auto" /> 折扣類型：
            <select v-model="selectedType" class="form-select" style="width: 150px;">
            <option value="">全部</option>
            <option value="percent">百分比</option>
            <option value="amount">金額</option>
            </select>
            <button class="btn btn-primary" @click="resetFilters">清除篩選</button>
        </div>
        <table class="table table-striped table-hover promotion-table">
            <thead>
            <tr class="text-center">
                <th class="col-title">活動標題</th>
                <th class="col-description">優惠內容</th>
                <th class="col-datetime">活動起訖</th>
                <th class="col-type">折扣類型</th>
                <th class="col-discount">折扣數值</th>
                <th class="col-min">門檻</th>
                <th class="col-code">優惠碼</th>
                <th class="col-limit">使用上限</th>
                <th class="col-per-user">每人上限</th>
                <th class="col-store">餐廳條件</th>
                <th class="col-tag">食物條件</th>
                <!-- <th>會員條件</th> -->
                <th class="col-actions">操作</th>
            </tr>
            </thead>
            <tbody class="text-center">
            <tr v-for="promotion in paginatedPromotions" :key="promotion.id">
    <!-- 編輯中 -->
            <template v-if="editingId === promotion.id">
                <!-- 標題 -->
                <td><input v-model="editedPromotion.title" class="form-control form-control-sm" /></td>
                <!-- 優惠內容 -->
                <td><input v-model="editedPromotion.description" class="form-control form-control-sm" /></td>
                <!-- 優惠時間 -->
                <td>
                <input type="datetime-local" v-model="editedPromotion.startTime" class="form-control form-control-sm mb-1" />
                <input type="datetime-local" v-model="editedPromotion.endTime" class="form-control form-control-sm" />
                </td>
                <!-- 折扣類型 -->
                <td>
                    <select v-model="editedPromotion.discountType" class="form-select form-select-sm">
                        <option value="">請選擇</option>
                        <option value="amount">金額折扣</option>
                        <option value="percent">百分比折扣</option>
                    </select>
                    </td>
                <!-- 折扣數值 -->
                <td>
                    <div class="d-flex align-items-center">
                        <input
                            v-if="editedPromotion.discountType"
                            type="number"
                            v-model.number="displayDiscountValue"
                            class="form-control form-control-sm"
                            :placeholder=" editedPromotion.discountType === 'amount' ? '請輸入 0 ~ 10000' : '請輸入 0 ~ 9.9'"
                            :step="editedPromotion.discountType === 'amount' ? 1 : 0.1"
                            :min="editedPromotion.discountType === 'amount' ? 1 : 0.1"
                            :max="editedPromotion.discountType === 'amount' ? 10000 : 9.9"inputmode="decimal"
                            @input="handleEditInputSanitization"
                        />
                        <span class="ms-2 small text-muted" v-if="editedPromotion.discountType === 'amount'">元</span>
                        <span class="ms-2 small text-muted" v-else-if="editedPromotion.discountType === 'percent'">折</span>
                    </div>
                </td>
                <!-- 消費門檻 -->
                <td><input v-model.number="editedPromotion.minSpend" class="form-control form-control-sm" /></td>
                <!-- 優惠碼 -->
                <td><input v-model="editedPromotion.code" class="form-control form-control-sm" /></td>
                <!-- 使用上限 -->
                <td><input v-model.number="editedPromotion.maxUsage" class="form-control form-control-sm" /></td>
                <!-- 每人上限 -->
                <td><input v-model.number="editedPromotion.userUsageLimit" class="form-control form-control-sm" /></td>
                <!-- 餐廳條件 -->
                <td>
                    <label><input type="checkbox" v-model="editedPromotion.bindRestaurant" /> 餐廳</label>
                    <input v-if="editedPromotion.bindRestaurant" v-model="editedPromotion.restaurantIdsInput" placeholder="ID" class="form-control form-control-sm mt-1" />
                </td>
                <!-- 食物條件 -->
                <td>
                    <label><input type="checkbox" v-model="editedPromotion.bindFood" /> 食物</label>
                    <input v-if="editedPromotion.bindFood" v-model="editedPromotion.foodCategoryIdsInput" placeholder="ID" class="form-control form-control-sm mt-1" />
                </td>
                <!-- 會員條件 -->
                <!-- <td>
                <label><input type="checkbox" v-model="editedPromotion.bindPlan" /> 會員</label>
                <input v-if="editedPromotion.bindPlan" v-model="editedPromotion.planIdsInput" placeholder="ID" class="form-control form-control-sm mt-1" />
                </td> -->

                <!--<td>{{ promotion.storeName || '無' }}</td>
                <td>{{ promotion.tagName || '無' }}</td>
                <td>{{ promotion.planName || '無' }}</td> -->

                <td>
                <button class="btn btn-sm btn-success me-1" @click="savePromotion">✅ 儲存</button>
                <button class="btn btn-sm btn-secondary" @click="cancelEdit">取消</button>
                </td>
            </template>

    <!-- 顯示中 -->
            <template v-else>
                <td class="text-start">{{ promotion.title }}</td>
                <td class="text-start">{{ promotion.description }}</td>
                <td class="text-center">
                    {{ promotion.startTimeStr }}<br />❘<br />{{ promotion.endTimeStr }}
                </td>
                <td>
                    {{ promotion.discountType === 'percent' ? '百分比' : '金額' }}
                </td>
                <td>
                    {{ promotion.discountType === 'percent' ? `${(promotion.discountValue )} 折` : `折抵 ${promotion.discountValue}元` }}
                </td>
                <td>{{ promotion.minSpend }}</td>
                <td>{{ promotion.code }}</td>
                <td>{{ promotion.maxUsage }}</td>
                <td>{{ promotion.userUsageLimit }}</td>
                <td>{{ promotion.storeName || '無' }}</td>
                <td>{{ promotion.tagName || '無' }}</td>
                <!-- <td>{{ promotion.planName || '無' }}</td> -->
                <td class="action-cell">
                    <div class="d-flex justify-content-center align-items-center gap-2 flex-wrap p-2">
                        <button class="btn btn-sm btn-edit" @click="editPromotion(promotion)">修改</button>
                        <button class="btn btn-sm btn-delete" @click="deletePromotion(promotion.id)">刪除</button>
                    </div>
                </td>
            </template>
            </tr>
    <!-- 新增列 -->
            <tr v-if="showNewRow" class="new-row">
                <!-- 標題 -->
                <td><input v-model="newPromotion.title" placeholder="標題" class="form-control form-control-sm" /></td>
                <!-- 內容 -->
                <td><input v-model="newPromotion.description" placeholder="內容" class="form-control form-control-sm" /></td>
                <!-- 起訖 -->
                <td>
                    <input type="datetime-local" v-model="newPromotion.startTime" class="form-control" />
                    <input type="datetime-local" v-model="newPromotion.endTime" class="form-control" />
                </td>
                <!-- 折扣類型 -->
                <td>
                    <select v-model="newPromotion.discountType" class="form-select form-select-sm" required>
                    <option value="">-</option>
                    <option value="amount">金額折扣</option>
                    <option value="percent">百分比折扣</option>
                    </select>
                </td>
                <!-- 折扣數值 -->
                <td>
                <div class="d-flex align-items-center">
                <input
                    v-if="newPromotion.discountType"
                    type="number"
                    v-model.number="newDisplayDiscountValue"
                    class="form-control form-control-sm"
                    :placeholder="
                    newPromotion.discountType === 'amount'
                        ? '請輸入 0 ~ 10000'
                        : '請輸入 0 ~ 9.9'
                    "
                    :step="newPromotion.discountType === 'amount' ? 1 : 0.1"
                    :min="newPromotion.discountType === 'amount' ? 0 : 0.0"
                    :max="newPromotion.discountType === 'amount' ? 10000 : 9.9"
                    inputmode="decimal"
                    @input="handleInputSanitization"
                />
                <span
                    class="ms-2 small text-muted"
                    v-if="newPromotion.discountType === 'amount'"
                >元</span>
                <span
                class="small text-muted mt-1"
                v-if="newPromotion.discountType === 'percent'"
            ></span>
            </div>
            </td>
                <!-- 門檻 -->
                <td><input type="number" v-model="newPromotion.minSpend" placeholder="門檻" class="form-control form-control-sm" /></td>
                <!-- 優惠碼 -->
                <td><input v-model="newPromotion.code" placeholder="優惠碼" class="form-control form-control-sm" /></td>
                <!-- 使用上限 -->
                <td><input type="number" v-model="newPromotion.maxUsage" placeholder="上限" class="form-control form-control-sm" /></td>
                <!-- 每人上限 -->
                <td><input type="number" v-model="newPromotion.userUsageLimit" placeholder="每人上限" class="form-control form-control-sm" /></td>
                <!-- 餐廳條件 -->
                <td>
                <label><input type="checkbox" v-model="newPromotion.bindRestaurant" /> 餐廳</label>
                <input v-if="newPromotion.bindRestaurant" v-model="newPromotion.restaurantIdsInput" placeholder="ID" class="form-control form-control-sm mt-1" />
                </td>
                <!-- 食物條件 -->
                <td>
                <label><input type="checkbox" v-model="newPromotion.bindFood" /> 食物</label>
                <input v-if="newPromotion.bindFood" v-model="newPromotion.foodCategoryIdsInput" placeholder="ID" class="form-control form-control-sm mt-1" />
                </td>
                <!-- 會員條件 -->
                <!-- <td>
                <label><input type="checkbox" v-model="newPromotion.bindPlan" /> 會員</label>
                <input v-if="newPromotion.bindPlan" v-model="newPromotion.planIdsInput" placeholder="ID" class="form-control form-control-sm mt-1" />
                </td>
                <td>—</td> -->
                <!-- 操作欄 -->
                <td class="action-cell">
                <div class="d-flex justify-content-center align-items-center gap-2 flex-wrap p-2">
                    <button class="btn btn-sm btn-primary btn-save" @click="createPromotion">儲存</button>
                    <button class="btn btn-sm btn-secondary btn-cancel" @click="resetNewPromotion(); showNewRow = false;">取消</button>
                </div>
                </td>
            </tr>
            </tbody>
        </table>
        <div class="d-flex flex-wrap gap-2">
            <button class="btn btn-add" @click="showNewRow = true">新增</button>
        </div>
        </div>

        <div class="pagination d-flex justify-content-end align-items-center pagebar-wrap">
        <button class="btn btn-outline-secondary me-2" :disabled="currentPage === 1" @click="currentPage--">&lt; 上一頁</button>
        <nav>
            <ul class="pagination mb-0">
            <li class="page-item disabled">
                <span class="page-link">頁數：{{ currentPage }} / {{ totalPages }}</span>
            </li>
            </ul>
        </nav>
        <button class="btn btn-outline-secondary ms-2" :disabled="currentPage === totalPages" @click="currentPage++">下一頁 &gt;</button>
        <div class="ms-3">
            <select class="form-select" v-model.number="itemsPerPage" style="width:120px; min-width: 90px;">
            <option v-for="s in [5, 10, 20]" :key="s" :value="s">{{ s }}/每頁</option>
            </select>
        </div>
        </div>
    </main>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import axios from '@/plungins/axios.js'
import Swal from 'sweetalert2'
import { watch } from 'vue'

const promotions = ref([])
const sidebarOpen = ref(false)
const currentPage = ref(1)
const itemsPerPage = ref(5)
const keyword = ref('')
const selectedType = ref('')
const showNewRow = ref(false) // 控制是否顯示新增列


//折扣類型
const getDiscountTypeText = (type) => {
    if (type === 'amount') return '金額折扣'
    if (type === 'percent') return '百分比折扣'
    return '—'
}
const displayDiscountValue = computed({
    get() {
        return editedPromotion.value.discountValue;
    },
    set(value) {
        editedPromotion.value.discountValue = value;
    }
});
const newDisplayDiscountValue = computed({
    get: () => newPromotion.value.discountValue,
    set: (val) => {
        newPromotion.value.discountValue = val
    }
})

//折扣數值範圍
const handleInputSanitization = (e) => {
    const val = e.target.value
    if (newPromotion.value.discountType === 'amount') {
        const parsed = parseInt(val)
        if (!isNaN(parsed)) {
        newPromotion.value.discountValue = Math.min(10000, Math.max(0, parsed))
        }
    } else {
        const parsed = parseFloat(val)
        if (!isNaN(parsed)) {
        newPromotion.value.discountValue = Math.min(9.9, Math.max(0.0, parsed))
        }
    }
}




// ➕ 新增用的欄位
const newPromotion = ref({
    title: '',
    description: '',
    startTime: '',
    endTime: '',
    discountType: '',
    minSpend: 0,
    code: '',
    maxUsage: 0,
    userUsageLimit: 0,
    bindRestaurant: false,
    restaurantIdsInput: '',
    bindFood: false,
    foodCategoryIdsInput: '',
    bindPlan: false,
    planIdsInput: ''
})

//切換折扣類型時，清空折扣數值
watch(() => newPromotion.value.discountType, () => {
  newPromotion.value.discountValue = null
})

const toggleSidebar = () => {
    sidebarOpen.value = !sidebarOpen.value
}

// ✅ 防止 null.toLowerCase() 的錯誤
const filteredPromotions = computed(() => {
    const search = (keyword.value || '').toLowerCase()
    return promotions.value.filter(p =>
        // 🔍 模糊搜尋：標題 or 優惠碼
        ((p.title || '').toLowerCase().includes(search) ||
        (p.code || '').toLowerCase().includes(search)) &&
        // 🔘 篩選折扣類型（如果有選）
        (selectedType.value ? p.discountType === selectedType.value : true)
    )
})

const paginatedPromotions = computed(() => {
    const start = (currentPage.value - 1) * itemsPerPage.value
    return filteredPromotions.value.slice(start, start + itemsPerPage.value)
})

const totalPages = computed(() =>
    Math.ceil(filteredPromotions.value.length / itemsPerPage.value)
)

// 🚀 載入優惠活動
const fetchPromotions = async () => {
    try {
        const response = await axios.get('/promotions')
        console.log('✅ 後端回傳：', response.data)
        promotions.value = response.data
    } catch (e) {
        console.error('❌ 載入後台優惠券失敗', e)
        alert('無法載入優惠券，請檢查後端連線或資料格式')
    }
}


const createPromotion = async () => {
  // ===== 驗證必填欄位 =====
const requiredFields = [
    { label: '活動標題', value: newPromotion.value.title },
    { label: '優惠內容', value: newPromotion.value.description },
    { label: '折扣類型', value: newPromotion.value.discountType },
    { label: '折扣數值', value: newPromotion.value.discountValue },
    { label: '最低消費門檻', value: newPromotion.value.minSpend },
    { label: '開始時間', value: newPromotion.value.startTime },
    { label: '結束時間', value: newPromotion.value.endTime },
    { label: '優惠碼', value: newPromotion.value.code }
]

const missing = requiredFields.find(field => !field.value)
    if (missing) {
        await Swal.fire(`請填寫 ${missing.label}`, '', 'warning')
        return
    }

  // ===== 建立 promotion 物件 =====
const promotion = {
    title: newPromotion.value.title,
    description: newPromotion.value.description,
    discountType: newPromotion.value.discountType,
    discountValue: newPromotion.value.discountValue,
    minSpend: newPromotion.value.minSpend,
    startTime: newPromotion.value.startTime,
    endTime: newPromotion.value.endTime,
    code: newPromotion.value.code,
    maxUsage: newPromotion.value.maxUsage || null,
    userUsageLimit: newPromotion.value.userUsageLimit || null,
    storeId: newPromotion.value.bindRestaurant
        ? parseInt(newPromotion.value.restaurantIdsInput)
        : null,
    tagId: newPromotion.value.bindFood
        ? parseInt(newPromotion.value.foodCategoryIdsInput)
        : null,
    planId: newPromotion.value.bindPlan
        ? parseInt(newPromotion.value.planIdsInput)
        : null
}
    try {
        const response = await axios.post('/promotions', promotion)
        await Swal.fire('✅ 優惠活動新增成功！', '', 'success')
        await fetchPromotions()
        resetNewPromotion()
        showNewRow.value = false // ✅ 儲存成功後自動收起新增欄位
    } catch (error) {
        console.error('❌ 新增失敗', error)
        Swal.fire('新增失敗', '請確認欄位格式或稍後再試', 'error')
    }
}


// 🔄 重設新增欄位
const resetNewPromotion = () => {
    newPromotion.value = {
        title: '',
        description: '',
        startTime: '',
        endTime: '',
        discountType: '',
        minSpend: 0,
        code: '',
        maxUsage: 0,
        userUsageLimit: 0,
        bindRestaurant: false,
        restaurantIdsInput: '',
        bindFood: false,
        foodCategoryIdsInput: '',
        bindPlan: false,
        planIdsInput: ''
    }
}

// ❌ 刪除
const deletePromotion = async (id) => {
    const result = await Swal.fire({
        title: '你確定要刪除這筆優惠券嗎？',
        icon: 'warning',
        showCancelButton: true,
        confirmButtonText: '確定刪除',
        cancelButtonText: '取消'
    })

    if (result.isConfirmed) {
        try {
        await axios.delete(`/promotions/${id}`)
        await Swal.fire('✅ 已刪除', '', 'success')
        await fetchPromotions()
        } catch (error) {
        console.error('❌ 刪除失敗', error)
        Swal.fire('刪除失敗', '請稍後再試', 'error')
        }
    }
}


// 📝 編輯
const editingId = ref(null) // 用來記錄目前正在編輯哪一筆
const editedPromotion = ref({}) // 暫存編輯內容

const editPromotion = (promotion) => {
    editingId.value = promotion.id
    editedPromotion.value = { ...promotion,
    bindRestaurant: !!promotion.storeName,
    restaurantIdsInput: promotion.storeId || '',
    bindFood: !!promotion.tagName,
    foodCategoryIdsInput: promotion.tagId || '',
    bindPlan: !!promotion.planName,
    planIdsInput: promotion.planId || '' ,
    discountValue: promotion.discountType === 'percent' ? promotion.discountValue : promotion.discountValue // 確保數值正確
    }
}
//切換折扣類型時，清空折扣數值
watch(() => editedPromotion.value.discountType, () => {
    editedPromotion.value.discountValue = null
})

const cancelEdit = () => {
    editingId.value = null
    editedPromotion.value = {}
}

const savePromotion = async () => {
    try {
        const updated = {
        ...editedPromotion.value,
        storeId: editedPromotion.value.bindRestaurant ? parseInt(editedPromotion.value.restaurantIdsInput) : null,
        tagId: editedPromotion.value.bindFood ? parseInt(editedPromotion.value.foodCategoryIdsInput) : null,
        planId: editedPromotion.value.bindPlan ? parseInt(editedPromotion.value.planIdsInput) : null
        }

        const response = await axios.put(`/promotions/${editingId.value}`, updated, {
        headers: {
            'Content-Type': 'application/json'
        }
        })

        await Swal.fire('✅ 更新成功', '', 'success')
        await fetchPromotions()
        cancelEdit()
        } catch (error) {
            console.error('❌ 更新失敗', error)
            Swal.fire('更新失敗', '請檢查欄位格式或再試一次', 'error')
        }
}


// 🔍 清除篩選條件
const resetFilters = () => {
    keyword.value = ''
    selectedType.value = ''
}

const prevPage = () => {
    if (currentPage.value > 1) currentPage.value--
}
const nextPage = () => {
    if (currentPage.value < totalPages.value) currentPage.value++
}

onMounted(fetchPromotions)
</script>

<style scoped>
/* 移除上下箭頭 */
input::-webkit-outer-spin-button,
input::-webkit-inner-spin-button {
    -webkit-appearance: none;
    margin: 0;
}
input[type='number'] {
    -moz-appearance: textfield;
}
/* 內容字大小 */
.promotion-table {
    font-size: 14px; /* 或可改成 0.8rem、12px 視覺調整 */
    table-layout: fixed;
    width: 100%;
}
/* 按鈕 */
.btn-add {
    background-color: #0d6efd;
    color: white;
    font-size: 12px;
}
.btn-delete {
    background-color: #dc3545;
    color: white;
    font-size: 12px;
}
.btn-edit {
    background-color: #198754;
    color: white;
    font-size: 12px;
}
.btn-search {
    background-color: #0dcaf0;
    color: black;
    font-size: 12px;
}
.btn-save, .btn-cancel {
  font-size: 12px;
  height: 30px;
  min-width: 18px;
 
}


/* 欄寬 */
.col-title { width: 130px; }
.col-description { width: 200px; }
.col-datetime { 
    width: 160px; 
    white-space: nowrap;
    text-align: center;
    vertical-align: middle;}
.col-type { width: 60px; }
.col-discount { width: 70px; }
.col-min { width: 60px; }
.col-code { width: 80px; }
.col-limit { width: 60px; }
.col-per-user { width: 60px; }
.col-store { width: 60px; }
.col-tag { width: 60px; }
.col-actions { width: 60px; min-width: 80px; }


.promotion-table th,
.promotion-table td {
    vertical-align: middle;
    height: 80px; /* 可視情況調整 80~120 */
    white-space: normal;
    word-break: break-word;
    padding: 8px; /* 調整內距 */
}


/* 新增欄位調整 */
.new-row input,
.new-row select,
.new-row span,
.new-row label {
  font-size: 10px; /* 或你想要的大小，例如 10px、0.8rem */
}
</style>