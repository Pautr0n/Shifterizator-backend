package com.shifterizator.shifterizatorbackend.language.mapper;

import com.shifterizator.shifterizatorbackend.language.dto.LanguageRequestDto;
import com.shifterizator.shifterizatorbackend.language.dto.LanguageResponseDto;
import com.shifterizator.shifterizatorbackend.language.model.Language;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class LanguageMapperTest {

    private final LanguageMapper mapper = new LanguageMapper();

    @Test
    void toDto_shouldMapAllFieldsCorrectly() {
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2024, 1, 2, 12, 0);

        Language language = Language.builder()
                .id(1L)
                .code("EN")
                .name("English")
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        LanguageResponseDto dto = mapper.toDto(language);

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.code()).isEqualTo("EN");
        assertThat(dto.name()).isEqualTo("English");
        assertThat(dto.createdAt()).isEqualTo(createdAt);
        assertThat(dto.updatedAt()).isEqualTo(updatedAt);
    }

    @Test
    void toEntity_shouldMapCodeAndName() {
        LanguageRequestDto dto = new LanguageRequestDto("ES", "Spanish");

        Language entity = mapper.toEntity(dto);

        assertThat(entity.getCode()).isEqualTo("ES");
        assertThat(entity.getName()).isEqualTo("Spanish");
        assertThat(entity.getId()).isNull();
        assertThat(entity.getCreatedAt()).isNull();
        assertThat(entity.getUpdatedAt()).isNull();
    }
}
