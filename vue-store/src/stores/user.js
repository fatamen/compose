// src/stores/user.js
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import axios from '@/plungins/axios.js'

export const useUserStore = defineStore('user', () => {
    // 🔥 原始用戶狀態（正式環境需要的）
    const fullName = ref(localStorage.getItem('userFullName') || '')
    const userId = ref(localStorage.getItem('userId') || '')
    const email = ref(localStorage.getItem('userEmail') || '')
    const token = ref(localStorage.getItem('token') || '')
    const isLogin = ref(false)

    // 🔥 業者狀態（測試環境需要的）
    const ownerFullName = ref('')
    const ownerEmail = ref('')
    const ownerId = ref('')

    // 🔥 餐廳註冊流程狀態（測試環境需要的）
    const storeId = ref('')
    const storeName = ref('')
    const address = ref({
        city: '',
        district: '',
        zip: '',
        street: '',
        door: '',
        enAddress: '',
        lat: '',
        lng: ''
    })

    // 🔥 餐廳完整資料（兩個環境都需要，但結構稍有不同）
    const storeProfile = ref({
        id: '',
        storeName: '',
        address: '',
        intro: '',
        phone: '',
        email: '',
        isEmailVerified: false
    })

    // 🔥 狀態判斷
    const isOwnerLogin = computed(() => !!ownerId.value)

    // === 原始用戶相關函數（正式環境需要的）===
    function setFullName(name) {
        fullName.value = name
        localStorage.setItem('userFullName', name)
    }

    function setUserId(id) {
        userId.value = id
        localStorage.setItem('userId', id)
    }

    function setEmail(data) {
        email.value = data
        localStorage.setItem('userEmail', data)
    }

    function setToken(data) {
        token.value = data
        localStorage.setItem('token', data)
    }

    function setLogin(data) {
        isLogin.value = data
    }

    // === 業者相關函數（測試環境需要的）===
    function setOwnerFullName(val) {
        ownerFullName.value = val
        localStorage.setItem('storeFullName', val)
    }

    function setOwnerEmail(val) {
        ownerEmail.value = val
        localStorage.setItem('storeEmail', val)
    }

    function setOwnerId(id) {
        ownerId.value = id
        localStorage.setItem('ownerId', id)
    }

    function ownerLogout() {
        ownerFullName.value = ''
        ownerEmail.value = ''
        ownerId.value = ''
        localStorage.removeItem('storeFullName')
        localStorage.removeItem('storeEmail')
        localStorage.removeItem('ownerId')
    }

    // === 餐廳相關函數（測試環境需要的）===
    function setStoreName(val) {
        storeName.value = val
        localStorage.setItem('storeName', val)
    }

    function setStoreId(val) {
        storeId.value = val
        localStorage.setItem('storeId', val)
    }

    function setAddressInfo(obj) {
        address.value = { ...address.value, ...obj }
        localStorage.setItem('address', JSON.stringify(address.value))
    }

    // === 店家資料相關函數（兩個環境都需要，合併功能）===
    function setStoreProfile(profile) {
        // 🔥 合併兩個版本的邏輯
        if (typeof profile === 'object' && profile !== null) {
            storeProfile.value = { ...storeProfile.value, ...profile }
        } else {
            storeProfile.value = profile
        }

        // 🔥 同步到 localStorage 以便與現有系統相容
        localStorage.setItem('storeProfile', JSON.stringify(storeProfile.value))

        // 🔥 如果有 id，也同步到 storeId
        if (profile && profile.id) {
            localStorage.setItem('storeId', profile.id)
            storeId.value = profile.id
        }

        console.log('📋 [Pinia] setStoreProfile:', storeProfile.value)
    }

    // === 從後端獲取店家資料（合併兩個版本）===
    async function fetchStoreProfile() {
        try {
            const ownerIdValue = ownerId.value || localStorage.getItem('ownerId')
            if (!ownerIdValue) {
                console.warn('⚠️ [Pinia] 沒有 ownerId，無法獲取 store profile')
                return
            }

            console.log(`🚀 [Pinia] 正在獲取 owner ${ownerIdValue} 的店家資料...`)

            try {
                // 🔥 嘗試兩個不同的 API 端點
                let response
                try {
                    // 正式環境的 API
                    response = await axios.get('/api/stores/profile/all', {
                        params: { ownerId: ownerIdValue }
                    })
                } catch (e) {
                    // 測試環境的 API
                    response = await axios.get('/api/stores/profile', {
                        params: { ownerId: ownerIdValue }
                    })
                }

                console.log('✅ [Pinia] 店家資料:', response.data)

                if (response.data) {
                    // 🔥 處理不同的回應格式
                    if (Array.isArray(response.data) && response.data.length > 0) {
                        // 如果是陣列，取第一個店家
                        const firstStore = response.data[0]
                        setStoreProfile(firstStore)
                        console.log('📋 [Pinia] 設定主要店家:', firstStore)
                    } else if (typeof response.data === 'object') {
                        // 如果是物件，直接設定
                        setStoreProfile(response.data)
                        console.log('📋 [Pinia] 設定店家資料:', response.data)
                    } else {
                        console.warn('⚠️ [Pinia] 該 owner 沒有店家資料')
                        setStoreProfile({})
                    }
                } else {
                    setStoreProfile({})
                }
            } catch (apiError) {
                // 🔥 如果 API 失敗，嘗試從 localStorage 讀取
                console.warn('⚠️ [Pinia] API 呼叫失敗，嘗試從 localStorage 讀取:', apiError.message)
                const storedProfile = localStorage.getItem('storeProfile')
                if (storedProfile) {
                    try {
                        const parsedProfile = JSON.parse(storedProfile)
                        setStoreProfile(parsedProfile)
                        console.log('📋 [Pinia] 從 localStorage 載入店家資料:', parsedProfile)
                    } catch (parseError) {
                        console.error('❌ [Pinia] 無法解析 localStorage 中的店家資料')
                        setStoreProfile({})
                    }
                } else {
                    setStoreProfile({})
                }
            }

        } catch (error) {
            console.error('❌ [Pinia] fetchStoreProfile failed:', error)
            setStoreProfile({})
        }
    }

    // === 🔥 關鍵功能：syncFromStorage（測試環境需要的）===
    function syncFromStorage() {
        // 同步原始用戶狀態
        fullName.value = localStorage.getItem('userFullName') || ''
        userId.value = localStorage.getItem('userId') || ''
        email.value = localStorage.getItem('userEmail') || ''
        token.value = localStorage.getItem('token') || ''

        // 同步業者狀態
        ownerFullName.value = localStorage.getItem('storeFullName') || ''
        ownerEmail.value = localStorage.getItem('storeEmail') || ''
        ownerId.value = localStorage.getItem('ownerId') || ''

        // 同步餐廳狀態
        storeId.value = localStorage.getItem('storeId') || ''
        storeName.value = localStorage.getItem('storeName') || ''

        // 同步地址資料
        try {
            const storedAddress = localStorage.getItem('address')
            if (storedAddress) {
                address.value = JSON.parse(storedAddress)
            }
        } catch {
            address.value = {
                city: '',
                district: '',
                zip: '',
                street: '',
                door: '',
                enAddress: '',
                lat: '',
                lng: ''
            }
        }

        // 同步店家資料
        try {
            const storedProfile = localStorage.getItem('storeProfile')
            if (storedProfile) {
                storeProfile.value = JSON.parse(storedProfile)
            }
        } catch {
            storeProfile.value = {
                id: '',
                storeName: '',
                address: '',
                intro: '',
                phone: '',
                email: '',
                isEmailVerified: false
            }
        }
    }

    // === 初始化函數（正式環境需要的）===
    function initializeFromStorage() {
        // 🔥 使用統一的 syncFromStorage
        syncFromStorage()
    }

    // === 全部清空函數（測試環境需要的）===
    function logoutAll() {
        // 清空原始用戶狀態
        fullName.value = ''
        userId.value = ''
        email.value = ''
        token.value = ''
        isLogin.value = false

        // 清空業者狀態
        ownerLogout()

        // 清空餐廳狀態
        storeId.value = ''
        storeName.value = ''
        address.value = {
            city: '', district: '', zip: '', street: '', door: '', enAddress: '', lat: '', lng: ''
        }
        storeProfile.value = {
            id: '',
            storeName: '',
            address: '',
            intro: '',
            phone: '',
            email: '',
            isEmailVerified: false
        }

        // 清空 localStorage
        localStorage.removeItem('userFullName')
        localStorage.removeItem('userId')
        localStorage.removeItem('userEmail')
        localStorage.removeItem('token')
        localStorage.removeItem('storeId')
        localStorage.removeItem('storeName')
        localStorage.removeItem('address')
        localStorage.removeItem('storeProfile')
    }

    // 🔥 初始化時同步（測試環境需要的）
    syncFromStorage()

    return {
        // 原始用戶狀態和方法（正式環境）
        fullName,
        setFullName,
        email,
        setEmail,
        token,
        setToken,
        userId,
        setUserId,
        isLogin,
        setLogin,

        // 業者狀態和方法（測試環境）
        ownerFullName,
        setOwnerFullName,
        ownerEmail,
        setOwnerEmail,
        ownerId,
        setOwnerId,
        isOwnerLogin,
        ownerLogout,

        // 餐廳狀態和方法（測試環境）
        storeId,
        setStoreId,
        storeName,
        setStoreName,
        address,
        setAddressInfo,

        // 店家資料（兩個環境都需要）
        storeProfile,
        setStoreProfile,
        fetchStoreProfile,

        // 工具方法
        syncFromStorage,
        initializeFromStorage,
        logoutAll
    }
}, {
    // ✨ 新增這個配置物件來啟用持久化
    persist: {
        storage: localStorage, // 指定使用 sessionStorage
        // 或者使用 localStorage: storage: localStorage,
        // key: 'my-cart-data', // 可選：自訂儲存到 sessionStorage/localStorage 的鍵名，預設是 store 的 id ('cart')
    }
});