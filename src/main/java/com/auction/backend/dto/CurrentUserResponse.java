package com.auction.backend.dto;

import com.auction.backend.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class CurrentUserResponse {
    private String id;
    private String email;
    private String fullName;
    private String phoneNumber;
    private Role role;
    private Boolean active;
}