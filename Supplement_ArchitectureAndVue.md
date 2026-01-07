# 備註 ｜ 前端架構優化與 Vue.js 導論 (Architecture & Vue)

## <a id="toc"></a>目錄

- [5-1 為什麼我們需要 Vue.js？](#CH5-1)
  - [Vanilla JS (原生 JS) 的極限](#CH5-1-1)
  - [Vue.js 的思維](#CH5-1-2)
- [5-2 Vue.js 快速上手 (CDN 模式)](#CH5-2)
  - [Hello Vue](#CH5-2-1)
  - [核心指令重點](#CH5-2-2)
- [5-3 實戰整合：用 Vue 實作登入與檔案上傳](#CH5-3)
  - [1. 登入元件 (Login)](#CH5-3-1)
  - [2. 檔案上傳元件 (File Upload)](#CH5-3-2)

---

### 序

恭喜你！學到這裡，你已經具備了手寫 Ajax、處理 JWT 登入驗證、以及檔案上傳下載的能力了。
但你可能發現，隨著功能變多，你的 JavaScript 檔充滿了 `document.getElementById`、`innerHTML` 以及一大堆的 `addEventListener`。

「只是想改個變數，為什麼要手動去抓 DOM 元素更新？」

這就是現代前端框架 (Vue, React) 誕生的原因。這章作為一個**額外的補充備註**，我們不談複雜的 Webpack/Vite 打包，直接用最簡單的 **CDN 模式**，讓你體驗「資料驅動」的魔力，並嘗試把上一章學到的技術「元件化」。

---

## <a id="CH5-1"></a>[5-1 為什麼我們需要 Vue.js？](#toc)

### <a id="CH5-1-1"></a>[Vanilla JS (原生 JS) 的極限](#toc)

試想一個簡單的「計數器」功能：

```javascript
let count = 0;
const btn = document.getElementById("btn");
const display = document.getElementById("display");

btn.addEventListener("click", () => {
  count++; // 1. 改資料
  display.innerText = count; // 2. 手動更新畫面 (最容易漏掉或寫錯)
});
```

這叫做 **命令式程式設計 (Imperative)**：你必須一步步告訴瀏覽器「怎麼做」。當網頁越來越大，維護這些 DOM 操作會變成惡夢。

### <a id="CH5-1-2"></a>[Vue.js 的思維](#toc)

Vue 提倡 **宣告式程式設計 (Declarative)** 與 **資料驅動 (Data-Driven)**。
你只要把變數 (`count`) 改掉，Vue 會自動幫你把所有用到 `count` 的畫面更新。你完全不需要寫 `document.getElementById`。

---

## <a id="CH5-2"></a>[5-2 Vue.js 快速上手 (CDN 模式)](#toc)

我們不用安裝 Node.js，直接在 HTML 引入 Vue。

### <a id="CH5-2-1"></a>[Hello Vue](#toc)

```html
<!DOCTYPE html>
<html lang="zh-TW">
  <head>
    <meta charset="UTF-8" />
    <title>Vue Hello</title>
    <!-- 1. 引入 Vue (Vue 3) -->
    <script src="https://unpkg.com/vue@3/dist/vue.global.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/axios/dist/axios.min.js"></script>
  </head>
  <body>
    <!-- 2. 定義掛載點 (Mount Point) -->
    <div id="app">
      <h1>{{ message }}</h1>
      <button @click="reverseMessage">反轉文字</button>

      <hr />

      <!-- v-for 列表渲染 -->
      <ul>
        <li v-for="user in users" :key="user.id">
          {{ user.name }} ({{ user.email }})
        </li>
      </ul>
    </div>

    <script>
      // Vue 3 Composition API 寫法
      const { createApp, ref, onMounted } = Vue;

      createApp({
        setup() {
          // 3. 定義狀態 (State)
          // ref 用來定義「會變動」的資料 (Reactive Data)
          const message = ref("Hello Vue!");
          const users = ref([]);

          // 4. 定義方法 (Methods)
          const reverseMessage = () => {
            message.value = message.value.split("").reverse().join("");
          };

          // 5. 生命週期 (Lifecycle)
          // onMounted 等同於 $(document).ready()
          onMounted(async () => {
            console.log("畫面載入完成，開始發 Ajax...");
            try {
              const res = await axios.get(
                "https://jsonplaceholder.typicode.com/users"
              );
              users.value = res.data; // 自動更新畫面！不用 document.getElementById
            } catch (err) {
              console.error(err);
            }
          });

          // 6. 必須 return 給 HTML 用
          return {
            message,
            users,
            reverseMessage,
          };
        },
      }).mount("#app");
    </script>
  </body>
</html>
```

### <a id="CH5-2-2"></a>[核心指令重點](#toc)

1.  **`{{ }}` (插值)**：把變數顯示在畫面上。
2.  **`v-model` (雙向綁定)**：用於 `<input>`，你打字變數就變，變數變框框內容就變。
3.  **`v-for` (迴圈)**：取代手寫 `forEach` + `innerHTML +=`。
4.  **`v-if` / `v-show` (判斷)**：決定元素要不要顯示。
5.  **`@click` (事件)**：取代 `addEventListener`。

---

## <a id="CH5-3"></a>[5-3 實戰整合：用 Vue 實作登入與檔案上傳](#toc)

現在我們用 Vue 來重構上一章的兩個功能：登入 (JWT) 與 檔案上傳。你會發現程式碼變得多乾淨。

### <a id="CH5-3-1"></a>[1. 登入元件 (Login)](#toc)

利用 `v-model` 自動抓取輸入框的值，再透過 axios 送出。

```html
<div id="login-app">
  <h3>登入系統</h3>
  <input type="text" v-model="username" placeholder="帳號" />
  <input type="password" v-model="password" placeholder="密碼" />
  <button @click="doLogin" :disabled="isLoading">
    {{ isLoading ? '登入中...' : '登入' }}
  </button>
  <p style="color: red">{{ errorMsg }}</p>
</div>

<script>
  const { createApp, ref } = Vue;

  createApp({
    setup() {
      const username = ref("");
      const password = ref("");
      const errorMsg = ref("");
      const isLoading = ref(false);

      const doLogin = async () => {
        // 清空錯誤訊息
        errorMsg.value = "";
        isLoading.value = true;

        try {
          const res = await axios.post("/api/login", {
            username: username.value,
            password: password.value,
          });

          // 登入成功，存 Token
          const token = res.data.token;
          localStorage.setItem("jwt_token", token);
          alert("登入成功！");
        } catch (err) {
          errorMsg.value = "登入失敗，請檢查帳號密碼";
        } finally {
          isLoading.value = false;
        }
      };

      return { username, password, errorMsg, isLoading, doLogin };
    },
  }).mount("#login-app");
</script>
```

### <a id="CH5-3-2"></a>[2. 檔案上傳元件 (File Upload)](#toc)

Vue 處理檔案比較特別，因為 `<input type="file">` 是唯讀的，不能用 `v-model`。我們需要用 `@change` 事件來抓檔案。

```html
<div id="upload-app">
  <h3>檔案上傳</h3>

  <!-- @change 當檔案被選擇時觸發 -->
  <input type="file" @change="handleFileChange" />

  <button @click="uploadFile" :disabled="!selectedFile">上傳</button>

  <!-- 預覽圖片 (如果有選檔案) -->
  <div v-if="previewUrl">
    <p>預覽：</p>
    <img :src="previewUrl" style="max-width: 200px;" />
  </div>
</div>

<script>
  createApp({
    setup() {
      const selectedFile = ref(null);
      const previewUrl = ref(null);

      // 1. 當使用者選擇檔案
      const handleFileChange = (event) => {
        const file = event.target.files[0];
        if (file) {
          selectedFile.value = file;
          // 建立預覽圖 URL
          previewUrl.value = URL.createObjectURL(file);
        }
      };

      // 2. 送出上傳
      const uploadFile = async () => {
        if (!selectedFile.value) return;

        const formData = new FormData();
        formData.append("myFile", selectedFile.value);
        formData.append("description", "Vue Upload Test");

        try {
          // 記得上一章教的攔截器嗎？這裡不用手動帶 Token，axios 攔截器會幫忙做！
          // (備註：本範例需搭配 Chapter 4 的 apiClient 設定，或需自行在全域設定 axios interceptors 才會生效)
          await axios.post("/api/upload", formData);
          alert("上傳成功！");
        } catch (err) {
          alert("上傳失敗");
        }
      };

      return { selectedFile, previewUrl, handleFileChange, uploadFile };
    },
  }).mount("#upload-app");
</script>
```
