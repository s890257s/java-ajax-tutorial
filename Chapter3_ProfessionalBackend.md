# 章節 3 ｜ 專業級後端 API 設計 (Professional Backend)

## <a id="toc"></a>目錄

- [3-1 資料傳輸物件 (DTO) 深度解析](#CH3-1)
- [3-2 物件轉換神器：BeanUtils vs MapStruct](#CH3-2)
- [3-3 接收資料的十八般武藝](#CH3-3)

---

### 序

在能跟前端順利溝通後，我們要把焦點轉回後端。
寫 API 不只是「能跑就好」，而是要寫得「安全、好維護」。這章我們要探討後端工程師最核心的技能：**如何設計優雅的資料流**。

---

## <a id="CH3-1"></a>[3-1 資料傳輸物件 (DTO) 深度解析](#toc)

初學者最愛犯的錯誤：**直接把 Entity (資料庫物件) 回傳給前端**。

### 為什麼不能用 Entity？

1.  **安全性 (Security)**：你的 User Entity 可能有 `password`、`salt` 等欄位。直接回傳等於裸奔。
2.  **循環參照 (Circular Reference)**：JPA 關聯中常見雙向參照（User <-> Order），轉 JSON 時會無限迴圈直到 `StackOverflowError`。
3.  **耦合度 (Coupling)**：前端依賴了資料庫的欄位名。如果你改了 DB schema，前端就掛了。

### DTO (Data Transfer Object)

DTO 是一個純粹的 POJO，裡面沒有商業邏輯，只有欄位。它的存在只有一個目的：**定義前後端的資料契約**。

通常我們會分：
*   **RequestDTO**：前端傳進來的 (只包含允許被修改的欄位)。
*   **ResponseDTO**：回傳給前端的 (只包含前端需要的欄位)。

---

## <a id="CH3-2"></a>[3-2 物件轉換神器：BeanUtils vs MapStruct](#toc)

有了 DTO，我們就會面臨一個痛苦的問題：**怎麼把 Entity 轉成 DTO？**
手寫 `dto.setName(entity.getName())` 寫十個欄位還行，寫一百個會瘋掉。

### 1. Spring BeanUtils (簡單但有雷)

Spring 自帶的工具。

```java
import org.springframework.beans.BeanUtils;

// 來源, 目的
BeanUtils.copyProperties(userEntity, userDto);
```

*   **優點**：不用多寫 code，只要欄位名一樣就能轉。
*   **缺點**：
    *   **效能較差**：因為是用 Reflection (反射) 在執行期間動態猜欄位。
    *   **除錯困難**：欄位填錯名字不會報錯，只會變成 null。
    *   **深拷貝問題**：對於 List 或巢狀物件處理很弱。

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

## <a id="CH3-3"></a>[3-3 接收資料的十八般武藝](#toc)

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

*   注意：這裡**不能**加 `@RequestBody`。
*   `@ModelAttribute` 可以省略不寫。

```java
// 前端: const formData = new FormData(); formData.append("name", "iPad");
@PostMapping("/api/upload")
public String upload(@ModelAttribute ProductDto dto) {
    // Spring 會依照參數名稱 (name, price) 去 setter 塞入值
    return "ok";
}
```

### 3. 其他常見格式 (補充)

*   **URL 路徑參數**: `@PathVariable` (如 `/users/123`)
*   **Query String**: `@RequestParam` (如 `/users?page=1`)
*   **XML**: 歷史遺產，用 `@RequestBody` 配合 Jackson XML extension 可處理。
*   **Binary / Stream**: 用 `InputStream` 或 `byte[]` 接，通常用於影像處理。
*   **GraphQL**: 另一種 API 查詢語言，只有一個 Endpoint，查詢結構由前端定義。
*   **WebSocket**: 雙向即時通訊，不走傳統 HTTP Request/Response 模式。

下一章，我們將挑戰最棘手的任務：**檔案上傳與下載**。
