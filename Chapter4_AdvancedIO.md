# 章節 4 ｜ 進階傳輸與檔案處理 (Advanced I/O)

## 目錄

- [4-1 檔案上傳實戰：FormData 與 MultipartFile](#CH4-1)
- [4-2 檔案下載與串流：處理 Blob 資料](#CH4-2)
- [4-3 複雜場景：同時上傳 JSON 與檔案](#CH4-3)

---

### 序

文字資料傳輸很簡單，但遇到二進位檔案（圖片、PDF、Excel），很多初學者就卡關了。
「為什麼後端接到是 null？」「為什麼下載的檔案打開是亂碼？」
這章我們來解決這些最棘手的 I/O 問題。

---

## <a id="CH4-1"></a>4-1 檔案上傳實戰：FormData 與 MultipartFile

要上傳檔案，JSON 格式是無能為力的（除非你把檔案轉 Base64 字串，但效率極差）。我們必須回歸到最原始的介面：`multipart/form-data`。

### 前端：FormData 物件

在 Ajax 中，我們使用 `FormData` 來模擬表單提交。

```javascript
/* HTML:
<input type="file" id="fileInput">
<button onclick="upload()">上傳</button>
*/

async function upload() {
    const fileInput = document.getElementById('fileInput');
    const file = fileInput.files[0]; // 取得使用者選的檔案

    // 建立 FormData
    const formData = new FormData();
    formData.append('myFile', file); // key 必須跟後端 @RequestParam 一樣
    formData.append('description', '這是一張測試圖片');

    try {
        // 注意：使用 FormData 時，瀏覽器會自動設定 Content-Type 為 multipart/form-data
        // 所以我們不用自己寫 header (寫了反而會錯，因為少了 boundary)
        const res = await axios.post('/api/upload', formData);
        console.log('上傳成功', res.data);
    } catch (err) {
        console.error('上傳失敗', err);
    }
}
```

### 後端：MultipartFile

Spring MVC 提供 `MultipartFile` 介面來操作上傳的檔案。

```java
@PostMapping("/api/upload")
public String uploadFile(
    @RequestParam("myFile") MultipartFile file, 
    @RequestParam("description") String description
) throws IOException {
    
    // 1. 檢查是否為空
    if (file.isEmpty()) {
        throw new RuntimeException("檔案不能為空");
    }

    // 2. 取得原始檔名
    String fileName = file.getOriginalFilename();
    long size = file.getSize();
    
    System.out.println("收到檔案: " + fileName + ", 大小: " + size);
    System.out.println("描述: " + description);

    // 3. 存檔 (假設存到 C:/uploads)
    File dest = new File("C:/uploads/" + fileName);
    file.transferTo(dest); // 最方便的存檔方法

    return "上傳成功";
}
```

---

## <a id="CH4-2"></a>4-2 檔案下載與串流：處理 Blob 資料

下載不像上傳那麼直觀。如果後端回傳二進位流，而你用一般的方式接，會變成一堆亂碼字串。

### 後端：ResponseEntity<Resource>

```java
@GetMapping("/api/download/{filename}")
public ResponseEntity<Resource> download(@PathVariable String filename) throws MalformedURLException {
    
    // 1. 讀取檔案
    Path path = Paths.get("C:/uploads/" + filename);
    Resource resource = new UrlResource(path.toUri());

    // 2. 設定 Header 告訴瀏覽器這是要下載的，不是要直接打開
    // attachment; filename="xxx.jpg"
    String headerValue = "attachment; filename=\"" + resource.getFilename() + "\"";

    return ResponseEntity.ok()
            .contentType(MediaType.IMAGE_JPEG) // 或 MediaType.APPLICATION_OCTET_STREAM (通用)
            .header(HttpHeaders.CONTENT_DISPOSITION, headerValue)
            .body(resource);
}
```

### 前端：responseType: 'blob'

Axios 預設會把回應當成 JSON 文字解析。遇到二進位檔，**必須**指定 `responseType`。

```javascript
async function downloadImage() {
    try {
        const res = await axios.get('/api/download/cat.jpg', {
            responseType: 'blob' // 關鍵！告訴 Axios 不要轉文字，給我二進位物件
        });

        // 此時 res.data 是一個 Blob 物件
        
        // 技巧：創造一個暫時的 URL 指向這個 Blob
        const url = window.URL.createObjectURL(new Blob([res.data]));
        
        // 1. 如果是要顯示圖片
        document.getElementById('img-preview').src = url;

        // 2. 如果是要觸發瀏覽器下載
        const link = document.createElement('a');
        link.href = url;
        link.setAttribute('download', 'cat.jpg'); // 設定下載檔名
        document.body.appendChild(link);
        link.click();
        link.remove();
        
        // 釋放記憶體
        window.URL.revokeObjectURL(url);

    } catch (err) {
        console.error('下載失敗', err);
    }
}
```

---

## <a id="CH4-3"></a>4-3 複雜場景：同時上傳 JSON 與檔案

有時候我們不只有簡單的字串描述，而是一個複雜的 JSON 物件要跟檔案一起傳。

### 策略：將 JSON 轉為 String 放入 FormData

雖然可以用 `@RequestPart` 處理 `application/json` + `multipart/form-data` 的混合請求，但前端處理起來比較麻煩且瀏覽器相容性有時有坑。
最穩的做法是：**把 JSON `stringify` 後當成一個普通字串欄位傳送**。

#### 前端

```javascript
const productData = {
    name: "高級相機",
    price: 50000,
    tags: ["3C", "Photography"]
};

const formData = new FormData();
formData.append('file', fileInput.files[0]);
// 把物件轉成字串
formData.append('productJson', JSON.stringify(productData));

axios.post('/api/products/upload', formData);
```

#### 後端

使用 Jackson 的 `ObjectMapper` 手動轉回來。

```java
@PostMapping("/api/products/upload")
public String createProductWithImage(
    @RequestParam("file") MultipartFile file,
    @RequestParam("productJson") String productJson // 接字串
) throws JsonProcessingException {
    
    // 1. 處理檔案
    storageService.store(file);

    // 2. 處理 JSON
    ObjectMapper mapper = new ObjectMapper();
    ProductDto product = mapper.readValue(productJson, ProductDto.class);
    
    System.out.println("新增商品: " + product.getName());
    
    return "OK";
}
```

這樣做的好處是：前端完全不用管 Request Header 的 Content-Type 如何切換，全部交給 FormData 自動處理，無比穩健。

下一章，我們將進入最後的整合：**Vue.js 元件化思維與安全性**。
