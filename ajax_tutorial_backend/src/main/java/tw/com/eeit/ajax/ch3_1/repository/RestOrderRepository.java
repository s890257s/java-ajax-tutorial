package tw.com.eeit.ajax.ch3_1.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import tw.com.eeit.ajax.ch3_1.model.RestOrder;

public interface RestOrderRepository extends JpaRepository<RestOrder, Long> {
    // 3-1-3: 找出該 User 的所有訂單
    List<RestOrder> findByUserId(Long userId);
}
