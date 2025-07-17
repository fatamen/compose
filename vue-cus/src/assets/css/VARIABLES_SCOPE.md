# CSS 變量作用域管理

## 問題背景

### 舊的方式（全局定義）

```css
:root {
  --restaurant-primary: #ffba20;
  --restaurant-bg-primary: #ffffff;
}
```

**問題：**

- 變量在整個頁面都可用
- 容易與其他專案的變量衝突
- 無法控制變量的作用域
- 可能被其他 CSS 意外覆蓋

### 新的方式（類別內定義）

```css
.goldenbowl-restaurant-theme {
  --gb-restaurant-primary: #ffba20;
  --gb-restaurant-bg-primary: #ffffff;
}
```

**優勢：**

- 變量只在特定類別內可用
- 避免與其他專案衝突
- 更好的封裝性
- 更容易維護

## 實現原理

### 1. CSS 變量作用域規則

CSS 變量的作用域遵循 CSS 的層疊規則：

```css
/* 全局作用域 */
:root {
  --global-color: red;
}

/* 類別作用域 */
.my-class {
  --local-color: blue;
}

/* 使用變量 */
.some-element {
  color: var(--global-color); /* 可以訪問 */
  background: var(--local-color); /* 只有在 .my-class 的子元素中才能訪問 */
}
```

### 2. 實際應用範例

```vue
<template>
  <!-- 這個 div 及其子元素可以使用餐廳主題變量 -->
  <div class="goldenbowl-restaurant-theme">
    <div class="restaurant-card">
      <h2>餐廳標題</h2>
      <p>餐廳描述</p>
    </div>
  </div>

  <!-- 這個 div 無法使用餐廳主題變量 -->
  <div class="other-component">
    <p>其他內容</p>
  </div>
</template>

<style scoped>
/* 在 .goldenbowl-restaurant-theme 內的元素可以使用這些變量 */
.restaurant-card {
  background: var(--gb-restaurant-bg-primary);
  color: var(--gb-restaurant-text-primary);
  border: 1px solid var(--gb-restaurant-border-light);
}

/* 在 .other-component 內的元素無法使用餐廳變量 */
.other-component {
  background: #f0f0f0; /* 必須使用具體顏色值 */
}
</style>
```

## 變量繼承機制

### 1. 向下繼承

```css
.goldenbowl-restaurant-theme {
  --gb-primary: #ffba20;
}

.goldenbowl-restaurant-theme .child-element {
  color: var(--gb-primary); /* 可以訪問父元素的變量 */
}
```

### 2. 向上查找

```css
.goldenbowl-restaurant-theme {
  --gb-primary: #ffba20;
}

.goldenbowl-restaurant-theme .deep-child {
  color: var(--gb-primary); /* 會向上查找最近的定義 */
}
```

## 向後兼容性實現

### 1. 變量映射

```css
.goldenbowl-restaurant-theme {
  /* 新變量 */
  --gb-restaurant-primary: #ffba20;

  /* 舊變量映射到新變量 */
  --restaurant-primary: var(--gb-restaurant-primary);
}
```

### 2. 類別映射

```css
/* 新類名 */
.gb-restaurant-card {
  background: var(--gb-restaurant-bg-primary);
}

/* 舊類名繼承新類名 */
.restaurant-card {
  composes: gb-restaurant-card;
}
```

## 最佳實踐

### 1. 命名規範

```css
/* 使用專案前綴 */
--gb-restaurant-primary
--gb-restaurant-bg-primary
--gb-restaurant-text-primary
```

### 2. 組織結構

```css
/* 主要變量定義 */
.goldenbowl-restaurant-theme {
  /* 顏色變量 */
  --gb-restaurant-primary: #ffba20;

  /* 間距變量 */
  --gb-restaurant-spacing-sm: 0.5rem;

  /* 字體變量 */
  --gb-restaurant-font-size: 1rem;
}

/* 功能類別 */
.gb-restaurant-card {
  /* 使用變量 */
  background: var(--gb-restaurant-bg-primary);
  padding: var(--gb-restaurant-spacing-sm);
  font-size: var(--gb-restaurant-font-size);
}
```

### 3. 使用方式

```vue
<template>
  <!-- 正確：在容器上應用主題類別 -->
  <div class="goldenbowl-restaurant-theme">
    <div class="restaurant-content">
      <!-- 內容 -->
    </div>
  </div>
</template>

<style scoped>
/* 正確：在主題類別內使用變量 */
.restaurant-content {
  background: var(--gb-restaurant-bg-primary);
  color: var(--gb-restaurant-text-primary);
}
</style>
```

## 調試技巧

### 1. 檢查變量是否可用

```css
.debug-element {
  /* 如果變量未定義，會使用後備值 */
  color: var(--gb-restaurant-primary, red);
}
```

### 2. 使用瀏覽器開發者工具

- 在 Elements 面板中檢查元素是否有正確的類別
- 在 Computed 面板中查看 CSS 變量的值
- 在 Styles 面板中查看變量的定義位置

## 遷移指南

### 從 :root 遷移到類別作用域

1. **創建容器類別**

```css
.goldenbowl-restaurant-theme {
  /* 將 :root 中的變量移動到這裡 */
}
```

2. **更新組件模板**

```vue
<!-- 舊方式 -->
<div class="restaurant-component">

<!-- 新方式 -->
<div class="restaurant-component goldenbowl-restaurant-theme">
```

3. **更新 CSS 變量名稱**

```css
/* 舊方式 */
--restaurant-primary

/* 新方式 */
--gb-restaurant-primary
```

4. **測試兼容性**

- 確保所有組件都正確應用主題類別
- 檢查變量是否在正確的作用域內可用
- 驗證向後兼容性是否正常工作
