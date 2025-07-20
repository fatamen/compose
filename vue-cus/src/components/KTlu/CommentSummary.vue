<template>
    <div class="comment-summary-container goldenbowl-restaurant-theme">
        <h4 class="summary-title">顧客評價</h4>
        <div class="summary-content">
            <div class="overall-score">
                <span class="score-value">{{ averageScore.toFixed(1) }}</span>
                <div class="stars">
                    <i v-for="n in 5" :key="n"
                        :class="['bi', n <= Math.round(averageScore) ? 'bi-star-fill' : 'bi-star']"></i>
                </div>
                <!-- <span class="review-count">評論數 ({{ totalReviews }})</span> -->
                <span class="review-count" @click="openComment()" style="cursor: pointer;">
                    ({{ totalReviews }} 則評論)
                </span>
            </div>
            <div class="score-distribution">
                <div v-for="score in 5" :key="score" class="score-row">
                    <span class="score-label">{{ score }}星</span>
                    <div class="progress-bar-container">
                        <div class="progress-bar" :style="{ width: getScorePercentage(score) + '%' }"></div>
                    </div>
                    <span class="score-count">{{ scoreCounts[score] || 0 }}</span>
                </div>
            </div>
        </div>
    </div>

    <!-- 新增：評論模態框 -->
    <Comment v-if="showComment" :storeId="selectedStoreId" @close="closeComment" />
</template>

<script setup>
import { computed, ref } from 'vue'
import { useUserStore } from '@/stores/user';
import Comment from '@/components/Jimmy/Comment.vue';

const userStore = useUserStore();
const emit = defineEmits(['openComment'])

const props = defineProps({
    comments: {
        type: Array,
        default: () => []
    },
    restaurant: { // 這個 props 現在接收的是 Home.vue 經過所有篩選和模式選擇後的結果
        type: Object,
        required: true,
        default: () => { },
    },
})

// 新增：控制評論模態框顯示的狀態
const showComment = ref(false);
const selectedStoreId = ref(null); // 用於儲存當前點擊的餐廳 ID

const openComment = (storeId) => {
    console.log("User:" + userStore.userId);
    console.log("Opening comments for store:", storeId);

    // 如果沒有傳入 storeId，嘗試從 restaurant 物件獲取
    let finalStoreId = storeId;
    if (!finalStoreId) {
        finalStoreId = getRestaurantId();
    }

    if (!finalStoreId) {
        console.warn("No store ID provided for opening comments");
        console.warn("Available data:", {
            restaurant: props.restaurant,
            comments: props.comments
        });
        return;
    }

    // 確保 storeId 是數字類型
    finalStoreId = parseInt(finalStoreId);
    if (isNaN(finalStoreId)) {
        console.warn("Invalid store ID:", storeId);
        return;
    }

    // 設定選中的餐廳 ID 並顯示評論模態框
    selectedStoreId.value = finalStoreId;
    showComment.value = true;

    console.log("Comment modal state:", {
        showComment: showComment.value,
        selectedStoreId: selectedStoreId.value
    });

    // 同時發射事件給父組件（保持向後兼容）
    emit('openComment', finalStoreId);
};

// 關閉評論模態框
const closeComment = () => {
    console.log("Closing comment modal");
    showComment.value = false;
    selectedStoreId.value = null;
};










// 計算平均分數
const averageScore = computed(() => {
    const comments = getComments()
    if (comments.length === 0) return 0
    const total = comments.reduce((sum, comment) => sum + (comment.score || 0), 0)
    return total / comments.length
})

// 計算總評論數
const totalReviews = computed(() => getComments().length)

// 計算各星級評論數量
const scoreCounts = computed(() => {
    const counts = { 1: 0, 2: 0, 3: 0, 4: 0, 5: 0 }
    const comments = getComments()
    comments.forEach(comment => {
        if (comment.score >= 1 && comment.score <= 5) {
            counts[comment.score]++
        }
    })
    return counts
})

// 獲取評論資料的統一方法
const getComments = () => {
    // 優先使用 props.comments
    if (props.comments && props.comments.length > 0) {
        return props.comments
    }

    // 如果 props.comments 為空，嘗試從 restaurant 物件中獲取
    if (props.restaurant && props.restaurant.comments) {
        return props.restaurant.comments
    }

    // 如果都沒有，返回空陣列
    return []
}

// 獲取餐廳 ID
const getRestaurantId = () => {
    console.log("Getting restaurant ID from:", props.restaurant);

    // 檢查 restaurant 物件是否存在
    if (!props.restaurant) {
        console.warn("Restaurant object is null or undefined");
        return null;
    }

    // 檢查所有可能的 ID 欄位
    console.log("Available restaurant properties:", Object.keys(props.restaurant));

    if (props.restaurant.id) {
        console.log("Using restaurant.id:", props.restaurant.id);
        return props.restaurant.id;
    }

    if (props.restaurant.storeId) {
        console.log("Using restaurant.storeId:", props.restaurant.storeId);
        return props.restaurant.storeId;
    }

    if (props.restaurant.store_id) {
        console.log("Using restaurant.store_id:", props.restaurant.store_id);
        return props.restaurant.store_id;
    }

    // 如果都沒有，嘗試從 comments 中獲取 storeId
    if (props.comments && props.comments.length > 0 && props.comments[0].storeId) {
        console.log("Using comment.storeId:", props.comments[0].storeId);
        return props.comments[0].storeId;
    }

    console.warn("No restaurant ID found in any expected field");
    console.warn("Restaurant object:", props.restaurant);
    return null;
}

const getScorePercentage = (score) => {
    if (totalReviews.value === 0) return 0
    return (scoreCounts.value[score] / totalReviews.value) * 100
}
</script>

<style scoped>
.comment-summary-container {
    width: 100%;
    padding: 1.5rem;
    background: var(--restaurant-bg-light);
    border: 1px solid var(--restaurant-border-light);
    border-radius: 12px;
    box-shadow: 0 4px 12px var(--restaurant-shadow-light);
    transition: all 0.3s ease;
}

.comment-summary-container:hover {
    box-shadow: 0 6px 20px var(--restaurant-shadow-light);
    transform: translateY(-2px);
}

.summary-title {
    color: var(--restaurant-primary);
    font-size: 1.25rem;
    font-weight: 600;
    margin-bottom: 1rem;
    text-shadow: 0 1px 2px var(--restaurant-shadow-light);
    position: relative;
    padding-bottom: 0.5rem;
}

.summary-title::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 0;
    width: 40px;
    height: 2px;
    background: var(--restaurant-primary);
    border-radius: 1px;
}

.summary-content {
    display: flex;
    flex-direction: column;
    gap: 1rem;
}

.overall-score {
    display: flex;
    align-items: center;
    gap: 1rem;
    padding: 1rem;
    background: var(--restaurant-bg-primary);
    border-radius: 8px;
    border: 1px solid var(--restaurant-border-light);
}

.score-value {
    font-size: 2rem;
    font-weight: 700;
    color: var(--restaurant-primary);
    min-width: 60px;
}

.stars {
    display: flex;
    gap: 0.25rem;
}

.stars .bi {
    font-size: 1.2rem;
    color: var(--restaurant-primary);
}

.stars .bi-star-fill {
    color: var(--restaurant-primary);
}

.stars .bi-star {
    color: var(--restaurant-border-light);
}

.review-count {
    color: #007bff;
    /* 藍色鏈接 */
    font-size: 0.9rem;
    font-weight: 500;
    margin-left: auto;
    text-decoration: underline;
    cursor: pointer;
    transition: color 0.2s ease;
}

.review-count:hover {
    color: #0056b3;
}

.score-distribution {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
}

.score-row {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    padding: 0.5rem;
    background: var(--restaurant-bg-primary);
    border-radius: 6px;
    border: 1px solid var(--restaurant-border-light);
    transition: all 0.2s ease;
}

.score-row:hover {
    background: var(--restaurant-bg-accent);
    border-color: var(--restaurant-primary-light);
}

.score-label {
    color: var(--restaurant-text-primary);
    font-size: 0.85rem;
    font-weight: 500;
    min-width: 40px;
}

.progress-bar-container {
    flex: 1;
    height: 8px;
    background: var(--restaurant-border-light);
    border-radius: 4px;
    overflow: hidden;
}

.progress-bar {
    height: 100%;
    background: var(--restaurant-primary);
    border-radius: 4px;
    transition: width 0.3s ease;
}

.score-count {
    color: var(--restaurant-text-secondary);
    font-size: 0.8rem;
    font-weight: 600;
    min-width: 30px;
    text-align: right;
}

/* 響應式設計 */
@media (max-width: 768px) {
    .comment-summary {
        padding: 1rem;
    }

    .overall-score {
        flex-direction: column;
        gap: 0.75rem;
        text-align: center;
    }

    .score-value {
        font-size: 1.75rem;
    }

    .review-count {
        margin-left: 0;
    }

    .score-row {
        padding: 0.75rem;
    }

    .score-label {
        font-size: 0.8rem;
        min-width: 35px;
    }

    .score-count {
        font-size: 0.75rem;
        min-width: 25px;
    }
}

@media (max-width: 480px) {
    .comment-summary {
        padding: 0.75rem;
    }

    .summary-title {
        font-size: 1.1rem;
    }

    .score-value {
        font-size: 1.5rem;
    }

    .stars .bi {
        font-size: 1rem;
    }
}
</style>