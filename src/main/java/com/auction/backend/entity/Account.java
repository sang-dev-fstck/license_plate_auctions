package com.auction.backend.entity;

import com.auction.backend.enums.Role;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "accounts")
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account extends BaseEntity {
    @Id
    private String id;

    @Indexed(unique = true)
    @Field("email") // Optional: Explicitly naming the field in MongoDB
    @EqualsAndHashCode.Include
    private String email;

    @ToString.Exclude // BẮT BUỘC: Giấu password khỏi log hệ thống
    private String password;

    @Indexed(unique = true)
    private String phoneNumber;

    private Role role;
    private Boolean isActive;
}
