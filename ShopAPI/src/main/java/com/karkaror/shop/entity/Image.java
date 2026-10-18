package com.karkaror.shop.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Basic(fetch = FetchType.LAZY)  // Hibernate игнорирует LAZY для byte[] без bytecode enhancement
    @Column(name = "image", nullable = false, columnDefinition = "bytea")
    private byte[] image;
}
