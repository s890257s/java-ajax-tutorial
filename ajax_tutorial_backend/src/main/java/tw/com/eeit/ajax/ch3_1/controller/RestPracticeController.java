package tw.com.eeit.ajax.ch3_1.controller;

import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import tw.com.eeit.ajax.ch3_1.model.RestOrder;
import tw.com.eeit.ajax.ch3_1.model.RestUser;
import tw.com.eeit.ajax.ch3_1.service.RestPracticeService;

@RestController
@RequestMapping("/api/ch3_1")
public class RestPracticeController {

    @Autowired
    private RestPracticeService restPracticeService;

    // 3-1-1 & 3-1-2: 基本 CRUD (Verbs & Resources)

    /**
     * 取得所有使用者
     * 3-1-4: 支援 Query String 篩選 (Keyword) 與分頁
     */
    @GetMapping("/users")
    public ResponseEntity<List<RestUser>> getAllUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort) {
        
    	// 若有分頁需求
    	if (page >= 0 && size > 0) {
    		Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
    		Page<RestUser> pageResult = restPracticeService.getUsersWithPaging(keyword, pageable);
    		// 注意: 直接回傳 List 會失去 Page 的 metadata (totalElements 等)，
    		// 實務上通常會 wrap 成 PageResponse DTO，但此處為求簡化直接回傳 Content。
    		return ResponseEntity.ok(pageResult.getContent());
    	}

        return ResponseEntity.ok(restPracticeService.getAllUsers(keyword));
    }

    /**
     * 取得單一使用者
     * 3-1-6: 善用 Status Code (404)
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<RestUser> getUserById(@PathVariable Long id) {
        RestUser user = restPracticeService.getUserById(id);
        if (user != null) {
            return ResponseEntity.ok(user); // 200 OK
        } else {
            return ResponseEntity.notFound().build(); // 404 Not Found
        }
    }

    /**
     * 新增使用者
     * 3-1-6: 善用 Status Code (201 Created + Location Header)
     */
    @PostMapping("/users")
    public ResponseEntity<RestUser> createUser(@RequestBody RestUser restUser) {
        RestUser createdUser = restPracticeService.createUser(restUser);

        // 建立 Location URI: /api/ch3_1/users/{id}
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdUser.getId())
                .toUri();

        return ResponseEntity.created(location).body(createdUser); // 201 Created
    }

    /**
     * 更新使用者 (整筆)
     * PUT: 冪等操作
     */
    @PutMapping("/users/{id}")
    public ResponseEntity<RestUser> updateUser(@PathVariable Long id, @RequestBody RestUser restUser) {
        RestUser updatedUser = restPracticeService.updateUser(id, restUser);
        if (updatedUser != null) {
            return ResponseEntity.ok(updatedUser);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 刪除使用者
     * 3-1-6: 善用 Status Code (204 No Content for success delete)
     */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        restPracticeService.deleteUser(id);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    // 3-1-3: 結構嵌套 (Nesting)

    /**
     * 取得某使用者的訂單 (Nested Resource)
     * GET /users/{id}/orders
     */
    @GetMapping("/users/{userId}/orders")
    public ResponseEntity<List<RestOrder>> getUserOrders(@PathVariable Long userId) {
        // 先檢查 User 是否存在 (選擇性)
        if (restPracticeService.getUserById(userId) == null) {
             return ResponseEntity.notFound().build();
        }
        
        List<RestOrder> orders = restPracticeService.getOrdersByUserId(userId);
        return ResponseEntity.ok(orders);
    }

    /**
     * 幫某使用者新增訂單
     * POST /users/{id}/orders
     */
    @PostMapping("/users/{userId}/orders")
    public ResponseEntity<RestOrder> createOrderForUser(
            @PathVariable Long userId,
            @RequestBody RestOrder order) {
        
        RestOrder createdOrder = restPracticeService.createOrderForUser(userId, order);
        
        if (createdOrder != null) {
        	// Location: /api/ch3_1/users/{userId}/orders/{orderId} (雖非標準但語意通順)
        	// 或者直接指向 /api/ch3_1/orders/{orderId} 如果有開放該 API
             URI location = ServletUriComponentsBuilder
                     .fromCurrentContextPath() // http://localhost:8080
                     .path("/api/ch3_1/users/{userId}/orders/{orderId}") // 假設我們想導回 nested path
                     .buildAndExpand(userId, createdOrder.getId())
                     .toUri();
             
            return ResponseEntity.created(location).body(createdOrder);
        } else {
            return ResponseEntity.notFound().build(); // User not found
        }
    }
}
