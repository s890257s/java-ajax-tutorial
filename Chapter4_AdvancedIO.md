# 章節 4 ｜ 全端安全性架構 (Full Stack Security)

## <a id="toc"></a>目錄

- [4-1 現代 Web 安全性：為什麼我們需要 JWT？](#CH4-1)
- [4-2 後端實作：Spring Security + JWT 整合](#CH4-2)
- [4-3 前端實作：Axios 攔截器與 Token 管理](#CH4-3)

---

### 序

在解決了資料傳輸 (IO) 的問題後，我們要進入全端開發最重要的一環：**安全性 (Security)**。
如何保護你的 API 不被駭客亂打？前後端分離下，怎麼做登入驗證？

這章我們將拋棄傳統的 Session，全面擁抱 **JWT (JSON Web Token)**。

---

## <a id="CH4-1"></a>[4-1 現代 Web 安全性：為什麼我們需要 JWT？](#toc)

在前後端分離的架構下，傳統的 Session-Cookie 驗證機制遇到了挑戰。

### 傳統 Session 的問題

1.  **狀態問題 (Stateful)**：Server 必須記憶體裡存著「所有登入的使用者」。如果 Server 當機重開，所有人都要重新登入。
2.  **擴展性問題 (Scalability)**：當你有兩台 Server (Load Balance) 時，使用者在 Server A 登入，下次請求被導到 Server B，Server B 不認識他。
3.  **跨網域問題 (CORS)**：Cookie 對於跨網域的限制非常嚴格（尤其是 Third-party cookie）。

### 救世主：JWT (JSON Web Token)

JWT 是一種 **Stateless (無狀態)** 的驗證機制。
它的核心概念是：**Server 不存使用者狀態，而是發給使用者一張「有簽名的識別證 (Token)」**。

#### JWT 的結構

它是三段 Base64 編碼的字串，中間用 `.` 連接：`Header.Payload.Signature`

1.  **Header**：演算法資訊 (如 `HS256`)。
2.  **Payload**：內容 (如 `userId: 1`, `role: ADMIN`, `exp: 2025-12-31`)。**注意：這裡不要放密碼！因為 Base64 是可以被解碼看到的。**
3.  **Signature**：簽章。Server 用一把**私鑰 (Secret Key)** 對前兩段進行加密雜湊。

#### 驗證流程

1.  **前端**：帳密登入 -> Server 驗證成功 -> Server 簽發 JWT 給前端。
2.  **前端**：把 JWT 存起來 (LocalStorage)。
3.  **前端**：下次發請求時，在 Header 帶上 `Authorization: Bearer <token>`。
4.  **後端**：收到請求 -> 用私鑰檢查 Signature 是否被篡改？ -> 若合法，從 Payload 拿出 userId -> 放行。

---

## <a id="CH4-2"></a>[4-2 後端實作：Spring Security + JWT 整合](#toc)

這通常是後端工程師最頭痛的部分。我們會簡化到最核心的配置。

### 1. 引入依賴 (Maven)

```xml
<!-- Spring Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<!-- JWT 工具庫 (jjwt) -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
```

### 2. JWT 工具類 (JwtUtil)

負責簽發 (Generate) 與 解析 (Parse) Token。

```java
@Component
public class JwtUtil {
    // 密鑰 (真實專案請放在配置文件並加密)
    private final String SECRET_KEY = "mySuperSecretKeyDoNotShareWithAnyone";
    private final long EXPIRATION_TIME = 86400000; // 1天 (毫秒)

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    // 1. 產生 Token
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // 2. 驗證並解析 Token (若過期或為偽造會拋出 Exception)
    public String validateTokenAndGetUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}
```

### 3. 攔截器 (JwtAuthenticationFilter)

這是最重要的守門員。它會攔截每一個請求，檢查 Header 有沒有 Token。

```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired private JwtUtil jwtUtil;
    @Autowired private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. 從 Header 拿 Token (Authorization: Bearer xxxxx)
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7); // 去掉 "Bearer "
            try {
                // 2. 驗證 Token
                String username = jwtUtil.validateTokenAndGetUsername(token);

                // 3. 告訴 Spring Security 這個人是誰 (設定 SecurityContext)
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (Exception e) {
                // Token 無效，就當作沒登入，不做任何事，讓它繼續往下走 (後面會被擋下)
                System.out.println("Token 無效: " + e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
}
```

### 4. 設定檔 (SecurityConfig)

告訴 Spring Security 哪些路徑要擋，哪些不用。

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // 前後端分離通常關閉 CSRF
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 設定為無狀態 (不使用 Session)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/login", "/api/register").permitAll() // 登入註冊頁面不用驗證
                .anyRequest().authenticated() // 其他所有 API 都要登入才能用
            )
            // 把我們的 JWT Filter 加在 UsernamePasswordAuthenticationFilter 之前
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // 這裡通常還需要設定 AuthenticationManager 與 UserDetailsService
}
```

---

## <a id="CH4-3"></a>[4-3 前端實作：Axios 攔截器與 Token 管理](#toc)

後端設好了，前端要負責兩件事：

1.  **存 Token**：登入成功後，把 Token 寫入 `localStorage`。
2.  **帶 Token**：發請求時，自動把 Token 帶上 Header。

### 1. 登入並儲存 (Login)

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

### 2. Axios 全局攔截器 (Interceptors)

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

下一章，我們要把這一切「元件化」，用 Vue.js 來優化我們的開發體驗。
