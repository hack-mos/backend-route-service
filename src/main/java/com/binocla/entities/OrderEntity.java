package com.binocla.entities;

import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.proxy.HibernateProxy;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "orders")
@Getter
@Setter
@ToString
public class OrderEntity extends PanacheEntity {
    @Column(name = "from_dock")
    private Integer fromDock;
    @Column(name = "to_dock")
    private Integer toDock;
    @Column(name = "remaining_seats")
    private Integer remainingSeats;
    @Column(name = "predicted_minutes")
    private Double predictedMinutes;
    @Column(name = "from_berth_position")
    private String fromBerthPosition;
    @Column(name = "to_berth_position")
    private String toBerthPosition;
    @Column(name = "taxi_id")
    private Long taxiId;
    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        OrderEntity that = (OrderEntity) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
