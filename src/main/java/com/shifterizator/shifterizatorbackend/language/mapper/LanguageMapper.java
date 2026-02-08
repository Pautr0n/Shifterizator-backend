package com.shifterizator.shifterizatorbackend.language.mapper;

import com.shifterizator.shifterizatorbackend.language.dto.LanguageRequestDto;
import com.shifterizator.shifterizatorbackend.language.dto.LanguageResponseDto;
import com.shifterizator.shifterizatorbackend.language.model.Language;
import org.springframework.stereotype.Component;

@Component
public class LanguageMapper {

    public LanguageResponseDto toDto(Language language) {
        return new LanguageResponseDto(
                language.getId(),
                language.getCode(),
                language.getName(),
                language.getCreatedAt(),
                language.getUpdatedAt()
        );
    }

    public Language toEntity(LanguageRequestDto dto) {
        return Language.builder()
                .code(dto.code())
                .name(dto.name())
                .build();
    }
}
