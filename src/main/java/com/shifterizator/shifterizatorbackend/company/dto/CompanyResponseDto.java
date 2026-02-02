package com.shifterizator.shifterizatorbackend.company.dto;

import java.time.LocalDateTime;

public record CompanyResponseDto(Long id,
                                 String name,
                                 String legalName,
                                 String taxId,
                                 String email,
                                 String phone,
                                 boolean isActive,
                                 LocalDateTime createdAt,
                                 LocalDateTime updatedAt) {
}
