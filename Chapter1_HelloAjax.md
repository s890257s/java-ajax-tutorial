# 章節 1 ｜ Hello AJAX

## 目錄

- [1-1 為什麼我們需要 Ajax？](#CH1-1)
- [1-2 後端的轉變：Spring Web MVC 核心解析](#CH1-2)
- [1-3 前端的接招：初探 fetch](#CH1-3)

---

### 序

嗨！歡迎來到 Ajax 的世界！

你可能已經習慣了寫 JSP 或 Thymeleaf，那種「使用者點按鈕 -> 伺服器運算 -> 回傳一個全新頁面」的模式。這種模式很好，很單純，但它有一個致命傷：**使用者體驗不夠流暢**。

試想一下，你正在使用 Google Maps，這是一個網頁，但你可以隨意拖拉地圖，畫面都不會閃爍或重整。如果每拖動一下，整個網頁都要重新載入，那會是多麼崩潰的體驗？

Ajax 就是讓網頁能「偷偷跟伺服器要資料」的技術。從今天開始，我們的後端伺服器要從「畫家」（畫好 HTML 給前端）轉職成「數據分析師」（只給資料，畫面讓前端自己畫）。

---

## `<a id="CH1-1"></a>`1-1 為什麼我們需要 Ajax？

### 傳統 Spring MVC 的痛點

還記得我們在寫 Spring MVC 時的標準起手式嗎？

1. 使用者填寫表單 (`<form>`)。
2. 按下 Submit。
3. 瀏覽器**暫停**運作，轉圈圈。
4. 伺服器處理完，回傳一個全新的 HTML。
5. 瀏覽器**丟棄**舊畫面，渲染新畫面。

這個流程稱為 **同步請求 (Synchronous Request)**。它的問題在於：

- **整頁刷新 (Full Page Refresh)**：即使你只改了一個小數字，整個頁面的 Header、Footer、側邊欄全部都要重繪，浪費頻寬也浪費效能。
- **狀態遺失**：如果你捲軸滑到一半，重整後通常會跳回最上面；如果正在播放影片，重整後影片就斷了。
- **互動卡頓**：等待伺服器回應的期間，使用者什麼都不能做，只能盯著白畫面。

> **💡 觀念小筆記：同步 vs. 非同步**
>
> - **同步 (Synchronous)**：
>   並非指「同時發生」，而是指「步調一致」。發出請求後必須停下來等待回應，雙方的進度是卡在一起的。就像**打電話**，對方沒掛斷前你不能去做別的事。
> - **非同步 (Asynchronous)**：
>   發出請求後不需等待，可以繼續執行後續動作。當回應到達時，再處理回傳的資料。就像**傳 LINE 訊息**，訊息傳出後你可以繼續做其他事，等對方回覆了再去讀取即可。

### Ajax 的救贖與前世今生

**Ajax** 這個詞全名為 **Asynchronous JavaScript and XML(非同步的 JS 和 XML 技術)**，最早由使用者體驗設計師 **Jesse James Garrett** 在 2005 年的一篇文章中提出。但其實相關技術早在這之前就存在於瀏覽器中了（例如 Microsoft 的 Outlook Web Access）。

#### 1. 非同步技術發展簡史

| 年份           | 事件                       | 重大影響                                                                                                                                                                                                                  |
| :------------- | :------------------------- | :------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| **1995** | Java Applets               | Java 早期嘗試在瀏覽器運行 Java Applets（Java 小程式），但因啟動慢、安全性差與手機不支援而被淘汰。                                                                                                                         |
| **1999** | Microsoft 推出 `XMLHTTP` | IE5 首次實作，作為 Outlook Web Access 的底層技術，是 Ajax 概念的先驅。                                                                                                                                                    |
| **2004** | Google Gmail 橫空出世      | 證明了網頁可以做到像桌面軟體一樣即時互動，不用一直重新整理頁面。                                                                                                                                                          |
| **2005** | **Ajax** 一詞誕生    | [**Jesse James Garrett**](https://zh.wikipedia.org/wiki/%E5%82%91%E8%A5%BF%C2%B7%E8%A9%B9%E5%A7%86%E5%A3%AB%C2%B7%E8%B3%88%E7%91%9E%E7%89%B9) 發表文章，統合了 HTML, CSS, JS, XMLHttpRequest 等技術，正式命名為 Ajax。 |
| **2006** | jQuery 發布                | 解決了瀏覽器相容性地獄，讓 Ajax 寫起來變得很簡單 (`$.ajax`)。                                                                                                                                                           |
| **2008** | Chrome V8 引擎發布         | JS 執行速度飛升，讓網頁能處理更複雜的資料邏輯。                                                                                                                                                                           |
| **2015** | ES6 & Fetch API            | 瀏覽器原生支援 Promise 與 fetch，前端進入現代化開發。                                                                                                                                                                     |

#### 2. 誕生的動機

在 Ajax 普及之前，Web 應用程式給人的感覺就是「卡頓、反應慢、不斷白畫面」。
當時的開發者有一個終極夢想：**讓網頁的操作體驗，能像桌面軟體 (Desktop App) 一樣流暢、即時。**

#### 3. Google 的推波助瀾

真正讓 Ajax 一戰成名並成為主流標準的，是 Google 推出的兩個殺手級應用：**Gmail** 與 **Google Maps**。
使用者驚訝地發現：「天啊！原來收信不用重新載入頁面？拉動地圖竟然不會有接縫？」這種震撼的體驗徹底改變了 Web 的發展軌跡。

#### 4. 核心概念

Ajax 並不是一個單一的全新技術，而是整合了瀏覽器既有的功能來達成：

- **非同步 (Asynchronous)**：請求發出後，JavaScript 引擎繼續執行，使用者可以繼續滑網頁，不用發呆等待伺服器回應。
- **局部更新 (Partial Update)**：拿到後端回傳的資料後，透過 DOM 操作，只更新畫面上「需要變動」的那個小區塊（例如只把購物車數字從 0 變成 1）。

> **💡 冷知識：XML 去哪了？**
> 雖然 Ajax 的 **X** 代表 XML，但因為 XML 格式過於冗長且解析麻煩，現代開發 99% 都是傳送輕量級的 **JSON** 格式了。
> 但因為 Ajax 這個名字已經深植人心，大家就繼續沿用自今，並沒有改名叫 AJAJ。
>
> **格式比較：**
>
> ```xml
> <!-- 以前：XML (標籤很多，巢狀結構複雜) -->
> <user>
>     <name>Allen</name>
>     <age>25</age>
>     <address>
>         <city>Taipei</city>
>         <street>Xinyi Rd</street>
>     </address>
>     <skills>
>         <skill>Java</skill>
>         <skill>Spring</skill>
>     </skills>
> </user>
> ```
>
> ```json
> // 現在：JSON (輕量，支援物件與陣列，直觀好讀)
> {
>   "name": "Allen",
>   "age": 25,
>   "address": {
>     "city": "Taipei",
>     "street": "Xinyi Rd"
>   },
>   "skills": ["Java", "Spring"]
> }
> ```

---

## `<a id="CH1-2"></a>`1-2 後端的轉變：Spring Web MVC 核心解析

要開始寫 Ajax，後端的思維必須做最大的翻轉。我們不再回傳 `String` (View Name)，而是要回傳 **資料**。

### 深入 Spring MVC 核心：請求是怎麼跑的？

當一個 HTTP 請求進入 Spring Boot 應用程式時，會經過以下流程：

1. **DispatcherServlet** (前端控制器)：這是 Spring MVC 的心臟。所有請求都會先經過它。
2. **HandlerMapping**：DispatcherServlet 問它：「誰負責處理這個 URL？」它會找到對應的 Controller 方法。
3. **Controller 執行**：你的程式碼開始跑，最後 Return 一個東西。

**關鍵的分歧點就在這裡：**

#### 傳統模式 (SSR)

如果你回傳 `return "index";`，配合 ViewResolver (如 Thymeleaf)，Spring 會去把 `index.html` 找出來，把 Model 資料塞進去，產成最終 HTML。

#### Ajax 模式 (REST API)

如果你加上了 `@ResponseBody`，Spring 就會切換模式：
「哦！這個人不想要 HTML，他想要把這個 Return 的物件直接變成 HTTP Response 的 Body。」

這時，**HttpMessageConverter** 就會介入。預設情況下，Spring Boot 內建的 **Jackson** 函式庫會接手，把你的 Java 物件 (List, Map, Object) 轉換成 **JSON 字串**。

### 實戰：第一個 JSON API

讓我們來寫一個簡單的 API，回傳使用者的購物車資訊。

```java
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

//@Controller + @ResponseBody = @RestController
@RestController
@RequestMapping("/api/v1")
public class CartController {

    // 模擬資料庫資料
    private List<String> cartItems = List.of("Apple", "Banana", "Cherry");

    @GetMapping("/cart")
    public List<String> getCart() {
        // 我們直接回傳 List，Spring 會自動幫我們轉成 JSON Array
        // Response Body: ["Apple", "Banana", "Cherry"]
        return cartItems;
    }

    @GetMapping("/user")
    public Map<String, Object> getUserInfo() {
        // 回傳 Map 會變成 JSON Object
        // Response Body: {"name": "Allen", "age": 25}
        return Map.of("name", "Allen", "age", 25);
    }
}
```

> **常見面試題：`@Controller` vs `@RestController`**
>
> - `@Controller`：預設回傳視圖名稱 (View Name)，要回傳資料需在方法上加 `@ResponseBody`。
> - `@RestController`：所有方法預設都隱含 `@ResponseBody`，專門用來寫 API。

---

## `<a id="CH1-3"></a>`1-3 前端的接招：初探 fetch

後端準備好了，前端要怎麼拿資料？
在古早時期（2010 年前），我們用 `XMLHttpRequest`，寫起來非常痛苦。
現在，瀏覽器內建了 **Fetch API**，語法優雅多了。

### 基本 Get 請求

```javascript
// 語法：fetch(url) (預設就是 GET)
// fetch 是一個「非同步」操作，所以它是回傳一個 Promise
fetch("/api/v1/cart")
  .then((response) => {
    // 第一階段：收到回應
    // 這裡可以檢查 HTTP 狀態碼 (response.status)
    // 注意：fetch 不會幫你自動轉 JSON，要手動呼叫 .json()
    return response.json();
  })
  .then((data) => {
    // 第二階段：拿到解析後的資料 (真正的 JSON 資料)
    console.log("購物車內容:", data);

    // 在這裡更新 DOM
    document.getElementById("output").innerText = data.join(", ");
  })
  .catch((error) => {
    // 捕捉網路錯誤
    console.error("發生錯誤:", error);
  });
```

### 觀察 HTTP 封包 (必學技能)

寫 Ajax 不會看 Network Tab，就像蒙眼開車一樣危險。請打開 Chrome 開發者工具 (F12) -> **Network**。

當你發送請求時，你會看到一個新的項目：

1. **Name**: `cart`
2. **Type**: `fetch` 或 `xhr`
3. **Status**: `200` (成功)

點進去後，重點看這兩頁：

- **Headers**:
  - `Request URL`: 確認網址對不對。
  - `Method`: GET 還是 POST？
  - `Content-Type`: 後端回傳的是 `application/json` 嗎？
- **Response**:
  - 這裡就是後端實際傳回來的原始文字，永遠以這裡看到的為準。後端跟你說他傳了，但這裡沒顯示，那就是沒傳。

### 練習

1. 試著啟動後端專案。
2. 在瀏覽器 Console 輸入上面的 fetch 程式碼。
3. 觀察 Console 輸出的資料。

恭喜！你已經完成了第一次的前後端分離對接！
下一章，我們要來詳細解剖前端 JavaScript 的必備知識，把底子打穩。
