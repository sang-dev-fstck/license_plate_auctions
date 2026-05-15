package com.auction.backend.entity;

import com.auction.backend.enums.Role;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "accounts")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account extends BaseEntity {
    @Id
    private String id;

    @Indexed(name = "uk_account_email", unique = true)
    private String email;

    @ToString.Exclude // BẮT BUỘC: Giấu password khỏi log hệ thống
    private String password;

    @Indexed(name = "uk_account_phone", unique = true)
    private String phoneNumber;

    private Role role;
    private Boolean active;

    private String fullName;

    @Version
    private Long version;

}

