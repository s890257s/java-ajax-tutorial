package tw.com.eeit.ajax.ch3_1.model;

import java.io.Serializable;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "rest_orders")
public class RestOrder implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double totalAmount;
    private String status; // PAID, UNPAID

    @CreationTimestamp
    private LocalDateTime createdAt;

    // 防止無窮迴圈
    @JsonIgnoreProperties("orders")
    @ManyToOne
    @JoinColumn(name = "user_id")
    private RestUser user;

    public RestOrder(Double totalAmount, String status) {
        this.totalAmount = totalAmount;
        this.status = status;
    }
}
