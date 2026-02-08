package com.shifterizator.shifterizatorbackend.language.service;

import com.shifterizator.shifterizatorbackend.language.dto.LanguageRequestDto;
import com.shifterizator.shifterizatorbackend.language.exception.LanguageNotFoundException;
import com.shifterizator.shifterizatorbackend.language.mapper.LanguageMapper;
import com.shifterizator.shifterizatorbackend.language.model.Language;
import com.shifterizator.shifterizatorbackend.language.repository.LanguageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LanguageServiceImplTest {

    @Mock
    private LanguageRepository languageRepository;

    @Mock
    private LanguageMapper languageMapper;

    @InjectMocks
    private LanguageServiceImpl languageService;

    @Test
    void create_shouldCreateLanguageSuccessfully() {
        LanguageRequestDto dto = new LanguageRequestDto("EN", "English");
        Language entity = Language.builder().code("EN").name("English").build();
        Language saved = Language.builder().id(1L).code("EN").name("English")
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();

        when(languageMapper.toEntity(dto)).thenReturn(entity);
        when(languageRepository.save(any(Language.class))).thenReturn(saved);

        Language result = languageService.create(dto);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCode()).isEqualTo("EN");
        assertThat(result.getName()).isEqualTo("English");
        verify(languageMapper).toEntity(dto);
        verify(languageRepository).save(any(Language.class));
    }

    @Test
    void update_shouldUpdateLanguageSuccessfully() {
        LanguageRequestDto dto = new LanguageRequestDto("ES", "Spanish");
        Language existing = Language.builder().id(1L).code("EN").name("English").build();

        when(languageRepository.findById(1L)).thenReturn(Optional.of(existing));

        Language result = languageService.update(1L, dto);

        assertThat(result.getCode()).isEqualTo("ES");
        assertThat(result.getName()).isEqualTo("Spanish");
        assertThat(existing.getCode()).isEqualTo("ES");
        assertThat(existing.getName()).isEqualTo("Spanish");
        verify(languageRepository).findById(1L);
    }

    @Test
    void update_shouldThrowWhenLanguageNotFound() {
        LanguageRequestDto dto = new LanguageRequestDto("ES", "Spanish");

        when(languageRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> languageService.update(99L, dto))
                .isInstanceOf(LanguageNotFoundException.class)
                .hasMessage("Language not found");
        verify(languageRepository).findById(99L);
    }

    @Test
    void delete_shouldDeleteLanguage() {
        Language language = Language.builder().id(1L).code("EN").name("English").build();

        when(languageRepository.findById(1L)).thenReturn(Optional.of(language));

        languageService.delete(1L);

        verify(languageRepository).findById(1L);
        verify(languageRepository).delete(language);
    }

    @Test
    void delete_shouldThrowWhenLanguageNotFound() {
        when(languageRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> languageService.delete(99L))
                .isInstanceOf(LanguageNotFoundException.class)
                .hasMessage("Language not found");
        verify(languageRepository).findById(99L);
        verify(languageRepository, never()).delete(any(Language.class));
    }

    @Test
    void findById_shouldReturnLanguage() {
        Language language = Language.builder().id(1L).code("EN").name("English").build();

        when(languageRepository.findById(1L)).thenReturn(Optional.of(language));

        Language result = languageService.findById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCode()).isEqualTo("EN");
        assertThat(result.getName()).isEqualTo("English");
        verify(languageRepository).findById(1L);
    }

    @Test
    void findById_shouldThrowWhenNotFound() {
        when(languageRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> languageService.findById(99L))
                .isInstanceOf(LanguageNotFoundException.class)
                .hasMessage("Language not found");
        verify(languageRepository).findById(99L);
    }

    @Test
    void findAll_shouldReturnAllLanguages() {
        Language lang1 = Language.builder().id(1L).code("EN").name("English").build();
        Language lang2 = Language.builder().id(2L).code("ES").name("Spanish").build();

        when(languageRepository.findAll()).thenReturn(List.of(lang1, lang2));

        List<Language> result = languageService.findAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getCode()).isEqualTo("EN");
        assertThat(result.get(1).getCode()).isEqualTo("ES");
        verify(languageRepository).findAll();
    }

    @Test
    void findAll_shouldReturnEmptyListWhenNoLanguages() {
        when(languageRepository.findAll()).thenReturn(List.of());

        List<Language> result = languageService.findAll();

        assertThat(result).isEmpty();
        verify(languageRepository).findAll();
    }
}
