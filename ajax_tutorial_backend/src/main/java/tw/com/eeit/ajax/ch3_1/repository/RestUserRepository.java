package tw.com.eeit.ajax.ch3_1.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import tw.com.eeit.ajax.ch3_1.model.RestUser;

public interface RestUserRepository extends JpaRepository<RestUser, Long> {
    // 3-1-4: 支援 Keyword 搜尋
    List<RestUser> findByNameContaining(String keyword);
    Page<RestUser> findByNameContaining(String keyword, Pageable pageable);
}
