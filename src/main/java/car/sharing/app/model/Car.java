package car.sharing.app.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@Setter
@EqualsAndHashCode
@ToString
@SQLDelete(sql = "UPDATE cars SET is_deleted = true WHERE id=?")
@SQLRestriction("is_deleted = false")
@Accessors(chain = true)
@Table(name = "cars")
public class Car {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String model;
    @Column(nullable = false)
    private String brand;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TypeName typeName;
    @Column(nullable = false)
    private int inventory;
    @Column(nullable = false)
    private BigDecimal dailyFee;
    @Column(nullable = false)
    private boolean isDeleted = false;
}
