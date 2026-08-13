package com.vk42.cbp.firstmodule.payments.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnTransformer;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IdempotencyKeyRecord {
    @Id
    private String id;

    @Column(columnDefinition = "jsonb")
    @ColumnTransformer(write = "?::jsonb")
    private String data;

    private Integer httpStatus;
}
