# 章節 2 ｜ Ajax 與後端 API 設計思維

## <a id="目錄"></a>目錄

- [2-1 為 Ajax 設計 Controller 回應](#CH2-1)
- [2-2 `@ResponseBody` 與資料回傳角色](#CH2-2)
- [2-3 JSON 作為前後端溝通格式](#CH2-3)
- [2-4 DTO 與 Entity 的責任劃分](#CH2-4)
- [2-5 HTTP Method 的語意](#CH2-5)
- [2-6 API 契約與欄位命名](#CH2-6)
- [2-7 驗證錯誤與失敗回應設計](#CH2-7)

---

### 序

歡迎來到第二章！

既然我們在上一章已經決定不回傳「畫面」而改傳「資料」了，那後端工程師的工作是不是變輕鬆了？哪怕只有一點點？（想太多，並沒有 XD）。

事實上，當後端只要專注在吐資料時，我們對於「資料品質」的要求反而變得更高了。以前 View 寫壞了頂多畫面破版，現在 API 設計不好，前端接到資料可是會直接報錯 Crash 給你看的！

這一章我們會深入探討，一個優雅、好用的後端 API 應該長什麼樣子。我們會聊到 JSON、DTO、RESTful 風格，還要教你怎麼跟前端訂下神聖不可侵犯的「API 契約」。

來吧，讓我們把後端變成一個專業的資料供應商！

---

## <a id="CH2-1"></a>[2-1 為 Ajax 設計 Controller 回應](#CH2-1)

### API 與傳統 MVC Controller 的差異

- **傳統 Controller**：邏輯結束點是 `return "viewName"`，框架會去尋找對應的 `.jsp` 或 `.html` 檔案進行渲染。
- **Ajax Controller**：邏輯結束點是 **Return Domain Object**（如 `User`, `List<Product>`），框架會負責將這些物件序列化為 JSON 格式字串。

### 為什麼 Controller 不再負責畫面

將畫面渲染職責移交給前端瀏覽器，實現了**關注點分離 (Separation of Concerns)**。
這帶來了一個巨大的好處：**後端 API 可以一魚多吃**。

同一個 `/api/products` 介面，可以同時服務：

1.  React / Vue 寫的 Web 前端
2.  iOS App
3.  Android App
4.  甚至是第三方的合作廠商

如果不這樣做，你就得為 Web 寫一個 JSP，為 iOS 寫一個 JSON API，維護起來會崩潰的。

---

## <a id="CH2-2"></a>[2-2 `@ResponseBody` 與資料回傳角色](#CH2-2)

### Spring MVC 在做的事情

當我們在 Spring MVC 的方法上加上 `@ResponseBody` 註解（或是直接在 class 上用 `@RestController`），我們是在告訴 Spring：

> 「嘿，Spring！這個方法的 return 值，請不要拿去解析成視圖路徑。請直接幫我把這個物件轉成字串，寫入 HTTP Response 的 Body 裡面。」

### JSON 回傳的意義

Spring 預設使用 **Jackson** 這個強大的函式庫來進行轉換。

```java
@ResponseBody
@GetMapping("/api/user")
public User getUser() {
    return new User("Allen", 30);
}
```

這個 `User` 物件會被自動轉換成：

```json
{
  "name": "Allen",
  "age": 30
}
```

> **進階觀念：MessageConverter**
> Spring MVC 透過 `HttpMessageConverter` 介面來決定要怎麼轉換資料。如果是物件轉 JSON，預設是用 `MappingJackson2HttpMessageConverter`。你甚至可以客製化它，例如把 Date 格式統一轉成 "yyyy-MM-dd"。

---

## <a id="CH2-3"></a>[2-3 JSON 作為前後端溝通格式](#CH2-3)

### 結構化資料的重要性

早期的 Ajax 甚至會回傳純文字或 HTML 片段，這讓前端很難處理資料（得用正規表示式或是 DOM Parser）。
JSON (JavaScript Object Notation) 提供了以「鍵-值對」(Key-Value) 和「陣列」(Array) 為基礎的結構，輕量且所有語言都支援。

### 資料格式穩定性的價值

一個定義良好的 JSON 結構，等於是前後端的契約。前端不用管後端是用 Java、Python 還是 Node.js 寫的，只要 JSON 格式不變，前端程式碼就能正常運作。

---

## <a id="CH2-4"></a>[2-4 DTO 與 Entity 的責任劃分](#CH2-4)

這是初學者最容易犯的錯誤：**直接把資料庫 Entity 回傳給前端**。

### 為什麼 Entity 不適合直接回前端

1.  **安全性風險**：Entity 可能包含敏感資料（如 `password`, `createTime`, `deleted` 標記），直接回傳等於把家底都亮給別人看。
2.  **循環參照 (Circular Reference)**：JPA 關聯中常見雙向參照（User 有 Orders，Order 有 User），Jackson 轉 JSON 時會陷入無窮迴圈直到 StackOverflow。
3.  **耦合度過高**：資料庫 Schema 改欄位名，前端就跟著掛掉？這太脆弱了。

### DTO (Data Transfer Object) 作為 API 契約

建立專門的 **DTO** 類別（例如 `UserResponseDTO`, `ProductCreateRequestDTO`）來作為 API 的輸入與輸出。

```java
// Entity (對應資料庫)
@Entity
public class User {
    private Long id;
    private String username;
    private String password; // 危險！
    private LocalDateTime createdAt;
}

// DTO (對應 API)
public class UserResponse {
    private String username;
    // 不包含 password
    // 可能把 id 加密或是根本不回傳
}
```

這樣這層緩衝讓後端可以自由重構資料庫，只要 DTO 維持不變，前端就不受影響。

---

## <a id="CH2-5"></a>[2-5 HTTP Method 的語意](#CH2-5)

好的 API 設計應該遵循 HTTP 協定原本的語意（RESTful 風格）。別再永遠只用 POST 了！

| Method     | 行為 | 意義                           | 範例                 |
| :--------- | :--- | :----------------------------- | :------------------- |
| **GET**    | 查詢 | 取得資源。安全且可被快取。     | `GET /products/1`    |
| **POST**   | 新增 | 建立資源。非冪等(Idempotent)。 | `POST /products`     |
| **PUT**    | 置換 | 更新**整筆**資源。             | `PUT /products/1`    |
| **PATCH**  | 修改 | 更新**部分**資源。             | `PATCH /products/1`  |
| **DELETE** | 刪除 | 移除資源。                     | `DELETE /products/1` |

> **💡 備註：冪等性 (Idempotence)**
> 一個操作如果執行一次跟執行 N 次的結果是一樣的，就稱為冪等。
> `GET`, `PUT`, `DELETE` 應該設計成冪等的。但 `POST` 通常不是（連送兩次訂單會買兩次）。

---

## <a id="CH2-6"></a>[2-6 API 契約與欄位命名](#CH2-6)

### 欄位名稱即契約

API 的 JSON Key 是不能隨意修改的。
後端 Java 慣用 `camelCase` (userName)，這與前端 JavaScript 的習慣一致，是最佳實踐。
避免使用 `snake_case` (user_name) 或 `PascalCase` (UserName) 造成前端困擾，除非團隊有特殊規範。

### 小改動造成的大影響

只要改錯一個欄位名稱（例如手滑把 `userid` 改成 `userId`），前端取 `data.userid` 就會變成 `undefined`。如果前端沒有做好防禦性程式設計，整個頁面可能會直接白屏崩潰。

**所以，改 API 之前，請先通知前端隊友，否則等待你的可能是殺氣騰騰的視線。**

---

## <a id="CH2-7"></a>[2-7 驗證錯誤與失敗回應設計](#CH2-7)

### 成功與失敗都是正常結果

API 不只要定義成功時回傳什麼，更要定義「失敗時」回傳什麼。

### HTTP 狀態碼與業務錯誤的區分

千萬不要不管發生什麼錯都回傳 `200 OK` 然後在裡面寫 `error: true`。這會讓前端很難利用 `fetch` 或 `axios` 的錯誤攔截機制。

- **HTTP 狀態碼 (4xx / 5xx)**：告訴瀏覽器或 HTTP Client 發生了什麼事。

  - `400 Bad Request`：參數驗證失敗。
  - `401 Unauthorized`：你沒登入。
  - `403 Forbidden`：你登入了但權限不足。
  - `404 Not Found`：找不到東西。
  - `500 Internal Server Error`：後端程式爆了（通常是 NullPointerException XD）。

- **業務錯誤訊息**：在 Response Body 中提供人類可讀的錯誤原因。

**好的錯誤回應設計範例：**

Response Status: `400 Bad Request`

```json
{
  "timestamp": "2023-10-27T10:00:00",
  "status": 400,
  "code": "STOCK_INSUFFICIENT", // 給程式判斷用的錯誤碼
  "message": "商品庫存不足，目前剩餘 2 件", // 給使用者看的訊息
  "path": "/api/orders"
}
```

---

[下一章：章節 3 ｜ 前端非同步思維與請求控制](./Chapter3_FrontendAsync.md)
