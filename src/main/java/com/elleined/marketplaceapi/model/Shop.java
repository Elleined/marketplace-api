package com.elleined.marketplaceapi.model;

import com.elleined.marketplaceapi.model.user.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tbl_shop")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class Shop {

    @Id
    @Column(
            name = "owner_id",
            nullable = false,
            updatable = false
    )
    private int id;

    @Column(name = "name",
            nullable = false,
            unique = true
    )
    private String name;

    @Column(name = "description")
    private String description;

    @Column(
            name = "picture",
            nullable = false
    )
    private String picture;

    // This is the primary key of this shop table
    @MapsId
    @OneToOne(optional = false)
    @JoinColumn(
            name = "owner_id",
            referencedColumnName = "user_id"
    )
    private User owner;
}
