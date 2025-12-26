# 章節 2 ｜ 前端工程師的武器庫 (Modern Frontend)

## <a id="toc"></a>目錄

- [2-1 JavaScript ES6+ 必備語法速成](#CH2-1)
- [2-2 非同步處理 (Asynchronous)](#CH2-2)
- [2-3 HTTP Client 的選擇：XHR vs Fetch vs Axios](#CH2-3)

---

### 序

歡迎來到第二章。

早期的 JS (ES6 以前) 語法鬆散，但自從 2015 年 ES6 (ECMAScript 2015) 發布後，JS 變得越來越像一門成熟的語言。

在開始寫 Ajax 之前，我們要先升級你的武器庫。這一章會帶你認識現代前端開發必備的語法糖與工具。

> 💡 小知識：ECMAScript 與 JavaScript 的關係
>
> ECMAScript (ES) 是 JavaScript 的「語言規格書 (Specification)」，定義了語法與核心行為。
> JavaScript (JS) 則是目前最主流、最廣泛使用的 ECMAScript 實作 (Implementation)。

---

## <a id="CH2-1"></a>[2-1 JavaScript ES6+ 必備語法速成](#toc)

### 1. 變數宣告：`const` vs `let`

ES6 以前我們使用 `var`，但它有奇怪的作用域問題、容易產生各種 BUG，現在請直接無視它。
現代開發請遵守這條黃金法則：**「能用 `const` 就用 `const`，真的要改變才用 `let`」**。

- **`const` (常數) - 預設首選**

  - **意義**：代表這個變數的**綁定 (Binding)** 不會被重新指派。
  - **好處**：
    1.  **安全**：避免變數被意外覆寫 (例如不小心把 `apiUrl` 改成別的字串)。
    2.  **可讀性**：看到 `const` 就知道，這個變數在函式後面不會變成別的東西。
  - **注意**：如果是物件/陣列，`const` 保護的是「變數不能重新指派」，但「內容」是可以修改的。

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

// 基本解構
const { username, email } = user;
console.log(username); // Alice
console.log(email); // Alice@example.com

// 進階：多層解構
const {
  address: { city },
} = user;
console.log(city); // Taipei
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

**注意：展開運算子僅為「淺拷貝 (Shallow Copy)」**
若物件內包含巢狀物件（例如 `address`），展開運算子只會複製參照。修改新物件的屬性會影響原物件。

若需**深層拷貝 (Deep Copy)**，最簡單的方式是使用 `JSON` 方法：

```javascript
const user = { name: "Alice", address: { city: "台北市" } };
const deepCopiedUser = JSON.parse(JSON.stringify(user));

deepCopiedUser.address.city = "台中市";
console.log(user.address.city); // 台北市 (不受影響)
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

  功能最強大的陣列方法(稍微複雜)，能將陣列元素經由回呼函式運算，最終化為「單一值」（可能是數字、物件或陣列）。

  **語法結構：**
  `array.reduce((acc, curr) => { ... }, initialValue)`

  - `acc` (accumulator): 累加值，即上一次回呼函式的回傳值。
  - `curr` (current): 當前處理的元素。
  - `initialValue`: 累加值的初始設定（選填，但建議加上）。

  ```javascript
  const numbers = [1, 2, 3, 4];
  const sum = numbers.reduce((acc, curr) => {
    return acc + curr;
  }, 0);
  // 初始值 0，依序加 1, 2, 3, 4 => 結果 10
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

**具名匯出 (Named Export)：**
可以匯出多個變數或函式。需使用 `{}` 接收。

```javascript
// math.js
export const add = (a, b) => a + b;
export const pi = 3.14;
```

**匯入具名成員：**
需使用 `{}` 且名稱需對應（或使用 `as` 重新命名）。

```javascript
import { add, pi } from "./math.js";
console.log(add(1, 2)); // 3
```

**預設匯出 (Default Export)：**
每個檔案只能有一個預設匯出，通常用於匯出元件或主要功能。不需 `{}` 接收。

```javascript
// user.js
const user = { name: "Alice" };
export default user;
```

**匯入預設成員：**
不需 `{}`，且可以自訂名稱。

```javascript
import currentUser from "./user.js";
```

---

## <a id="CH2-2"></a>[2-2 非同步處理 (Asynchronous)](#toc)

### 1. 概念 (Concept)

JavaScript 是**單執行緒 (Single Threaded)** 的語言，這意味著它一次只能做一件事。
如果執行一個耗時操作（例如：跟伺服器要資料需要 3 秒），主程式就會被卡住 (Blocking)，導致畫面凍結，使用者體驗極差。

**非同步 (Asynchronous)** 就是解決方案：把耗時的任務交給瀏覽器背景處理，主程式繼續往下跑，等任務好了再回來通知我。

> **💡 備註: 關於瀏覽器中 JavaScript 的運行機制 (此不討論 node.js)**
> JavaScript 在瀏覽器中運行時，只有一條主要執行緒，並依循「解析 → 執行」的順序處理程式碼；也因此，JS 在同一時間只能進行一項任務。
> 當遇到耗時或複雜的工作時，JS 會將這些任務交由瀏覽器處理，避免阻塞主執行緒，確保畫面與操作仍維持順暢。
> 瀏覽器本身擁有多條專責執行緒，例如：
>
> - Timer Thread：負責管理 setTimeout、setInterval
> - Network Thread：處理 AJAX、fetch 等網路請求
> - I/O Thread：負責 DOM 事件、檔案讀取等輸入輸出操作
> - Rendering Thread：掌控畫面排版與重新繪製
> - Web Worker Thread：提供真正獨立的 JS 執行緒，專門處理大量計算；與主執行緒完全隔離，無法操作 DOM，也不具備 window 物件。
>   模擬行為可參考 https://www.jsv9000.app/

### 2. 回呼地獄 (Callback Hell)，以下為演示程式碼，此寫法已被淘汰

非同步任務（如 `fetch`、`setTimeout`）雖然會依照程式碼順序**逐行啟動**，但啟動後會交由瀏覽器在背景處理，**完成時間不固定**  
以下寫法**無法保證執行結果的順序**，若有資料依賴關係，將會導致嚴重的邏輯錯誤。

```javascript
// 隨機取得延遲時間 0~999 毫秒
const getRandomDelay = () => Math.floor(Math.random() * 1000);

// 模擬非同步操作
const asyncProcess = (str) => {
  setTimeout(() => {
    console.log("執行操作: " + str);
  }, getRandomDelay());
};

// 執行非同步操作，完成時間不固定，無法保證順序，會導致嚴重的邏輯錯誤。
asyncProcess("1. 取得使用者...");
asyncProcess("2. 根據使用者取得訂單...");
asyncProcess("3. 根據訂單取得商品總數...");
asyncProcess("4. 根據商品總數計算總價...");
asyncProcess("5. 顯示總價...");
```

我們現在把原方法 `asyncProcess(str)`，改為 `asyncProcess(str, successCallback)`。
也就是說，當 asyncProcess 執行成功後，會主動呼叫 successCallback。

```javascript
// 把「方法」當成參數傳入 asyncProcess，asyncProcess 執行成功後，會主動呼叫(即是 callback function)。
const asyncProcess = (str, successCallback) => {
  setTimeout(() => {
    console.log("執行操作: " + str);
    successCallback(); // 主動執行下一個方法
  }, getRandomDelay());
};

// 逐步執行非同步操作
asyncProcess("1. 取得使用者...", () => {
  asyncProcess("2. 根據使用者取得訂單...", () => {
    // 依序串接...
  });
});
```

但執行非同步操作，不能保證一定會成功，應要考慮失敗時該執行的程式。
故在原方法中加入錯誤處理，方法會變成 `asyncProcess(str, successCallback, errorCallback)`。

```javascript
// 補上錯誤處理，此範例使用隨機數決定成敗，真實程式會有明確的錯誤處理判斷
const asyncProcess = (str, successCallback, errorCallback) => {
  setTimeout(() => {
    console.log("執行操作: " + str);

    // 成功率 90%
    if (Math.random() <= 0.9) {
      successCallback(); // 成功執行
    } else {
      errorCallback(); // 失敗執行
    }
  }, getRandomDelay());
};

// 完整寫法，Callback Hell，回呼地獄；難以維護、難以閱讀，也有「波動拳 Hadouken」的別稱。
asyncProcess(
  "1. 取得使用者...",
  () => {
    asyncProcess(
      "2. 根據使用者取得訂單...",
      () => {
        asyncProcess(
          "3. 根據訂單取得商品總數...",
          () => {
            asyncProcess(
              "4. 根據商品總數計算總價...",
              () => {
                asyncProcess(
                  "5. 顯示總價...",
                  () => {
                    console.log("全部完成 Success!");
                  },
                  () => console.log("失敗: 5")
                );
              },
              () => console.log("失敗: 4")
            );
          },
          () => console.log("失敗: 3")
        );
      },
      () => console.log("失敗: 2")
    );
  },
  () => console.log("失敗: 1")
);
```

<div style="display: flex; justify-content: center;">
  <img src="./images/Hadouken.webp" width="300" style="border-radius: 10px;">
</div>
<p align="center"><i>Callback Hell 又被戲稱為波動拳 (Hadouken)</i></p>

### 3. Promise

ES6 引入了 Promise (承諾)，這是現代非同步開發的基石。簡單來說，它將非同步操作標準化為一個物件，讓你用更優雅的方式處理「未來才會發生」的事件。

Promise 有三種狀態：

1.  **Pending** (等待中)：初始狀態，操作尚未完成。
2.  **Fulfilled** (已實現/成功)：操作成功完成 -> 觸發 `.then()`。
3.  **Rejected** (已拒絕/失敗)：操作失敗 -> 觸發 `.catch()`。

**實戰：解決 Callback Hell**

Promise 最主要的功能就是解決回呼地獄的「縮排」與「錯誤處理」問題。它將巢狀結構改為「鏈式呼叫 (Chaining)」，讓程式碼由上而下線性執行，閱讀性大幅提升。

```javascript
// Promise 基本語法
const myPromise = new Promise((resolve, reject) => {
  // 執行非同步任務 (例如 API 請求、計時器等)
  const isSuccess = true;

  if (isSuccess) {
    resolve("任務成功！這是回傳的資料"); // 進入 .then()
  } else {
    reject("任務失敗...這是錯誤訊息"); // 進入 .catch()
  }
});

// 使用 Promise
myPromise
  .then((data) => {
    // 成功時的處理邏輯
    console.log(data);
  })
  .catch((error) => {
    // 失敗時的處理邏輯
    console.error(error);
  })
  .finally(() => {
    // 無論成功或失敗，最後都會執行的區塊 (例如：隱藏讀取動畫)
    console.log("程序結束");
  });
```

我們將上面的 `asyncProcess` 改寫為 Promise 版本，請看差異：

```javascript
// 1. 定義：回傳一個 Promise 物件
const asyncProcess = (str) => {
  return new Promise((resolve, reject) => {
    setTimeout(() => {
      console.log("執行操作: " + str);

      // 模擬成功與失敗
      if (Math.random() <= 0.9) {
        resolve(); // 成功 -> 對應 .then()
      } else {
        reject("執行失敗: " + str); // 失敗 -> 對應 .catch()
      }
    }, getRandomDelay());
  });
};

// 2. 呼叫：優雅的鏈式串接
asyncProcess("1. 取得使用者...")
  .then(() => {
    // 步驟 1 成功後，回傳下一個 Promise (步驟 2)
    return asyncProcess("2. 根據使用者取得訂單...");
  })
  .then(() => asyncProcess("3. 根據訂單取得商品總數...")) // 箭頭函式簡寫
  .then(() => asyncProcess("4. 根據商品總數計算總價..."))
  .then(() => asyncProcess("5. 顯示總價..."))
  .then(() => console.log("全部完成 Success!"))
  .catch((error) => {
    // ⚠️ 強大的錯誤處理：只要中間任何一步驟失敗，就會直接跳到這裡！
    // 不用像 Callback 每一層都要寫一次錯誤處理。
    console.error("嗚嗚，流程中斷了:", error);
  });
```

這樣是不是清爽多了？而且只要一個 `.catch()` 就能捕獲整條鏈上的錯誤！

### 4. Async / Await

ES8 (2017) 推出的 `async` / `await` 是 Promise 的語法糖，也是目前**最主流、最推薦**的寫法。
它讓非同步程式碼讀起來跟「同步程式碼」幾乎一樣，不僅直觀，還能直接使用 `try-catch` 進行錯誤處理。

- **`async`**：將函式標記為非同步，該函式會自動回傳一個 Promise。
- **`await`**：只能在 `async` 函式內使用。它會暫停程式執行，直到 Promise 解析完成並回傳結果。

**基本語法結構**

使用 `await` 可以直接將 Promise 的解析結果 (Resolve Value) 賦值給變數，就像寫同步程式碼一樣直覺。

```javascript
const getData = async () => {
  try {
    // await 會暫停並等待 Promise 完成，然後將結果回傳給變數
    const user = await fetchUser();
    console.log("取得使用者:", user);

    // 接續執行下一個非同步任務
    const orders = await fetchOrders(user.id);
    console.log("取得訂單:", orders);
  } catch (error) {
    // 統一捕捉錯誤 (包含 user 或 orders 失敗)
    console.error("發生錯誤:", error);
  }
};
```

**用 Async/Await 改寫 Callback Hell**

我們將上面的 Promise 鏈式寫法，改成 Async/Await 版本。你會發現，那些 `.then()` 全部都不見了，程式碼變得非常乾淨！

```javascript
// 建立一個 async 函式來管理流程
const startFlow = async () => {
  try {
    console.log("任務開始...");

    // 就像寫同步程式碼一樣，一行一行往下執行
    await asyncProcess("1. 取得使用者...");
    await asyncProcess("2. 根據使用者取得訂單...");
    await asyncProcess("3. 根據訂單取得商品總數...");
    await asyncProcess("4. 根據商品總數計算總價...");
    await asyncProcess("5. 顯示總價...");

    console.log("全部完成 Success!");
  } catch (error) {
    // 只要上面任何一行出錯，就會直接跳到這裡
    console.error("嗚嗚，流程中斷了:", error);
  }
};

// 執行函式
startFlow();
```

**比較一下差異：**

1.  **Callback**: 向右長胖 (波動拳)，巢狀地獄。
2.  **Promise**: 向下生長，但還是要寫很多 `.then()` 跟回呼函式。
3.  **Async/Await**: **乾淨俐落**，就像在寫一般同步的程式碼。

---

## <a id="CH2-3"></a>[2-3 HTTP Client 的選擇：XHR vs Fetch vs Axios](#toc)

學會了 Async/Await 這些強大的「內功心法」後，我們接下來要挑選一把趁手的「兵器」來發送網路請求。
前端界主要有三種發送 HTTP Request 的方式，讓我們來看看它們的演進與優缺點。

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
