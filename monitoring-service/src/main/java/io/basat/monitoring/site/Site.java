package io.basat.monitoring.site;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;


@Entity
@Table(name = "sites")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Site {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // tenants live in auth_db, so this is a plain column taken from the JWT, not a foreign key
    @Column(name = "tenant_id",nullable = false,updatable = false)
    private UUID tenantId;

    @Column(nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "site_type", nullable = false, length = 30)
    private SiteType type;

    @Column(name = "floor_area_m2", precision = 10, scale = 2)
    private BigDecimal floorAreaM2;

    @Column(length = 100)
    private String city;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public Site(UUID tenantId, String name, SiteType type, BigDecimal floorAreaM2, String city) {
        this.tenantId = tenantId;
        this.name = name;
        this.type = type;
        this.floorAreaM2 = floorAreaM2;
        this.city = city;
    }

    public void update(String name, SiteType type, BigDecimal floorAreaM2, String city) {
        this.name = name;
        this.type = type;
        this.floorAreaM2 = floorAreaM2;
        this.city = city;
    }


}
