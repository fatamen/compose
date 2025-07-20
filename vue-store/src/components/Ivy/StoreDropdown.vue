    <template>
        <div class="user-dropdown-container">
        <!-- 未登入 -->
        <span
            v-if="!storeFullName"
            style="cursor:pointer"
            @click="$emit('show-login')">
            <i class="bi bi-person-circle me-1"></i>
            餐廳使用者*
        </span>
        <!-- 已登入 -->
        <div v-else>
            <a href="#" class="user-link" @click.prevent="toggleDropdown">
            <i class="bi bi-person-circle me-1"></i>
            <span>{{ storeFullName }}</span>
            </a>
            <div class="dropdown-menu" v-if="showDropdown">
            <ul>
                <li @click="navigateTo('editStoreUser')">
                <i class="bi bi-person me-2"></i>餐廳業者資料
                </li>
                <li @click="navigateTo('editStore')">
                <i class="bi bi-shop me-2"></i>餐廳資料
                </li>
                <li @click="logout">
                <i class="bi bi-box-arrow-right me-2"></i>登出
                </li>
            </ul>
            </div>
        </div>
        </div>
    </template>
    
    <script setup>
    import { ref, onMounted, onUnmounted, computed } from 'vue'
    import { useRouter } from 'vue-router'
    import { useUserStore } from '@/stores/user.js'
    
    const emit = defineEmits(['show-login'])
    
    const showDropdown = ref(false)
    const router = useRouter()
    const store = useUserStore()
    const storeFullName = computed(() => store.ownerFullName)
    
    const toggleDropdown = () => { showDropdown.value = !showDropdown.value }
    const navigateTo = (path) => {
        router.push(`/${path}`)
        showDropdown.value = false
    }
    const logout = () => {
        store.ownerLogout()
        showDropdown.value = false
        if (router.currentRoute.value.path !== '/store') {
        router.push('/store')
        }
        emit('show-login')
    }
    const handleClickOutside = (event) => {
        if (!event.target.closest('.user-dropdown-container')) {
        showDropdown.value = false
        }
    }
    onMounted(() => {
        document.addEventListener('click', handleClickOutside)
    })
    onUnmounted(() => {
        document.removeEventListener('click', handleClickOutside)
    })
    </script>

    <style scoped>
    .user-dropdown-container {
    position: relative;
    display: inline-block;
    }

    .user-link {
    color: white;
    text-decoration: none;
    font-size: 16px;
    padding: 8px 12px;
    cursor: pointer;
    }

    .user-link:hover {
    background-color: rgba(255, 255, 255, 0.1);
    border-radius: 4px;
    }

    .dropdown-menu {
    position: absolute;
    top: 100%;
    right: 0;
    background-color: white;
    border: 1px solid #ddd;
    border-radius: 4px;
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
    z-index: 1000;
    /* 確保 z-index 足夠高 */
    min-width: 160px;
    margin-top: 5px;
    display: block;
    /* 添加此行，確保顯示 */
    }

    .dropdown-menu ul {
    list-style: none;
    padding: 0;
    margin: 0;
    }

    /* 游標滑到使用者按鈕會變色 */
    .dropdown-menu li {
    padding: 12px 16px;
    font-size: 14px;
    color: #333;
    cursor: pointer;
    border-radius: 4px;
    transition: background-color 0.2s;
    }

    /* 游標滑到下拉式選單會變底色 */
    .dropdown-menu li:hover {
    background-color: #fff3cd;
    }

    /* 游標滑到下拉式選單圖示會變色 */
    .dropdown-menu li:hover i {
    color: #ffba20;
    }

    /* 預設為黃色文字 */
    .dropdown-menu li:last-child {
    border-top: 1px solid #ddd;
    color: #ffba20;
    font-weight: 500;
    }

    /* 滑鼠移上去時反轉背景與文字顏色 */
    .dropdown-menu li:last-child:hover {
    color: #5c3202;
    }
    </style>