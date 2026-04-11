package com.auction.backend.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountResponse {
    private String id;
    private String email;
    private String phoneNumber;
    private String fullName;
}
