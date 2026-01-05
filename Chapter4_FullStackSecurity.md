# 章節 4 ｜ 全端安全性架構 (Full Stack Security)

## <a id="toc"></a>目錄

- [4-1 現代 Web 安全性：為什麼我們需要 JWT？](#CH4-1)
  - [傳統 Session 機制：回憶過去 (The Stateful Era)](#CH4-1-1)
  - [現代解決方案：JWT (JSON Web Token)](#CH4-1-2)
  - [JWT 的結構解密](#CH4-1-3)
  - [JWT 與 Session 超級比一比](#CH4-1-4)
  - [JWT 的現實雷點 (Pitfalls)](#CH4-1-5)
  - [總結：驗證流程](#CH4-1-6)
- [4-2 後端實作：Spring Security + JWT 整合](#CH4-2)
  - [1. 引入依賴 (Maven)](#CH4-2-1)
  - [2. JWT 工具類 (JwtUtil)](#CH4-2-2)
  - [3. 攔截器 (JwtAuthenticationFilter)](#CH4-2-3)
  - [4. 設定檔 (SecurityConfig)](#CH4-2-4)
  - [5. 驗證服務 (AuthService)](#CH4-2-5)
  - [6. 全域錯誤處理 (GlobalExceptionHandler)](#CH4-2-6)
- [4-3 後端簡化配置 (Simplified Configuration)](#CH4-3)
  - [1. 引入依賴 (Maven)](#CH4-3-1)
  - [2. JWT 工具類 (JwtUtil)](#CH4-3-2)
  - [3. 攔截器 (簡化版 JwtAuthenticationFilter)](#CH4-3-3)
  - [4. 驗證服務 (AuthService)](#CH4-3-4)
  - [5. 簡化版設定檔 (SecurityConfig)](#CH4-3-5)
- [4-4 前端實作：Axios 攔截器與 Token 管理](#CH4-4)
  - [1. 建立 axios 實例 (apiClient.js)](#CH4-4-1)
  - [2. 請求攔截器 (附帶 Token)](#CH4-4-2)
  - [3. 回應攔截器 (統一處理錯誤)](#CH4-4-3)
  - [4. 登出機制 (Logout)](#CH4-4-4)
- [4-5 結語：Web 開發的完整拼圖](#CH4-5)

---

### [序](#toc)

在解決了資料傳輸 (IO) 的問題後，我們要進入全端開發最重要的一環：**安全性 (Security)**。
如何保護你的 API 不被駭客亂打？前後端分離下，怎麼做登入驗證？

這章我們將拋棄傳統的 Session，全面擁抱 **JWT (JSON Web Token)**。

---

## <a id="CH4-1"></a>[4-1 現代 Web 安全性：為什麼我們需要 JWT？](#toc)

在前後端分離的架構下，傳統的 Session-Cookie 驗證機制遇到了挑戰。我們需要一種更現代、更適合分散式系統的解決方案。

### <a id="CH4-1-1"></a>[傳統 Session 機制：回憶過去 (The Stateful Era)](#toc)

在 JWT 出現之前，Web 開發(如 JSP/Servlet, Spring MVC) 主要是依賴 **Session** 與 **Cookie** 來識別使用者。

#### 運作流程

- **登入**：使用者輸入帳密。
- **建立 Session**：伺服器驗證通過後，在**伺服器記憶體 (RAM)** 中建立一個 `HttpSession` 物件，並產生一個唯一的 Session ID (如 `JSESSIONID`)。
- **回傳 Cookie**：伺服器在 HTTP Response Header 中加入 `Set-Cookie: JSESSIONID=XYZ123;`。
- **後續請求**：瀏覽器之後的每次請求，都會自動在 Header 帶上 `Cookie: JSESSIONID=XYZ123`。
- **識別**：伺服器拿著這個 ID 去記憶體翻找：「喔！這是 Alice 的 Session」，然後放行。

> **👻 複習：傳統 Session 寫法 (Spring MVC)**
>
> 雖然我們現在比較少用，但看懂舊程式碼也是一種技能：
>
> ```java
> @PostMapping("/login")
> public String login(String username, String password, HttpSession session) {
>     if (checkUser(username, password)) {
>         // 關鍵：將使用者資訊存入 "伺服器記憶體"
>         // 伺服器會給這個 User 一個專屬的記憶體空間
>         session.setAttribute("user", new User("Alice", "Admin"));
>
>         // Tomcat 會自動幫你把 JSESSIONID 塞給前端 Cookie，你不用寫程式碼
>         return "loginSuccess";
>     }
>     return "loginFail";
> }
> ```

#### Session 的三大痛點

雖然 Session 很方便 (不用自己管加密)，但在現代架構下卻有致命傷：

1.  **狀態問題 (Stateful) - 伺服器負擔大**
    Server 必須在記憶體裡存著「所有登入的使用者」。如果有 100 萬人在線，Server 的 RAM 就會被塞爆。且一旦 Server 當機重開，記憶體清空，這 100 萬人就被強制登出了。
2.  **擴展性問題 (Scalability) - 負載平衡的惡夢**
    當網站流量變大，你加開了第二台 Server (Server B)。
    - 使用者在 Server A 登入 (Session 在 A 的記憶體)。
    - 下一次請求被 Load Balancer 導到了 Server B。
    - **Server B：我不認識你啊！(因為 Session 不在我的記憶體)**
    - _雖然可以用 Sticky Session 或 Redis Session 解決，但架構變得更複雜。_
3.  **跨網域與行動端問題 (CORS & Mobile)**
    - **Cookie** 對於跨網域 (Cross-Domain) 的限制非常嚴格，前後端不同網址時很容易被瀏覽器擋掉。
    - **App (iOS/Android)** 原生不支援 Cookie 機制，要模擬 Cookie 行為很麻煩。

### <a id="CH4-1-2"></a>[現代解決方案：JWT (JSON Web Token)](#toc)

為了解決上述問題，**JWT** 應運而生。它是一種 **Stateless (無狀態)** 的驗證機制。

它的核心概念是：**「Server 不存使用者的狀態，而是發給使用者一張有『防偽簽名』的識別證 (Token)。」**
就像看電影的票根，工讀生不用記住你的臉，只要看票根是真的就能入場。

### <a id="CH4-1-3"></a>[JWT 的結構解密](#toc)

一句話講重點：**JWT 是一種「把身分與狀態，打包成可被驗證、不可竄改字串」的憑證格式。**

它長這樣：`xxxxx.yyyyy.zzzzz` (三段以 `.` 分隔的 Base64URL 字串)
意義是：`Header.Payload.Signature` (標頭.內容.簽名)

#### Part 1: Header (標頭)

這部分主要用來描述 JWT 的元數據 (Metadata)，告訴伺服器應該如何處理這個 Token。它通常包含兩個關鍵資訊：

1.  **`alg` (Algorithm)**：指定簽名所使用的雜湊演算法（如 `HS256` 對稱加密 或 `RS256` 非對稱加密）。
2.  **`typ` (Type)**：Token 的類型，在 JWT 中固定為 `JWT`。

```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```

_這段 JSON 會經過 Base64Url 編碼成為第一段字串。_

#### Part 2: Payload (內容)

這是最重要的地方，存放我們真正要傳遞的資訊，這些資訊被稱為 **Claims (聲明)**。
Claims 依照用途可以分為三種類型：

1.  **Registered Claims (註冊聲明)**：
    JWT 規範定義的標準欄位，建議使用但不強制。

    - `iss` (Issuer): 發行者
    - `sub` (Subject): 主題 (通常放 User ID)
    - `exp` (Expiration Time): 過期時間 (Unix Timestamp，必要！)
    - `iat` (Issued At): 簽發時間
    - `aud` (Audience): 接收者

2.  **Public Claims (公開聲明，原則上期末專案中不會有機會使用)**：
    可以由使用者自定義，但為了避免名稱衝突，建議在 [IANA JSON Web Token Registry](https://www.iana.org/assignments/jwt/jwt.xhtml) 進行註冊，或是將其定義為一個包含抗衝突命名空間的 URI (例如 `https://example.com/is_admin`)。

3.  **Private Claims (私有聲明)**：
    前後端約定好的自定義欄位，用來傳遞業務資料。

**範例 Payload：**

```json
{
  "sub": "user_123", // Registered: User ID
  "name": "Alice", // Private: 自定義欄位
  "role": "ADMIN", // Private: 權限
  "exp": 1735660800 // Registered: 過期時間
}
```

> **⚠️ 重要觀念：JWT ≠ 加密**
> Payload 只是透過 **Base64Url 編碼**，**並沒有加密 (Not Encrypted)**。
> 意思是 **「任何拿到 Token 的人，都可以透過 Base64 解碼看到裡面的 Payload 內容」**。
> 👉 **絕對不要在 Payload 放密碼、身分證字號、信用卡號等機敏資料。**

#### Part 3: Signature (簽章)

**這是 JWT 安全的靈魂**，它的用途只有一個：**「確保 Payload 沒有被竄改」**。

簽章的產生過程：

1. 取出編碼後的 Header。
2. 取出編碼後的 Payload。
3. 把兩者用 `.` 接起來：`HeaderInBase64.PayloadInBase64`。
4. 使用 Server 獨有的 **Private Key (私鑰)** 對這串字串進行演算法雜湊 (如 HMACSHA256)。

**公式如下：**

```javascript
// HS256 簽名公式概念
Signature = HMACSHA256(
  base64UrlEncode(header) + "." + base64UrlEncode(payload),
  secret_key // 只有 Server 知道這把鑰匙 (絕對不能外流)
);
```

**驗證原理 (Anti-tamper)：**
當駭客把 Payload 裡的 `"role": "USER"` 偷改成 `"role": "ADMIN"` 時：

1. Payload 的 Base64 字串變了。
2. 駭客沒有 Server 的 `secret_key`，無法重新計算出正確的新 Signature。
3. Server 收到 Token 後，用自己的 Key 重算一次，發現跟駭客傳來的不一樣 ❌。
4. **驗證失敗，直接拒絕請求 (401 Unauthorized)**。

---

### <a id="CH4-1-4"></a>[JWT 與 Session 超級比一比](#toc)

| 比較項目     | Session (狀態在 Server)               | JWT (狀態在 Client)                     |
| :----------- | :------------------------------------ | :-------------------------------------- |
| **狀態儲存** | Server 記憶體 / Redis                 | Client 端 (LocalStorage/Cookie)         |
| **擴充性**   | 困難 (需解決 Server 間同步問題)       | **容易** (Server 不存狀態，隨便加機器)  |
| **登出機制** | **即時** (Server 刪掉 Session 即失效) | **不即時** (Token 發出後在過期前都有效) |
| **資安風險** | Session Hijack(會話劫持)              | Token 外洩 (被偷走等於帳號被盜)         |
| **適用場景** | 單體架構、傳統 MVC                    | **前後端分離、微服務、App**             |

### <a id="CH4-1-5"></a>[JWT 的現實雷點 (Pitfalls)](#toc)

JWT 不是銀彈，使用時必須注意以下限制：

1.  **無法即時登出**
    因為 Server 不存狀態，一旦 Token 發給使用者，在 `exp` 過期前都有效。即使你後端刪除帳號，他手上的 Token 依然能通過驗證。
    _(解法：搭配 Redis 做黑名單，但這又變回 Stateful 了)_。

2.  **Token 體積較大**
    比起只是一個短字串的 JSESSIONID，JWT 包含大量資訊，Header 請求會變大。Payload 不要塞太多無意義的資料。

3.  **儲存風險 (XSS)**
    通常我們把 JWT 存在 `localStorage`，但這容易被 XSS 攻擊讀取。較安全的做法是存在 **HttpOnly Cookie** (防止 JS 讀取)，但這會犧牲一些跨網域的便利性。

> **💡 實務小撇步：Access Token + Refresh Token**
> 為了安全性，通常會將 Access Token 效期設很短 (如 15 分鐘)，過期後用另一張長效的 Refresh Token (如 7 天) 去換新的。這樣就算 Access Token 被偷，駭客也只能使用 15 分鐘。

### <a id="CH4-1-6"></a>[總結：驗證流程](#toc)

1.  **登入**：前端傳帳密，後端驗證成功後**簽發 JWT**。
2.  **儲存**：前端將 JWT 存起來 (LocalStorage)。
3.  **攜帶**：前端發請求時，Header 帶上 `Authorization: Bearer <token>`。
4.  **驗證**：後端**驗算簽章** (Signature) 與**檢查效期** (exp)。
    - 通過 👉 解析 Payload 裡的 UserID，直接放行 (完全不用查 DB 或 Session)。
    - 失敗 👉 401 Unauthorized。

---

## <a id="CH4-2"></a>[4-2 後端實作：Spring Security + JWT 整合](#toc)

這通常是後端工程師最頭痛的部分，以下介紹較為完整的整合方式。

### <a id="CH4-2-1"></a>[1. 引入依賴 (Maven)](#toc)

```xml
<!-- === Spring Security === -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<!-- === JWT 工具庫 (jjwt) === -->
<!-- jjwt 介面 -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.13.0</version>
</dependency>
<!-- jjwt 實作 -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.13.0</version>
    <scope>runtime</scope>
</dependency>
<!-- 讓 jjwt 使用 Jackson 進行 JSON 解析(Springboot 預設使用 Jackson) -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.13.0</version>
    <scope>runtime</scope>
</dependency>
```

### <a id="CH4-2-2"></a>[2. JWT 工具類 (JwtUtil)](#toc)

負責簽發與解析 Token。

```java
@Component
public class JwtUtil {
    // 私鑰 (真實專案請放在配置文件並加密)
    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor("my_super_secret_key_do_not_share_with_anyone".getBytes());

    // JWT 有效時間，單位為秒
    private final long EXPIRATION_IN_SECONDS = 60 * 60;

    /**
     * 產生 JWT
     */
    public String generateToken(String userId, String role) {
        return Jwts.builder() // 使用 builder 模式設定 token
                .subject(userId) // 設定主題(subject)，通常放唯一識別的 User ID
                .claim("role", role) // 設定自定義的 claim，可隨需求增加，但建議不要存放太多資料
                // .claim("role", List.of("user", "admin")) // 也可存放物件
                .issuedAt(new Date()) // 設定發行時間
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_IN_SECONDS * 1000)) // 設定到期時間
                .signWith(SECRET_KEY) // 使用私鑰簽名
                .compact(); // 產生 token
    }

    /**
     * 解析並驗證 JWT，若驗證失敗則拋出異常
     */
    public Claims getClaims(String token) {
      return Jwts.parser() // 使用 parser() 取得解析器
          .verifyWith(SECRET_KEY) // 設定解密用密鑰
          .build() // 建立解析器
          .parseSignedClaims(token) // 解析 token
          .getPayload(); // 取得解析後結果
    }

    /**
     * 取得主題 (通常是 User ID)
     */
    public String getSubject(String token) {
      return getClaims(token).getSubject();
    }

    /**
     * 取得自定義的 claim
     */
    public String getValue(String token, String key) {
      return (String) getClaims(token).get(key);
    }

    /**
     * 驗證 Token 是否合法
     */
    public Boolean isTokenValid(String token) {
      getSubject(token); // 若 token 有任何異常，則由 jjwt 套件直接拋出錯誤。
      return true; // 能走到回傳表示驗證通過，token 合法
    }

}
```

### <a id="CH4-2-3"></a>[3. 攔截器 (JwtAuthenticationFilter)](#toc)

這是最重要的守門員。它會攔截每一個 HTTP 請求，檢查 Header 有沒有 Token。  
在此標準實作中，我們包含了完整的異常捕獲 (Try-Catch)，能將 Token 過期或錯誤的訊息，精準地轉發給 `GlobalExceptionHandler` 處理，回傳正確的狀態碼 (401/403)。

```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final HandlerExceptionResolver handlerExceptionResolver;
	private final MemberService memberService;
	private final JwtUtil jwtUtil;

	public JwtAuthenticationFilter(HandlerExceptionResolver handlerExceptionResolver, MemberService memberService,
			JwtUtil jwtUtil) {
		this.handlerExceptionResolver = handlerExceptionResolver;
		this.memberService = memberService;
		this.jwtUtil = jwtUtil;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		// 從 headers 中取得 Authorization header
		final String authHeader = request.getHeader("Authorization");

		// 若 http 請求的 headers 中不包含 Authorization；或者 headers 中包含
		// Authorization，但格式不合法，則直接交由 Spring Security 處理
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}

		// 提取 jwt token
		final String jwtToken = authHeader.substring(7);

		// 檢驗 jwt 是否有效，若解析過程中出現錯誤，則會由 jwtUtil（基於 jjwt 實現）拋出異常。
		// 捕獲異常後，轉發給 GlobalExceptionHandler，並在那定義回應狀態碼。
		try {
			jwtUtil.isTokenValid(jwtToken);
		} catch (Exception e) {
			handlerExceptionResolver.resolveException(request, response, null, e);
			return;
		}

		// 讀取 member 資料
		Integer memberId = Integer.valueOf(jwtUtil.getSubject(jwtToken));
		MemberDto memberDto = memberService.getById(memberId);

		// 若管理員則給予管理員權限
		Set<SimpleGrantedAuthority> auths = new HashSet<>();
		if (Objects.equals(memberDto.getRole(), "ADMIN")) {
			auths.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
		}

		/**
		 * UsernamePasswordAuthenticationToken 為 Spring Security 設計用於表示「已認證身份」的標準物件
		 * 參數一: 認證成功的使用者物件
		 * 參數二: 憑證、密碼等物件，但在 JWT 驗證中不須提供(若是傳統 MVC 表單認證才須提供)
		 * 參數三: 權限列表物件
		 */
		UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
				memberDto, null, auths);

		// 在此次 HTTP 請求(context)中，儲存驗證成功的 user
		SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

		// 繼續執行過濾鏈
		filterChain.doFilter(request, response);
	}

}
```

### <a id="CH4-2-4"></a>[4. 設定檔 (SecurityConfig)](#toc)

告訴 Spring Security 哪些路徑要擋，哪些不用。

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final HandlerExceptionResolver handlerExceptionResolver;

	public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
			HandlerExceptionResolver handlerExceptionResolver) {
		this.jwtAuthenticationFilter = jwtAuthenticationFilter;
		this.handlerExceptionResolver = handlerExceptionResolver;
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		// CORS: 設定允許的 domain、method、header
		CorsConfiguration corsConfiguration = new CorsConfiguration();
		corsConfiguration.setAllowedOrigins(List.of("*"));
		corsConfiguration.setAllowedMethods(List.of("*"));
		corsConfiguration.setAllowedHeaders(List.of("*"));

		// 設定開放的 URL，無須登入
		List<String> allowedURL = List.of(
			"/test/**", // 開放測試用程式，無須權限驗證
			"/api/auth/**", // 開放認證相關程式，如登入、登出
			"/api/**" // 開發時故意全部開放
		);

		return http // 使用 HttpSecurity http 物件展開串聯設定
				.cors(cros -> cros.configurationSource(request -> corsConfiguration)) // 使用自訂的 corsConfiguration
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // jwt 無狀態
				.csrf(AbstractHttpConfigurer::disable) // 因無狀態，故不用考慮 CSRF(跨站請求偽造) 問題
				.authorizeHttpRequests(auth -> {
					// 設定權限主要位置，比對順序由上往下，先比對成功則先放行。

					// 將 allowedURL 中的所有路徑設為無須登入即可訪問
					for (String url : allowedURL) {
						auth.requestMatchers(url).permitAll();
					}

					// Admin API 只有管理員角色才可以存取。
					auth.requestMatchers("/api/admin/**").hasRole("ADMIN");

					// 除了開放的 api 以外，其他都要有"登入狀態"才能存取
					auth.anyRequest().authenticated();
				})
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class) // 新增自訂過濾器，以處理 jwt 驗證與解析
				.exceptionHandling(exceptionHandling -> {
					/**
					 * === 轉發驗證錯誤 ===
					 * 為什麼要轉發錯誤?
					 * Spring Security 的驗證在 Spring MVC 的執行流程之前進行。
					 * 當驗證失敗（如未授權訪問或權限不足）時，Spring Security 會拋出錯誤，
					 * 但這些異常不會進入 MVC 層，因此無法被 GlobalExceptionHandler 捕獲。
					 *
					 * 故我們在此處使用 HandlerExceptionResolver 將異常轉發，
					 * 以便統一由全局異常處理器進行處理。
					 */
					exceptionHandling.authenticationEntryPoint((req, resp, exception) -> {
						handlerExceptionResolver.resolveException(req, resp, null, exception);
					});
				}).build();
	}
}

```

> **💡 知識補充：什麼是 CORS (Cross-Origin Resource Sharing)？**
>
> 瀏覽器有一項安全機制叫「同源政策 (Same-Origin Policy)」，預設會阻止「A 網站的 JS」去呼叫「B 網站的 API」，除非 B 網站明確說「好，我允許 A 來呼叫我」。
>
> 1.  **同源定義**：協定 (http/https)、網域 (domain)、埠號 (port) 三者完全相同。
>     - 前端：`http://localhost:5500` (Live Server)
>     - 後端：`http://localhost:8080` (Spring Boot)
>     - **Port 不同，就是跨域！**
> 2.  **CORS Headers**：
>     - 當瀏覽器發現是跨域請求時，會先發送一個 `OPTIONS` (預檢) 請求。
>     - 後端必須回傳 `Access-Control-Allow-Origin: *` 等 Header，瀏覽器才會真正發送原本的 GET/POST 請求。
>     - 上述程式碼中的 `.cors(...)` 就是在幫我們自動加上這些 Headers。

> **💡 備註：何謂 CSRF (Cross-Site Request Forgery, 跨站請求偽造)？**
>
> CSRF 是一種攻擊方式，攻擊者誘導已登入的使用者瀏覽惡意網頁，該網頁會利用瀏覽器「自動帶上 Cookie」的特性，在使用者不知情的情況下，向目標伺服器發送偽造的請求（例如：轉帳、修改密碼）。
>
> **為什麼使用 JWT 可以關閉 CSRF 防護？**
>
> 1. **不依賴 Cookie**：
>    傳統 Session 機制將 Session ID 存於 Cookie，瀏覽器發送請求時會自動帶上。而 JWT 通常存放在前端的 `LocalStorage` 中，並透過 Header 的 `Authorization` 欄位手動發送。
> 2. **無法自動偽造**：
>    瀏覽器**不會**自動將 LocalStorage 的內容放入 Header。攻擊者在第三方網站發送請求時，無法取得使用者的 JWT，因此無法通過驗證。
> 3. **無狀態性**：
>    由於後端不儲存 Session，且請求必須顯式攜帶 Token，這天然地防禦了 CSRF 攻擊。

### <a id="CH4-2-5"></a>[5. 驗證服務 (AuthService)](#toc)

為了方便在 Controller 或 Service 層取得「當前登入的使用者」，我們封裝一個 `AuthService`。

```java
@Service
public class AuthService {
    /**
     * 取得當前登入的使用者資訊
     * 我們在 Filter 中已將驗證成功的 MemberDto 放入 SecurityContextHolder
     */
    public MemberDto getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof MemberDto) {
            return (MemberDto) authentication.getPrincipal();
        }

        return null; // 未登入或無法識別
    }
}
```

### <a id="CH4-2-6"></a>[6. 全域錯誤處理 (GlobalExceptionHandler)](#toc)

為了讓前端能收到統一格式的錯誤訊息，並正確處理 401/403 等狀態碼，我們使用 Spring 的 `@ControllerAdvice` 來集中處理異常。

```java
@ControllerAdvice
public class GlobalExceptionHandler {

	/* === 其他內部錯誤，直接回應 === */
	// 其他內部錯誤 > 500
	@ExceptionHandler(Exception.class)
	public ResponseEntity<String> handleInternalServerException(Exception exception) {
		exception.printStackTrace(); // 在 console 區列印出錯誤，以便錯誤追蹤
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exception.getMessage());
	}

	/* === 未登入或權限不足 === */
	// 未登入 > 401
	@ExceptionHandler(InsufficientAuthenticationException.class)
	public ResponseEntity<String> handleInsufficientAuthenticationException(Exception exception) {
    exception.printStackTrace();
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("未登入或無權存取此資源。");
	}

	/* === 權限不足(例如刪除別人的貼文) === */
	// 權限不足 > 403
	@ExceptionHandler(AccessDeniedException.class) // AccessDeniedException 是自定義錯誤
	public ResponseEntity<String> handleAccessDeniedException(Exception exception) {
    exception.printStackTrace();
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body("你無權執行此操作");
	}

	/* === 請求參數檢驗失敗(null 或空白) === */
	// 參數錯誤 > 400
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<String> handleValidationException(MethodArgumentNotValidException exception) {
    exception.printStackTrace();
		StringBuilder errorMessage = new StringBuilder();

		exception.getBindingResult().getAllErrors().forEach(error -> {
			if (error instanceof FieldError fieldError) {
				errorMessage.append(fieldError.getField());
				errorMessage.append(": ");
				errorMessage.append(fieldError.getDefaultMessage());
				errorMessage.append("、");
			} else {
				errorMessage.append(error.getDefaultMessage());
			}
		});

		// 若最後一個字是「、」則移除。
		if (errorMessage.lastIndexOf("、") == errorMessage.length() - 1) {
			errorMessage.deleteCharAt(errorMessage.length() - 1);
		}

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage.toString());
	}

	/* === 登入失敗 === */
	// 帳號或密碼錯誤 > 401
	@ExceptionHandler(IncorrectAccountOrPasswordException.class) // IncorrectAccountOrPasswordException 是自定義錯誤
	public ResponseEntity<String> handleIncorrectAccountOrPasswordException(Exception exception) {
    exception.printStackTrace();
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("登入失敗，帳號或密碼錯誤。");
	}

	// 帳號被禁用 > 403
	@ExceptionHandler(AccountDisabledException.class) // AccountDisabledException 是自定義錯誤
	public ResponseEntity<String> handleAccountDisabledException(Exception exception) {
    exception.printStackTrace();
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body("登入失敗，此帳號已被禁止使用。");
	}

	/* === JWT === */
	// jwt 過期 > 401
	@ExceptionHandler(ExpiredJwtException.class)
	public ResponseEntity<String> handleJwtExpiredException(Exception exception) {
    exception.printStackTrace();
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("jwt token 已過期，請重新登入。");
	}

	// jwt 解析錯誤 > 401
	@ExceptionHandler(JwtException.class)
	public ResponseEntity<String> handleJwtException(Exception exception) {
    exception.printStackTrace();
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("jwt token 不合法。");
	}
}
```

---

## <a id="CH4-3"></a>[4-3 後端簡化配置 (Simplified Configuration)](#toc)

為了降低學習門檻，在這提供一套「簡化版」的 Security 配置。

它的核心精神是：**「雖然有做登入檢查，但預設不擋任何權限 (Permit All)」**。
只要 Token 合法，我們就將使用者資訊放入 Context；若 Token 無效或沒帶，也不會特別阻擋請求，讓後續的 Controller 決定如何處理。

### <a id="CH4-3-1"></a>[1. 引入依賴 (Maven)](#toc)

與標準版相同，確保 `pom.xml` 有以下依賴：

```xml
<!-- === Spring Security === -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<!-- === JWT 工具庫 (jjwt) === -->
<!-- jjwt 介面 -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.13.0</version>
</dependency>
<!-- jjwt 實作 -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.13.0</version>
    <scope>runtime</scope>
</dependency>
<!-- 讓 jjwt 使用 Jackson 進行 JSON 解析(Springboot 預設使用 Jackson) -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.13.0</version>
    <scope>runtime</scope>
</dependency>
```

### <a id="CH4-3-2"></a>[2. JWT 工具類 (JwtUtil)](#toc)

負責簽發 (Generate) 與 解析 (Parse) Token。

```java
@Component
public class JwtUtil {
    // 私鑰 (真實專案請放在配置文件並加密)
    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor("my_super_secret_key_do_not_share_with_anyone".getBytes());

    // JWT 有效時間，單位為秒
    private final long EXPIRATION_IN_SECONDS = 60 * 60;

    /**
     * 產生 JWT
     */
    public String generateToken(String userId, String role) {
        return Jwts.builder() // 使用 builder 模式設定 token
                .subject(userId) // 設定主題(subject)，通常放唯一識別的 User ID
                .claim("role", role) // 設定自定義的 claim，可隨需求增加，但建議不要存放太多資料
                // .claim("role", List.of("user", "admin")) // 也可存放物件
                .issuedAt(new Date()) // 設定發行時間
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_IN_SECONDS * 1000)) // 設定到期時間
                .signWith(SECRET_KEY) // 使用私鑰簽名
                .compact(); // 產生 token
    }

    /**
     * 解析並驗證 JWT，若驗證失敗則拋出異常
     */
    public Claims getClaims(String token) {
      return Jwts.parser() // 使用 parser() 取得解析器
          .verifyWith(SECRET_KEY) // 設定解密用密鑰
          .build() // 建立解析器
          .parseSignedClaims(token) // 解析 token
          .getPayload(); // 取得解析後結果
    }

    /**
     * 取得主題 (通常是 User ID)
     */
    public String getSubject(String token) {
      return getClaims(token).getSubject();
    }

    /**
     * 取得自定義的 claim
     */
    public String getValue(String token, String key) {
      return (String) getClaims(token).get(key);
    }

    /**
     * 驗證 Token 是否合法
     */
    public Boolean isTokenValid(String token) {
      getSubject(token); // 若 token 有任何異常，則由 jjwt 套件直接拋出錯誤。
      return true; // 能走到回傳表示驗證通過，token 合法
    }

}
```

### <a id="CH4-3-3"></a>[3. 攔截器 (簡化版 JwtAuthenticationFilter)](#toc)

在這個簡化版本中，我們**移除了錯誤處理 (Try-Catch) 與權限轉換 (Role)** 的邏輯，讓程式碼更乾淨易懂。
若是 Token 格式錯誤或過期，`jwtUtil` 會直接拋出例外 (導致 HTTP 500)。

```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final HandlerExceptionResolver handlerExceptionResolver;
	private final MemberService memberService;
	private final JwtUtil jwtUtil;

	public JwtAuthenticationFilter(HandlerExceptionResolver handlerExceptionResolver, MemberService memberService,
			JwtUtil jwtUtil) {
		this.handlerExceptionResolver = handlerExceptionResolver;
		this.memberService = memberService;
		this.jwtUtil = jwtUtil;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		// 從 headers 中取得 Authorization header
		final String authHeader = request.getHeader("Authorization");

		// 若 http 請求的 headers 中不包含 Authorization；或者 headers 中包含
		// Authorization，但格式不合法，則直接交由 Spring Security 處理
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}

		// 提取 jwt token
		final String jwtToken = authHeader.substring(7);

		// 讀取 member 資料
		Integer memberId = Integer.valueOf(jwtUtil.getSubject(jwtToken));
		MemberDto memberDto = memberService.getById(memberId);

		/**
		 * UsernamePasswordAuthenticationToken 為 Spring Security 設計用於表示「已認證身份」的標準物件
		 * 參數一: 認證成功的使用者物件
		 * 參數二: 憑證、密碼等物件，但在 JWT 驗證中不須提供(若是傳統 MVC 表單認證才須提供)
		 * 參數三: 權限列表物件
		 */
		UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
				memberDto, null, null);

		// 在此次 HTTP 請求(context)中，儲存驗證成功的 user
		SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

		// 繼續執行過濾鏈
		filterChain.doFilter(request, response);
	}

}
```

### <a id="CH4-3-4"></a>[4. 驗證服務 (AuthService)](#toc)

為了方便在 Controller 或 Service 層取得「當前登入的使用者」，我們封裝一個 `AuthService`。

```java
@Service
public class AuthService {
    /**
     * 取得當前登入的使用者資訊
     * 由於我們在 SecurityConfig 設定了 .anyRequest().permitAll()，
     * 所以這裡取得的 authentication 可能是 null (未登入) 或 AnonymousAuthenticationToken。
     */
    public MemberDto getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof MemberDto) {
            return (MemberDto) authentication.getPrincipal();
        }

        return null; // 未登入或無法識別
    }
}
```

### <a id="CH4-3-5"></a>[5. 簡化版設定檔 (SecurityConfig)](#toc)

這是與標準版最大的差異。我們將 `authorizeHttpRequests` 全部設為 `permitAll()`，意思是不管有沒有 Token，路徑都會放行。
但我們還是保留了 `JwtAuthenticationFilter`，確保如果使用者有帶 Token，我們依然能解析出身分並放入 Context，供後續程式使用。

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // CORS: 設定允許的 domain、method、header
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOrigins(List.of("*"));
        corsConfiguration.setAllowedMethods(List.of("*"));
        corsConfiguration.setAllowedHeaders(List.of("*"));

        return http
            // 1. 開啟 CORS (前端 5500 -> 後端 8080，不同 Port 視為跨域)
            .cors(cros -> cros.configurationSource(request -> corsConfiguration))
            // 2. 關閉 CSRF (因為是無狀態 API)
            .csrf(csrf -> csrf.disable())
            // 3. 關閉 Session (改用 JWT)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // 4. 允許所有請求
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            )
            // 5. 插入 JWT Filter (雖然允許所有請求，但還是要解析 Token 才知道是誰)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }
}
```

---

## <a id="CH4-4"></a>[4-4 前端實作：Axios 攔截器與 Token 管理](#toc)

後端設好了，前端要負責兩件事：

1.  **存 Token**：登入成功後，把 Token 寫入 `localStorage`。
2.  **帶 Token**：發請求時，自動把 Token 帶上 Header。

### 登入並儲存 (Login)

```javascript
async function login(username, password) {
  try {
    const res = await axios.post("/api/login", { username, password });

    // 假設後端回傳 { token: "eyJhb..." }
    const token = res.data.token;

    // 存入 LocalStorage
    localStorage.setItem("jwt_token", token);
    alert("登入成功！");
  } catch (err) {
    alert("登入失敗");
  }
}
```

### Axios 全局攔截器 (Interceptors)

我們不希望每次發請求 (`axios.get...`) 都要手動寫 Header。我們可以用 Interceptor 來「劫持」所有請求，統一加工。

```javascript
// 建立一個 axios 實體 (建議不要汙染全域 axios)
const api = axios.create({
  baseURL: "http://localhost:8080/api",
});

// === Request 攔截器 (發出請求前) ===
api.interceptors.request.use(
  (config) => {
    // 從 LocalStorage 拿 Token
    const token = localStorage.getItem("jwt_token");
    if (token) {
      // 如果有 token，就加到 Header
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// === Response 攔截器 (收到回應後) ===
api.interceptors.response.use(
  (response) => {
    return response; // 成功就直接回傳
  },
  (error) => {
    // 統一處理 401 (未授權)
    if (error.response && error.response.status === 401) {
      alert("登入逾時，請重新登入");
      localStorage.removeItem("jwt_token"); // 清除無效 token
      window.location.href = "/login.html"; // 導回登入頁
    }
    return Promise.reject(error);
  }
);

// 之後使用 api.get() 就會自動帶 Token 了！
```

### 總結

現在的架構已經非常完整：

1.  使用者輸入帳密。
2.  後端驗證通過，簽發 JWT。
3.  前端收到 JWT，存入 LocalStorage。
4.  之後所有請求，Axios 自動帶上 JWT。
5.  後端 Filter 攔截檢查 JWT，合法才放行。
6.  若 JWT 過期，後端回傳 401，前端自動導回登入頁。

## <a id="CH4-5"></a>[4-5 結語：Web 開發的完整拼圖](#toc)

**REST API -> Ajax -> IO -> Security**

恭喜你！這就是現代 Web 開發的完整拼圖。
這門課帶你走過了從後端 API 設計，到前端 Ajax 串接，再到安全性與 Token 機制的實作。

雖然技術是不斷更新的（比如前端有 Vue, React, Angular），但這幾章學到的 **HTTP 標準、Token 機制、IO 串流原理**，是這十年來都沒有變過的 Web 基礎。掌握了這些底層觀念，未來的路，你可以走得更穩、更遠。

Happy Coding! 🚀

---

> **備註**：如果你對現代前端框架感興趣，可以以此為基礎，繼續閱讀本教學的附錄章節：**前端架構優化與 Vue.js 導論**。
