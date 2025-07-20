<template>
    <div class="modal-bg" v-if="show">
        <div class="modal-dialog modal-dialog-centered modal-lg">
            <div class="modal-content">
                <div class="modal-header border-0 pb-0 justify-content-between">
                    <button class="btn nav-btn" @click="emit('back')">
                        <svg width="28" height="28" fill="none" viewBox="0 0 24 24">
                            <path d="M15 6l-6 6 6 6" stroke="#222" stroke-width="2" stroke-linecap="round"
                                stroke-linejoin="round" />
                        </svg>
                    </button>
                    <button type="button" class="btn-close custom-close" @click="emit('close')"></button>
                </div>
                
                <div class="modal-body">
                    <div v-if="errorMsg" class="alert alert-danger mb-3">{{ errorMsg }}</div>
                    
                    <h3 class="fw-bold mb-2">驗證您的商家地址</h3>
                    <div class="mb-3 text-secondary" style="font-size:15px;">
                        消費者及外送夥伴將透過下方地址找到您的餐廳或商店
                    </div>

                    <form @submit.prevent="goNext">
                        <!-- 城市 -->
                        <div class="mb-3 position-relative">
                            <select class="form-select no-arrow" v-model="city" @change="searchLocation" required>
                                <option value="" disabled>城市 *</option>
                                <option v-for="item in cityList" :key="item.city" :value="item.city">{{ item.city }}</option>
                            </select>
                            <button class="clear-btn" v-if="city" @mousedown.prevent="clearCity" type="button"
                                style="right:11px;top:13px;">
                                <svg width="18" height="18" fill="none" viewBox="0 0 22 22">
                                    <path d="M6 6l10 10M16 6L6 16" stroke="#ccc" stroke-width="2" stroke-linecap="round" />
                                </svg>
                            </button>
                        </div>
                        
                        <!-- 地區 -->
                        <template v-if="city">
                            <div class="mb-3">
                                <select class="form-select no-arrow" v-model="district" @change="searchLocation" required>
                                    <option value="" disabled>地區 *</option>
                                    <option v-for="d in districtList" :key="d.name" :value="d.name">{{ d.name }}</option>
                                </select>
                            </div>
                            <!-- 郵遞區域 -->
                            <div class="mb-3">
                                <input type="text" class="form-control" v-model="zip" placeholder="郵遞區域 *" readonly required />
                            </div>
                            <!-- 街道 -->
                            <div class="mb-3">
                                <input type="text" class="form-control" v-model="street" placeholder="街道名稱 *"
                                    @blur="searchLocation" required />
                            </div>
                            <!-- 門牌 -->
                            <div class="mb-3">
                                <input type="text" class="form-control" v-model="door" placeholder="門牌號碼及樓層(其他地址詳細資訊) *"
                                    @blur="searchLocation" required />
                            </div>
                            <!-- 公司地址英文 -->
                            <div class="mb-3">
                                <input type="text" class="form-control" v-model="enAddress" placeholder="您的公司地址英文翻譯 *"
                                    required />
                            </div>
                        </template>

                        <!-- Leaflet 地圖 -->
                        <div class="mb-3 d-flex align-items-center">
                            <button type="button" class="btn btn-sm btn-secondary me-2" @click="searchLocation">
                                查詢位置
                            </button>
                            <small class="text-muted">（離開欄位或按此按鈕都會自動定位）</small>
                        </div>
                        <div ref="mapContainer" style="height:220px; border-radius:12px; border: 1px solid #ddd;"></div>

                        <!-- 緯經度 -->
                        <div v-if="lat && lng" class="mb-3 text-secondary">
                            緯度：<span class="fw-bold">{{ lat }}</span>，
                            經度：<span class="fw-bold">{{ lng }}</span>
                        </div>

                        <!-- 商家地址（自動帶入、readonly） -->
                        <div class="mb-3">
                            <label class="form-label">商家地址</label>
                            <input type="text" class="form-control" :value="mainAddress" placeholder="商家地址" readonly />
                        </div>

                        <div v-if="error" class="text-danger mb-3">{{ error }}</div>

                        <button type="submit" class="btn btn-main w-100" :disabled="loading || !isFormValid">
                            <span v-if="loading">處理中...</span>
                            <span v-else>繼續</span>
                        </button>
                    </form>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { useUserStore } from '@/stores/user.js'
import { cityDistrictMap } from '@/assets/cityDistrictMap.js'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import axios from '@/plungins/axios.js'

const props = defineProps({ 
    show: Boolean,
    storeData: Object  // 從 RegisterStoreInfoModal 傳來的店家資料
})

const emit = defineEmits(['close', 'submit', 'back'])

const userStore = useUserStore()
const city = ref('')
const district = ref('')
const zip = ref('')
const street = ref('')
const door = ref('')
const enAddress = ref('')
const lat = ref('')
const lng = ref('')
const errorMsg = ref('')
const error = ref('')
const loading = ref(false)

const mapContainer = ref(null)
let map = null
let marker = null

// 當 Modal 顯示時初始化
watch(() => props.show, async (show) => {
    if (show) {
        // 重置錯誤狀態
        errorMsg.value = ''
        error.value = ''
        
        // 如果有之前儲存的地址資料，載入它們
        if (userStore.address && userStore.address.city) {
            city.value = userStore.address.city
            district.value = userStore.address.district
            zip.value = userStore.address.zip
            street.value = userStore.address.street
            door.value = userStore.address.door
            enAddress.value = userStore.address.enAddress
            lat.value = userStore.address.lat
            lng.value = userStore.address.lng
        }
        
        // 等待 DOM 更新後初始化地圖
        await nextTick()
        initMap()
    } else {
        // Modal 關閉時清理地圖
        if (map) {
            map.remove()
            map = null
            marker = null
        }
    }
})

// 初始化地圖
function initMap() {
    if (!mapContainer.value) return
    
    try {
        map = L.map(mapContainer.value, {
            zoomControl: false,
            dragging: false,
            scrollWheelZoom: false,
            doubleClickZoom: false,
            attributionControl: false,
        }).setView([25.0340, 121.5645], 13)
        
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png').addTo(map)

        if (lat.value && lng.value) {
            const p = [parseFloat(lat.value), parseFloat(lng.value)]
            marker = L.marker(p).addTo(map)
            map.setView(p, 17)
        }
    } catch (e) {
        console.error('地圖初始化失敗:', e)
        errorMsg.value = '地圖載入失敗，但不影響地址驗證功能'
    }
}

// 城市和地區資料
const cityList = cityDistrictMap
const districtList = computed(() => {
    const c = cityDistrictMap.find(i => i.city === city.value)
    return c ? c.districts : []
})

// 監聽城市和地區變化，自動填入郵遞區號
watch([city, district], () => {
    const c = cityDistrictMap.find(i => i.city === city.value)
    const d = c?.districts.find(i => i.name === district.value)
    zip.value = d?.zip || ''
})

// 完整地址
const mainAddress = computed(() =>
    [zip.value, city.value, district.value, street.value, door.value]
        .filter(Boolean).join(' ')
)

// 表單驗證
const isFormValid = computed(() => 
    city.value && district.value && zip.value && street.value && 
    door.value && enAddress.value && lat.value && lng.value
)

// 監聽地址變化，自動查詢位置
watch([city, district, street, door], ([c, d, s, o]) => {
    if (c && d && s && o) {
        searchLocation()
    } else {
        lat.value = ''
        lng.value = ''
        if (marker) {
            marker.remove()
            marker = null
        }
    }
})

// 使用後端 API 查詢經緯度
async function searchLocation() {
    if (!mainAddress.value) return
    
    errorMsg.value = ''
    try {
        const url = `/api/geo/latlng?address=${encodeURIComponent(mainAddress.value)}`
        console.log(mainAddress.value)
        
        const res = await axios.get(url)
        console.log(res)
        if (!res.data.success) {
            errorMsg.value = res.data.message || '查無經緯度，請確認地址格式'
            lat.value = ''
            lng.value = ''
            if (marker) {
                marker.remove()
                marker = null
            }
            return
        }
        
        lat.value = res.data.lat
        lng.value = res.data.lng || res.data.lng

        // 更新地圖
        if (map) {
            const p = [parseFloat(lat.value), parseFloat(lng.value)]
            map.setView(p, 17)
            if (!marker) {
                marker = L.marker(p).addTo(map)
            } else {
                marker.setLatLng(p)
            }
        }
    } catch (e) {
        console.error('地址查詢失敗:', e)
        errorMsg.value = 'API 請求失敗，請稍後再試'
        lat.value = ''
        lng.value = ''
        if (marker) {
            marker.remove()
            marker = null
        }
    }
}

// 清除城市選擇
function clearCity() {
    city.value = ''
    district.value = ''
    zip.value = ''
    enAddress.value = ''
}

// 提交地址資料
async function goNext() {
    if (!props.storeData?.storeId) {
        error.value = '找不到 storeId，請重新開始註冊流程'
        return
    }

    if (!isFormValid.value) {
        error.value = '請完整填寫所有必要欄位'
        return
    }

    loading.value = true
    error.value = ''

    try {
        // 儲存地址資訊到 Pinia
        userStore.setAddressInfo({
            city: city.value,
            district: district.value,
            zip: zip.value,
            street: street.value,
            door: door.value,
            enAddress: enAddress.value,
            lat: lat.value,
            lng: lng.value
        })

        const payload = {
            storeId: props.storeData.storeId,
            address: mainAddress.value,
            city: city.value,
            district: district.value,
            zip: zip.value,
            street: street.value,
            door: door.value,
            enAddress: enAddress.value,
            lat: lat.value,
            lng: lng.value
        }

        const res = await axios.post('/api/stores/updateAddress', payload)
        
        if (res.data.success) {
            // 成功後將完整資料傳給父組件，進入下一步
            const addressData = {
                ...payload,
                mainAddress: mainAddress.value
            }
            
            emit('submit', addressData)
        } else {
            error.value = res.data.message || '地址更新失敗'
        }
    } catch (e) {
        console.error('地址提交失敗:', e)
        if (e.response) {
            error.value = `伺服器錯誤 (HTTP ${e.response.status})：${e.response.data?.message || ''}`
        } else {
            error.value = '儲存失敗，請稍後再試'
        }
    } finally {
        loading.value = false
    }
}

// 組件卸載時清理地圖
onUnmounted(() => {
    if (map) {
        map.remove()
        map = null
        marker = null
    }
})
</script>

<style scoped>
.modal-bg {
    position: fixed;
    inset: 0;
    background: rgba(0, 0, 0, 0.08);
    z-index: 9999;
    display: flex;
    align-items: center;
    justify-content: center;
}

.modal-dialog {
    max-width: 650px;
    width: 95vw;
    margin: 0 auto;
    max-height: 90vh;
    overflow-y: auto;
}

.modal-content {
    background: #fff !important;
    border-radius: 20px;
    box-shadow: 0 2px 24px 4px rgba(0, 0, 0, 0.10);
    border: none;
    padding: 1.5rem;
    
    width: 100%;
    max-width: 600px;
    max-height: 90vh;
    overflow-y: auto;
}

.nav-btn {
    background: none;
    border: none;
    padding: 0;
    margin-left: -5px;
    margin-top: -5px;
    box-shadow: none;
    outline: none;
}

.form-control, .form-select {
    border-radius: 8px;
    border: 2px solid #ddd;
    font-size: 16px;
    padding: 12px;
}

.form-control:focus, .form-select:focus {
    border-color: #ffba20;
    box-shadow: 0 0 0 1px #ffba2021;
}

.btn-main {
    background: #ffba20;
    color: #fff;
    font-weight: bold;
    font-size: 20px;
    border-radius: 10px;
    border: none;
    transition: filter 0.15s;
    box-shadow: 0 2px 8px 1px #ffba200f;
    height: 48px;
}

.btn-main:hover {
    filter: brightness(1.08);
    background: #ffba20;
}

.btn-main:disabled {
    opacity: 0.6;
    cursor: not-allowed;
}

.text-danger {
    color: #dc3545;
    font-size: 14px;
}

.clear-btn {
    background: none;
    border: none;
    position: absolute;
    right: 9px;
    top: 10px;
    cursor: pointer;
    z-index: 10;
    padding: 0;
}

select.no-arrow {
    appearance: none;
    -webkit-appearance: none;
    background-image: none !important;
}

select.no-arrow::-ms-expand {
    display: none;
}

.alert {
    padding: 12px;
    border-radius: 8px;
    border: 1px solid transparent;
}

.alert-danger {
    color: #721c24;
    background-color: #f8d7da;
    border-color: #f5c6cb;
}
</style>