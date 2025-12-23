# 章節 4 ｜ Ajax 程式碼的成長與結構化

## <a id="目錄"></a>目錄

- [4-1 當 Ajax 程式碼開始膨脹](#CH4-1)
- [4-2 資料狀態與畫面呈現的關係](#CH4-2)
- [4-3 前端程式碼分層概念](#CH4-3)
- [4-4 Ajax 與 SPA 的關係](#CH4-4)
- [4-5 Ajax 與 Vue 的角色對照](#CH4-5)
- [4-6 為什麼學過 Ajax，Vue 會變得好理解](#CH4-6)

---

### 序

歡迎來到第四章！如果你的 Ajax 程式碼開始寫得越來越長，長到你自己都不想看第二眼的時候，那這章就是為你準備的。

我們一開始學 `fetch` 覺得很酷，但如果一個頁面有十個 API 要打，每個 `fetch` 裡都要寫 `try-catch`、都要處理 JSON、都要手動更新 DOM... 你的 JavaScript 檔案很快就會變成一坨難以維護的「義大利麵」。

這一章，我們要來學著怎麼把這些程式碼「收納」好。我們會聊聊怎麼把 API 呼叫抽離出來，也會談談為什麼我們會需要像 Vue 這樣的框架來拯救我們脫離「手動操作 DOM」的苦海。

這是一個從「寫出會動的程式」進化到「寫出漂亮架構」的關鍵轉折點喔！

---

## <a id="CH4-1"></a>[4-1 當 Ajax 程式碼開始膨脹](#CH4-1)

### 請求邏輯分散

如果每個按鈕的 click 事件都直接寫 `fetch(...)`，專案中會充斥著大量重複的程式碼：

- 重複的 Base URL (如 `http://localhost:8080/api`)
- 重複的 Header 設定 (如 `Authorization: Bearer ...`)
- 重複的錯誤處理邏輯 (每個請求都要檢查 401 嗎？)

### 畫面與資料邏輯混雜

jQuery 時代常見的寫法，在 Ajax callback 裡面直接拼接 HTML 字串、操作 DOM class、綁定事件。

```javascript
// 意大利麵條式程式碼 (Spaghetti Code)
fetch("/api/users")
  .then((res) => res.json())
  .then((users) => {
    users.forEach((user) => {
      // 在邏輯裡面寫畫面，混亂的開始
      $("#list").append(
        `<div class="user ${user.isActive ? "active" : ""}">${user.name}</div>`
      );
    });
  });
```

這導致修改畫面要動邏輯，修改邏輯又要怕弄壞畫面。

---

## <a id="CH4-2"></a>[4-2 資料狀態與畫面呈現的關係](#CH4-2)

### 態未集中管理的後果

試想一個購物車功能，右上角的圖示要顯示數量，結帳按鈕要顯示總金額，側邊欄要顯示商品清單。
如果我們用傳統方式，在 `fetch` 回來後，要手動去抓這三個 DOM 元素來修改。

如果不小心漏改了一個，或是順序錯了，就會發生「畫面顯示與實際資料不一致」的 Bug（例如：資料庫已經刪除了，購物車數字卻沒變少）。

這就是所謂的 **UI State Sync (介面狀態同步)** 問題。

---

## <a id="CH4-3"></a>[4-3 前端程式碼分層概念](#CH4-3)

為了治理解構混亂，我們應該在前端引入分層架構，就像後端有 Controller / Service / Dao 一樣。

### 1. API 層 (Access Layer)

專門負責發送 HTTP 請求。這裡不處理畫面，只處理「跟伺服器溝通」。

```javascript
// api/userApi.js
const BASE_URL = "/api/users";

export const userApi = {
  getAll: () => fetch(BASE_URL).then((res) => res.json()),
  getById: (id) => fetch(`${BASE_URL}/${id}`).then((res) => res.json()),
  create: (data) =>
    fetch(BASE_URL, {
      method: "POST",
      body: JSON.stringify(data),
      // ... headers
    }),
};
```

### 2. Service 層 (Business Logic) + View 層

在沒有框架時，我們可能會寫一個 `UserService.js` 來處理資料轉換，然後在 `index.js` (View) 裡面呼叫。

```javascript
// index.js (View)
import { userApi } from "./api/userApi.js";

async function initPage() {
  const users = await userApi.getAll(); // 呼叫 API 層
  renderList(users); // 更新畫面
}
```

這樣至少我們把 `fetch` 的細節藏起來了。

---

## <a id="CH4-4"></a>[4-4 Ajax 與 SPA 的關係](#CH4-4)

### 單頁應用 (SPA) 的真正意義

SPA (Single Page Application) 是 Ajax 技術的極致應用。
整個網頁只有一個 `index.html` 檔案，所有的頁面切換、內容更新，全部都由 JavaScript 透過 Ajax 動態載入資料並渲染完成。

使用者感覺不到「換頁」的閃動，體驗如原生 App 般流暢。

### 由資料驅動畫面 (Data-Driven)

SPA 的核心哲學是：**UI 是 State 的函數** (`UI = f(State)`)。

我們不再手動操作 DOM (`querySelector`, `innerHTML`)，而是專注於改變 **資料 (State)**。當資料改變時，框架（如 Vue / React）會自動偵測並幫我們更新畫面。

---

## <a id="CH4-5"></a>[4-5 Ajax 與 Vue 的角色對照](#CH4-5)

現在我們可以把 Vue 的拼圖拼上去了：

| 角色         | 職責                                                 | 負責人                   |
| :----------- | :--------------------------------------------------- | :----------------------- |
| **搬運工**   | 負責跟後端 API 要麵粉（資料）。                      | **Ajax (fetch / axios)** |
| **麵包師傅** | 負責管理麵粉庫存（狀態），並把麵粉做成麵包（畫面）。 | **Vue.js**               |

Vue 並不負責發送請求，它通常會配合 `axios` 或原生 `fetch` 來使用。

```javascript
// Vue 3 Composition API 範例
const { createApp, ref, onMounted } = Vue;

createApp({
  setup() {
    const products = ref([]); // 狀態 (State)

    onMounted(async () => {
      // Ajax 取得資料，更新 State
      // Vue 會自動偵測到 products 變了，自動更新畫面
      products.value = await productApi.getAll();
    });

    return { products };
  },
}).mount("#app");
```

---

## <a id="CH4-6"></a>[4-6 為什麼學過 Ajax，Vue 會變得好理解](#CH4-6)

很多初學者直接學 Vue 會覺得很抽象，但如果你先懂了 Ajax，你會發現 Vue 的很多設計都是為了**解決原生 Ajax 的痛點**：

1.  **Vue 的 `v-for`**：就是在解決我們以前寫 `forEach` 拼接 HTML 字串的痛苦。
2.  **Vue 的 `v-model`**：就是在解決我們手動監聽 `input` 事件抓值的麻煩。
3.  **Vue 的 `created / mounted`**：就是我們以前寫 `$(document).ready()` 發起初始化 Ajax 請求的地方。

所以，恭喜你！學完這一章，你已經拿到通往現代前端框架的入場券了。

---

[下一章：章節 5 ｜ 前端分離架構與安全設計](./Chapter5_ArchitectureSecurity.md)
