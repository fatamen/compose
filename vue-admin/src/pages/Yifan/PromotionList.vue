<template>
        <h2>優惠活動</h2>
        <!-- 黑色 Overlay -->
        <div v-if="sidebarOpen" class="overlay" @click="toggleSidebar"></div>

        <!-- Main Content -->
        <main class="admin-main p-4">


        <!-- 表格卡片 -->
        <div class="table-card mb-4">
            <!-- Filter Bar -->


            <div class="filter-bar mb-4 d-flex flex-wrap align-items-center gap-2">
            <div>搜尋：</div><input v-model="keyword" placeholder="搜尋活動標題..." class="form-control w-auto" />折扣類型：
            <select v-model="selectedType" class="form-select" style="width: 150px;">

                <option value="">全部</option>
                <option value="percentage">百分比</option>
                <option value="fixed">金額</option>
            </select>
            <button class="btn btn-primary" @click="resetFilters">清除篩選</button>
            </div>
            <table class="table table-striped table-hover promotion-table">
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
                <tr v-for="promotion in paginatedPromotions" :key="promotion.promotion_id">
                    <td>{{ promotion.title }}</td>
                    <td>{{ promotion.description }}</td>
                    <td>{{ promotion.start_time }} ~ {{ promotion.end_time }}</td>
                    <td>{{ promotion.discount_type }}</td>
                    <td>{{ promotion.min_spend }}</td>
                    <td>{{ promotion.code }}</td>
                    <td>{{ promotion.max_usage }}</td>
                    <td>{{ promotion.user_usage_limit }}</td>
                    <td>{{ promotion.restaurant_ids.length > 0 ? promotion.restaurant_ids.join(', ') : '無' }}</td>
                    <td>{{ promotion.food_category_ids.length > 0 ? promotion.food_category_ids.join(', ') : '無' }}</td>
                    <td>{{ promotion.plan_ids.length > 0 ? promotion.plan_ids.join(', ') : '無' }}</td>
                    <td>
                    <button class="btn btn-sm btn-edit me-1">修改</button>
                    <button class="btn btn-sm btn-delete" @click="deletePromotion(promotion.promotion_id)">刪除</button>
                    </td>
                </tr>

            <!-- 新增列 -->
                <tr>
                <td><input v-model="newPromotion.title" placeholder="標題" class="form-control form-control-sm" /></td>
                <td><input v-model="newPromotion.description" placeholder="內容" class="form-control form-control-sm" /></td>
                <td>
                    <input type="datetime-local" v-model="newPromotion.start_time" class="form-control form-control-sm mb-1" />
                    <input type="datetime-local" v-model="newPromotion.end_time" class="form-control form-control-sm" />
                </td>
                <td><input v-model="newPromotion.discount_type" placeholder="類型" class="form-control form-control-sm" /></td>
                <td><input type="number" v-model="newPromotion.min_spend" placeholder="門檻" class="form-control form-control-sm" /></td>
                <td><input v-model="newPromotion.code" placeholder="優惠碼" class="form-control form-control-sm" /></td>
                <td><input type="number" v-model="newPromotion.max_usage" placeholder="上限" class="form-control form-control-sm" /></td>
                <td><input type="number" v-model="newPromotion.user_usage_limit" placeholder="每人上限" class="form-control form-control-sm" /></td>

                <td>
                    <label><input type="checkbox" v-model="newPromotion.bindRestaurant" /> 餐廳</label>
                    <input v-if="newPromotion.bindRestaurant" v-model="newPromotion.restaurant_ids_input" placeholder="ID, 逗號分隔" class="form-control form-control-sm mt-1" />
                </td>
                <td>
                    <label><input type="checkbox" v-model="newPromotion.bindFood" /> 食物</label>
                    <input v-if="newPromotion.bindFood" v-model="newPromotion.food_category_ids_input" placeholder="ID, 逗號分隔" class="form-control form-control-sm mt-1" />
                </td>
                <td>
                    <label><input type="checkbox" v-model="newPromotion.bindPlan" /> 會員</label>
                    <input v-if="newPromotion.bindPlan" v-model="newPromotion.plan_ids_input" placeholder="ID, 逗號分隔" class="form-control form-control-sm mt-1" />
                </td>
                <td>—</td>
            </tr>
        </tbody>
    </table>
        <!-- 新增 / 取消 -->
        <div class="action-buttons mb-4">
            <button class="btn btn-add">➕ 新增</button>
            <button class="btn btn-cancel" @click="resetNewPromotion">✖️ 取消</button>
        </div>
    </div>


<!-- 分頁區塊（訂閱方案版式） -->
<div class="pagination d-flex justify-content-end align-items-center pagebar-wrap">
    <button class="btn btn-outline-secondary me-2"
        :disabled="currentPage === 1"
        @click="currentPage > 1 && (currentPage--)"
    >&lt; 上一頁</button>
    <nav>
        <ul class="pagination mb-0">
            <li class="page-item disabled">
                <span class="page-link">頁數：{{ currentPage }} / {{ totalPages }}</span>
            </li>
        </ul>
    </nav>
    <button class="btn btn-outline-secondary ms-2"
        :disabled="currentPage === totalPages"
        @click="currentPage < totalPages && (currentPage++)"
    >下一頁 &gt;</button>
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

const promotions = ref([])
const sidebarOpen = ref(false)
const currentPage = ref(1)
const itemsPerPage = ref(5)
const keyword = ref('')
const selectedType = ref('')

const newPromotion = ref({
    title: '', description: '', start_time: '', end_time: '',
    discount_type: '', min_spend: 0, code: '', max_usage: 0, user_usage_limit: 0,
    bindRestaurant: false, restaurant_ids_input: '',
    bindFood: false, food_category_ids_input: '',
    bindPlan: false, plan_ids_input: '',
})

const toggleSidebar = () => {
    sidebarOpen.value = !sidebarOpen.value
}

const filteredPromotions = computed(() => {
    return promotions.value.filter(p =>
        p.title.toLowerCase().includes(keyword.value.toLowerCase()) &&
        (selectedType.value ? p.discount_type === selectedType.value : true)
    )
})

const paginatedPromotions = computed(() => {
    const start = (currentPage.value - 1) * itemsPerPage.value
    return filteredPromotions.value.slice(start, start + itemsPerPage.value)
})

const totalPages = computed(() => Math.ceil(filteredPromotions.value.length / itemsPerPage.value))

const fetchPromotions = () => {
    promotions.value = [
        { promotion_id: 1, title: '週年慶9折', description: '全店9折', start_time: '2025-06-01', end_time: '2025-06-30', discount_type: 'percentage', min_spend: 0, code: 'ANNI2025', max_usage: 1000, user_usage_limit: 1, restaurant_ids: ['1'], food_category_ids: [], plan_ids: [] },
        { promotion_id: 2, title: '拉麵活動', description: '滿500折100', start_time: '2025-06-12', end_time: '2025-06-18', discount_type: 'fixed', min_spend: 500, code: 'RAMEN100', max_usage: 200, user_usage_limit: 1, restaurant_ids: [], food_category_ids: ['2'], plan_ids: [] },
        { promotion_id: 3, title: '銀會員禮', description: '每月送 2 張 60 折價券', start_time: '2025-06-01', end_time: '2025-06-30', discount_type: 'fixed', min_spend: 0, code: 'SILVER60', max_usage: null, user_usage_limit: 2, restaurant_ids: [], food_category_ids: [], plan_ids: ['2'] },
    ]
}

const createPromotion = () => {
    const newId = promotions.value.length ? promotions.value[promotions.value.length - 1].promotion_id + 1 : 1
    const promotion = {
        promotion_id: newId, ...newPromotion.value,
        restaurant_ids: newPromotion.value.bindRestaurant ? newPromotion.value.restaurant_ids_input.split(',').map(id => id.trim()).filter(id => id) : [],
        food_category_ids: newPromotion.value.bindFood ? newPromotion.value.food_category_ids_input.split(',').map(id => id.trim()).filter(id => id) : [],
        plan_ids: newPromotion.value.bindPlan ? newPromotion.value.plan_ids_input.split(',').map(id => id.trim()).filter(id => id) : [],
    }
    promotions.value.push(promotion)
    resetNewPromotion()
}

const resetNewPromotion = () => {
    newPromotion.value = { title: '', description: '', start_time: '', end_time: '', discount_type: '', min_spend: 0, code: '', max_usage: 0, user_usage_limit: 0, bindRestaurant: false, restaurant_ids_input: '', bindFood: false, food_category_ids_input: '', bindPlan: false, plan_ids_input: '' }
}

const deletePromotion = (id) => promotions.value = promotions.value.filter(p => p.promotion_id !== id)
const resetFilters = () => { keyword.value = ''; selectedType.value = '' }
const prevPage = () => { if (currentPage.value > 1) currentPage.value-- }
const nextPage = () => { if (currentPage.value < totalPages.value) currentPage.value++ }

onMounted(fetchPromotions)
</script>

<style>
/* 這裡可以移除，只保留公共 admin-style.css 即可 */
</style>
