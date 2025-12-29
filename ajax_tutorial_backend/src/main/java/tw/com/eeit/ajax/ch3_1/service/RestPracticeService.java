package tw.com.eeit.ajax.ch3_1.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import tw.com.eeit.ajax.ch3_1.model.RestOrder;
import tw.com.eeit.ajax.ch3_1.model.RestUser;
import tw.com.eeit.ajax.ch3_1.repository.RestOrderRepository;
import tw.com.eeit.ajax.ch3_1.repository.RestUserRepository;

@Service
public class RestPracticeService {

    @Autowired
    private RestUserRepository userRepository;

    @Autowired
    private RestOrderRepository orderRepository;

    // --- User Logic ---

    public List<RestUser> getAllUsers(String keyword) {
        if (keyword != null && !keyword.isEmpty()) {
            return userRepository.findByNameContaining(keyword);
        }
        return userRepository.findAll();
    }
    
    public Page<RestUser> getUsersWithPaging(String keyword, Pageable pageable) {
        if (keyword != null && !keyword.isEmpty()) {
            return userRepository.findByNameContaining(keyword, pageable);
        }
    	return userRepository.findAll(pageable);
    }

    public RestUser getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public RestUser createUser(RestUser user) {
        return userRepository.save(user);
    }

    public RestUser updateUser(Long id, RestUser updatedUser) {
        return userRepository.findById(id).map(user -> {
            user.setName(updatedUser.getName());
            user.setEmail(updatedUser.getEmail());
            user.setActive(updatedUser.getActive());
            // 這裡不做部分更新 (PATCH)，而是整筆替換 (PUT) 語意
            return userRepository.save(user);
        }).orElse(null);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    // --- Order Logic (Nesting) ---

    public List<RestOrder> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    public RestOrder createOrderForUser(Long userId, RestOrder order) {
        return userRepository.findById(userId).map(user -> {
            order.setUser(user); // 關聯起來
            order.setStatus("CREATED");
            return orderRepository.save(order);
        }).orElse(null);
    }
}
