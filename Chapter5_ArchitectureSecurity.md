# 章節 5 ｜ 前後端分離架構與安全設計

## <a id="目錄"></a>目錄

- [5-1 什麼是前後端分離設計](#CH5-1)
- [5-2 前後端分離後的信任問題](#CH5-2)
- [5-3 Session 型驗證機制](#CH5-3)
- [5-4 Token 型驗證與 JWT](#CH5-4)
- [5-5 Session 與 JWT 的取捨](#CH5-5)
- [5-6 Spring Security 的角色定位](#CH5-6)
- [5-7 瀏覽器安全模型與跨域請求（CORS）](#CH5-7)
- [5-8 安全設計的整體觀念](#CH5-8)

---

### 序

終於來到最後一章了！這一章我們要來談談「信任」。

在前後端分離的架構下，前端和後端就像是分隔兩地的筆友。前端說：「嘿，我是 Allen，我要看我的訂單。」後端要怎麼確認這個請求真的是 Allen 發的，而不是這禮拜第三次試圖盜帳號的駭客？

這牽涉到很多安全驗證的問題。我們會聊聊 Session 和 JWT 這對宿敵的愛恨情仇，還有那個每個前端工程師都遇過、每看必生氣的「CORS 跨域錯誤」。

這章可能比較硬一點，沒有太多程式碼要寫，但這些觀念將決定你寫出來的系統是固若金湯，還是一用就倒。

深呼吸，我們開始囉！

---

## <a id="CH5-1"></a>[5-1 什麼是前後端分離設計](#CH5-1)

### 傳統 MVC 與前後端分離的差異

- **傳統**：後端包山包海（路由、業務邏輯、HTML 渲染、用戶驗證）。前端只是嵌在 HTML 裡的一點點 JS 點綴。
- **分離**：前端是**獨立的應用程式**（可能用 Vue/React 寫，打包後部署在 CDN 或 Nginx）。後端是**純粹的 API Server**。兩者透過 HTTP API 溝通。

### 前端與後端職責重劃

在分離架構下，**後端必須變得「六親不認」**。
後端不知道現在是誰在用網頁，它只認 API 請求帶來的「信物」。前端如果不送任何請求，後端根本不知道使用者的存在。

---

## <a id="CH5-2"></a>[5-2 前後端分離後的信任問題](#CH5-2)

### API 對誰開放

API 預設是無狀態且公開的。任何人只要知道 URL (`POST /api/transfer_money`)，都可以用 Postman 發送請求。

### 後端如何判斷請求者身分

後端不能再依賴「使用者剛才登入過」這個記憶（因為 HTTP 是無狀態的）。
每一次請求，前端都必須附帶某種「信物 (Credential)」，證明「我是 Allen，我有權限」。這就像你去 Costco，每次進門都要亮會員卡一樣。

---

## <a id="CH5-3"></a>[5-3 Session 型驗證機制](#CH5-3)

這是最經典的驗證方式，但在分離架構下面臨挑戰。

### Session 的基本運作流程

1.  使用者 **登入成功**。
2.  伺服器建立一個 `Session` 物件存在記憶體，並產成一個 ID (`JSESSIONID`)。
3.  伺服器把這個 ID 放在 `Set-Cookie` Header 回傳給瀏覽器。
4.  瀏覽器很乖，以後每次請求都會自動帶上這個 Cookie。
5.  伺服器看到 Cookie，就知道你是誰。

### Session 在前後端分離的限制

- **跨域問題**：如果前端在 `www.example.com`，後端在 `api.example.com`，預設 Cookie 是傳不過去的（瀏覽器隱私限制）。
- **擴展性**：如果後端有多台伺服器 (Cluster)，User A 在 Server 1 登入存了 Session，下次請求跑到 Server 2 會因為找不到 Session 而被踢出。需要做 Session 同步 (Redis) 或黏滯 (Sticky Session)。

---

## <a id="CH5-4"></a>[5-4 Token 型驗證與 JWT](#CH5-4)

**JSON Web Token (JWT)** 是目前前後端分離的主流方案。

### 為什麼需要 Token

Token 是一個自帶資訊的「通行證」。伺服器不需要查記憶體或資料庫就能驗證。

### JWT 的設計概念

JWT 是一串被加密簽章 (Signed) 的字串，包含三個部分（用 `.` 分隔）：

1.  **Header**：演算法資訊。
2.  **Payload**：內含資訊 (Claims)，例如 `userId: 123`, `role: ADMIN`, `exp: 2025-12-31`。
3.  **Signature**：用後端的密鑰 (Secret Key) 對前兩段進行簽章，防止被篡改。

> **注意：** JWT 的 Payload 只是 Base64 編碼，**並沒有加密**！任何人都可以解碼看到內容。所以**千萬不要在 JWT 裡面放密碼**。

前端拿到 JWT 後，通常存放在 `localStorage`，以後每次請求都在 Header 帶上：
`Authorization: Bearer <token>`

---

## <a id="CH5-5"></a>[5-5 Session 與 JWT 的取捨](#CH5-5)

| 特性              | Session                            | JWT                                 |
| :---------------- | :--------------------------------- | :---------------------------------- |
| **狀態**          | 有狀態 (Stateful)，Server 要存資料 | 無狀態 (Stateless)，Server 不存資料 |
| **跨域**          | 難搞 (Cookie 限制)                 | 容易 (HTTP Header)                  |
| **踢人 (Revoke)** | 容易 (Server 刪掉 Session 就好)    | 困難 (Token 發出去就像潑出去的水)   |
| **適用場景**      | 傳統 MVC、企業內部系統             | 行動 App、SPA、微服務               |

---

## <a id="CH5-6"></a>[5-6 Spring Security 的角色定位](#CH5-6)

**Spring Security** 是 Spring 生態系中最強大的安全框架，負責**認證 (Authentication)** 與 **授權 (Authorization)**。

它像是一個大門警衛 (Filter Chain)，擋在 Controller 之前。所有請求進來，先經過警衛檢查：

1.  你有沒有帶信物 (Cookie 或 Token)？
2.  你的信物是真的嗎？
3.  你的身分能去這個地方嗎 (Role Check)？

如果都通過，才放行給 Controller。

---

## <a id="CH5-7"></a>[5-7 瀏覽器安全模型與跨域請求（CORS）](#CH5-7)

### C - O - R - S (Cross-Origin Resource Sharing)

這是一個讓前端工程師聞風喪膽的字眼。

### 同源政策 (Same-Origin Policy)

瀏覽器的安全鐵律：為了怕壞人亂搞，`a.com` 的 JavaScript 預設 **不能讀取** `b.com` 的回應資料。

### 前後端分離為何一定會遇到跨域

開發階段通常：

- 前端跑在 `localhost:5173` (Vite)
- 後端跑在 `localhost:8080` (Spring Boot)

因為 Port 不同，瀏覽器判定為「不同源 (Cross-Origin)」。當前端 fetch 後端時，瀏覽器會擋下回應，並在 Console 噴出紅色錯誤。

### 解法

後端必須在 Response Header 加上允可令：
`Access-Control-Allow-Origin: http://localhost:5173`

在 Spring Boot 中，我們通常會寫一個 `CorsConfig` 來全域設定這件事。

---

## <a id="CH5-8"></a>[5-8 安全設計的整體觀念](#CH5-8)

1.  **前端不應信任自己**：前端做的任何驗證（必填、長度限制）都只是為了使用者體驗（防呆），不是為了安全。駭客可以繞過前端直接打 API。
2.  **後端不應信任任何請求**：所有進來的資料都要驗證 (Validation)。這就是為什麼我們要在 DTO 上面加 `@NotNull`, `@Size` 註解。
3.  **安全是架構設計的一部分**：安全不是最後才加掛上去的功能，而是在設計 API 與架構時就必須考量的核心。

---

恭喜你！完成了 Ajax 與前後端分離課程的學習！
現在你已經具備了現代化 Web 開發的核心觀念，可以開始嘗試自己動手打造一個完整的 SPA 專案囉！
