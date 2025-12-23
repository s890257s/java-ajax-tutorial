# 章節 3 ｜ 前端非同步思維與請求控制

## <a id="目錄"></a>目錄

- [3-1 同步與非同步流程的差異](#CH3-1)
- [3-2 Promise 的存在意義](#CH3-2)
- [3-3 async / await 的閱讀模型](#CH3-3)
- [3-4 Ajax 請求的錯誤與例外處理](#CH3-4)
- [3-5 HTTP 狀態碼與業務結果](#CH3-5)
- [3-6 UI 與資料狀態的關係](#CH3-6)

---

### 序

歡迎來到第三章！這章可是前端開發的重頭戲。

如果你以前寫過 Java，習慣了程式碼「一行一行乖乖往下跑」的規律世界，剛接觸 JavaScript 的非同步（Async）特性時，可能會覺得世界觀被顛覆了。

「為什麼我明明先呼叫了要資料的函式，下一行印出來卻是 `undefined`？」
「為什麼 console.log 的順序跟我寫的不一樣？」

別擔心，這不是靈異現象，單純是因為 JavaScript 的大腦（單執行緒）不想為了等你拿資料而發呆停工。這章我們要來駕馭這個特性，學會用 `Promise` 和最優雅的 `async/await` 來指揮非同步流程，讓程式碼乖乖聽話！

---

## <a id="CH3-1"></a>[3-1 同步與非同步流程的差異](#目錄)

### 程式碼順序 vs 實際完成順序

在傳統同步程式（例如 Java 的主要執行緒）中，程式碼是一行一行執行的，第三行一定會等第二行跑完才跑。

但在 JavaScript 的世界，為了不讓網頁畫面「卡死」（因為 JS 只有一個執行緒在跑 UI），耗時的任務（如發請求）會被丟到背景去。

```javascript
console.log("1. 開始點餐");

// 這是一個非同步操作
fetch("/api/order").then(() => {
  console.log("3. 餐點送到了");
});

console.log("2. 去找位子坐");
```

**實際執行結果：**

```text
1. 開始點餐
2. 去找位子坐
3. 餐點送到了
```

### 非同步造成的思維落差

初學者最大的坑，就是試圖用「同步」的思維寫「非同步」的程式：

```javascript
let myData;

fetch("/api/data").then((response) => {
  myData = response; // 用意是想存進去
});

console.log(myData); // 這裡永遠是 undefined！！！
// 因為 fetch 還沒回來，JS 已經執行到這一行了
```

---

## <a id="CH3-2"></a>[3-2 Promise 的存在意義](#目錄)

為了讓非同步操作更好管理，ES6 引入了 `Promise`（承諾）。

### Promise 解決了什麼問題

在 Promise 出現之前，我們依賴 Callback Function，一旦邏輯複雜（例如：先登入，成功後拿 ID，再用 ID 拿訂單，再拿訂單詳情...），就會寫出可怕的**回呼地獄 (Callback Hell)**，程式碼像波動拳一樣往右縮排。

### 非同步結果的管理方式

Promise 將未來才會發生的事件（成功或失敗）物件化。一個 Promise 只有三種狀態：

1.  **Pending**：擱置中（還在等後端回傳）。
2.  **Fulfilled**：已實現（拿到資料了，呼叫 `resolve`）。
3.  **Rejected**：已拒絕（失敗了，呼叫 `reject`）。

這讓我們可以用鏈式語法 (`.then().then()`) 來把邏輯拉直。

---

## <a id="CH3-3"></a>[3-3 async / await 的閱讀模型](#目錄)

雖然 Promise 解決了縮排問題，但一堆 `.then` 還是不夠直觀。ES8 推出的 `async / await` 才是真正的救星。

### 非同步流程的線性化

`async / await` 讓我們能用「看起來像同步」的方式寫非同步程式碼：

```javascript
// 宣告這個函式是非同步的
async function initData() {
  console.log("1. 開始");

  // await 會「暫停」函式執行，直到 Promise 回傳結果
  // 但不會卡住整個瀏覽器 UI (因為它是在 async function 內暫停)
  const response = await fetch("/api/data");
  const data = await response.json();

  console.log("2. 拿到資料了:", data); // 在這裡可以直接用 data，不用 callback
}
```

### 可讀性與維護性

這大大降低了心智負擔。要注意的是，`await` 只能用在 `async function` 裡面。

> **進階觀念：Top-level await**
> 在最新的 ES Modules 模組化環境中，我們可以在檔案的最外層直接使用 `await`，不需要包在 async function 裡了！這對初始化設定非常方便。

---

## <a id="CH3-4"></a>[3-4 Ajax 請求的錯誤與例外處理](#目錄)

Ajax 請求可能在很多環節出錯，我們必須分層處理。使用 `async/await` 時，最強大的武器就是 `try...catch`。

```javascript
async function loadUserData() {
  try {
    const res = await fetch("/api/user/123");

    // 注意：fetch 遇到 404/500 不會進 catch，必須手動檢查 ok 狀態
    if (!res.ok) {
      throw new Error(`HTTP Error: ${res.status}`);
    }

    const data = await res.json();
    renderUser(data);
  } catch (error) {
    // 1. 網路斷線
    // 2. JSON 解析失敗
    // 3. 手動 throw 的 Error
    console.error("發生悲劇:", error);
    alert("資料載入失敗，請稍後再試！");
  } finally {
    // 不管成功失敗都會執行，適合用來關閉 Loading 轉圈圈
    hideLoading();
  }
}
```

---

## <a id="CH3-5"></a>[3-5 HTTP 狀態碼與業務結果](#目錄)

### HTTP 成功 ≠ 業務成功

有些 API 設計會不管結果如何都回傳 HTTP 200，然後在 JSON 裡面寫 `success: false`（雖然不推薦，但很常見）。

### 前端如何判斷請求是否成功

標準做法是兩道鎖：

1.  **第一關**：檢查 HTTP Status 是否為 2xx (`response.ok`)。
2.  **第二關**：檢查 Response Body 的內容（例如 `code === 0` 或 `status === 'success'`）。

```javascript
const res = await fetch('/api/login', { ... });

if (!res.ok) {
    // 處理 401, 500 等 HTTP 錯誤
    handleHttpError(res.status);
    return;
}

const result = await res.json();
if (result.code !== 0) {
    // 處理「帳號密碼錯誤」這種業務邏輯失敗
    showError(result.message);
    return;
}

// 這裡才是真正的成功
loginSuccess();
```

---

## <a id="CH3-6"></a>[3-6 UI 與資料狀態的關係](#目錄)

Ajax 互動不僅是請求資料，更重要的是**回饋介面狀態**給使用者。一個好的 Ajax 體驗通常包含這三個狀態變數：

1.  **Loading (Boolean)**：請求發出前設為 `true`（顯示轉圈），請求結束（無論成功失敗）設為 `false`。
2.  **Data (Object/Array)**：存放成功拿到的資料。
3.  **Error (String/Object)**：存放錯誤訊息，有值就顯示錯誤畫面。

**練習看看：標準的 Fetch 樣板**

```javascript
let isLoading = false;
let errorMsg = null;
let products = [];

async function fetchProducts() {
  // 1. Reset 狀態
  isLoading = true;
  errorMsg = null;
  updateUI(); // 顯示 Loading

  try {
    const res = await fetch("/api/products");
    if (!res.ok) throw new Error("伺服器忙線中");

    products = await res.json(); // 2. 更新資料
  } catch (err) {
    errorMsg = err.message; // 3. 捕捉錯誤
  } finally {
    isLoading = false; // 4. 結束 Loading
    updateUI(); // 根據 data, errorMsg, isLoading 重新渲染畫面
  }
}
```

> **本章重點**
> 前端開始主導資料流程，並負責將「資料的狀態」轉譯為「畫面的狀態」。

---

[下一章：章節 4 ｜ Ajax 程式碼的成長與結構化](./Chapter4_CodeStructure.md)
