# 章節 3 ｜ 專業級後端 API 設計 (Professional Backend)

## <a id="toc"></a>目錄

- [3-1 什麼是 RESTful API？](#CH3-1)
- [3-2 資料傳輸物件 (DTO) 深度解析](#CH3-2)
- [3-3 物件轉換神器：BeanUtils vs MapStruct](#CH3-3)
- [3-4 接收資料的十八般武藝](#CH3-4)

---

### 序

在能跟前端順利溝通後，我們要把焦點轉回後端。
寫 API 不只是「能跑就好」，而是要寫得「安全、好維護」。這章我們要探討後端工程師最核心的技能：**如何設計優雅的資料流**。

---

## <a id="CH3-1"></a>[3-1 什麼是 RESTful API？](#toc)

後端工程師最常聽到的詞就是 **RESTful API**。它不是一種程式語言，也不是一種協定，而是一種 **「軟體架構風格」 (Architectural Style)**。

REST 全名 **Representational State Transfer** (表現層狀態轉移)。這三個字拆開來解釋，就能理解它的精髓：

1.  **Representational (表現層 / 表現形式)**

    - **資源 (Resource)** 是一個抽象概念 (例如：`/api/users/1` 這位使用者)。
    - **表現形式 (Representation)** 則是資源的具體呈現方式。同一個資源，可以有不同的長相：

      ```json
      // JSON 格式 (最常用)
      { "id": 1, "name": "Alice", "role": "Admin" }
      ```

      ```xml
      <!-- XML 格式 -->
      <user>
          <id>1</id>
          <name>Alice</name>
          <role>Admin</role>
      </user>
      ```

      ```html
      <!-- HTML 格式 -->
      <div>
        <h1>Alice</h1>
        <p>Role: Admin</p>
      </div>
      ```

    - _重點：Server 傳給你的不是「Alice 本人」，而是「Alice 的表現形式」。_

2.  **State (狀態)**

    - 指的是資源在當下的「資料內容」。
    - 接續上面的例子，當你拿到 Representation 時，你就擁有了該資源的 **狀態**：

      ```javascript
      /*
       以下格式表達狀態
       id 是 1
       姓名是 Alice
       角色是 Admin
       */
      {
        "id": 1,
        "name": "Alice",
        "role": "Admin"
      }
      ```

    - 同時也隱含了 **Stateless (無狀態)** 的原則：Server 不會記得「你剛才看過 Alice」。每次請求都是獨立的，你必須自己把狀態帶給 Server (例如身分識別的 Token)。

3.  **Transfer (轉移)**

    - 這是 REST 最容易被忽略，但也最核心的概念。
    - **定義**：Client 與 Server 之間，透過 HTTP Request / Response，來回交換 Representation 的過程。

    REST 強調**系統狀態不是靠 Server 的 Session 維持，而是靠「來回傳遞」逐步推進**。這意味著 Server 沒有記憶，每一次請求都是獨立的。

    **舉個「訂單流程」的例子：**

    - **① Client：我要看看訂單**
      - Client: `GET /orders/123`
      - Server 回傳 Representation: `{ "id": 123, "status": "CREATED" }`
      - 👉 **Transfer #1**：Client 獲知當前狀態為「已建立」。
    - **② Client：好，那我付款**
      - Client: `POST /orders/123/payments`
      - Server 回傳 Representation: `{ "id": 123, "status": "PAID" }`
      - 👉 **Transfer #2**：Client 獲知狀態更新為「已付款」。

    **為什麼這樣設計？**

    - **可擴充 (Scalable)**：因為 Server 不用記住「你走到哪一步」，所以請求可以由任意一台 Server 處理。
    - **可快取 (Cacheable)**：因為是純粹的資源傳遞，GET 請求很容易被 CDN 快取。

**總結：**
**REST = Client 透過 HTTP，反覆取得資源的表示 (Representation)，逐步更新自己對系統的狀態 (State) 理解。**

---

### 1. 基本介紹：資源 (Resources) 與 動詞 (Verbs)

在 RESTful 的設計理念中，我們將網路上的事物抽象為 **資源 (Resource)**。而設計 API 時的核心原則就是：「**URL 是名詞，HTTP Method 是動詞**」。

#### === URL 僅代表資源 (名詞)===

**URL (Uniform Resource Locator)** 就像是網路上的地址，它應該只負責「定位資源」，因此 **只能包含名詞**，不應出現動詞。

- ❌ **錯誤示範 (URL 含動詞)**：

  - `/api/getAllUsers` (get 為動詞)
  - `/api/create/user` (create 為動詞、user 為單數)
  - `/api/users/update?id=1` (update 為動詞)

- ✅ **正確示範 (僅含名詞)**：
  - `/api/users` (代表使用者集合)
  - `/api/users/1` (代表特定使用者資源)

#### === HTTP 請求方法 (HTTP Methods) - 定義動詞 ===

要對資源進行什麼操作，應由 **HTTP 協定定義的標準請求方法** 來決定。常見的方法與應用場景如下：

- **GET (讀取)**
  - **定義**：請求獲取指定資源的表示形式。應為安全且冪等。
  - **場景**：取得使用者列表、查詢單筆訂單詳情。
- **POST (新增)**
  - **定義**：提交實體以建立新的資源。
  - **場景**：註冊會員、建立新訂單、上傳檔案。
- **PUT (整筆替換)**
  - **定義**：用請求的 Payload **完整替換** 目標資源 (All or Nothing)。
  - **場景**：更新使用者的「所有」資料 (若某些欄位沒傳，可能會被清空)。
- **PATCH (部分更新)**
  - **定義**：對資源進行 **部分修改**。
  - **場景**：只修改密碼、只變更狀態 (Active/Inactive)。
- **DELETE (刪除)**
  - **定義**：請求伺服器刪除指定資源。
  - **場景**：刪除文章、移除我的最愛。

> **📝 備註：所有 HTTP Method 簡介**
>
> 除了上述常用的 CRUD 方法外，HTTP 1.1 還定義了其他方法：
>
> - **HEAD**
>   與 GET 完全相同，但**只回傳 Header，不回傳 Body**。常用於檢查資源是否存在或檢查版本 (Last-Modified)。
> - **OPTIONS**
>   詢問 Server 對該資源支援哪些 Method。常用於 **CORS (跨來源資源共享)** 的預檢請求 (Preflight Request)。
> - **TRACE**
>   回傳 Server 收到的請求內容，用於測試或診斷 (但有安全性風險，通常會關閉)。
> - **CONNECT**
>   將連線改為 Tunnel 模式，主要用於 SSL/HTTPS 代理伺服器。

---

### 2. API 寫法、HTTP 請求與語意

RESTful 風格強調使用 **HTTP Method (動詞)** 來表達你的意圖 (CRUD)：

<table aria-label="HTTP 請求方法與操作對照" class="table table-sm margin-top-none">
<thead>
<tr>
<th><strong>資源 (Resource)</strong></th>
<th><strong>GET (查詢)</strong><br><small>✅ 冪等</small></th>
<th><strong>POST (新增)</strong><br><small>❌ 非冪等</small></th>
<th><strong>PUT (置換)</strong><br><small>✅ 冪等</small></th>
<th><strong>PATCH (修改)</strong><br><small>❌ 非冪等</small></th>
<th><strong>DELETE (刪除)</strong><br><small>✅ 冪等</small></th>
</tr>
</thead>
<tbody>
<tr>
<td><code>/users</code></td>
<td>取得所有使用者</td>
<td>新增一個使用者</td>
<td>語意錯誤</td>
<td>語意錯誤</td>
<td>語意錯誤</td>
</tr>
<tr>
<td><code>/users/{id}</code></td>
<td>取得指定 ID 使用者</td>
<td>語意錯誤</td>
<td>整筆替換指定資料</td>
<td>部分更新指定資料</td>
<td>刪除指定 ID 使用者</td>
</tr>
</tbody>
</table>

> **📝 備註：深入理解冪等性 (Idempotency)**
>
> **1. 定義**
> 這是 HTTP 規範中的術語，意思是：「**對同一筆資料進行一次亦或多次操作，伺服器的最終狀態應該是相同的**」。
>
> **2. 為什麼這很重要？**
>
> - **決定能否「安全重試 (Retry)」**：這是實務上最重要的用途。當 Client 發出請求但因網路斷線沒收到回應時：
>   - 若是 **冪等** 操作 (如 `PUT`, `DELETE`)：Client 可以放心地自動重試，不用擔心副作用 (例如重複扣款)。
>   - 若 **非冪等** 操作 (如 `POST`)：絕對不能隨便自動重試，否則可能會造成「重複下單」或「重複建立資料」的災難。
> - **快取機制 (Caching)**：CDN 或瀏覽器只敢對冪等且安全的方法 (如 `GET`) 做快取。
>
> **3. 範例對照**
>
> - ✅ **冪等 (`DELETE /users/1`)**：第一次請求刪除成功；第二次請求雖會回傳 404 (Not Found)，但資料庫「沒有該使用者」的**狀態**維持不變。
> - ❌ **非冪等 (`POST /users`)**：每次請求都會「新增」一筆全新的資料 (Id 1, Id 2, ...)，改變了伺服器的狀態。
>
> **4. 為什麼 PATCH 不是冪等？**
> 雖然只是改欄位，看起來很像冪等，但 HTTP 協定允許 PATCH 包含「操作指令」而不僅是數據。
>
> - **場景 A (像冪等)**：`{ "email": "new@example.com" }` -> 重試 N 次結果都一樣 (Email 都是新的)。
> - **場景 B (非冪等)**：`{ "operation": "add", "value": 100 }` (如增加餘額) -> 重試 N 次會導致餘額重複增加。
>
> 因為 PATCH 允許場景 B 的存在，所以規範將其定義為 **非冪等**，瀏覽器與 CDN 也不會對其進行自動重試或快取。

> **💡 思考練習：該怎麼為「結帳 (Checkout)」API 命名？**
>
> 假設你要設計一個 API，讓會員 ID=1 進行購物車結帳付款。直覺上你可能會想用動詞：
>
> - ❌ **直覺想法**：`POST /users/1/pay` 或 `POST /checkout`
> - **問題點**：REST 風格不喜歡動詞出現在 URL。
>
> **RESTful 的拆解思路**：
> 「結帳」這個動作，其實本質上就是「建立了一筆訂單」。所以我們應該把重點放在產生的**資源 (Resource)** 上。
>
> - ✅ **正確解答**：`POST /users/1/orders`
> - **語意**：對使用者 1 新增一筆訂單資源 (Create Order)。這樣既符合 REST 的名詞規範，也清楚表達了業務含義。

---

### 3. 結構嵌套 (Nesting)

RESTful 的 URL **不是在描述動作，而是在描述「資源之間的關係」**。所謂結構嵌套 (Nested Resources)，就是**把子資源掛在父資源之下，用 URL 階層表達「從屬關係」**。

#### === 核心概念：URL 就像資料夾 ===

把 URL 想像成檔案總管的資料夾結構：

- `/users/42/orders` -> 在 `users` 資料夾裡的 `42` 號資料夾裡的 `orders` 資料夾。
- 語意翻譯：**「42 號使用者的訂單列表」**。

不是在說「我要做什麼」，而是在說「**這個東西屬於誰**」。

#### === 典型範例 ===

**Type A：一對多 (最常見)**
重點在於資源的歸屬。

- `GET /users/42/orders` (取得 42 的所有訂單)
- `POST /users/42/orders` (幫 42 新增一張訂單)

**Type B：強從屬關係 (子資源無法獨立存在)**

- `GET /orders/99/items` (取得 99 號訂單的明細)
- _Item 離開 Order 就沒有意義，且 `/items/123` 單獨存在很怪，所以非常適合嵌套。_

**Type C：多層嵌套 (Deep Nesting)**

- `GET /companies/3/departments/7/employees`
- 語意清楚，但 **⚠️ 建議不要超過 2~3 層**，以免 URL 過長難以維護。

#### 什麼時候「不該」嵌套？

**❌ 錯誤 1：子資源其實是獨立概念**

- `POST /users/42/products`
- 如果 `Product` 是全站共用的商品，不應該隸屬於 User。應該用 `/products`。

**❌ 錯誤 2：把「查詢條件」當成結構**

- `GET /orders/2024/paid`
- 「2024」和「paid」其實是篩選條件，不是從屬資源。
- ✅ 正確：`GET /orders?year=2024&status=paid`

#### === 設計判斷法 ===

如果不確定該不該嵌套，問自己三個問題：

1.  **子資源能不能離開父資源獨立存在？**
2.  **這個 URL 是在描述關係，還是在描述條件？**
3.  **如果我只給子資源 ID，語意還完整嗎？**

只要有一題答「不行 / 不完整」，那就很適合用嵌套。

> **💡 實作提醒：URL 是語意，不是程式結構**
>
> 雖然 URL 是 `/users/42/orders`，但不代表你後端一定要寫一個 `UserOrderController`。
> 你完全可以用 `OrderController` 接收 `userId` 參數。**URL 是設計給使用者看的語意，不代表綁死後端的實作結構。**

> **💡 深度思考：訂單跟會員一定要嵌套嗎？**
>
> 這是初學者常問的問題：「訂單明明有唯一的 Order ID，為什麼還要掛在 User 底下？」
>
> **結論：商業語意上「不可」獨立，但技術實作上「可以」獨立。**
>
> 1.  **從語意層面來看**：
>     訂單不會憑空出現，一定歸屬於某個會員。
>     使用 `/users/{uid}/orders/{oid}` 可以強調這種「從屬關係」，並且自帶「權限範圍」的暗示（只能查該使用者的）。**適合前台 App 使用。**
>
> 2.  **從技術層面來看**：
>     資料庫裡 `order_id` 通常是 Primary Key，全域唯一。所以 `/orders/{oid}` 絕對能在 DB 撈到資料。
>     **適合後台管理員、客服系統使用**（因為客服不在乎是誰下的，他只要查這張單）。
>
> **懶人判斷表**：看你的 API 是給誰用的。
>
> - **給會員用 (存取自己)**：✅ 推薦嵌套 `/users/{id}/orders` (語意清楚、權限明確)。
> - **給管理員 (批次管理)**：❌ 不需要嵌套，直接 `/orders/{id}` 或 `/orders` (方便快速查詢)。

---

### 4. 查詢參數設計 (Filtering, Sorting, Pagination)

在 RESTful API 中，**查詢條件通常放在 URL 的 Query String**，用來描述「**我要找哪些資源、用什麼條件找**」。

#### === 核心概念：Path vs Query ===

REST 的設計哲學將 URL 分為兩個部分，職責必須分清楚：

| 部分      | 用途                     | 範例                              |
| :-------- | :----------------------- | :-------------------------------- |
| **Path**  | **定位資源 (Resource)**  | `/api/users` (我要找 User)        |
| **Query** | **篩選條件 (Condition)** | `?status=active` (我要找啟用中的) |

- ✅ **正確**：`GET /api/users?status=active` (篩選 User 集合)
- ❌ **錯誤**：`/api/users/active` (active 像是資源的一種？容易混淆)

> **💡 隨堂測驗：如果要找「年齡大於 30 歲」的使用者？**
>
> ❌ **錯誤設計**：`/api/users/age/gt/30` (把運算邏輯寫進路徑，雖然看得懂，但不符合 Restful 風格)。
>
> ✅ **正確思路**：Query String 才是放條件的地方。雖然 REST 沒有規定運算子怎麼寫，但常見有：
>
> - `?ageGt=30` (直白)
> - `?minAge=30` (語意清楚)

#### === 常見查詢類型 ===

**A. 篩選 (Filtering)**
最基本款，直接用欄位名稱當 Key。

- `GET /users?role=admin`
- `GET /products?category=book&inStock=true`

**B. 分頁 (Pagination)**
**這是 REST 基本技能**，絕對不要一次撈全部資料回傳。

- **Page/Size 模式**：`GET /posts?page=1&size=20` (第 1 頁，每頁 20 筆)
- **Offset/Limit 模式**：`GET /posts?offset=0&limit=20` (跳過 0 筆，抓 20 筆)

**C. 排序 (Sorting)**

- `GET /users?sort=createdAt,desc`
- `GET /users?sortBy=createdAt&order=desc` (另一種常見寫法)

**D. 搜尋 (Search / Keyword)**
通常用於模糊查詢或跨欄位搜索。

- `GET /articles?q=spring`
- `GET /products?keyword=iphone`

**E. 範圍 (Range)**
常見於時間、金額。

- `GET /orders?minPrice=1000`
- `GET /logs?from=2025-01-01&to=2025-01-31`

#### === RESTful 的完整範例 ===

將上述組合起來，一個優雅的 API 呼叫應該長這樣：

```http
GET /api/orders?status=PAID&from=2025-01-01&sort=createdAt&order=desc&page=0&size=20
```

> **翻譯白話文：**
> 「我要查 **Orders (資源)**，條件是 **已付款且 1 月份 (篩選)**，按 **建立時間倒序 (排序)**，給我 **第 1 頁的 20 筆資料 (分頁)**。」

#### === 常見反模式 (Anti-patterns) ===

- ❌ **用 POST 來查詢**：`POST /api/users/search` (除非條件長到 URL 塞不下，否則請用 GET，因為 POST 不能被 Cache)。
- ❌ **把查詢條件塞進 Path**：`/api/users/age/30` (讓人搞不清 age 是資源還是條件)。
- ❌ **動詞型 Query**：`?getActive=true` (REST 偏好名詞與狀態，不要寫成程式碼 function 名稱)。

> **💡 什麼是反模式 (Anti-pattern)？**
> 反模式是指在軟體工程中，那些看似直覺、被廣泛使用，但實際上會導致維護困難、效能低下或違反標準協定的設計方式。在 RESTful API 的世界裡，避開反模式是為了確保 API 的**一致性**、**可快取性**與**語義化**，讓介面更具備可預測性。

### === 版本控制 ===

API 一旦發布給別人用，就不能隨便改，否則依賴你的前端或 APP 會壞掉。當有「破壞性更新」時，必須升級版本。
常見做法有兩種：

1.  **URI Versioning (最常見)**：直接寫在路徑裡。
    - `/api/v1/users`
    - `/api/v2/users`
2.  **Header Versioning**：寫在 HTTP Header 裡 (較隱晦，但 URL 乾淨)。
    - Header: `Accept-version: v1`

### === HTTP 狀態碼 (Status Codes) ===

正確使用狀態碼是後端工程師的基本素養。將狀態碼表格化整理如下：

#### 1xx (資訊回應 - Information)

通常由底層協議自動處理，應用層較少直接操作。

| 代碼    | 定義                | 情境說明                                                  |
| :------ | :------------------ | :-------------------------------------------------------- |
| **100** | Continue            | Server 允許你繼續傳送 Body (通常用於大檔案上傳前的確認)。 |
| **101** | Switching Protocols | 協議切換 (例如 HTTP 升級為 WebSocket)。                   |

#### 2xx (成功 - Success)

| 代碼    | 定義       | 情境說明                                                                                            |
| :------ | :--------- | :-------------------------------------------------------------------------------------------------- |
| **200** | OK         | 標準成功回應 (通常用於 GET 查詢、PUT 修改)。                                                        |
| **201** | Created    | 資源**建立成功** (通常用於 POST)。<br>💡 _建議在 Header 回傳 `Location` 告知新資源位置。_           |
| **202** | Accepted   | 請求已接受，但**尚未處理完成**。<br>💡 _情境：非同步任務 (如匯出報表)，前端需輪詢 (Polling) 結果。_ |
| **204** | No Content | 執行成功，但**沒有內容**需回傳。<br>💡 _情境：DELETE 刪除成功，或 PUT 更新成功但不想回傳 Payload。_ |

#### 3xx (重導向 - Redirection)

| 代碼    | 定義               | 情境說明                                                                              |
| :------ | :----------------- | :------------------------------------------------------------------------------------ |
| **301** | Moved Permanently  | **永久搬家**。<br>💡 _情境：網域更換或 SEO 權重轉移。瀏覽器會快取新網址。_            |
| **302** | Found              | **暫時搬家 (舊標準)**。<br>⚠️ _注意：可能導致 POST 被瀏覽器改成 GET。_                |
| **303** | See Other          | 請去另一個地方看結果。<br>💡 _情境：POST 成功後導向 GET 結果頁 (避免 F5 重複送單)。_  |
| **304** | Not Modified       | **資源未修改** (搭配快取 Header)。<br>💡 _效能關鍵：告訴前端「用你快取的那份就好」。_ |
| **307** | Temporary Redirect | **暫時搬家 (嚴格版)**。<br>✅ _保證 POST 導向後 HTTP Method 不變。_                   |
| **308** | Permanent Redirect | **永久搬家 (嚴格版)**。<br>✅ _保證 POST 導向後 HTTP Method 不變。_                   |

> **📌 [302 vs 307] 特別說明：**
> 302 在舊版實作中，常導致 POST 請求被瀏覽器改成 GET (資料遺失)。
> 如果你的 API 是內部轉發 (如 `/api/v1` -> `/api/v2`) 且包含 POST 方法，**請務必使用 307** 以確保 Body 內容不遺失。

#### 4xx (客戶端錯誤 - Client Error)

問題出在「請求方」(前端/使用者)，Server 是正常的。

| 代碼    | 定義               | 情境說明                                                                                             |
| :------ | :----------------- | :--------------------------------------------------------------------------------------------------- |
| **400** | Bad Request        | **請求格式錯誤**。<br>💡 _情境：必傳參數漏了、JSON 格式壞掉。_                                       |
| **401** | Unauthorized       | **未認證 (Who are you?)**。<br>💡 _情境：未登入、Token 過期/無效。_                                  |
| **403** | Forbidden          | **權限不足 (I know you, but No)**。<br>💡 _情境：一般會員想進後台、試圖存取他人訂單。_               |
| **404** | Not Found          | **找不到資源**。<br>💡 _情境：ID 不存在、網址打錯。_                                                 |
| **405** | Method Not Allowed | **方法不支援**。<br>💡 _情境：API 只寫了 GET，你卻用 POST 打進來。_                                  |
| **409** | Conflict           | **資源衝突**。<br>💡 _情境：註冊已存在的帳號、併發修改 (編輯期間，資料已被他人更新，導致更新失敗)。_ |
| **429** | Too Many Requests  | **請求過於頻繁**。                                                                                   |

#### 5xx (伺服器端錯誤 - Server Error)

問題出在「收件人」(後端/伺服器)，前端參數沒錯，是我們掛了。

| 代碼    | 定義                  | 情境說明                                                                       |
| :------ | :-------------------- | :----------------------------------------------------------------------------- |
| **500** | Internal Server Error | **伺服器內部錯誤**。<br>💡 _預設錯誤代碼，表示你沒有做好後端錯誤分類_          |
| **502** | Bad Gateway           | **閘道錯誤**。<br>💡 _情境：Nginx 活著，但後面的 App Server 掛了。_            |
| **503** | Service Unavailable   | **服務暫時無法使用**。<br>💡 _情境：伺服器過載、停機維護中。_                  |
| **504** | Gateway Timeout       | **閘道逾時**。<br>💡 _情境：Nginx 等不到後端 App Server 的回應 (程式跑太久)。_ |

> **💡 補充說明：什麼是 Nginx？**
>
> Nginx 是一款高效能的 **Web 伺服器 (Web Server)** 與 **反向代理伺服器 (Reverse Proxy)**。在現代後端架構中，它通常扮演「守門員」的角色：
>
> 1.  **反向代理**：隱藏後端真實伺服器的 IP，統一由 Nginx 接收請求再轉發，提升安全性。
> 2.  **負載平衡 (Load Balancing)**：將大量流量平均分配給多台後端伺服器，避免單一伺服器過載。
> 3.  **靜態資源處理**：直接處理圖片、CSS、JS 等檔案，減輕後端程式（如 Java/Python）的負擔。
> 4.  **SSL 終止**：統一在 Nginx 層處理 HTTPS 加密連線，後端伺服器只需跑簡單的 HTTP。
>
> 當你在狀態碼看到 **502 (Bad Gateway)** 或 **504 (Gateway Timeout)** 時，通常就是 Nginx 這個「中間人」連不到後端程式，或是等後端程式跑太久而逾時。

> **💡 補充說明：正向代理 vs 反向代理**
>
> 很多人會搞混這兩個概念，簡單來說，區別在於「代理誰」：
>
> 1.  **正向代理 (Forward Proxy)**：
>
>     - **代理對象**：客戶端 (Client)。
>     - **情境**：你要「翻牆」看外部網站，或是公司內部電腦透過一台 Proxy 上網。
>     - **作用**：隱藏客戶端真實 IP、繞過存取限制。伺服器只知道代理伺服器來找它，不知道背後是誰。
>
> 2.  **反向代理 (Reverse Proxy)**：
>     - **代理對象**：伺服器端 (Server)。
>     - **情境**：Nginx 擋在多台 App Server 前面。
>     - **作用**：隱藏伺服器真實 IP、負載平衡、SSL 憑證管理。客戶端只知道 Nginx，不知道背後到底是哪台伺服器在處理。
>
> **一句話總結：** 正向代理隱藏「真正的客戶端」，反向代理隱藏「真正的伺服器」。

### 7. RESTful 六大約束 (Constraints)

要稱為 RESTful，必須符合以下原則 (面試常考)：

1.  **Client-Server**：前後端分離，各司其職。
2.  **Stateless (無狀態)**：**最重要！** 伺服器不保存 Client 的狀態 (Session)，每次請求都必須包含所有驗證資訊 (如 Token)。這讓伺服器容易擴展 (Scale-out)。
3.  **Cacheable (可快取)**：回應要標示是否可被瀏覽器或 CDN 快取。
4.  **Uniform Interface (統一介面)**：URL 命名風格一致、標準的 HTTP 動詞。
5.  **Layered System (分層系統)**：Client 不知道他連的是正牌 Server 還是中間的 Load Balancer，結構可以隨意抽換。
6.  **Code on Demand (可選)**：Server 可以傳送 executable code 給 Client (如 JS)，這點現在已經是 Web 標準了。

### 8. 其他風格 API 補充

REST 雖然是主流，但不是唯一：

- **GraphQL (Facebook)**：
  - **痛點**：REST 常有 "Over-fetching" (拿太多不需要的欄位) 或 "Under-fetching" (要打好幾隻 API 才湊齊資料) 的問題。
  - **特色**：只有一個 Endpoint (`/graphql`)。前端可以**自己寫 Query 語言**決定要拿什麼欄位。
  - **適用**：前端需求變化極快、資料關聯複雜的應用。
- **gRPC (Google)**：
  - **特色**：使用 HTTP/2 + Protobuf (二進制格式)。比 JSON 輕量非常多，速度極快。
  - **適用**：微服務 (Microservices) 之間的內部傳輸，要求極致效能的場景。

---

## <a id="CH3-2"></a>[3-2 資料傳輸物件 (DTO) 深度解析](#toc)

初學者最愛犯的錯誤：**直接把 Entity (資料庫物件) 回傳給前端**。

### 為什麼不能用 Entity？

1.  **安全性 (Security)**：你的 User Entity 可能有 `password`、`salt` 等欄位。直接回傳等於裸奔。
2.  **循環參照 (Circular Reference)**：JPA 關聯中常見雙向參照（User <-> Order），轉 JSON 時會無限迴圈直到 `StackOverflowError`。
3.  **耦合度 (Coupling)**：前端依賴了資料庫的欄位名。如果你改了 DB schema，前端就掛了。

### DTO (Data Transfer Object)

DTO 是一個純粹的 POJO，裡面沒有商業邏輯，只有欄位。它的存在只有一個目的：**定義前後端的資料契約**。

通常我們會分：

- **RequestDTO**：前端傳進來的 (只包含允許被修改的欄位)。
- **ResponseDTO**：回傳給前端的 (只包含前端需要的欄位)。

---

## <a id="CH3-3"></a>[3-3 物件轉換神器：BeanUtils vs MapStruct](#toc)

有了 DTO，我們就會面臨一個痛苦的問題：**怎麼把 Entity 轉成 DTO？**
手寫 `dto.setName(entity.getName())` 寫十個欄位還行，寫一百個會瘋掉。

### 1. Spring BeanUtils (簡單但有雷)

Spring 自帶的工具。

```java
import org.springframework.beans.BeanUtils;

// 來源, 目的
BeanUtils.copyProperties(userEntity, userDto);
```

- **優點**：不用多寫 code，只要欄位名一樣就能轉。
- **缺點**：
  - **效能較差**：因為是用 Reflection (反射) 在執行期間動態猜欄位。
  - **除錯困難**：欄位填錯名字不會報錯，只會變成 null。
  - **深拷貝問題**：對於 List 或巢狀物件處理很弱。

### 2. MapStruct (業界標準，強烈推薦)

MapStruct 是一個 **Annotation Processor**。它會在 **編譯時期 (Compile Time)** 自動幫你生成「手寫版」的轉換程式碼。效能跟手寫一模一樣快！

#### 步驟一：引入依賴 (Maven)

```xml
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct</artifactId>
    <version>1.5.5.Final</version>
</dependency>
<!-- 處理器放在 build plugin 或 annotationProcessorPaths -->
```

#### 步驟二：定義介面 (Mapper)

```java
@Mapper(componentModel = "spring") // 讓它自動變成 Spring Bean
public interface UserMapper {

    // 基本轉換：欄位名一樣自動對應
    UserDto toDto(User entity);

    // 反向轉換
    User toEntity(UserDto dto);

    // List 轉換：它會自動跑迴圈呼叫上面的 toDto
    List<UserDto> toDtoList(List<User> list);
}
```

#### 步驟三：處理欄位名稱不一致 (`@Mapping`)

```java
@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(source = "createdDate", target = "orderDate") // 來源 -> 目標
    @Mapping(source = "user.name", target = "customerName") // 甚至可以鑽進去拿屬性
    @Mapping(target = "internalCode", ignore = true) // 忽略不轉
    OrderDto toDto(Order order);
}
```

#### 步驟四：自定義邏輯 (`@AfterMapping`)

如果有些邏輯無法直接對應（例如把 `firstName` + `lastName` 變成 `fullName`），可以用 Java 寫。

```java
@Mapper(componentModel = "spring")
public abstract class UserMapper {

    // 必須宣告成 abstract class 才能寫實作
    public abstract UserDto toDto(User entity);

    @AfterMapping // MapStruct 轉完欄位後，會自動呼叫這個方法
    protected void afterToDto(User entity, @MappingTarget UserDto dto) {
        dto.setFullName(entity.getFirstName() + " " + entity.getLastName());

        if (entity.getStatus() == 1) {
            dto.setStatusLabel("啟用中");
        } else {
            dto.setStatusLabel("停用");
        }
    }
}
```

---

## <a id="CH3-4"></a>[3-4 接收資料的十八般武藝](#toc)

後端 Controller 要怎麼接前端丟過來的東西？主要看 `Content-Type`。

### 1. JSON (`application/json`) -> `@RequestBody`

這是 Ajax 最常用的方式。前端送 JSON 物件，後端用 Entity 或 DTO 接。

```java
@PostMapping("/api/products")
public Product create(@RequestBody ProductDto dto) {
    // Spring 會把 JSON 字串反序列化成 Java 物件
    return productService.save(dto);
}
```

### 2. 表單資料 (`application/x-www-form-urlencoded` 或 `multipart/form-data`) -> `@ModelAttribute`

如果你前端是用 `<form>` 傳統提交，或者 AJAX 用 `FormData` 物件（通常為了上傳檔案），就要用這個。

- 注意：這裡**不能**加 `@RequestBody`。
- `@ModelAttribute` 可以省略不寫。

```java
// 前端: const formData = new FormData(); formData.append("name", "iPad");
@PostMapping("/api/upload")
public String upload(@ModelAttribute ProductDto dto) {
    // Spring 會依照參數名稱 (name, price) 去 setter 塞入值
    return "ok";
}
```

### 3. 其他常見格式 (補充)

- **URL 路徑參數**: `@PathVariable` (如 `/users/123`)
- **Query String**: `@RequestParam` (如 `/users?page=1`)
- **XML**: 歷史遺產，用 `@RequestBody` 配合 Jackson XML extension 可處理。
- **Binary / Stream**: 用 `InputStream` 或 `byte[]` 接，通常用於影像處理。
- **GraphQL**: 另一種 API 查詢語言，只有一個 Endpoint，查詢結構由前端定義。
- **WebSocket**: 雙向即時通訊，不走傳統 HTTP Request/Response 模式。

下一章，我們將挑戰最棘手的任務：**檔案上傳與下載**。
