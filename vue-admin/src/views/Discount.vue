<template>
    <div class="container mt-4">
        <h2>優惠活動</h2>
        <!-- 搜尋+下拉 -->
        <div class="d-flex align-items-center mb-3 gap-2 flex-wrap">
            <label class="form-label mb-0">搜尋：</label>
            <input
                type="text"
                class="form-control"
                style="width:200px"
                v-model="search"
                placeholder="搜尋活動標題..."
            >
            <label class="form-label mb-0 ms-2">折扣類型：</label>
            <select class="form-select" style="width:150px" v-model="type">
            <option value="">全部</option>
            <option v-for="dis in Array.from(new Set(discounts.map(rec => dis.discountType)))"
                    :key="dis"
                    :value="dis">
                {{ dis }}
            </option>
            </select>
            <button class="btn btn-primary ms-2" @click="clearFilter">清除篩選</button>
        </div>
        <table class="table table-bordered">
            <thead>
                <tr>
                    <th>活動標題</th>
                    <th>優惠內容</th>
                    <th>起訖</th>
                    <th>折扣類型</th>
                    <th>門檻</th>
                    <th>優惠碼</th>
                    <th>使用上限</th>
                    <th>每人上限</th>
                    <th>餐廳條件</th>
                    <th>食物條件</th>
                    <th>會員條件</th>
                    <th>操作</th>
                </tr>
            </thead>
            <tbody>
                <tr v-for="discount in pagedDiscounts" :key="discount.id">
                    <td>{{ discount.title }}</td>
                    <td>{{ discount.detail }}</td>
                    <td>{{ discount.dateStart }} ~ {{ discount.dateEnd }}</td>
                    <td>{{ discount.type }}</td>
                    <td>{{ discount.threshold }}</td>
                    <td>{{ discount.code }}</td>
                    <td>{{ discount.limit }}</td>
                    <td>{{ discount.limitPerUser }}</td>
                    <td>{{ discount.restaurantCondition }}</td>
                    <td>{{ discount.foodCondition }}</td>
                    <td>{{ discount.memberCondition }}</td>
                    <td>{{ discount.operation }}</td>
                    <td>
                        <a href="#">編輯</a> |
                        <a href="#">查看</a>
                    </td>
                </tr>
                <tr v-if="pagedDiscounts.length === 0">
                    <td colspan="12" class="text-center">查無資料</td>
                </tr>
            </tbody>
        </table>
        <!-- 分頁控制 -->
        <div class="d-flex justify-content-end align-items-center pagebar-wrap">
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
                <select class="form-select" v-model.number="pageSize">
                    <option v-for="s in pageSizes" :key="s" :value="s">{{ s }}/每頁</option>
                </select>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import axios from 'axios'

const discounts = ref([])
const search = ref('')
const type = ref('')

function clearFilter() {
    search.value = ''
    type.value = ''
}

const fetchDiscounts = async () => {
    try {
        const res = await axios.get('http://localhost:8080/api/discounts')
        discounts.value = res.data
    } catch (err) {
        discounts.value = []
    }
}

// 分頁
const page = ref(1)
const pageSize = ref(10)
const pageSizes = [10, 20, 30]

// 搜尋與折扣類型過濾
const filteredDiscounts = computed(() => {
    return discounts.value.filter(d =>
        (search.value === '' || d.title?.includes(search.value)) &&
        (type.value === '' || d.type === type.value)
    )
})

// 分頁後資料
const pagedDiscounts = computed(() => {
    const start = (page.value - 1) * pageSize.value
    const end = start + pageSize.value
    return filteredDiscounts.value.slice(start, end)
})

const totalPages = computed(() =>
    Math.max(1, Math.ceil(filteredDiscounts.value.length / pageSize.value))
)

onMounted(fetchDiscounts)

watch([page, pageSize], () => {
    // 換頁或換每頁數時不保留勾選
    // 無勾選欄位可省略
})

watch(pageSize, () => {
    page.value = 1
})
</script>