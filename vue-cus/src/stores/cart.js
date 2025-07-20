import {
    defineStore
} from 'pinia'
import {
    ref,
    computed
} from 'vue'

export const useCartStore = defineStore('cart', () => {
    // 購物車結構：{ restaurantId: { restaurant, items } }
    const cartByRestaurant = ref({})
    // ✨ 新增的購物車模態框顯示狀態
    const isCartVisible = ref(false)

    // 購物車顯示
    const showCart = () => {
        isCartVisible.value = true
    }
    // 購物車隱藏
    const hideCart = () => {
        isCartVisible.value = false
    }

    // 計算屬性
    const cartCount = computed(() => {
        return Object.values(cartByRestaurant.value).reduce((total, restaurantCart) => {
            return total + restaurantCart.items.reduce((sum, item) => sum + item.quantity, 0)
        }, 0)
    })

    const totalAmount = computed(() => {
        return Object.values(cartByRestaurant.value).reduce((total, restaurantCart) => {
            return total + restaurantCart.items.reduce((sum, item) => sum + (item.price * item.quantity), 0)
        }, 0)
    })
    //回傳數量
    const restaurantCount = computed(() => {
        return Object.keys(cartByRestaurant.value).length
    })

    const cartItems = computed(() => {
        // 返回所有餐廳的商品列表（扁平化）
        return Object.values(cartByRestaurant.value).flatMap(restaurantCart => restaurantCart.items)
    })

    const cartRestaurants = computed(() => {
        return Object.values(cartByRestaurant.value).map(restaurantCart => restaurantCart.restaurant)
    })

    // 新增到購物車
    const addToCart = (item, restaurant) => {
        // item 從restaurantMenu.vue傳送近來
        const restaurantId = restaurant.id

        // 如果該餐廳還沒有購物車，創建一個
        if (!cartByRestaurant.value[restaurantId]) {
            cartByRestaurant.value[restaurantId] = {
                restaurant: {
                    id: restaurant.id,
                    name: restaurant.name,
                    image: restaurant.photo,
                    status: '',
                    content: '',
                },
                items: []
            }
        }
        // 如果有取出該餐廳的內容
        const restaurantCart = cartByRestaurant.value[restaurantId]

        // 創建唯一的商品ID，包含規格信息
        const itemKey = generateItemKey(item)
        const existingItemIndex = restaurantCart.items.findIndex(cartItem => generateItemKey(cartItem) === itemKey)

        if (existingItemIndex > -1) {
            // 如果找到相同規格的商品，增加數量
            restaurantCart.items[existingItemIndex].quantity += item.quantity
            restaurantCart.items[existingItemIndex].total += item.total

            // restaurantCart.items[existingItemIndex].
        } else {
            // 如果沒有找到相同規格的商品，添加新項目
            restaurantCart.items.push({
                ...item
            })
        }
    }

    // 生成商品唯一鍵，包含規格信息
    const generateItemKey = (item) => {
        const baseKey = `${item.food.id}`

        return `${baseKey}`
    }

    const updateQuantity = (itemId, newQuantity, restaurantId) => {
        if (!cartByRestaurant.value[restaurantId]) return

        if (newQuantity <= 0) {
            removeItem(itemId, restaurantId)
            return
        }

        const itemIndex = cartByRestaurant.value[restaurantId].items.findIndex(item => item.food.id === itemId)
        if (itemIndex > -1) {
            cartByRestaurant.value[restaurantId].items[itemIndex].quantity = newQuantity
        }
    }

    const removeItem = (itemId, restaurantId) => {
        if (!cartByRestaurant.value[restaurantId]) return

        cartByRestaurant.value[restaurantId].items = cartByRestaurant.value[restaurantId].items.filter(item => item.food.id !== itemId)

        // 如果該餐廳沒有商品了，移除該餐廳的購物車
        if (cartByRestaurant.value[restaurantId].items.length === 0) {
            delete cartByRestaurant.value[restaurantId]
        }
    }
    // 清除全部的餐廳
    const clearCart = () => {
        cartByRestaurant.value = {}
    }
    // 清除單一家的餐廳
    const clearRestaurantCart = (restaurantId) => {
        delete cartByRestaurant.value[restaurantId]
    }

    const getRestaurantCart = (restaurantId) => {
        return cartByRestaurant.value[restaurantId] || null
    }

    const getRestaurantTotal = (restaurantId) => {
        const restaurantCart = cartByRestaurant.value[restaurantId]
        if (!restaurantCart) return 0

        return restaurantCart.items.reduce((sum, item) => sum + (item.price * item.quantity), 0)
    }

    const getRestaurantItemCount = (restaurantId) => {
        const restaurantCart = cartByRestaurant.value[restaurantId]
        if (!restaurantCart) return 0

        return restaurantCart.items.reduce((sum, item) => sum + item.quantity, 0)
    }


    // --- ✨ 新增：再訂購函式 ✨ ---
    const reorder = (oldOrder) => {
        if (!oldOrder || !oldOrder.store || !oldOrder.orderDetails || oldOrder.orderDetails.length === 0) {
            console.warn('無效的舊訂單數據，無法再訂購。', oldOrder);
            return;
        }

        // 確保 restaurant 物件的 key 與 addToCart 期望的一致
        const restaurantToAddToCart = {
            id: oldOrder.store.id,
            name: oldOrder.store.name,
            // 由於您的 oldOrder.store.photo 是圖片路徑，addToCart 期望 'image'
            photo: oldOrder.store.photo,
            // 根據您的 addToCart 函數定義，可能需要添加其他 restaurant 屬性，
            // 或確保 addToCart 能夠處理這些額外的屬性
        };

        // 遍歷舊訂單的每個商品，並添加到購物車
        oldOrder.orderDetails.forEach(orderItem => {
            // orderItem 是您的 orderDetails 陣列中的單個元素
            // 它的結構是 { food: {id, name, image}, price, quantity, ... }
            const itemToAddToCart = {
                // Pinia Store 的 addToCart 期望的 item 參數的結構
                // food: { id, name, image } 已經存在於 orderItem.food
                food: {
                    id: orderItem.food.id,
                    name: orderItem.food.name,
                    imgResource: orderItem.food.image // 這裡使用的是 item.food.image
                },
                id: orderItem.food.id, // 用 food.id 作為購物車內商品的唯一ID
                quantity: orderItem.quantity,
                price: orderItem.price,
                // 通常不傳 subTotal/total 給 addToCart，因為購物車會自行計算
                // 但如果您的 addToCart 依賴這個，確保它符合期望
                total: orderItem.price * orderItem.quantity // 計算單項總價
            };

            // 呼叫現有的 addToCart 函式
            addToCart(itemToAddToCart, restaurantToAddToCart);
        });

        console.log('舊訂單已加入購物車:', oldOrder.id);
        showCart(); // 確保再訂購後購物車彈窗顯示
    };



    // 結帳相關方法
    const checkoutSingleRestaurant = (restaurantId) => {
        const restaurantCart = cartByRestaurant.value[restaurantId]
        if (!restaurantCart) return null

        const orderData = {
            store: restaurantCart.restaurant,
            // 餐廳資訊
            orderDetails: [...restaurantCart.items],
            total: restaurantCart.total ?? getRestaurantTotal(restaurantId),
            status: restaurantCart.status,
            createTime: restaurantCart.create_time,
            content: restaurantCart.content,
            pickupTime: restaurantCart.pickup_time,
            user: restaurantCart.user,
            promotionId: restaurantCart.promotionId, // 新增 promotionId

        }

        // 移除該餐廳的購物車
        clearRestaurantCart(restaurantId)

        return orderData
    }

    const checkoutAllRestaurants = () => {
        const orders = []

        Object.keys(cartByRestaurant.value).forEach(restaurantId => {
            const orderData = checkoutSingleRestaurant(restaurantId)
            if (orderData) {
                orders.push(orderData)
            }
        })

        return orders
    }


    return {
        // 狀態
        cartByRestaurant,
        isCartVisible, // ✨ 確保 isCartVisible 被暴露

        // 計算屬性
        cartCount,
        totalAmount,
        restaurantCount,
        cartItems,
        cartRestaurants,

        // 方法
        addToCart,
        updateQuantity,
        removeItem,
        clearCart,
        clearRestaurantCart,
        getRestaurantCart,
        getRestaurantTotal,
        getRestaurantItemCount,
        showCart, // ✨ 確保 showCart 被暴露
        hideCart, // ✨ 確保 hideCart 被暴露

        // 結帳方法
        checkoutSingleRestaurant,
        checkoutAllRestaurants,
        reorder // ✨ 新增：再訂購函式
    }
}, {
    // ✨ 新增這個配置物件來啟用持久化
    persist: {
        storage: sessionStorage, // 指定使用 sessionStorage
        // 或者使用 localStorage: storage: localStorage,
        // key: 'my-cart-data', // 可選：自訂儲存到 sessionStorage/localStorage 的鍵名，預設是 store 的 id ('cart')
    }
})