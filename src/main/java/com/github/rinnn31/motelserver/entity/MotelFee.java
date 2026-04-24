package com.github.rinnn31.motelserver.entity;

import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(
    name = "motel_fee",
    indexes = {
        @Index(name = "idx_motel_id_type", columnList = "motel_id, type")
    }
)
public class MotelFee {
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(name = "unit_price")
    private int unitPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "calculation_type")
    private CalculationType calculationType;

    @Column(length = 20)
    private String name;

    @ManyToOne
    @JoinColumn(name = "motel_id", referencedColumnName = "id", nullable = false)
    private Motel motel;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(int unitPrice) {
        this.unitPrice = unitPrice;
    }

    public CalculationType getCalculationType() {
        return calculationType;
    }

    public void setCalculationType(CalculationType calculationType) {
        this.calculationType = calculationType;
    }

    public Motel getMotel() {
        return motel;
    }

    public void setMotel(Motel motel) {
        this.motel = motel;
    }
}
