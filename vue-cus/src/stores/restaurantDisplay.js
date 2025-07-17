// stores/restaurantDisplay.js
import { defineStore } from 'pinia';

export const useRestaurantDisplayStore = defineStore('restaurantDisplay', {
  state: () => ({
    // true 表示顯示全部餐廳，false 表示顯示已收藏餐廳
    // 預設為 true (顯示全部)
    showAllRestaurants: true,
  }),
  actions: {
    toggleDisplayMode() {
      this.showAllRestaurants = !this.showAllRestaurants;
    },
    setShowAllRestaurants(value) {
      this.showAllRestaurants = value;
    }
  },
  // 可以選擇是否要持久化這個狀態，如果用戶希望下次打開應用程式時保持上次的選擇
  // plugins: [
  //   createPersistedState({
  //     key: 'restaurantDisplayMode',
  //     storage: localStorage,
  //   }),
  // ],
});