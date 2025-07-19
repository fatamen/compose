<template>
    <div class="comment-summary goldenbowl-restaurant-theme">
        <h4 class="summary-title">顧客評價</h4>
        <div class="summary-content">
            <div class="overall-score">
                <span class="score-value">{{ averageScore.toFixed(1) }}</span>
                <div class="stars">
                    <i v-for="n in 5" :key="n"
                        :class="['bi', n <= Math.round(averageScore) ? 'bi-star-fill' : 'bi-star']"></i>
                </div>
                <span class="review-count">評論數 ({{ totalReviews }})</span>
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
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
    comments: {
        type: Array,
        default: () => []
    }
})

const averageScore = computed(() => {
    if (props.comments.length === 0) return 0
    const total = props.comments.reduce((sum, comment) => sum + (comment.score || 0), 0)
    return total / props.comments.length
})

const totalReviews = computed(() => props.comments.length)

const scoreCounts = computed(() => {
    const counts = { 1: 0, 2: 0, 3: 0, 4: 0, 5: 0 }
    props.comments.forEach(comment => {
        if (comment.score >= 1 && comment.score <= 5) {
            counts[comment.score]++
        }
    })
    return counts
})

const getScorePercentage = (score) => {
    if (totalReviews.value === 0) return 0
    return (scoreCounts.value[score] / totalReviews.value) * 100
}
</script>

<style scoped>
.comment-summary {
    width: 100%;
    padding: 1.5rem;
    background: var(--restaurant-bg-light);
    border: 1px solid var(--restaurant-border-light);
    border-radius: 12px;
    box-shadow: 0 4px 12px var(--restaurant-shadow-light);
    transition: all 0.3s ease;
}

.comment-summary:hover {
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
    color: var(--restaurant-text-secondary);
    font-size: 0.9rem;
    font-weight: 500;
    margin-left: auto;
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