<template><!--CartModal.vue ted 負責-->
    <div class="cart-modal-overlay goldenbowl-restaurant-theme" @click="closeModal">
        <div class="cart-modal-content" @click.stop>
            <div class="cart-header">
                <h4 class="cart-title">購物車 ({{ totalItems }})</h4>
                <button class="close-btn" @click="closeModal">×</button>
            </div>

            <div class="cart-body">
                <div v-if="restaurantCount === 0" class="empty-cart">
                    <div class="empty-cart-icon">🛒</div>
                    <p>購物車是空的</p>
                    <p class="empty-cart-desc">快去選購美味的商品吧！</p>
                </div>

                <div v-else class="cart-restaurants">
                    <div v-for="(restaurantCart, restaurantId) in cartByRestaurant" :key="restaurantId"
                        class="restaurant-section">
                        <!-- 餐廳標題 -->
                        <div class="restaurant-header">
                            <div class="restaurant-info">
                                <router-link :to="`/restaurant/${ restaurantId }`">
                                <img :src="restaurantCart.restaurant.image" :alt="restaurantCart.restaurant.name"
                                    class="restaurant-image" />
                                </router-link>    
                                <div class="restaurant-details">
                                    <h5 class="restaurant-name">{{ restaurantCart.restaurant.name }}</h5>
                                    <span class="restaurant-item-count">{{ restaurantCart.items.length }} 項商品</span>
                                </div>
                            </div>
                            <div class="restaurant-actions">
                                <button class="checkout-restaurant-btn" @click="checkoutRestaurant(restaurantId)">
                                    結帳此餐廳 (NT${{ getRestaurantTotal(restaurantId) }})
                                </button>
                                <button class="clear-restaurant-btn" @click="clearRestaurant(restaurantId)"
                                    title="清空此餐廳">
                                    <i class="pi pi-trash"></i>
                                </button>
                            </div>
                        </div>

                        <!-- 該餐廳的商品列表 -->
                        <div class="restaurant-items">
                            <div v-for="item in restaurantCart.items" :key="item.id" class="cart-item">
                                <div class="item-image">
                                    <img :src="item.image" :alt="item.name" />
                                </div>

                                <div class="item-details">
                                    <h5 class="item-name">{{ item.food.name }}</h5>
                                    <div class="item-price">NT${{ item.price }}</div>
                                </div>

                                <div class="item-controls">
                                    <div class="quantity-controls">
                                        <button class="quantity-btn"
                                            @click="updateQuantity(item.food.id, item.quantity - 1, restaurantId)"
                                            :disabled="item.quantity <= 1">-</button>
                                        <span class="quantity-display">{{ item.quantity }}</span>
                                        <button class="quantity-btn"
                                            @click="updateQuantity(item.food.id, item.quantity + 1, restaurantId)">+</button>
                                    </div>

                                    
                                </div>

                                <div class="item-total">
                                    NT${{ item.price * item.quantity }}
                                    <button class="remove-btn" @click="removeItem(item.food.id, restaurantId)" title="移除商品">
                                        <i class="pi pi-trash"></i>
                                    </button>
                                </div>
                            </div>
                        </div>

                        <!-- 餐廳小計 -->
                        <div class="restaurant-subtotal">
                            <span>此餐廳小計：</span>
                            <span class="subtotal-amount">NT${{ getRestaurantTotal(restaurantId) }}</span>
                        </div>
                    </div>
                </div>
            </div>

            <div v-if="restaurantCount > 0" class="cart-footer">
                <!-- <div class="cart-summary">
                    <div class="summary-row">
                        <span>餐廳數量</span>
                        <span>{{ restaurantCount }} 家</span>
                    </div>
                    <div class="summary-row">
                        <span>商品小計</span>
                        <span>NT${{ totalAmount }}</span>
                    </div>
                    <div class="summary-row total-row">
                        <span>總計</span>
                        <span>NT${{ totalAmount + deliveryFee }}</span>
                    </div>
                </div> -->

                <div class="cart-actions">
                    <button class="continue-shopping-btn" @click="closeModal">
                        繼續購物
                    </button>
                </div>
            </div>


        </div>
    </div>

    
</template>

<script setup>
import { computed } from 'vue'
import '@/assets/css/restaurant-theme.css'

const props = defineProps({
    cartByRestaurant: {
        type: Object,
        required: true
    },
    totalAmount: {
        type: Number,
        required: true
    }
})
// 移除 'checkout-all' 事件定義 ted ted
const emit = defineEmits(['close', 'update-quantity', 'remove-item', 'checkout-restaurant', 'clear-restaurant'])
// 定義事件
// 计算属性
const totalItems = computed(() => {
    return Object.values(props.cartByRestaurant).reduce((total, restaurantCart) => {
        return total + restaurantCart.items.reduce((sum, item) => sum + item.quantity, 0)
    }, 0)
})

const restaurantCount = computed(() => {
    return Object.keys(props.cartByRestaurant).length
})

const deliveryFee = computed(() => {
    // 满额免运费逻辑
    return props.totalAmount >= 300 ? 0 : 50
})

// 方法
const closeModal = () => {
    emit('close')
}

const updateQuantity = (itemId, newQuantity, restaurantId) => {
    emit('update-quantity', itemId, newQuantity, restaurantId)
}

const removeItem = (itemId, restaurantId) => {
    emit('remove-item', itemId, restaurantId)
}
// 結帳此餐廳
const checkoutRestaurant = (restaurantId) => {
    emit('checkout-restaurant', restaurantId)
    console.log('發送事件結帳此餐廳')
}
// 清空購物車
const clearRestaurant = (restaurantId) => {
    emit('clear-restaurant', restaurantId)
}

const getRestaurantTotal = (restaurantId) => {
    const restaurantCart = props.cartByRestaurant[restaurantId]
    if (!restaurantCart) return 0

    return restaurantCart.items.reduce((sum, item) => sum + (item.price * item.quantity), 0)
}

const hasSelectedOptions = (selectedOptions) => {
    if (!selectedOptions) return false
    return Object.values(selectedOptions).some(option => {
        if (Array.isArray(option)) {
            return option.length > 0
        }
        return option !== null && option !== undefined
    })
}

const formatOptions = (optionValue) => {
    // 这里应该根据实际的选项数据来格式化显示
    // 暂时简化处理
    if (Array.isArray(optionValue)) {
        return optionValue.join(', ')
    }
    return optionValue
}



</script>

<style scoped>

.cart-modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 3500;
    padding: 1rem;
}

.cart-modal-content {
    background: var(--restaurant-bg-primary);
    border: 1px solid var(--restaurant-border-light);
    border-radius: 12px;
    width: 100%;
    max-width: 800px;
    max-height: 90vh;
    display: flex;
    flex-direction: column;
    box-shadow: 0 12px 40px var(--restaurant-shadow-dark);
}

.cart-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 1.5rem;
    border-bottom: 1px solid var(--restaurant-border-light);
    background: var(--restaurant-gradient-light);
    flex-shrink: 0;
}

.cart-title {
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

.cart-body {
    flex: 1;
    overflow-y: auto;
    padding: 1rem 1.5rem;
}

.empty-cart {
    text-align: center;
    padding: 3rem 1rem;
    color: var(--restaurant-text-secondary);
    background: var(--restaurant-bg-light);
    border-radius: 8px;
    margin: 1rem 0;
}

.empty-cart-icon {
    font-size: 4rem;
    margin-bottom: 1rem;
}

.empty-cart p {
    margin: 0.5rem 0;
    color: var(--restaurant-text-primary);
}

.empty-cart-desc {
    font-size: 0.9rem;
    color: var(--restaurant-text-muted);
}

.cart-restaurants {
    display: flex;
    flex-direction: column;
    gap: 2rem;
}

.restaurant-section {
    border: 1px solid var(--restaurant-border-light);
    border-radius: 8px;
    overflow: hidden;
    background: var(--restaurant-bg-light);
}

.restaurant-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 1rem;
    background: var(--restaurant-gradient-light);
    border-bottom: 1px solid var(--restaurant-border-light);
}

.restaurant-info {
    display: flex;
    align-items: center;
    gap: 1rem;
}

.restaurant-image {
    width: 50px;
    height: 50px;
    border-radius: 8px;
    object-fit: cover;
}

.restaurant-name {
    font-size: 1.1rem;
    font-weight: 600;
    color: var(--restaurant-text-primary);
    margin: 0 0 0.25rem 0;
}

.restaurant-item-count {
    font-size: 0.85rem;
    color: var(--restaurant-text-secondary);
}

.restaurant-actions {
    display: flex;
    align-items: center;
    gap: 0.5rem;
}

.checkout-restaurant-btn {
    background: var(--restaurant-gradient-primary);
    color: var(--restaurant-text-primary);
    border: 1px solid var(--restaurant-primary-light);
    padding: 0.5rem 1rem;
    border-radius: 6px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.3s ease;
    font-size: 0.85rem;
}

.checkout-restaurant-btn:hover {
    background: var(--restaurant-primary-hover);
    transform: translateY(-1px);
    box-shadow: 0 4px 12px var(--restaurant-shadow-medium);
}

.clear-restaurant-btn {
    background: none;
    border: 1px solid var(--restaurant-border-medium);
    color: var(--restaurant-text-secondary);
    padding: 0.5rem;
    border-radius: 6px;
    cursor: pointer;
    transition: all 0.2s ease;
}

.clear-restaurant-btn:hover {
    background: var(--restaurant-shadow-light);
    color: var(--restaurant-error);
}

.restaurant-items {
    padding: 1rem;
}

.cart-item {
    display: grid;
    grid-template-columns: 80px 1fr auto auto;
    gap: 1rem;
    align-items: start;
    padding: 1rem;
    background: var(--restaurant-bg-primary);
    border: 1px solid var(--restaurant-border-light);
    border-radius: 8px;
    transition: all 0.2s ease;
    margin-bottom: 1rem;
}

.cart-item:hover {
    box-shadow: 0 2px 8px var(--restaurant-shadow-light);
    border-color: var(--restaurant-primary-light);
}

.item-image {
    width: 80px;
    height: 80px;
    border-radius: 6px;
    overflow: hidden;
}

.item-image img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.item-details {
    min-width: 0;
}

.item-name {
    font-size: 1rem;
    font-weight: 600;
    color: var(--restaurant-text-primary);
    margin: 0 0 0.5rem 0;
}

.item-options {
    font-size: 0.85rem;
    color: var(--restaurant-text-secondary);
    margin-bottom: 0.5rem;
}

.option-text {
    display: block;
    margin-bottom: 0.25rem;
    padding: 0.25rem 0.5rem;
    background: var(--restaurant-bg-secondary);
    border-radius: 4px;
    border: 1px solid var(--restaurant-border-light);
}

.item-specification {
    font-size: 0.85rem;
    color: var(--restaurant-text-secondary);
    margin-bottom: 0.5rem;
    padding: 0.25rem 0.5rem;
    background: var(--restaurant-bg-secondary);
    border-radius: 4px;
    border: 1px solid var(--restaurant-border-light);
}

.spec-label {
    font-weight: 500;
    color: var(--restaurant-text-primary);
}

.spec-value {
    color: var(--restaurant-primary-dark);
    font-weight: 500;
}

.item-notes {
    font-size: 0.85rem;
    color: var(--restaurant-text-secondary);
    margin-bottom: 0.5rem;
    padding: 0.25rem 0.5rem;
    background: var(--restaurant-bg-secondary);
    border-radius: 4px;
    border: 1px solid var(--restaurant-border-light);
}

.notes-label {
    font-weight: 500;
    color: var(--restaurant-text-primary);
}

.notes-text {
    font-style: italic;
}

.item-price {
    font-size: 0.9rem;
    color: var(--restaurant-primary-dark);
    font-weight: 500;
}

.item-controls {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 0.5rem;
}

.quantity-controls {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    padding: 0.5rem;
    background: var(--restaurant-bg-primary);
    border-radius: 6px;
    border: 1px solid var(--restaurant-border-light);
}

.quantity-btn {
    width: 2rem;
    height: 2rem;
    border: 1px solid var(--restaurant-border-medium);
    background: var(--restaurant-bg-primary);
    border-radius: 4px;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 1rem;
    font-weight: 600;
    transition: all 0.3s ease;
    color: var(--restaurant-text-primary);
}

.quantity-btn:hover:not(:disabled) {
    background: var(--restaurant-gradient-primary);
    color: var(--restaurant-text-primary);
    border-color: var(--restaurant-primary);
    transform: translateY(-1px);
}

.quantity-btn:disabled {
    opacity: 0.5;
    cursor: not-allowed;
}

.quantity-display {
    font-size: 1rem;
    font-weight: 600;
    min-width: 1.5rem;
    text-align: center;
    color: var(--restaurant-text-primary);
}

.remove-btn {
    background: none;
    border: none;
    cursor: pointer;
    padding: 0.25rem;
    border-radius: 4px;
    transition: all 0.2s ease;
    color: var(--restaurant-text-secondary);
}

.remove-btn:hover {
    background: var(--restaurant-shadow-light);
    color: var(--restaurant-error);
}

.item-total {
    font-size: 1rem;
    font-weight: 600;
    color: var(--restaurant-primary-dark);
    text-align: right;
}

.restaurant-subtotal {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 1rem;
    background: var(--restaurant-bg-secondary);
    border-top: 1px solid var(--restaurant-border-light);
    font-weight: 600;
    color: var(--restaurant-text-primary);
}

.subtotal-amount {
    color: var(--restaurant-primary-dark);
    font-size: 1.1rem;
}

.cart-footer {
    flex-shrink: 0;
    border-top: 1px solid var(--restaurant-border-light);
    padding: 1.5rem;
    background: var(--restaurant-bg-secondary);
    border-radius: 0 0 12px 12px;
}

.cart-summary {
    margin-bottom: 1.5rem;
    padding: 1rem;
    background: var(--restaurant-bg-primary);
    border-radius: 8px;
    border: 1px solid var(--restaurant-border-light);
}

.summary-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0.5rem 0;
    color: var(--restaurant-text-secondary);
}

.total-row {
    border-top: 1px solid var(--restaurant-border-medium);
    padding-top: 1rem;
    margin-top: 0.5rem;
    font-weight: 600;
    font-size: 1.1rem;
    color: var(--restaurant-primary-dark);
}

.cart-actions {
    display: flex;
    gap: 1rem;
}

.continue-shopping-btn {
    flex: 1;
    padding: 0.75rem 1rem;
    border: 2px solid var(--restaurant-primary);
    background: var(--restaurant-bg-primary);
    color: var(--restaurant-primary);
    border-radius: 8px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.3s ease;
    box-shadow: 0 2px 6px var(--restaurant-shadow-light);
}

.continue-shopping-btn:hover {
    background: var(--restaurant-gradient-primary);
    color: var(--restaurant-text-primary);
    transform: translateY(-2px);
    box-shadow: 0 4px 12px var(--restaurant-shadow-medium);
}

.checkout-all-btn {
    flex: 2;
    padding: 0.75rem 1rem;
    background: var(--restaurant-gradient-primary);
    color: var(--restaurant-text-primary);
    border: 1px solid var(--restaurant-primary-light);
    border-radius: 8px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.3s ease;
    box-shadow: 0 2px 8px var(--restaurant-shadow-light);
}

.checkout-all-btn:hover {
    background: var(--restaurant-primary-hover);
    transform: translateY(-2px);
    box-shadow: 0 6px 16px var(--restaurant-shadow-medium);
}

/* 响应式设计 */
@media (max-width: 768px) {
    .cart-modal-content {
        margin: 0.5rem;
        max-width: none;
        max-height: 95vh;
    }

    .cart-header,
    .cart-body,
    .cart-footer {
        padding: 1rem;
    }

    .restaurant-header {
        flex-direction: column;
        align-items: flex-start;
        gap: 1rem;
    }

    .restaurant-actions {
        width: 100%;
        justify-content: space-between;
    }

    .cart-item {
        grid-template-columns: 60px 1fr;
        grid-template-rows: auto auto;
        gap: 0.75rem;
    }

    .item-image {
        width: 60px;
        height: 60px;
    }

    .item-controls {
        grid-column: 1;
        grid-row: 2;
        flex-direction: row;
        justify-content: flex-start;
    }

    .item-total {
        grid-column: 2;
        grid-row: 2;
        text-align: right;
        align-self: end;
    }

    .cart-actions {
        flex-direction: column;
    }
}

@media (max-width: 480px) {
    .cart-actions {
        gap: 0.75rem;
    }

    .continue-shopping-btn,
    .checkout-all-btn {
        padding: 0.875rem;
    }
}
</style>