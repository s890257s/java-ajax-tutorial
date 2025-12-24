# 章節 5 ｜ 前端架構優化與 Vue.js 導論 (Architecture & Vue)

## 目錄

- [5-1 為什麼我們需要 Vue.js？](#CH5-1)
- [5-2 Vue.js 快速上手 (CDN 模式)](#CH5-2)
- [5-3 前後端分離的最後一哩路：安全](#CH5-3)

---

### 序

恭喜你！學到這裡，你已經具備了手寫 Ajax、處理各種資料傳輸的能力了。
但你可能發現，隨著功能變多，你的 JavaScript 檔充滿了 `document.getElementById` 和 `innerHTML` 的拼接字串。

「只是想改個變數，為什麼要手動去抓 DOM 元素更新？」
這就是現代前端框架 (Vue, React) 誕生的原因。
這章我們不談複雜的 Webpack/Vite 打包，直接用最簡單的 **CDN 模式**，讓你體驗「資料驅動」的魔力。

---

## <a id="CH5-1"></a>5-1 為什麼我們需要 Vue.js？

### Vanilla JS (原生 JS) 的極限

試想一個簡單的「計數器」功能：

```javascript
let count = 0;
const btn = document.getElementById('btn');
const display = document.getElementById('display');

btn.addEventListener('click', () => {
    count++; // 1. 改資料
    display.innerText = count; // 2. 手動更新畫面 (最容易漏掉或寫錯)
});
```

這叫做 **命令式編程 (Imperative)**：你必須一步步告訴瀏覽器「怎麼做」。

### Vue.js 的思維

Vue 提倡 **宣告式編程 (Declarative)** 與 **資料驅動 (Data-Driven)**。
你只要把變數 (`count`) 改掉，Vue 會自動幫你把所有用到 `count` 的畫面更新。你完全不需要寫 `document.getElementById`。

---

## <a id="CH5-2"></a>5-2 Vue.js 快速上手 (CDN 模式)

我們不用安裝 Node.js，直接在 HTML 引入 Vue。

### Hello Vue

```html
<!DOCTYPE html>
<html lang="zh-TW">
<head>
    <meta charset="UTF-8">
    <title>Vue Hello</title>
    <!-- 1. 引入 Vue -->
    <script src="https://unpkg.com/vue@3/dist/vue.global.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/axios/dist/axios.min.js"></script>
</head>
<body>

    <!-- 2. 定義掛載點 (Mount Point) -->
    <div id="app">
        <h1>{{ message }}</h1>
        <button @click="reverseMessage">反轉文字</button>
        
        <hr>
        
        <!-- v-for 列表渲染 -->
        <ul>
            <li v-for="user in users" :key="user.id">
                {{ user.name }} ({{ user.email }})
            </li>
        </ul>
    </div>

    <script>
        const { createApp, ref, onMounted } = Vue;

        createApp({
            setup() {
                // 3. 定義狀態 (State)
                // ref 用來定義「會變動」的資料
                const message = ref("Hello Vue!");
                const users = ref([]);

                // 4. 定義方法 (Methods)
                const reverseMessage = () => {
                    message.value = message.value.split('').reverse().join('');
                };

                // 5. 生命週期 (Lifecycle)
                // onMounted 等同於 $(document).ready()
                onMounted(async () => {
                    console.log("畫面載入完成，開始發 Ajax...");
                    try {
                        const res = await axios.get('https://jsonplaceholder.typicode.com/users');
                        users.value = res.data; // 自動更新畫面！
                    } catch (err) {
                        console.error(err);
                    }
                });

                // 6. 必須 return 給 HTML 用
                return {
                    message,
                    users,
                    reverseMessage
                };
            }
        }).mount('#app');
    </script>
</body>
</html>
```

### 核心指令重點
1.  **`{{ }}` (插值)**：把變數顯示在畫面上。
2.  **`v-model` (雙向綁定)**：用於 `<input>`，你打字變數就變，變數變框框內容就變。
3.  **`v-for` (迴圈)**：取代手寫 `forEach` + `innerHTML +=`。
4.  **`v-if` / `v-show` (判斷)**：決定元素要不要顯示。
5.  **`@click` (事件)**：取代 `addEventListener`。

---

## <a id="CH5-3"></a>5-3 前後端分離的最後一哩路：安全

最後，我們來看兩個上線前必解的問題。

### 1. CORS (跨域資源共享)

當你的前端 (localhost:5500) 呼叫後端 (localhost:8080) 時，瀏覽器會報錯：
`Access to XMLHttpRequest at ... from origin ... has been blocked by CORS policy`

**解法**：在 Spring Boot 加上全域設定。

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 允許所有路徑
                .allowedOrigins("http://127.0.0.1:5500") // 允許的前端網域 (不可以寫 * 若有帶 Cookie)
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowCredentials(true); // 允許帶 Cookie
    }
}
```

### 2. CSRF (跨站請求偽造)

如果你的 API 是依賴 Cookie (Session) 進行驗證的，就會有被偽造請求的風險。
但因為我們採用前後端分離，通常會改用 **Token (如 JWT)** 放在 Header 傳輸，這天然就能防禦 CSRF。

所以在 Spring Security 中，我們通常會**關閉** CSRF 防護：

```java
http.csrf().disable();
```

---

### 結語

恭喜你完成了這門課程！
從最基本的 fetch，到後端的 DTO 設計，再到檔案上傳與 Vue.js 的整合。你現在已經具備了開發一個現代化全端應用的基石。

接下來的旅程，建議你可以深入學習：
1.  **Vue CLI / Vite**：學習真正的元件化開發 (`.vue` 檔)。
2.  **Spring Security + JWT**：實作完整的登入驗證系統。

祝你在程式的路上越走越遠，Happy Coding!
