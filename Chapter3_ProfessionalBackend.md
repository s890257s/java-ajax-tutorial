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
REST 全名 **Representational State Transfer** (表現層狀態轉移)。聽起來很抽象，簡單來說就是：**把這個世界看作是一堆「資源 (Resource)」，我們透過 HTTP 動詞來對這些資源進行操作。**

### 1. 基本介紹：資源 (Resources) 與 動詞 (Verbs)

在 RESTful 的設計理念中，我們將網路上的事物抽象為 **資源 (Resource)**。而設計 API 時的核心原則就是：「**URL 是名詞，HTTP Method 是動詞**」。

#### === URL 僅代表資源 (名詞)===

**URL (Uniform Resource Locator)** 就像是網路上的地址，它應該只負責「定位資源」，因此 **只能包含名詞**，不應出現動詞。

- ❌ **錯誤示範 (URL 含動詞)**：`/api/getAllUsers`, `/api/createUser`, `/api/deleteUser?id=1`
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
  - **定義**：對資源進行 **部分修改** (Partial Update)。
  - **場景**：只修改密碼、只變更狀態 (Active/Inactive)。
- **DELETE (刪除)**
  - **定義**：請求伺服器刪除指定資源。
  - **場景**：刪除文章、移除我的最愛。

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
> - **決定能否「安全重試 (Retry)」**：這是實務上最重要的用途。當 Client 發出請求但因網路斷線沒收到回應時：
>   - 若是 **冪等** 操作 (如 `PUT`, `DELETE`)：Client 可以放心地自動重試，不用擔心副作用 (例如重複扣款)。
>   - 若 **非冪等** 操作 (如 `POST`)：絕對不能隨便自動重試，否則可能會造成「重複下單」或「重複建立資料」的災難。
> - **快取機制 (Caching)**：CDN 或瀏覽器只敢對冪等且安全的方法 (如 `GET`) 做快取。
>
> **3. 範例對照**
> - ✅ **冪等 (`DELETE /users/1`)**：第一次請求刪除成功；第二次請求雖會回傳 404 (Not Found)，但資料庫「沒有該使用者」的**狀態**維持不變。
> - ❌ **非冪等 (`POST /users`)**：每次請求都會「新增」一筆全新的資料 (Id 1, Id 2, ...)，改變了伺服器的狀態。
>
> **4. 常見面試題：為什麼 PATCH 不是冪等？**
> 雖然只是改欄位，看起來很像冪等，但 HTTP 協定允許 PATCH 包含「操作指令」而不僅是數據。
> - **場景 A (像冪等)**：`{ "email": "new@example.com" }` -> 重試 N 次結果都一樣 (Email 都是新的)。
> - **場景 B (非冪等)**：`{ "operation": "add", "value": 100 }` (如增加餘額) -> 重試 N 次會導致餘額重複增加。
>
> 因為 PATCH 允許場景 B 的存在，所以規範將其定義為 **非冪等**，瀏覽器與 CDN 也不會對其進行自動重試或快取。

> **💡 思考練習：該怎麼為「結帳 (Checkout)」API 命名？**
>
> 假設你要設計一個 API，讓會員 ID=1 進行購物車結帳付款。直覺上你可能會想用動詞：
> - ❌ **直覺想法**：`POST /users/1/pay` 或 `POST /checkout`
> - **問題點**：REST 風格不喜歡動詞出現在 URL。
>
> **RESTful 的拆解思路**：
> 「結帳」這個動作，其實本質上就是「建立了一筆訂單」。所以我們應該把重點放在產生的**資源 (Resource)** 上。
>
> - ✅ **正確解答**：`POST /users/1/orders`
> - **語意**：對使用者 1 新增一筆訂單資源 (Create Order)。這樣既符合 REST 的名詞規範，也清楚表達了業務含義。



### 3. 進階結構嵌套 (Nesting)

如果資源有「階層關係」，我們可以設計嵌套的 URL。
例如：「取得 使用者 ID=1 的 所有訂單」

- **URL**: `GET /users/1/orders`
- **URL**: `GET /users/1/orders/5` (取得該使用者的第 5 號訂單)

注意：建議嵌套**不要超過兩層**，否則 URL 會變得太長且難以維護。例如 `/users/1/orders/5/items/2` 就太深了，可以改為直接存取 `/orders/5/items/2`。

### 4. 查詢條件 (Filtering, Sorting, Pagination)

對於複雜的查詢，不要改變 URL 結構，而是使用 **Query String (查詢字串)**。

- **過濾 (Filtering)**：`GET /users?role=admin&active=true`
- **排序 (Sorting)**：`GET /users?sort=age,desc`
- **分頁 (Pagination)**：`GET /users?page=2&size=10` (取得第 2 頁，每頁 10 筆)

### 5. 版本控制 (Versioning)

API 一旦發布給別人用，就不能隨便改，否則依賴你的前端或 APP 會壞掉。當有「破壞性更新」時，必須升級版本。
常見做法有兩種：

1.  **URI Versioning (最常見)**：直接寫在路徑裡。
    - `/api/v1/users`
    - `/api/v2/users`
2.  **Header Versioning**：寫在 HTTP Header 裡 (較隱晦，但 URL 乾淨)。
    - Header: `Accept-version: v1`

### 6. HTTP 狀態碼 (Status Codes)

正確使用狀態碼，可以讓前端知道發生什麼事，而不是永遠回傳 200 然後在 Body 裡寫 "Error"。

- **2xx (成功)**
  - `200 OK`：請求成功 (GET, PUT)。
  - `201 Created`：新增成功 (POST)。
  - `204 No Content`：成功但沒內容回傳 (DELETE 常用)。
- **3xx (重導向)**
  - `301 Moved Permanently`：永久搬家。
  - `304 Not Modified`：快取未過期，不用重新下載。
- **4xx (客戶端錯誤 - 你傳錯了)**
  - `400 Bad Request`：參數錯誤、格式不對。
  - `401 Unauthorized`：未登入、沒 Token。
  - `403 Forbidden`：有登入但沒權限 (例如一般會員想刪除管理員)。
  - `404 Not Found`：找不到資源。
  - `405 Method Not Allowed`：這裡不支援 POST 請求。
- **5xx (伺服器錯誤 - 我壞了)**
  - `500 Internal Server Error`：程式爆了 (NullPointerException, DB 連不上)。

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
