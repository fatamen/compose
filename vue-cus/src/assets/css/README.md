# GoldenBowl Restaurant Theme CSS

## 概述

這個 CSS 文件為 GoldenBowl 專案的餐廳模組提供了專用的樣式主題，使用命名空間來避免與其他專案的樣式衝突。

## 使用方法

### 1. 基本使用

在組件中使用新的命名空間：

```vue
<template>
  <div class="your-component goldenbowl-restaurant-theme">
    <!-- 你的內容 -->
  </div>
</template>

<style scoped>
@import "@/assets/css/restaurant-theme.css";
</style>
```

### 2. CSS 變量

所有 CSS 變量都使用 `gb-` 前綴來避免衝突：

```css
.your-component {
  background: var(--gb-restaurant-bg-primary);
  color: var(--gb-restaurant-text-primary);
  border: 1px solid var(--gb-restaurant-border-light);
}
```

### 3. 預定義類別

使用預定義的類別來快速應用樣式：

```vue
<template>
  <div class="gb-restaurant-card">
    <button class="gb-restaurant-btn-primary">按鈕</button>
  </div>
</template>
```

## 可用的 CSS 變量

### 主色調

- `--gb-restaurant-primary`: #ffba20
- `--gb-restaurant-primary-hover`: #e6a61d
- `--gb-restaurant-primary-light`: #ffcd52
- `--gb-restaurant-primary-dark`: #766C08

### 背景色

- `--gb-restaurant-bg-primary`: #ffffff
- `--gb-restaurant-bg-secondary`: #E7E7E7
- `--gb-restaurant-bg-light`: #f8f8f8
- `--gb-restaurant-bg-card`: #ffffff

### 文字色

- `--gb-restaurant-text-primary`: #333333
- `--gb-restaurant-text-secondary`: #766C08
- `--gb-restaurant-text-muted`: #888888

### 邊框色

- `--gb-restaurant-border-light`: #E7E7E7
- `--gb-restaurant-border-medium`: #cccccc
- `--gb-restaurant-border-dark`: #999999

### 漸變

- `--gb-restaurant-gradient-primary`: 主漸變
- `--gb-restaurant-gradient-light`: 淺色漸變

## 預定義類別

- `.gb-restaurant-theme`: 基本主題類別
- `.gb-restaurant-card`: 卡片樣式
- `.gb-restaurant-btn-primary`: 主要按鈕
- `.gb-restaurant-glow`: 發光效果
- `.gb-restaurant-gradient-bg`: 漸變背景

## 向後兼容

為了保持向後兼容性，舊的變量名稱和類別仍然可用，但建議使用新的命名空間。

## 注意事項

1. 所有變量都定義在 `.goldenbowl-restaurant-theme` 類別下
2. 使用 `gb-` 前綴避免與其他專案衝突
3. 確保在組件中正確導入 CSS 文件
