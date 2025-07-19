<template>
    <div class="restaurant-banner goldenbowl-restaurant-theme">
        <!-- 🔥 新增：輪播容器 -->
        <div class="carousel-container" v-if="bannerImages.length > 1">
            <!-- 圖片輪播 -->
            <div class="carousel-wrapper">
                <img v-for="(image, index) in bannerImages" :key="index" :src="image" :alt="restaurant.name + '店面圖片'"
                    class="banner-image carousel-image" :class="{ active: index === currentImageIndex }"
                    @error="handleImageError" />
            </div>

            <!-- 🔥 左右切換按鈕 -->
            <button class="carousel-btn carousel-btn-prev" @click="previousImage" v-if="bannerImages.length > 1">
                ‹
            </button>
            <button class="carousel-btn carousel-btn-next" @click="nextImage" v-if="bannerImages.length > 1">
                ›
            </button>

            <!-- 🔥 圓點指示器 -->
            <div class="carousel-dots" v-if="bannerImages.length > 1">
                <span v-for="(image, index) in bannerImages" :key="index" class="carousel-dot"
                    :class="{ active: index === currentImageIndex }" @click="goToImage(index)"></span>
            </div>

            <!-- 🔥 圖片數量顯示 -->
            <div class="image-counter" v-if="bannerImages.length > 1">
                {{ currentImageIndex + 1 }} / {{ bannerImages.length }}
            </div>
        </div>

        <!-- 🔥 單張圖片顯示 -->
        <img v-else :src="bannerImages[0] || defaultPhoto" :alt="restaurant.name + '店面圖片'" class="banner-image"
            @error="handleImageError" />
    </div>
</template>

<script setup>
import '@/assets/css/restaurant-theme.css'
import { useImageUrl } from '../../composables/useImageUrl.js'
import { ref, computed, onMounted, onUnmounted } from 'vue'

// 🔥 新增：使用圖片處理邏輯
const { getImageUrl, defaultPhoto } = useImageUrl();

// 🔥 新增：輪播相關的響應式變數
const currentImageIndex = ref(0);
const autoSlideInterval = ref(null);

const props = defineProps({
    restaurant: {
        type: Object,
        required: true
    }
})

// 🔥 新增：處理餐廳圖片的函數
const bannerImages = computed(() => {
    if (!props.restaurant.photo) {
        return [defaultPhoto];
    }

    // 如果有多張圖片（分號分隔）
    if (typeof props.restaurant.photo === 'string' && props.restaurant.photo.includes(';')) {
        return props.restaurant.photo.split(';')
            .filter(path => path.trim())
            .map(path => getImageUrl(path.trim()));
    }

    // 單張圖片
    return [getImageUrl(props.restaurant.photo)];
});

// 🔥 新增：輪播控制函數
const nextImage = () => {
    currentImageIndex.value = (currentImageIndex.value + 1) % bannerImages.value.length;
};

const previousImage = () => {
    currentImageIndex.value = currentImageIndex.value === 0
        ? bannerImages.value.length - 1
        : currentImageIndex.value - 1;
};

const goToImage = (index) => {
    currentImageIndex.value = index;
};

// 🔥 新增：自動輪播
const startAutoSlide = () => {
    if (bannerImages.value.length > 1) {
        autoSlideInterval.value = setInterval(() => {
            nextImage();
        }, 5000); // 每5秒切換一次
    }
};

const stopAutoSlide = () => {
    if (autoSlideInterval.value) {
        clearInterval(autoSlideInterval.value);
        autoSlideInterval.value = null;
    }
};

// 🔥 新增：圖片載入錯誤處理
const handleImageError = (event) => {
    console.warn('餐廳 Banner 圖片載入失敗，使用預設圖片:', event.target.src);
    event.target.src = defaultPhoto;
};

// 🔥 生命週期管理
onMounted(() => {
    startAutoSlide();
});

onUnmounted(() => {
    stopAutoSlide();
});
</script>

<style scoped>
.restaurant-banner {
    position: relative;
    width: 100%;
    height: 400px;
    margin: 0 0 0 0;
    overflow: hidden;
    border-bottom: 3px solid var(--restaurant-primary);
    box-shadow: 0 4px 12px var(--restaurant-shadow-light);
}

/* 🔥 新增：輪播容器樣式 */
.carousel-container {
    position: relative;
    width: 100%;
    height: 100%;
}

.carousel-wrapper {
    position: relative;
    width: 100%;
    height: 100%;
}

.banner-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block;
    transition: transform 0.3s ease;
}

.banner-image:hover {
    transform: scale(1.02);
}

/* 🔥 新增：輪播圖片樣式 */
.carousel-image {
    position: absolute;
    top: 0;
    left: 0;
    opacity: 0;
    transition: opacity 0.5s ease-in-out;
}

.carousel-image.active {
    opacity: 1;
}

/* 🔥 新增：切換按鈕樣式 */
.carousel-btn {
    position: absolute;
    top: 50%;
    transform: translateY(-50%);
    background: rgba(0, 0, 0, 0.5);
    color: white;
    border: none;
    font-size: 24px;
    width: 40px;
    height: 40px;
    border-radius: 50%;
    cursor: pointer;
    z-index: 10;
    transition: background 0.3s ease;
}

.carousel-btn:hover {
    background: rgba(0, 0, 0, 0.7);
}

.carousel-btn-prev {
    left: 15px;
}

.carousel-btn-next {
    right: 15px;
}

/* 🔥 新增：圓點指示器樣式 */
.carousel-dots {
    position: absolute;
    bottom: 20px;
    left: 50%;
    transform: translateX(-50%);
    display: flex;
    gap: 8px;
    z-index: 10;
}

.carousel-dot {
    width: 10px;
    height: 10px;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.5);
    cursor: pointer;
    transition: background 0.3s ease;
}

.carousel-dot.active {
    background: white;
}

.carousel-dot:hover {
    background: rgba(255, 255, 255, 0.8);
}

/* 🔥 新增：圖片計數器樣式 */
.image-counter {
    position: absolute;
    top: 15px;
    right: 15px;
    background: rgba(0, 0, 0, 0.6);
    color: white;
    padding: 4px 8px;
    border-radius: 12px;
    font-size: 12px;
    font-weight: bold;
    z-index: 10;
}

/* 響應式設計 */
@media (max-width: 768px) {
    .restaurant-banner {
        height: 300px;
        width: 100%;
    }
}

@media (max-width: 480px) {
    .restaurant-banner {
        height: 250px;
        width: 100%;
    }
}
</style>