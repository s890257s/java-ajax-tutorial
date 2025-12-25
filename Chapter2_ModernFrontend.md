# 章節 2 ｜ 前端工程師的武器庫 (Modern Frontend)

## <a id="toc"></a>目錄

- [2-1 JavaScript ES6+ 必備語法速成](#CH2-1)
- [2-2 非同步處理 (Asynchronous)](#CH2-2)
- [2-3 HTTP Client 的選擇：XHR vs Fetch vs Axios](#CH2-3)

---

### 序

歡迎來到第二章。

Java 工程師寫 JavaScript 最常遇到的問題就是：**「為什麼這個 JS 寫法跟我以前學的不一樣？」**
早期的 JS (ES5) 語法鬆散，但自從 2015 年 ES6 (ECMAScript 2015) 發布後，JS 變得越來越像一門成熟的語言。

在開始大量寫 Ajax 之前，我們要先升級你的武器庫。這一章會帶你認識現代前端開發必備的語法糖與工具。

---

## <a id="CH2-1"></a>[2-1 JavaScript ES6+ 必備語法速成](#toc)

### 1. 變數宣告：`const` vs `let`

以前我們有 `var`，但它有奇怪的作用域問題，現在請直接無視它。
現代開發請遵守這條黃金法則：**「能用 `const` 就用 `const`，真的要變才用 `let`」**。

- **`const` (常數) - 預設首選**

  - **意義**：代表這個變數的**綁定 (Binding)** 不會被重新指派。
  - **好處**：
    1.  **安全**：避免變數被意外覆寫 (例如不小心把 `apiUrl` 改成別的字串)。
    2.  **可讀性**：看到 `const` 就知道，這個變數在函式後面不會變成別的東西。
  - **注意**：如果是物件/陣列，`const` 保護的是「變數不能指派給別人」，但「內容」是可以修改的。

- **`let` (變數)**
  - 只有在真的需要**重新賦值**時才使用，例如 `for` 迴圈的 `i`，或狀態切換 (`isLoading` false -> true)。

```javascript
// 1. const 保護變數不被重新賦值
const API_URL = "http://localhost:8080";
// API_URL = "abc"; // ❌ Error! 救你一命

// 2. 但物件內容是可以改的
const user = { name: "Alice" };
user.name = "Bob"; // ✅ OK! 只是改內容，記憶體位址沒變

// 3. 真的要變才用 let
let count = 0;
count++; // ✅ OK!
```

> **備註：為什麼使用 `const` 是現代開發趨勢？**
>
> 1.  **降低「意外被改掉」的風險 (這超重要)**
>
>     ```javascript
>     const apiUrl = "/api/users";
>     // ... 經過 500 行 ...
>     apiUrl = "/api/admin"; // ❌ 直接報錯，救你一命。
>     ```
>
> 2.  **程式碼一眼就知道「會不會變」**
>
>     ```javascript
>     const users = fetchUsers(); // 這是資料來源，不會改變。
>     let selectedUser = null; // 這是互動狀態，會改變。
>     ```
>
>     光看關鍵字，可讀性直接 +1 等級。
>
> 3.  **跟現代框架 (React/Vue) 很合拍**
>
>     現代前端更喜歡使用以下寫法
>
>     ```javascript
>     const newList = oldList.map(...); // 建立新陣列，保留原始資料
>     ```
>
>     而不是
>
>     ```javascript
>     list.push(...); // 直接修改原始資料
>     ```
>
>     const 在這裡就是在幫你「用語言層面提醒自己」：別偷偷修改原資料

### 2. 箭頭函式 (Arrow Functions)

Java 的 Lambda 是學 JS 的。這不只是語法糖，還解決了 `this` 指向混亂的問題。

```javascript
// 傳統函式
function add(a, b) {
  return a + b;
}

// 箭頭函式
const add = (a, b) => a + b; // 連 return 都可以省略

// 應用在 Ajax Callback
fetch(url).then((response) => response.json());
```

> **進階觀念：關於 `this` 的指向**
>
> 箭頭函式沒有自己的 `this`，它會**繼承外層宣告時**的 `this`。
> 這在撰寫物件方法或 Ajax Callback 時要特別注意：
>
> - 如果你需要 `this` 指向物件本身（例如物件的方法），請用**傳統函式**。
> - 如果你是在物件方法內呼叫 Ajax，想要在 Callback 內使用物件的屬性，請用**箭頭函式**（讓 `this` 維持指向物件）。
>
> ```javascript
> const user = {
>   name: "Alice",
>
>   // 1. 傳統函式：this 指向呼叫者 (user)
>   sayHi: function () {
>     console.log("Hi, I am " + this.name);
>   },
>
>   // 2. 箭頭函式：this 繼承自外層 (Window)，無法指向 user 物件
>   sayHiArrow: () => {
>     console.log("Hi, I am " + this.name); // undefined
>   },
>
>   // 3. 實戰應用：在 Callback 中使用箭頭函式
>   testAsync: function () {
>     setTimeout(() => {
>       // ✅ 正確：箭頭函式會保留外層 (testAsync) 的 this (即 user)
>       console.log("Async: I am " + this.name);
>     }, 100);
>
>     setTimeout(function () {
>       // ❌ 錯誤：傳統 callback 的 this 會指向 Window
>       console.log("Async: I am " + this.name);
>     }, 100);
>   },
> };
> ```

### 3. Template 模板字串 (Template Literals)

別再用 `+` 號拼湊 HTML 字串了，既不美觀又容易出錯。

```javascript
const name = "Alice";
const age = 30;

// 傳統寫法 (使用 + 號拼接)
const oldHtml =
  '<div class="card">' +
  "<h1>" +
  name +
  "</h1>" +
  "<p>Age: " +
  age +
  "</p>" +
  "</div>";

// 反引號 (Backticks) ` `
const html = `
    <div class="card">
        <h1>${name}</h1>
        <p>Age: ${age}</p>
    </div>
`;
```

### 4. 解構賦值 (Destructuring Assignment)

從物件或陣列中快速提取資料。這在處理 Ajax 回傳的 JSON 時超級好用。

```javascript
const user = {
  id: 1,
  username: "Alice",
  email: "Alice@example.com",
  address: { city: "Taipei" },
};

// 傳統寫法
const name = user.username;
const city = user.address.city;

// 解構寫法 (變數名稱必須對應 Key)
const {
  username,
  email,
  address: { city },
} = user;
console.log(username, city); // Alice, Taipei
```

### 5. 展開運算子 (Spread Operator)

使用 `...` 符號，可以快速展開陣列或物件，常用於複製或合併資料。

```javascript
// 陣列展開
const arr1 = [1, 2, 3];
const arr2 = [...arr1, 4, 5]; // [1, 2, 3, 4, 5]

// 物件展開 (淺拷貝)
const user = { name: "Alice", age: 25 };
const updatedUser = { ...user, age: 26, city: "Taipei" };
// { name: "Alice", age: 26, city: "Taipei" }
```

### 6. 常用陣列方法

現代 JS 處理資料流的核心，告別傳統 `for` 迴圈。這些方法大部分**不會改變原陣列**，而是回傳新結果。

#### Transformation (轉換)

- **`map()`**：將陣列中的每個元素轉換成新元素。
  ```javascript
  const nums = [1, 2, 3];
  const doubled = nums.map((n) => n * 2); // [2, 4, 6]
  ```
- **`reduce()`**：將陣列歸納為單一值 (例如加總)。
  ```javascript
  const sum = nums.reduce((acc, curr) => acc + curr, 0); // 6
  ```

#### Filtering & Finding (過濾與尋找)

- **`filter()`**：保留符合條件的元素。
  ```javascript
  const evens = nums.filter((n) => n % 2 === 0); // [2]
  ```
- **`find()`**：回傳**第一個**符合條件的元素。
  ```javascript
  const found = nums.find((n) => n > 1); // 2
  ```
- **`slice()`**：回傳陣列的一部分 (切片)。
  ```javascript
  const parts = nums.slice(0, 2); // [1, 2] (不包含 index 2)
  ```

#### Checking (檢查)

- **`includes()`**：檢查陣列是否包含某個值。
  ```javascript
  nums.includes(2); // true
  ```
- **`some()`**：檢查是否有**至少一個**元素符合條件。
  ```javascript
  nums.some((n) => n > 2); // true
  ```
- **`every()`**：檢查是否**所有**元素都符合條件。
  ```javascript
  nums.every((n) => n > 0); // true
  ```

### 7. 模組化 (Modules)

將程式拆分成小檔案，易於維護與重用。使用 `export` 匯出，`import` 匯入。
(注意：在瀏覽器直接使用需要 `<script type="module">`，通常會搭配 Webpack/Vite 等工具打包)

```javascript
// math.js
export const add = (a, b) => a + b;
export default function log(msg) {
  console.log(msg);
}

// main.js
import log, { add } from "./math.js";

log(add(1, 2));
```

---

## <a id="CH2-2"></a>[2-2 非同步處理 (Asynchronous)](#toc)

### 1. 概念 (Concept)

JavaScript 是**單執行緒 (Single Threaded)** 的語言，這意味著它一次只能做一件事。
如果執行一個耗時操作（例如：跟伺服器要資料需要 3 秒），主程式就會被卡住 (Blocking)，導致畫面凍結，使用者體驗極差。

**非同步 (Asynchronous)** 就是解決方案：把耗時的任務交給瀏覽器背景處理，主程式繼續往下跑，等任務好了再回來通知我。

### 2. 回呼地獄 (Callback Hell)

早期的做法是把函式當參數傳進去 (Callback)，等任務完成後呼叫。但當依賴變多時，會變成波動拳。

```javascript
getData(function (a) {
  getMoreData(a, function (b) {
    getMoreData(b, function (c) {
      // 巢狀地獄，可讀性極差，難以維護
    });
  });
});
```

### 3. Promise

ES6 引入了 Promise (承諾)，將非同步操作標準化為一個物件。它有三種狀態：

1.  **Pending** (等待中)
2.  **Fulfilled** (成功) -> 執行 `.then()`
3.  **Rejected** (失敗) -> 執行 `.catch()`

Promise 解決了縮排問題，變成「鏈式呼叫 (Chaining)」。

```javascript
fetch(url)
  .then((res) => res.json())
  .then((data) => console.log(data))
  .catch((err) => console.error(err));
```

### 4. Async / Await

ES8 (2017) 引入，這是 Promise 的**語法糖**，也是目前**最推薦**的寫法。
它讓非同步程式碼看起來就像同步程式碼一樣直觀，try-catch 也可以直接使用。

- **`async`**：宣告函式為非同步，回傳 Promise。
- **`await`**：暫停執行，直到 Promise 解析完成。

```javascript
async function initData() {
  try {
    console.log("1. 開始請求");

    // 程式在此暫停，等待回應
    const response = await fetch("/api/user");
    const user = await response.json();

    console.log("2. 拿到使用者:", user.name);
  } catch (error) {
    console.error("發生錯誤:", error);
  }
}
```

---

## <a id="CH2-3"></a>[2-3 HTTP Client 的選擇：XHR vs Fetch vs Axios](#toc)

既然有了 `fetch`，為什麼大家還是喜歡用第三方套件 `axios`？

### 選手 1: XMLHttpRequest (XHR)

- **優點**：相容性好（甚至支援 IE6）。
- **缺點**：語法醜陋、Callback 地獄、不支援 Promise。
- **結論**：除了考古，別用了。

### 選手 2: Fetch API

- **優點**：瀏覽器內建，不用下載 jar/npm 包。
- **缺點**：
  - **不會自動拋出錯誤**：404 或 500 對 fetch 來說也是「成功連線」，不會進 `catch`，要手動判斷 `response.ok`。
  - **不會自動轉 JSON**：每次都要多寫 `.then(res => res.json())`。
  - **不支援請求逾時 (Timeout)**：要搭配 AbortController，很麻煩。
  - **無法攔截請求 (Interceptors)**：沒辦法全域統一加 Token。

### 選手 3: Axios

這是目前業界標準的 HTTP Client 函式庫。

- **優點**：
  - **自動轉換 JSON**：拿到資料直接就是 Object。
  - **更合理的錯誤處理**：只要 Status Code 不是 2xx，直接拋出 Exception 進 `catch`。
  - **攔截器 (Interceptors)**：可以在請求送出前（如加 Token）或回應回來後（如統一處理 401）插入邏輯。
  - **寫法簡潔**。

#### Axios 實例化範例

在專案中，我們通常不會直接用 `axios.get`，而是會先創造一個實例 (Instance) 來設定共用參數。

```html
<!-- 引入 Axios CDN -->
<script src="https://cdn.jsdelivr.net/npm/axios/dist/axios.min.js"></script>

<script>
  // 建立一個專屬於我們後端的 api client
  const apiClient = axios.create({
    baseURL: "http://localhost:8080/api", // 基礎路徑
    timeout: 5000, // 5秒沒回應就報錯
    headers: { "Content-Type": "application/json" },
  });

  // 請求攔截器：每次發送前自動執行
  apiClient.interceptors.request.use((config) => {
    console.log("準備發送請求囉！");
    // 之後學到 Token 可以在這裡與 localStorage 結合
    // config.headers.Authorization = 'Bearer ' + token;
    return config;
  });

  // 使用
  async function getUser() {
    try {
      // 只要寫 /user 就好，不用寫完整網址
      // 拿到的 res.data 直接就是物件
      const res = await apiClient.get("/user");
      console.log(res.data);
    } catch (err) {
      console.error(err);
    }
  }
</script>
```

從下一章開始，我們將全面使用 **Axios** (配合 CDN 引入) 來進行開發。
