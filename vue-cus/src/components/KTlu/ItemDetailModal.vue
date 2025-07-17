<template>
    <div v-if="show" class="modal-overlay goldenbowl-restaurant-theme" @click="closeModal">
        <div class="modal-content" @click.stop>
            <div class="modal-header">
                <h4 class="modal-title">商品詳情</h4>
                <button class="close-btn" @click="closeModal">×</button>
            </div>

            <div class="modal-body">
                <div class="item-image">
                    <img :src="item.imgResource" :alt="item.name" />

                </div>

                <div class="item-details">
                    <h3 class="item-name">{{ item.name }}</h3>
                    <p class="item-description">{{ item.description }}</p>

                    <div class="price-section">
                        <span v-if="item.originalPrice && item.originalPrice !== item.discountPrice"
                            class="original-price">NT${{ item.originalPrice }}</span>
                        <span class="current-price">NT${{ item.discountPrice || item.price }}</span>
                    </div>

                    <!-- 冰量選項 -->
                    <div v-if="hasIceOptions" class="option-group ice-options">
                        <div class="option-header">
                            <h6 class="option-title">冰量</h6>
                            <span class="option-required">必選</span>
                        </div>
                        <div class="option-items">
                            <label v-for="iceOption in iceOptions" :key="iceOption.value" class="option-item">
                                <input type="radio" :name="'iceLevel'" :value="iceOption.value"
                                    v-model="selectedIceLevel" />
                                <span class="option-label">
                                    <span class="option-name">{{ iceOption.name }}</span>
                                    <span v-if="iceOption.badge" class="option-badge">{{ iceOption.badge }}</span>
                                </span>
                                <span class="option-price">NT${{ iceOption.price }}</span>
                            </label>
                        </div>
                    </div>

                    <!-- 甜度選項 -->
                    <div v-if="hasSweetnessOptions" class="option-group sweetness-options">
                        <div class="option-header">
                            <h6 class="option-title">甜度</h6>
                            <span class="option-required">必選</span>
                        </div>
                        <div class="option-items">
                            <label v-for="sweetnessOption in sweetnessOptions" :key="sweetnessOption.value"
                                class="option-item">
                                <input type="radio" :name="'sweetnessLevel'" :value="sweetnessOption.value"
                                    v-model="selectedSweetnessLevel" />
                                <span class="option-label">
                                    <span class="option-name">{{ sweetnessOption.name }}</span>
                                    <span v-if="sweetnessOption.badge" class="option-badge">{{ sweetnessOption.badge
                                    }}</span>
                                </span>
                                <span class="option-price">NT${{ sweetnessOption.price }}</span>
                            </label>
                        </div>
                    </div>

                    <!-- 尺寸選項 -->
                    <div v-if="hasSizeOptions" class="option-group size-options">
                        <div class="option-header">
                            <h6 class="option-title">尺寸</h6>
                            <span class="option-required">必選</span>
                        </div>
                        <div class="option-items">
                            <label v-for="sizeOption in sizeOptions" :key="sizeOption.value" class="option-item">
                                <input type="radio" :name="'sizeLevel'" :value="sizeOption.value"
                                    v-model="selectedSizeLevel" />
                                <span class="option-label">
                                    <span class="option-name">{{ sizeOption.name }}</span>
                                    <span v-if="sizeOption.badge" class="option-badge">{{ sizeOption.badge }}</span>
                                </span>
                                <span class="option-price">NT${{ sizeOption.price }}</span>
                            </label>
                        </div>
                    </div>

                    <!-- 溫度選項 -->
                    <div v-if="hasTemperatureOptions" class="option-group temperature-options">
                        <div class="option-header">
                            <h6 class="option-title">溫度</h6>
                            <span class="option-required">必選</span>
                        </div>
                        <div class="option-items">
                            <label v-for="tempOption in temperatureOptions" :key="tempOption.value" class="option-item">
                                <input type="radio" :name="'temperatureLevel'" :value="tempOption.value"
                                    v-model="selectedTemperatureLevel" />
                                <span class="option-label">
                                    <span class="option-name">{{ tempOption.name }}</span>
                                    <span v-if="tempOption.badge" class="option-badge">{{ tempOption.badge }}</span>
                                </span>
                                <span class="option-price">NT${{ tempOption.price }}</span>
                            </label>
                        </div>
                    </div>

                    <!-- 配料選項 -->
                    <div v-if="hasToppingOptions" class="option-group topping-options">
                        <div class="option-header">
                            <h6 class="option-title">{{ toppingTitle }}</h6>
                            <span class="option-limit">最多可選 {{ maxToppings }} 個</span>
                        </div>
                        <div class="option-items">
                            <label v-for="topping in toppingOptions" :key="topping.value" class="option-item">
                                <input type="checkbox" :value="topping.value" v-model="selectedToppings"
                                    :disabled="!canSelectMoreToppings && !selectedToppings.includes(topping.value)" />
                                <span class="option-label">
                                    <span class="option-name">{{ topping.name }}</span>
                                </span>
                                <span class="option-price">+NT${{ topping.price }}</span>
                            </label>
                        </div>
                    </div>

                    <!-- 其他商品选项 -->
                    <div v-if="item.options && item.options.length > 0" class="item-options">
                        <h5>其他選項</h5>
                        <div v-for="option in item.options" :key="option.id" class="option-group">
                            <label class="option-label">{{ option.name }}</label>
                            <div class="option-items">
                                <label v-for="optionItem in option.items" :key="optionItem.id" class="option-item">
                                    <input v-if="option.type === 'radio'" type="radio" :name="option.id"
                                        :value="optionItem.id" v-model="selectedOptions[option.id]" />
                                    <input v-else type="checkbox" :value="optionItem.id"
                                        v-model="selectedOptions[option.id]" />
                                    <span>{{ optionItem.name }}</span>
                                    <span v-if="optionItem.price > 0" class="option-price">
                                        +NT${{ optionItem.price }}
                                    </span>
                                </label>
                            </div>
                        </div>
                    </div>

                    <!-- 数量选择 -->
                    <div class="quantity-section">
                        <label class="quantity-label">數量</label>
                        <div class="quantity-controls">
                            <button class="quantity-btn" @click="decreaseQuantity" :disabled="quantity <= 1">-</button>
                            <span class="quantity-display">{{ quantity }}</span>
                            <button class="quantity-btn" @click="increaseQuantity">+</button>
                        </div>
                    </div>

                    <!-- 备注 -->
                    <div class="notes-section">
                        <label class="notes-label">備註</label>
                        <textarea v-model="notes" placeholder="特殊要求或備註..." class="notes-input" rows="3"></textarea>
                    </div>
                </div>
            </div>

            <div class="modal-footer">
                <div class="total-price">
                    總計：NT${{ totalPrice }}
                </div>
                <button class="add-to-cart-btn" @click="addToCart">
                    加入購物車 ({{ quantity }})
                </button>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import '@/assets/css/restaurant-theme.css'


const props = defineProps({
    item: {
        type: Object,
        required: true
    },
    show: {
        type: Boolean,
        default: false
    }
})

const emit = defineEmits(['close', 'add-to-cart'])

// 组件状态
const quantity = ref(1)
const selectedOptions = ref({})
const notes = ref('')
const selectedIceLevel = ref('normal')
const selectedSweetnessLevel = ref('normal')
const selectedSizeLevel = ref('normal')
const selectedTemperatureLevel = ref('normal')
const selectedToppings = ref([])

// 冰量選項
const iceOptions = ref([
    { value: 'more', name: '多冰', price: 0 },
    { value: 'normal', name: '正常冰', price: 0 },
    { value: 'less', name: '少冰', price: 0 },
    { value: 'light', name: '微冰', price: 0, badge: '🔥 人氣精選 ✨' },
    { value: 'none', name: '去冰', price: 0 }
])

// 甜度選項
const sweetnessOptions = ref([
    { value: 'less', name: '少糖', price: 0 },
    { value: 'normal', name: '正常甜', price: 0 },
    { value: 'more', name: '多糖', price: 0 },
    { value: 'extra', name: '加糖', price: 0 }
])

// 尺寸選項
const sizeOptions = ref([
    { value: 'small', name: '小杯', price: 0 },
    { value: 'normal', name: '中杯', price: 0 },
    { value: 'large', name: '大杯', price: 0 },
    { value: 'extra', name: '超大杯', price: 0 }
])

// 溫度選項
const temperatureOptions = ref([
    { value: 'hot', name: '熱', price: 0 },
    { value: 'normal', name: '正常', price: 0 },
    { value: 'cold', name: '冰', price: 0 },
    { value: 'extra', name: '超冰', price: 0 }
])

// 配料選項
const toppingOptions = ref([
    { value: 'pearl', name: '珍珠', price: 10 },
    { value: 'taro', name: '小芋圓', price: 15 },
    { value: 'crystal', name: '寒天晶球', price: 15 },
    { value: 'jelly', name: '金萱茶凍', price: 10 },
    { value: 'grass', name: '仙草', price: 10 },
    { value: 'pudding', name: '布丁', price: 10 }
])

// 計算屬性
const hasIceOptions = computed(() => {
    // 根據商品類型決定是否顯示冰量選項
    return props.item.category && (
        props.item.category.includes('奶茶') ||
        props.item.category.includes('拿鐵') ||
        props.item.category.includes('茶') ||
        props.item.category.includes('飲品')
    )
})

const hasSweetnessOptions = computed(() => {
    return props.item.category && (
        props.item.category.includes('奶茶') ||
        props.item.category.includes('茶') ||
        props.item.category.includes('飲品')
    )
})

const hasSizeOptions = computed(() => {
    return props.item.category && (
        props.item.category.includes('奶茶') ||
        props.item.category.includes('茶') ||
        props.item.category.includes('飲品')
    )
})

const hasTemperatureOptions = computed(() => {
    return props.item.category && (
        props.item.category.includes('奶茶') ||
        props.item.category.includes('茶') ||
        props.item.category.includes('飲品')
    )
})

const hasToppingOptions = computed(() => {
    // 根據商品類型決定是否顯示配料選項
    return props.item.category && (
        props.item.category.includes('奶茶') ||
        props.item.category.includes('茶') ||
        props.item.category.includes('飲品')
    )
})

const toppingTitle = computed(() => {
    return props.item.category && props.item.category.includes('茶') ? '冰茶有料' : '配料選擇'
})

const maxToppings = computed(() => {
    return 2 // 最多可選2個配料
})

const canSelectMoreToppings = computed(() => {
    return selectedToppings.value.length < maxToppings.value
})

// 计算总价
const totalPrice = computed(() => {
    let basePrice = props.item.discountPrice || props.item.price
    let optionsPrice = 0

    // 计算冰量价格
    if (hasIceOptions.value) {
        const iceOption = iceOptions.value.find(option => option.value === selectedIceLevel.value)
        if (iceOption) {
            optionsPrice += iceOption.price
        }
    }

    // 计算甜度价格
    if (hasSweetnessOptions.value) {
        const sweetnessOption = sweetnessOptions.value.find(option => option.value === selectedSweetnessLevel.value)
        if (sweetnessOption) {
            optionsPrice += sweetnessOption.price
        }
    }

    // 计算尺寸价格
    if (hasSizeOptions.value) {
        const sizeOption = sizeOptions.value.find(option => option.value === selectedSizeLevel.value)
        if (sizeOption) {
            optionsPrice += sizeOption.price
        }
    }

    // 计算溫度价格
    if (hasTemperatureOptions.value) {
        const tempOption = temperatureOptions.value.find(option => option.value === selectedTemperatureLevel.value)
        if (tempOption) {
            optionsPrice += tempOption.price
        }
    }

    // 计算配料价格
    selectedToppings.value.forEach(toppingValue => {
        const topping = toppingOptions.value.find(t => t.value === toppingValue)
        if (topping) {
            optionsPrice += topping.price
        }
    })

    // 计算其他选项价格
    if (props.item.options) {
        props.item.options.forEach(option => {
            const selected = selectedOptions.value[option.id]
            if (selected) {
                if (Array.isArray(selected)) {
                    // 多选
                    selected.forEach(optionItemId => {
                        const optionItem = option.items.find(item => item.id === optionItemId)
                        if (optionItem) optionsPrice += optionItem.price || 0
                    })
                } else {
                    // 单选
                    const optionItem = option.items.find(item => item.id === selected)
                    if (optionItem) optionsPrice += optionItem.price || 0
                }
            }
        })
    }

    return (basePrice + optionsPrice) * quantity.value
})

// 方法
const closeModal = () => {
    emit('close')
}

const increaseQuantity = () => {
    quantity.value++
}

const decreaseQuantity = () => {
    if (quantity.value > 1) {
        quantity.value--
    }
}

const addToCart = () => {
    // 計算單件商品的總價（包含所有選項）
    const singleItemPrice = totalPrice.value / quantity.value

    const cartItem = {
        id: props.item.id,
        name: props.item.name,
        price: singleItemPrice, // 使用包含選項的單件價格
        image: props.item.image,
        quantity: quantity.value,
        selectedOptions: { ...selectedOptions.value },
        selectedIceLevel: selectedIceLevel.value,
        selectedSweetnessLevel: selectedSweetnessLevel.value,
        selectedSizeLevel: selectedSizeLevel.value,
        selectedTemperatureLevel: selectedTemperatureLevel.value,
        selectedToppings: [...selectedToppings.value],
        notes: notes.value,
        totalPrice: totalPrice.value
    }

    emit('add-to-cart', cartItem)

    // 重置状态
    quantity.value = 1
    selectedOptions.value = {}
    selectedIceLevel.value = 'normal'
    selectedSweetnessLevel.value = 'normal'
    selectedSizeLevel.value = 'normal'
    selectedTemperatureLevel.value = 'normal'
    selectedToppings.value = []
    notes.value = ''
}

// 监听显示状态变化，重置表单
watch(() => props.show, (newVal) => {
    if (newVal) {
        quantity.value = 1
        selectedOptions.value = {}
        selectedIceLevel.value = 'light' // 預設微冰
        selectedSweetnessLevel.value = 'normal'
        selectedSizeLevel.value = 'normal'
        selectedTemperatureLevel.value = 'normal'
        selectedToppings.value = []
        notes.value = ''

        // 初始化选项
        if (props.item.options) {
            props.item.options.forEach(option => {
                if (option.type === 'checkbox') {
                    selectedOptions.value[option.id] = []
                } else {
                    selectedOptions.value[option.id] = null
                }
            })
        }
    }
})
</script>


<template>
    <div v-if="show" class="modal-overlay goldenbowl-restaurant-theme" @click="closeModal">
        <div class="modal-content" @click.stop>
            <div class="modal-header">
                <h4 class="modal-title">商品詳情</h4>
                <button class="close-btn" @click="closeModal">×</button>
            </div>

            <div class="modal-body">
                <div class="item-image">
                    <img :src="item.image" :alt="item.name" />
                </div>

                <div class="item-details">
                    <h3 class="item-name">{{ item.name }}</h3>
                    <p class="item-description">{{ item.description }}</p>

                    <div class="price-section">
                        <span v-if="item.originalPrice && item.originalPrice !== item.discountPrice"
                            class="original-price">NT${{ item.originalPrice }}</span>
                        <span class="current-price">NT${{ item.discountPrice || item.price }}</span>
                    </div>

                    <!-- 冰量選項 -->
                    <div v-if="hasIceOptions" class="option-group ice-options">
                        <div class="option-header">
                            <h6 class="option-title">冰量</h6>
                            <span class="option-required">必選</span>
                        </div>
                        <div class="option-items">
                            <label v-for="iceOption in iceOptions" :key="iceOption.value" class="option-item">
                                <input type="radio" :name="'iceLevel'" :value="iceOption.value"
                                    v-model="selectedIceLevel" />
                                <span class="option-label">
                                    <span class="option-name">{{ iceOption.name }}</span>
                                    <span v-if="iceOption.badge" class="option-badge">{{ iceOption.badge }}</span>
                                </span>
                                <span class="option-price">NT${{ iceOption.price }}</span>
                            </label>
                        </div>
                    </div>

                    <!-- 甜度選項 -->
                    <div v-if="hasSweetnessOptions" class="option-group sweetness-options">
                        <div class="option-header">
                            <h6 class="option-title">甜度</h6>
                            <span class="option-required">必選</span>
                        </div>
                        <div class="option-items">
                            <label v-for="sweetnessOption in sweetnessOptions" :key="sweetnessOption.value"
                                class="option-item">
                                <input type="radio" :name="'sweetnessLevel'" :value="sweetnessOption.value"
                                    v-model="selectedSweetnessLevel" />
                                <span class="option-label">
                                    <span class="option-name">{{ sweetnessOption.name }}</span>
                                    <span v-if="sweetnessOption.badge" class="option-badge">{{ sweetnessOption.badge
                                        }}</span>
                                </span>
                                <span class="option-price">NT${{ sweetnessOption.price }}</span>
                            </label>
                        </div>
                    </div>

                    <!-- 尺寸選項 -->
                    <div v-if="hasSizeOptions" class="option-group size-options">
                        <div class="option-header">
                            <h6 class="option-title">尺寸</h6>
                            <span class="option-required">必選</span>
                        </div>
                        <div class="option-items">
                            <label v-for="sizeOption in sizeOptions" :key="sizeOption.value" class="option-item">
                                <input type="radio" :name="'sizeLevel'" :value="sizeOption.value"
                                    v-model="selectedSizeLevel" />
                                <span class="option-label">
                                    <span class="option-name">{{ sizeOption.name }}</span>
                                    <span v-if="sizeOption.badge" class="option-badge">{{ sizeOption.badge }}</span>
                                </span>
                                <span class="option-price">NT${{ sizeOption.price }}</span>
                            </label>
                        </div>
                    </div>

                    <!-- 溫度選項 -->
                    <div v-if="hasTemperatureOptions" class="option-group temperature-options">
                        <div class="option-header">
                            <h6 class="option-title">溫度</h6>
                            <span class="option-required">必選</span>
                        </div>
                        <div class="option-items">
                            <label v-for="tempOption in temperatureOptions" :key="tempOption.value" class="option-item">
                                <input type="radio" :name="'temperatureLevel'" :value="tempOption.value"
                                    v-model="selectedTemperatureLevel" />
                                <span class="option-label">
                                    <span class="option-name">{{ tempOption.name }}</span>
                                    <span v-if="tempOption.badge" class="option-badge">{{ tempOption.badge }}</span>
                                </span>
                                <span class="option-price">NT${{ tempOption.price }}</span>
                            </label>
                        </div>
                    </div>

                    <!-- 配料選項 -->
                    <div v-if="hasToppingOptions" class="option-group topping-options">
                        <div class="option-header">
                            <h6 class="option-title">{{ toppingTitle }}</h6>
                            <span class="option-limit">最多可選 {{ maxToppings }} 個</span>
                        </div>
                        <div class="option-items">
                            <label v-for="topping in toppingOptions" :key="topping.value" class="option-item">
                                <input type="checkbox" :value="topping.value" v-model="selectedToppings"
                                    :disabled="!canSelectMoreToppings && !selectedToppings.includes(topping.value)" />
                                <span class="option-label">
                                    <span class="option-name">{{ topping.name }}</span>
                                </span>
                                <span class="option-price">+NT${{ topping.price }}</span>
                            </label>
                        </div>
                    </div>

                    <!-- 其他商品选项 -->
                    <div v-if="item.options && item.options.length > 0" class="item-options">
                        <h5>其他選項</h5>
                        <div v-for="option in item.options" :key="option.id" class="option-group">
                            <label class="option-label">{{ option.name }}</label>
                            <div class="option-items">
                                <label v-for="optionItem in option.items" :key="optionItem.id" class="option-item">
                                    <input v-if="option.type === 'radio'" type="radio" :name="option.id"
                                        :value="optionItem.id" v-model="selectedOptions[option.id]" />
                                    <input v-else type="checkbox" :value="optionItem.id"
                                        v-model="selectedOptions[option.id]" />
                                    <span>{{ optionItem.name }}</span>
                                    <span v-if="optionItem.price > 0" class="option-price">
                                        +NT${{ optionItem.price }}
                                    </span>
                                </label>
                            </div>
                        </div>
                    </div>

                    <!-- 数量选择 -->
                    <div class="quantity-section">
                        <label class="quantity-label">數量</label>
                        <div class="quantity-controls">
                            <button class="quantity-btn" @click="decreaseQuantity" :disabled="quantity <= 1">-</button>
                            <span class="quantity-display">{{ quantity }}</span>
                            <button class="quantity-btn" @click="increaseQuantity">+</button>
                        </div>
                    </div>

                    <!-- 备注 -->
                    <div class="notes-section">
                        <label class="notes-label">備註</label>
                        <textarea v-model="notes" placeholder="特殊要求或備註..." class="notes-input" rows="3"></textarea>
                    </div>
                </div>
            </div>

            <div class="modal-footer">
                <div class="total-price">
                    總計：NT${{ totalPrice }}
                </div>
                <button class="add-to-cart-btn" @click="addToCart">
                    加入購物車 ({{ quantity }})
                </button>
            </div>
        </div>
    </div>
</template>



<style scoped>
.modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 2000;
    padding: 1rem;
}

.modal-content {
    background: var(--restaurant-bg-primary);
    border: 1px solid var(--restaurant-border-light);
    border-radius: 12px;
    width: 100%;
    max-width: 500px;
    max-height: 90vh;
    display: flex;
    flex-direction: column;
    box-shadow: 0 12px 40px var(--restaurant-shadow-dark);
}

.modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 1.5rem;
    border-bottom: 1px solid var(--restaurant-border-light);
    background: var(--restaurant-gradient-light);
    flex-shrink: 0;
}

.modal-title {
    font-size: 1.25rem;
    font-weight: 600;
    color: var(--restaurant-text-primary);
    margin: 0;
}

.close-btn {
    background: none;
    border: none;
    font-size: 2rem;
    color: var(--restaurant-text-secondary);
    cursor: pointer;
    padding: 0;
    width: 2rem;
    height: 2rem;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 50%;
    transition: all 0.2s ease;
}

.close-btn:hover {
    color: var(--restaurant-primary);
    background: var(--restaurant-shadow-light);
}

.modal-body {
    flex: 1;
    overflow-y: auto;
    padding: 1.5rem;
}

.item-image {
    width: 100%;
    height: 200px;
    border-radius: 8px;
    overflow: hidden;
    margin-bottom: 1rem;
}

.item-image img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.item-details {
    display: flex;
    flex-direction: column;
    gap: 1rem;
}

.item-name {
    font-size: 1.5rem;
    font-weight: 600;
    color: var(--restaurant-text-primary);
    margin: 0;
}

.item-description {
    color: var(--restaurant-text-secondary);
    margin: 0;
    line-height: 1.5;
}

.price-section {
    display: flex;
    align-items: center;
    gap: 0.5rem;
}

.original-price {
    color: var(--restaurant-text-muted);
    text-decoration: line-through;
    font-size: 0.9rem;
}

.current-price {
    color: var(--restaurant-primary-dark);
    font-size: 1.25rem;
    font-weight: 600;
}

/* 選項組樣式 */
.option-group {
    border: 1px solid var(--restaurant-border-light);
    border-radius: 8px;
    padding: 1rem;
    margin-bottom: 1rem;
}

.ice-options {
    background: linear-gradient(135deg, #ffeef0 0%, #fff5f5 100%);
}

.sweetness-options {
    background: linear-gradient(135deg, #e0f7fa 0%, #e0f7fa 100%);
}

.size-options {
    background: linear-gradient(135deg, #e8f5e9 0%, #e8f5e9 100%);
}

.temperature-options {
    background: linear-gradient(135deg, #f3e5f5 0%, #f3e5f5 100%);
}

.topping-options {
    background: var(--restaurant-bg-primary);
}

.option-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 1rem;
}

.option-title {
    font-weight: 600;
    color: var(--restaurant-text-primary);
    margin: 0;
}

.option-required {
    background: var(--restaurant-primary);
    color: white;
    padding: 0.25rem 0.5rem;
    border-radius: 4px;
    font-size: 0.75rem;
    font-weight: 500;
}

.option-limit {
    background: var(--restaurant-bg-secondary);
    color: var(--restaurant-text-secondary);
    padding: 0.25rem 0.5rem;
    border-radius: 4px;
    font-size: 0.75rem;
}

.option-items {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
}

.option-item {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    padding: 0.5rem;
    border-radius: 6px;
    cursor: pointer;
    transition: all 0.2s ease;
}

.option-item:hover {
    background: var(--restaurant-bg-light);
}

.option-item input[type="radio"],
.option-item input[type="checkbox"] {
    margin: 0;
}

.option-label {
    flex: 1;
    display: flex;
    align-items: center;
    gap: 0.5rem;
}

.option-name {
    color: var(--restaurant-text-primary);
    font-weight: 500;
}

.option-badge {
    background: var(--restaurant-primary-light);
    color: var(--restaurant-primary-dark);
    padding: 0.125rem 0.375rem;
    border-radius: 4px;
    font-size: 0.7rem;
    font-weight: 500;
}

.option-price {
    color: var(--restaurant-primary-dark);
    font-weight: 500;
    font-size: 0.9rem;
}

/* 數量選擇 */
.quantity-section {
    display: flex;
    align-items: center;
    gap: 1rem;
    padding: 1rem;
    background: var(--restaurant-bg-light);
    border-radius: 8px;
}

.quantity-label {
    font-weight: 600;
    color: var(--restaurant-text-primary);
}

.quantity-controls {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    background: var(--restaurant-bg-primary);
    border: 1px solid var(--restaurant-border-light);
    border-radius: 6px;
    padding: 0.25rem;
}

.quantity-btn {
    width: 2rem;
    height: 2rem;
    border: none;
    background: var(--restaurant-bg-primary);
    border-radius: 4px;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 1rem;
    font-weight: 600;
    transition: all 0.2s ease;
}

.quantity-btn:hover:not(:disabled) {
    background: var(--restaurant-primary);
    color: white;
}

.quantity-btn:disabled {
    opacity: 0.5;
    cursor: not-allowed;
}

.quantity-display {
    min-width: 2rem;
    text-align: center;
    font-weight: 600;
    color: var(--restaurant-text-primary);
}

/* 備註 */
.notes-section {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
}

.notes-label {
    font-weight: 600;
    color: var(--restaurant-text-primary);
}

.notes-input {
    padding: 0.75rem;
    border: 1px solid var(--restaurant-border-light);
    border-radius: 6px;
    resize: vertical;
    font-family: inherit;
    background: var(--restaurant-bg-primary);
    color: var(--restaurant-text-primary);
}

.notes-input:focus {
    outline: none;
    border-color: var(--restaurant-primary);
    box-shadow: 0 0 0 2px var(--restaurant-shadow-light);
}

.modal-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 1.5rem;
    border-top: 1px solid var(--restaurant-border-light);
    background: var(--restaurant-bg-secondary);
    flex-shrink: 0;
}

.total-price {
    font-size: 1.1rem;
    font-weight: 600;
    color: var(--restaurant-primary-dark);
}

.add-to-cart-btn {
    background: var(--restaurant-gradient-primary);
    color: var(--restaurant-text-primary);
    border: 1px solid var(--restaurant-primary-light);
    padding: 0.75rem 1.5rem;
    border-radius: 8px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.3s ease;
}

.add-to-cart-btn:hover {
    background: var(--restaurant-primary-hover);
    transform: translateY(-1px);
    box-shadow: 0 4px 12px var(--restaurant-shadow-medium);
}

/* 響應式設計 */
@media (max-width: 768px) {
    .modal-content {
        margin: 0.5rem;
        max-width: none;
        max-height: 95vh;
    }

    .modal-header,
    .modal-body,
    .modal-footer {
        padding: 1rem;
    }

    .option-item {
        flex-direction: column;
        align-items: flex-start;
        gap: 0.5rem;
    }

    .option-price {
        align-self: flex-end;
    }
}
</style>