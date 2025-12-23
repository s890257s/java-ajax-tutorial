# 章節 1 ｜ 從頁面導向到資料導向

## <a id="目錄"></a>目錄

- [1-1 傳統 Spring MVC 的請求／回應模型](#CH1-1)
- [1-2 表單提交與整頁刷新](#CH1-2)
- [1-3 整頁刷新的隱性成本](#CH1-3)
- [1-4 Ajax 的核心概念](#CH1-4)
- [1-5 Controller 回傳畫面 vs 回傳資料](#CH1-5)
- [1-6 Ajax 對使用者體驗與互動模式的影響](#CH1-6)

---

### 序

嗨！歡迎來到 Ajax 課程的第一章！

還記得以前我們寫網頁的時候，每次按下按鈕、送出表單，整個畫面都會「閃」一下，然後重新載入嗎？那種感覺就像是為了換一個燈泡，但卻把整間房子的裝潢都拆掉重做一樣。

在這個章節，我們要來聊聊網頁開發模式是怎麼從這種「動不動就整頁重刷」的古早味，一路進化到現在大家習慣的「滑順」體驗。我們會探討這背後最關鍵的轉變，也就是後端如何從一位「畫家」（畫好畫面給你），轉職成一位「數據分析師」（只給你純資料，畫面你自己畫）。

準備好這場思維升級之旅了嗎？讓我們開始吧！

---

## <a id="CH1-1"></a>[1-1 傳統 Spring MVC 的請求／回應模型](#目錄)

在傳統的網頁開發模式（例如使用 JSP 或 Thymeleaf）中，瀏覽器與伺服器的互動非常單純且直接：**一個請求，換這一個頁面**。

### Request → Controller → View

1.  **使用者操作**：點擊網頁上的連結或送出表單。
2.  **發送請求**：瀏覽器發送一個 HTTP Request 給伺服器。
3.  **後端處理**：Spring MVC 的 Controller 接收請求，呼叫 Service 處理業務邏輯，拿到資料。
4.  **渲染畫面**：Controller 將資料塞入 View (模板)，伺服器將 HTML 渲染完成。
5.  **回傳回應**：伺服器回傳完整的 HTML 給瀏覽器。

### 以「畫面回傳」為核心的設計思維

在這種模式下，後端的邏輯緊密綁定前端的呈現。Controller 不僅要關心「資料是什麼」，還要關心「資料要顯示在哪個 View 上」。

```java
// 傳統 Controller：只是想改個數量，卻要重繪整張圖
@Controller
public class CartController {

    @PostMapping("/cart/update")
    public String updateQuantity(@RequestParam Long productId, @RequestParam Integer newQty, Model model) {
        // 1. 處理業務邏輯 (只改了一行資料)
        cartService.updateQuantity(productId, newQty);

        // 2. 為了回傳畫面，必須重新撈取「所有」購物車資料
        List<CartItem> items = cartService.getCurrentCart();
        model.addAttribute("cartItems", items);

        // 3. 回傳整個 View 名稱，觸發整頁渲染
        return "cart/page";
    }
}
```

**對應的 Thymeleaf 頁面 (View)：**

```html
<!-- cart/page.html -->
<table>
  <tr th:each="item : ${cartItems}">
    <td th:text="${item.name}">商品名稱</td>
    <td>
      <!-- 即使只是按個 +1，這也是一個傳統表單提交 -->
      <!-- 按下 Submit -> 瀏覽器轉圈 -> 整頁白屏重繪 -->
      <form action="/cart/update" method="post">
        <input type="hidden" name="productId" th:value="${item.id}" />
        <input type="number" name="newQty" th:value="${item.quantity}" />
        <button type="submit">更新</button>
      </form>
    </td>
  </tr>
</table>
```

> **💡 深入觀念：什麼是 SSR (Server-Side Rendering)？**
>
> 這種由後端直接生成完整 HTML 的方式，我們稱為 **伺服器端渲染 (SSR)**。
>
> **運作原理**：
> 當瀏覽器發送請求時，伺服器先將資料查詢出來，並與模板 (Template, 如 JSP、Thymeleaf) 結合，在伺服器端「組裝」好完整的 HTML 字串，最後才傳送給瀏覽器。
>
> **SSR 的優點**：
>
> 1.  **SEO 友善 (Search Engine Optimization)**：搜尋引擎爬蟲一抓就能看到完整的標題與內容，有利於網站排名（因為內容都在 HTML 裡了）。
> 2.  **首屏載入可見性高 (FCP 較快)**：使用者瀏覽器收到回應的瞬間就有畫面看，不需要等待 JavaScript 下載並執行完畢。
>
> **SSR 的缺點**：
>
> 1.  **伺服器負擔較大**：每次有人訪問頁面，伺服器都要重新運算、組裝 HTML，流量大時對 CPU 消耗明顯。
> 2.  **使用者體驗不連續**：每次切換頁面都是「整頁刷新」，會出現瞬間白畫面，互動感較差。
> 3.  **前後端耦合**：前端邏輯寫在後端專案裡，職責分工不乾淨。

---

## <a id="CH1-2"></a>[1-2 表單提交與整頁刷新](#目錄)

### 表單 submit 的實際流程

當你在 HTML 表單按下 Submit 按鈕時，瀏覽器會執行預設的行為：

1.  收集表單內 `<input>` 的資料。
2.  組合成 HTTP Request (通常是 POST)。
3.  **暫停** 當前頁面的所有執行。
4.  等待伺服器回應。
5.  收到回應後，**丟棄** 當前頁面，重新載入並渲染新的 HTML。

### 為什麼只改一小塊畫面，卻整頁重來

舉個例子，你在逛購物網站，只是在購物車多加了一件商品。
在傳統模式下，雖然你只動了購物車那個小區塊的數字，但伺服器必須重新回傳「整個包含 Header、Footer、商品列表、側邊欄」的 HTML。

這就像是你只是想換一張桌布，結果建商把整棟房子拆了重蓋給你一樣，既浪費資源又沒有效率。

---

## <a id="CH1-3"></a>[1-3 整頁刷新的隱性成本](#目錄)

整頁刷新（Full Page Refresh）帶來的體驗損耗往往比開發者想像的還大：

1.  **使用者輸入狀態的中斷**
    - 如果不小心填錯表單被退回，原本填好的欄位可能被清空（除非後端很貼心地幫你回填 value，但這很累人）。
2.  **捲軸位置與互動上下文的遺失**
    - 你滑到頁面底部看詳情，點擊某個操作導致重整，結果頁面又跳回最上面，真的很惱人！
3.  **多區塊被迫同步刷新**
    - 如果頁面上有正在播放的影片，或是即時的聊天視窗，整頁刷新會強制中斷這些體驗。

---

## <a id="CH1-4"></a>[1-4 Ajax 的核心概念](#目錄)

為了拯救使用者的體驗，**Ajax (Asynchronous JavaScript and XML)** 登場了！雖然後來大家都傳 JSON 不傳 XML 了，但這個名字還是沿用至今。

### 非同步請求 (Asynchronous Request)

Ajax 允許瀏覽器透過 JavaScript（使用 `XMLHttpRequest` 或現代的 `fetch` API）在「背景」偷偷發送 HTTP 請求。

這意味著：**請求發送後，使用者仍然可以繼續與頁面互動，不用盯著白畫面轉圈圈。**

### 局部更新而非整頁刷新

當 JavaScript 收到後端回傳的資料後，可以透過 DOM 操作，精準地只更新頁面上需要變動的那一塊區域。

**練習看看：使用 fetch 發送請求**

```javascript
// 假設我們有一個按鈕，點擊後要去要資料
document.querySelector("#loadBtn").addEventListener("click", () => {
  // 1. 在背景發送請求，不會換頁
  fetch("/api/products")
    .then((response) => response.json()) // 2. 解析 JSON 資料
    .then((data) => {
      // 3. 拿到資料，用 JS 局部更新畫面
      updateProductList(data);
    });
});

function updateProductList(data) {
  const list = document.querySelector("#list");
  list.innerHTML = ""; // 清空舊資料

  data.forEach((item) => {
    list.innerHTML += `<li>${item.name} - $${item.price}</li>`;
  });
}
```

---

## <a id="CH1-5"></a>[1-5 Controller 回傳畫面 vs 回傳資料](#目錄)

隨著 Ajax 的引入，後端 Controller 的角色發生了本質上的轉變：

### View 導向的 Controller (傳統)

- **回傳型別**：`String` (View Name)
- **回傳內容**：完整的 HTML
- **關注點**：畫面長什麼樣子

### Data 導向的 Controller (Ajax)

- **回傳型別**：資料物件 (Java Bean, List, Map)
- **回傳內容**：JSON 格式的純資料
- **關注點**：資料正不正確

```java
// Ajax Controller
@RestController // 這是 @Controller + @ResponseBody 的縮寫
public class ProductApiController {

    @GetMapping("/api/products")
    public List<Product> list() {
        return productService.findAll();
        // Spring 會自動把 List<Product> 轉成 JSON 陣列回傳
    }
}
```

> **進階觀念：CSR (Client-Side Rendering)**
> 這種由前端透過 Ajax 拿資料，再由 JavaScript 產生 HTML 的方式，稱為 CSR。它的優點是使用者體驗極佳（像 App 一樣流暢），缺點是初次載入可能較慢（要下載 JS bundle），且 SEO 需要特別處理（雖然 Google 現在已經看得懂 JS 了）。

---

## <a id="CH1-6"></a>[1-6 Ajax 對使用者體驗與互動模式的影響](#目錄)

- **從等待頁面到即時回饋**
  - 使用者按讚，愛心瞬間變紅（JS 先改 DOM），背景再默默送請求通知後端。使用者感覺不到任何延遲。
- **使用者行為的改變**
  - 現代使用者習慣了 App 般的流暢體驗，對於無故的整頁白屏容忍度變低。
  - **無限捲動 (Infinite Scroll)**：滑到底部自動載入更多，完全取代了傳統的「上一頁/下一頁」。
  - **即時搜尋 (Autocomplete)**：打字打到一半，建議選單就跳出來了。

這就是為什麼我們要學 Ajax。它不只是一個技術，更是一種「以使用者為中心」的設計哲學。

---

[下一章：章節 2 ｜ Ajax 與後端 API 設計思維](./Chapter2_BackendApiDesign.md)
