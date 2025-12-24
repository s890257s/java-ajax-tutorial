# 章節 2 ｜ 前端工程師的武器庫 (Modern Frontend)

## 目錄

- [2-1 JavaScript ES6+ 必備語法速成](#CH2-1)
- [2-2 非同步的演進：從 Callback 到 Async/Await](#CH2-2)
- [2-3 HTTP Client 的選擇：XHR vs Fetch vs Axios](#CH2-3)

---

### 序

歡迎來到第二章。

Java 工程師寫 JavaScript 最常遇到的問題就是：**「為什麼這個 JS 寫法跟我以前學的不一樣？」**
早期的 JS (ES5) 語法鬆散，但自從 2015 年 ES6 (ECMAScript 2015) 發布後，JS 變得越來越像一門成熟的語言。

在開始大量寫 Ajax 之前，我們要先升級你的武器庫。這一章會帶你認識現代前端開發必備的語法糖與工具。

---

## <a id="CH2-1"></a>2-1 JavaScript ES6+ 必備語法速成

### 1. 變數宣告：跟 `var` 說再見

以前我們這三種都傻傻分不清楚。現在請遵守以下原則：

*   **`const` (常數)**：**預設首選**。如果不打算重新賦值 (Reallocate)，就用它。注意：如果是物件或陣列，雖然不能換新的，但可以修改裡面的內容。
*   **`let` (變數)**：只有在「真的需要改變變數指向」時才用（例如 `for` 迴圈的索引 `i`）。
*   **`var`**：**禁止使用**。它的作用域 (Scope) 很詭異，是 Bug 的溫床。

```javascript
const API_URL = "http://localhost:8080";
// API_URL = "abc"; // Error!

const user = { name: "Allen" };
user.name = "Bob"; // OK! 只是改內容，沒改記憶體位址

let count = 0;
count++; // OK!
```

### 2. 箭頭函式 (Arrow Function)

Java 的 Lambda 是學 JS 的。這不只是語法糖，還解決了 `this` 指向混亂的問題。

```javascript
// 傳統函式
function add(a, b) {
    return a + b;
}

// 箭頭函式
const add = (a, b) => a + b; // 連 return 都可以省略

// 應用在 Ajax Callback
fetch(url).then(response => response.json());
```

### 3. 解構賦值 (Destructuring)

從物件或陣列中快速提取資料。這在處理 Ajax 回傳的 JSON 時超級好用。

```javascript
const user = {
    id: 1,
    username: "allen",
    email: "allen@example.com",
    address: { city: "Taipei" }
};

// 傳統寫法
const name = user.username;
const city = user.address.city;

// 解構寫法 (變數名稱必須對應 Key)
const { username, email, address: { city } } = user;
console.log(username, city); // allen, Taipei
```

### 4. 字串樣板 (Template Literals)

別再用 `+` 號拼湊 HTML 字串了，很醜又容易錯。

```javascript
const name = "Allen";
const age = 30;

// 反引號 (Backticks) ` `
const html = `
    <div class="card">
        <h1>${name}</h1>
        <p>Age: ${age}</p>
    </div>
`;
```

### 5. 物件屬性縮寫 (Object Property Shorthand)

如果 Key 和 Value 的變數名稱一樣，可以偷懶。

```javascript
const name = "Allen";
const age = 30;

// 傳統
const user1 = { name: name, age: age };

// 縮寫
const user2 = { name, age }; // 效果一樣
```

---

## <a id="CH2-2"></a>2-2 非同步的演進：從 Callback 到 Async/Await

這是前端最難的一關，請仔細閱讀。

我們在第一章提過，JS 是**單執行緒**的。耗時的操作（如網路請求）不能卡住主程式，所以必須丟到背景執行，等好了再通知我。

### Version 1: Callback Hell (回呼地獄)

最早期的做法，把函式當參數傳進去。

```javascript
getData(function(a) {
    getMoreData(a, function(b) {
        getMoreData(b, function(c) {
            getMoreData(c, function(d) {
                // 波動拳！可讀性極差
            });
        });
    });
});
```

### Version 2: Promise (承諾)

ES6 引入。把非同步操作包裝成一個物件。它有三種狀態：
1.  **Pending** (等待中)
2.  **Resolved/Fulfilled** (成功) -> 執行 `.then()`
3.  **Rejected** (失敗) -> 執行 `.catch()`

Promise 解決了縮排問題，變成「鏈式呼叫」。

```javascript
fetch(url)
    .then(res => res.json())
    .then(data => console.log(data))
    .catch(err => console.error(err));
```

### Version 3: Async / Await (終極型態)

ES8 (2017) 引入。這是 Promise 的語法糖，讓非同步程式碼看起來像同步程式碼。**這是現代標準寫法**。

*   **`async`**：宣告這個函式是非同步的，它會自動回傳一個 Promise。
*   **`await`**：(只能在 async 函式內用) **暫停**程式執行，直到 Promise 算完拿到結果，再繼續往下跑。

```javascript
// 定義一個 async 函式
async function initData() {
    try {
        console.log("1. 開始請求");
        
        // 程式執行到這裡會「等待」，不像 Promise 會直接往下跑
        const response = await fetch('/api/user');
        const user = await response.json();
        
        console.log("2. 拿到使用者:", user.name);
        
        const response2 = await fetch(`/api/orders/${user.id}`);
        const orders = await response2.json();
        
        console.log("3. 拿到訂單:", orders);
        
    } catch (error) {
        // 統一捕捉所有錯誤 (網路斷線、JSON 解析失敗...)
        console.error("發生錯誤:", error);
    }
}

initData();
console.log("4. 這一行會比 '2. 拿到使用者' 先執行");
```

---

## <a id="CH2-3"></a>2-3 HTTP Client 的選擇：XHR vs Fetch vs Axios

既然有了 `fetch`，為什麼大家還是喜歡用第三方套件 `axios`？

### 選手 1: XMLHttpRequest (XHR)
*   **優點**：相容性好（甚至支援 IE6）。
*   **缺點**：語法醜陋、Callback 地獄、不支援 Promise。
*   **結論**：除了考古，別用了。

### 選手 2: Fetch API
*   **優點**：瀏覽器內建，不用下載 jar/npm 包。
*   **缺點**：
    *   **不會自動拋出錯誤**：404 或 500 對 fetch 來說也是「成功連線」，不會進 `catch`，要手動判斷 `response.ok`。
    *   **不會自動轉 JSON**：每次都要多寫 `.then(res => res.json())`。
    *   **不支援請求逾時 (Timeout)**：要搭配 AbortController，很麻煩。
    *   **無法攔截請求 (Interceptors)**：沒辦法全域統一加 Token。

### 選手 3: Axios
這是目前業界標準的 HTTP Client 函式庫。

*   **優點**：
    *   **自動轉換 JSON**：拿到資料直接就是 Object。
    *   **更合理的錯誤處理**：只要 Status Code 不是 2xx，直接拋出 Exception 進 `catch`。
    *   **攔截器 (Interceptors)**：可以在請求送出前（如加 Token）或回應回來後（如統一處理 401）插入邏輯。
    *   **寫法簡潔**。

#### Axios 實例化範例

在專案中，我們通常不會直接用 `axios.get`，而是會先創造一個實例 (Instance) 來設定共用參數。

```html
<!-- 引入 Axios CDN -->
<script src="https://cdn.jsdelivr.net/npm/axios/dist/axios.min.js"></script>

<script>
    // 建立一個專屬於我們後端的 api client
    const apiClient = axios.create({
        baseURL: 'http://localhost:8080/api', // 基礎路徑
        timeout: 5000, // 5秒沒回應就報錯
        headers: { 'Content-Type': 'application/json' }
    });

    // 請求攔截器：每次發送前自動執行
    apiClient.interceptors.request.use(config => {
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
            const res = await apiClient.get('/user');
            console.log(res.data);
        } catch (err) {
            console.error(err);
        }
    }
</script>
```

從下一章開始，我們將全面使用 **Axios** (配合 CDN 引入) 來進行開發。
