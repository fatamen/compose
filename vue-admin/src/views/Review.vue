<template>
    <h2>評價檢舉審核</h2>
    <div class="container mt-4">

        <!-- 書籤 Tabs -->
        <div class="bookmark-tabs mb-4">
            <button
                v-for="tab in tabs"
                :key="tab.key"
                class="bookmark-tab"
                :class="{ active: currentTab === tab.key }"
                @click="setTab(tab.key)"
            >
                <span v-html="currentTab === tab.key ? tab.iconActive : tab.icon" class="me-1"></span>
                {{ tab.text }}
                <span class="tab-count">{{ tab.count }}</span>
            </button>
            <div class="bookmark-underline"></div>
        </div>

        <!-- 搜尋與篩選 -->
        <div class="d-flex align-items-center gap-2 mb-3 flex-wrap">
            <div>搜尋：</div>
            <input v-model="keyword" placeholder="檢舉人ID..." class="form-control w-auto" style="min-width:160px;" />
            <div class="ms-2">評價ID：</div>
            <input v-model="identityKeyword" placeholder="評價ID..." class="form-control w-auto" style="min-width:120px;" />
            <button class="btn btn-primary ms-2" @click="resetFilters">清除篩選</button>
        </div>

        <!-- 全選 -->
        <div class="mb-2">
            <input type="checkbox" :checked="isAllSelected" @change="toggleAll" />
            全選
        </div>

        <!-- 表格 -->
        <table class="table table-striped table-hover review-table">
            <thead>
                <tr>
                    <th></th>
                    <th>評價ID</th>
                    <th>檢舉人ID</th>
                    <th>審核狀態</th>
                    <th>操作</th>
                </tr>
            </thead>
            <tbody>
                <tr
                    v-for="review in pagedReviews"
                    :key="review.id"
                    @click="showDetail(review)"
                    style="cursor:pointer"
                >
                    <td @click.stop>
                        <input type="checkbox" v-model="selected" :value="review.id" />
                    </td>
                    <td>{{ review.identity }}</td>
                    <td>{{ review.name }}</td>
                    <td>
                        {{ review.status }}
                        <br v-if="review.detail" />
                        <small v-if="review.detail">{{ review.detail }}</small>
                    </td>
                    <td @click.stop>
                        <span class="text-link me-3" @click="confirmReview(review.id)">確認</span>
                        <span class="text-link text-danger" @click="rejectReview(review.id)">駁回</span>
                    </td>
                </tr>
                <tr v-if="pagedReviews.length === 0">
                    <td colspan="6" class="text-center">查無資料</td>
                </tr>
            </tbody>
        </table>

        <!-- 分頁控制 -->
        <div class="pagination d-flex justify-content-end align-items-center pagebar-wrap">
            <button class="btn btn-outline-secondary me-2" :disabled="page === 1" @click="page > 1 && (page--)">&lt; 上一頁</button>
            <nav>
                <ul class="pagination mb-0">
                    <li class="page-item disabled">
                        <span class="page-link">頁數：{{ page }} / {{ totalPages }}</span>
                    </li>
                </ul>
            </nav>
            <button class="btn btn-outline-secondary ms-2" :disabled="page === totalPages" @click="page < totalPages && (page++)">下一頁 &gt;</button>
            <div class="ms-3">
                <select class="form-select" v-model.number="pageSize" style="min-width:90px;">
                    <option v-for="s in pageSizes" :key="s" :value="s">{{ s }}/每頁</option>
                </select>
            </div>
        </div>

        <!-- 彈窗 -->
        <div v-if="modalReview" class="modal-mask" @click.self="closeModal">
            <div class="modal-dialog">
                <h5>評價詳細資訊</h5>
                <table class="table table-sm">
                    <tr><th>評價ID</th><td>{{ modalReview.identity }}</td></tr>
                    <tr><th>檢舉人ID</th><td>{{ modalReview.name }}</td></tr>
                    <tr><th>狀態</th><td>{{ modalReview.status }}</td></tr>
                    <tr v-if="modalReview.detail"><th>說明</th><td>{{ modalReview.detail }}</td></tr>
                </table>
                <div class="text-end">
                    <button class="btn btn-secondary" @click="closeModal">關閉</button>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'

// 假資料（正式請用API fetch）
const reviews = ref([
    { id: 1, identity: 'r001', name: 'user123', status: '待審核', detail: '內容有疑慮' },
    { id: 2, identity: 'r002', name: 'user456', status: '已駁回', detail: '無違規' },
    { id: 3, identity: 'r003', name: 'user789', status: '已審核', detail: '' }
])

// 書籤Tab
const currentTab = ref('all')
const filteredCounts = computed(() => ({
    all: reviews.value.length,
    approved: reviews.value.filter(w => w.status.includes('已') && !w.status.includes('駁回')).length,
    pending: reviews.value.filter(w => w.status.includes('待')).length,
    rejected: reviews.value.filter(w => w.status.includes('駁回')).length,
}))
const tabs = computed(() => [
    {
        key: 'all',
        text: '全部',
        icon: `<svg width="20" height="20" style="vertical-align:-3px" fill="#888" viewBox="0 0 16 16"><path d="M2 2v13.5l6-3.25 6 3.25V2z"/></svg>`,
        iconActive: `<svg width="20" height="20" style="vertical-align:-3px" fill="#111" viewBox="0 0 16 16"><path d="M2 2v13.5l6-3.25 6 3.25V2z"/></svg>`,
        count: filteredCounts.value.all,
    },
    {
        key: 'approved',
        text: '已審核',
        icon: `<svg width="20" height="20" style="vertical-align:-3px" fill="#888" viewBox="0 0 16 16"><circle cx="8" cy="8" r="7.5" stroke="#888" fill="none"/><path d="M6.5 8.8l-1.2-1.2-.7.7 1.9 1.9 3.5-3.5-.7-.7z" fill="#888"/></svg>`,
        iconActive: `<svg width="20" height="20" style="vertical-align:-3px" fill="#111" viewBox="0 0 16 16"><circle cx="8" cy="8" r="7.5" stroke="#111" fill="none"/><path d="M6.5 8.8l-1.2-1.2-.7.7 1.9 1.9 3.5-3.5-.7-.7z" fill="#111"/></svg>`,
        count: filteredCounts.value.approved,
    },
    {
        key: 'pending',
        text: '待審核',
        icon: `<svg width="20" height="20" style="vertical-align:-3px" fill="#888" viewBox="0 0 16 16"><circle cx="8" cy="8" r="7.5" stroke="#888" fill="none"/><path d="M8 4v5h4" stroke="#888" stroke-width="1.5" fill="none"/></svg>`,
        iconActive: `<svg width="20" height="20" style="vertical-align:-3px" fill="#111" viewBox="0 0 16 16"><circle cx="8" cy="8" r="7.5" stroke="#111" fill="none"/><path d="M8 4v5h4" stroke="#111" stroke-width="1.5" fill="none"/></svg>`,
        count: filteredCounts.value.pending,
    },
    {
        key: 'rejected',
        text: '已駁回',
        icon: `<svg width="20" height="20" style="vertical-align:-3px" fill="#888" viewBox="0 0 16 16"><circle cx="8" cy="8" r="7.5" stroke="#888" fill="none"/><path d="M4 8l4 4 4-4" stroke="#888" stroke-width="1.5" fill="none"/></svg>`,
        iconActive: `<svg width="20" height="20" style="vertical-align:-3px" fill="#d12" viewBox="0 0 16 16"><circle cx="8" cy="8" r="7.5" stroke="#d12" fill="none"/><path d="M4 8l4 4 4-4" stroke="#d12" stroke-width="1.5" fill="none"/></svg>`,
        count: filteredCounts.value.rejected,
    }
])
const setTab = (key) => {
    currentTab.value = key
    page.value = 1
    selected.value = []
}

// 篩選
const keyword = ref('')
const identityKeyword = ref('')
const filteredReviews = computed(() => {
    let arr = reviews.value
    if (currentTab.value === 'pending') arr = arr.filter(w => w.status.includes('待'))
    else if (currentTab.value === 'approved') arr = arr.filter(w => w.status.includes('已') && !w.status.includes('駁回'))
    else if (currentTab.value === 'rejected') arr = arr.filter(w => w.status.includes('駁回'))
    if (keyword.value) arr = arr.filter(w => w.name.includes(keyword.value))
    if (identityKeyword.value) arr = arr.filter(w => w.identity.includes(identityKeyword.value))
    return arr
})

// 分頁
const page = ref(1)
const pageSize = ref(5)
const pageSizes = [5, 10, 20]
const pagedReviews = computed(() => {
    const start = (page.value - 1) * pageSize.value
    const end = start + pageSize.value
    return filteredReviews.value.slice(start, end)
})
const totalPages = computed(() => Math.max(1, Math.ceil(filteredReviews.value.length / pageSize.value)))

// 全選
const selected = ref([])
const isAllSelected = computed(() =>
    selected.value.length === pagedReviews.value.length && pagedReviews.value.length !== 0
)
const toggleAll = () => {
    if (isAllSelected.value) selected.value = []
    else selected.value = pagedReviews.value.map(review => review.id)
}
watch([page, pageSize], () => { selected.value = [] })
watch(pageSize, () => { page.value = 1 })

// 彈窗
const modalReview = ref(null)
const showDetail = (review) => { modalReview.value = { ...review } }
const closeModal = () => { modalReview.value = null }

// 操作
const confirmReview = (id) => { alert(`確認 ID: ${id}`) }
const rejectReview = (id) => { alert(`駁回 ID: ${id}`) }

// 清除篩選
function resetFilters() {
    keyword.value = ''
    identityKeyword.value = ''
}
</script>

<style scoped>
/* 書籤tab樣式 */
.bookmark-tabs {
    display: flex;
    align-items: flex-end;
    border-bottom: 2px solid #ffcd75;
    background: #fff;
    gap: 0.5rem;
    padding-left: 10px;
    position: relative;
    min-height: 48px;
}
.bookmark-tab {
    border: 1px solid #eee;
    background: #fff;
    border-bottom: none;
    border-radius: 6px 6px 0 0;
    margin-bottom: -2px;
    padding: 0.55rem 2rem 0.45rem 1.5rem;
    font-weight: 500;
    font-size: 1rem;
    color: #888;
    display: flex;
    align-items: center;
    transition: background .12s, color .12s;
    box-shadow: none;
    cursor: pointer;
    position: relative;
}
.bookmark-tab.active {
    background: #fff;
    border-color: #ffcd75 #ffcd75 #fff #ffcd75;
    color: #111;
    font-weight: bold;
    z-index: 2;
}
.bookmark-tab:not(.active):hover {
    background: #faf6ef;
    color: #222;
}
.bookmark-underline {
    position: absolute;
    left: 0; right: 0; bottom: -2px;
    height: 2px;
    background: #ffcd75;
    z-index: 1;
}
.tab-count {
    display: inline-block;
    margin-left: 8px;
    background: #f5ba3a11;
    color: #d48806;
    font-size: .98em;
    padding: 0 8px;
    border-radius: 10px;
    min-width: 32px;
    text-align: center;
}
/* 彈窗 */
.modal-mask {
    position: fixed; left:0; top:0; width:100vw; height:100vh;
    background: rgba(0,0,0,.18);
    z-index: 1000;
    display: flex; align-items: center; justify-content: center;
}
.modal-dialog {
    background: #fff;
    border-radius: 10px;
    padding: 2rem 2rem 1rem 2rem;
    min-width: 320px;
    max-width: 95vw;
    box-shadow: 0 8px 32px rgba(0,0,0,.16);
    animation: modalPop .2s;
}
@keyframes modalPop {
    0% { transform: scale(.85);}
    100% { transform: scale(1);}
}
/* 表格 hover 行效果 */
.review-table tbody tr:hover {
    background: #f8f9fa !important;
}
</style>
