<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'

const route = useRoute()
const router = useRouter()
const subscriptions = ref([])

const fetchSubscriptions = async () => {
    try {
        const res = await axios.get('http://localhost:8080/api/subscriptions')
        subscriptions.value = res.data
    } catch (err) {
        subscriptions.value = []
    }
}

const selectAll = ref(false)
const selected = ref([])

const currentFilter = ref(route.query.filter || 'all')

// ----------- 分頁相關 -----------
const page = ref(1)
const pageSize = ref(10)
const pageSizes = [10, 20, 30]

const filteredSubscriptions = computed(() => {
    let result = subscriptions.value
    if (currentFilter.value === 'pending') {
        result = result.filter(r => r.status.includes('待'))
    } else if (currentFilter.value === 'approved') {
        result = result.filter(r => r.status.includes('已'))
    }
    return result
})

// 分頁後的資料
const pagedSubscriptions = computed(() => {
    const start = (page.value - 1) * pageSize.value
    const end = start + pageSize.value
    return filteredSubscriptions.value.slice(start, end)
})

const totalPages = computed(() =>
    Math.max(1, Math.ceil(filteredSubscriptions.value.length / pageSize.value))
)

watch(() => route.query.filter, (val) => {
    if (val === 'pending' || val === 'approved' || val === 'all') {
        currentFilter.value = val
    } else {
        currentFilter.value = 'all'
    }
    page.value = 1 // 切換 filter 時回到第1頁
})

onMounted(() => {
    fetchSubscriptions()
    if (!route.query.filter) {
        router.replace({ query: { filter: 'all' } })
        currentFilter.value = 'all'
    }
})

const setFilter = (filter) => {
    if (route.query.filter === filter) return
    router.replace({ query: { filter } })
    selected.value = []
    page.value = 1
}

const toggleAll = () => {
    if (isAllSelected.value) {
        selected.value = []
    } else {
        selected.value = pagedSubscriptions.value.map(subscription => subscription.id)
    }
}
const isAllSelected = computed(() =>
    selected.value.length === pagedSubscriptions.value.length && pagedSubscriptions.value.length !== 0
)

const batchApprove = () => {
    alert('批次通過: ' + selected.value.join(', '))
    selected.value = []
}
const batchReject = () => {
    alert('批次駁回: ' + selected.value.join(', '))
    selected.value = []
}

const countAll = computed(() => subscriptions.value.length)
const countPending = computed(() => subscriptions.value.filter(s => s.status.includes('待')).length)
const countApproved = computed(() => subscriptions.value.filter(s => s.status.includes('已')).length)

watch(selected, () => {
    selectAll.value = selected.value.length === pagedSubscriptions.value.length && pagedSubscriptions.value.length !== 0
})
watch([page, pageSize], () => {
    // 換頁或換每頁數時要自動清空全選與選取
    selected.value = []
    selectAll.value = false
})

// 換每頁數量時，頁碼也回到第1頁
watch(pageSize, () => {
    page.value = 1
})

</script>

<template>
    <div class="container mt-4">
        <h2>訂閱方案</h2>
        <!-- 狀態選單 -->
        <div class="d-flex mb-3 border-bottom pb-2">
            <button class="btn btn-link"
                :class="{ 'tab-active': currentFilter === 'all' }"
                @click="setFilter('all')"
            >全部 ({{ countAll }})</button>
            <button class="btn btn-link"
                :class="{ 'tab-active': currentFilter === 'pending' }"
                @click="setFilter('pending')"
            >待審核 ({{ countPending }})</button>
            <button class="btn btn-link"
                :class="{ 'tab-active': currentFilter === 'approved' }"
                @click="setFilter('approved')"
            >已審核 ({{ countApproved }})</button>
        </div>

        <div class="mb-2">
            <input type="checkbox" :checked="isAllSelected" @change="toggleAll"> 全部勾選
            <button class="btn btn-primary me-2" @click="batchApprove">批次通過</button>
            <button class="btn btn-danger" @click="batchReject">批次駁回</button>
        </div>

        <table class="table table-bordered">
            <thead>
                <tr>
                    <th></th>
                    <th>訂閱方案</th>
                    <th>訂閱期限</th>
                    <th>費用</th>
                    <th>操作</th>
                </tr>
            </thead>
            <tbody>
                <tr v-for="subscription in pagedSubscriptions" :key="subscription.id">
                    <td><input type="checkbox" v-model="selected" :value="subscription.id"></td>
                    <td>{{ subscription.plan }}</td>
                    <td>{{ subscription.deadline }}</td>
                    <td>{{ subscription.price }}</td>
                    <td><br v-if="subscription.detail">
                        <small v-if="subscription.detail">{{ subscription.detail }}</small>
                    </td>
                    <td>
                        <a href="#">編輯</a> |
                        <a href="#">查看</a>
                    </td>
                </tr>
            </tbody>
        </table>
        <!-- 分頁控制 -->
        <div class="d-flex justify-content-end align-items-center pagebar-wrap">
            <nav>
                <ul class="pagination mb-0">
                    <li class="page-item" :class="{ disabled: page === 1 }">
                        <button class="page-link" @click="page > 1 && (page--)" :disabled="page === 1">‹</button>
                    </li>
                    <li class="page-item disabled"><span class="page-link">{{ page }}</span></li>
                    <li class="page-item" :class="{ disabled: page === totalPages }">
                        <button class="page-link" @click="page < totalPages && (page++)" :disabled="page === totalPages">›</button>
                    </li>
                </ul>
            </nav>
            <div class="ms-3">
                <select class="form-select" v-model.number="itemsPerPage" style="width:120px; min-width: 90px;">
                    <option v-for="s in [5, 10, 20]" :key="s" :value="s">{{ s }}/每頁</option>
                </select>
            </div>
        </div>
    </div>
</template>