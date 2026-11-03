package io.basat.monitoring.meter;

import io.basat.monitoring.site.Site;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "meters")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Meter {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // the tenant is reached through the site, so a meter can never belong to another company than its site
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "site_id", nullable = false, updatable = false)
    private Site site;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "serial_number", nullable = false, unique = true, length = 100)
    private String serialNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "meter_type", nullable = false, length = 30)
    private MeterType type;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public Meter(Site site, String name, String serialNumber, MeterType type) {
        this.site = site;
        this.name = name;
        this.serialNumber = serialNumber;
        this.type = type;
    }

    public void update(String name, String serialNumber, MeterType type) {
        this.name = name;
        this.serialNumber = serialNumber;
        this.type = type;
    }

}
