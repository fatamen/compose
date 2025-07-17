<template>
    <div class="pt-4">
        <h4 class="mb-4 fw-bold">待辦事項清單</h4>
        <div class="row justify-content-start gap-3" style="max-width: 90vw;">
            <div class="col-auto card-item text-center clickable" @click="goToAccount('pending')">
                <h2 class="number text-primary mb-1">0</h2>
                <div class="item-title">待帳號審核</div>
            </div>
            <div class="col-auto card-item text-center clickable" @click="goToAccount('approved')">
                <h2 class="number text-primary mb-1">0</h2>
                <div class="item-title">已帳號審核</div>
            </div>
            <div class="col-auto card-item text-center">
                <h2 class="number text-primary mb-1">0</h2>
                <div class="item-title">待餐廳審核</div>
            </div>
            <div class="col-auto card-item text-center">
                <h2 class="number text-primary mb-1">0</h2>
                <div class="item-title">已餐廳審核</div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
const router = useRouter()

const goToAccount = (filter) => {
    router.push({ path: '/account', query: { filter } })
}
</script>

<style scoped>

</style>