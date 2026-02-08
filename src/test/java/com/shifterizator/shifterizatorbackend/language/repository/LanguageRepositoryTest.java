package com.shifterizator.shifterizatorbackend.language.repository;

import com.shifterizator.shifterizatorbackend.language.model.Language;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class LanguageRepositoryTest {

    @Autowired
    private LanguageRepository repository;

    private Language english;
    private Language spanish;

    @BeforeEach
    void setUp() {
        english = Language.builder().code("EN").name("English").build();
        spanish = Language.builder().code("ES").name("Spanish").build();
        repository.saveAll(List.of(english, spanish));
    }

    @Test
    void findById_shouldReturnLanguageWhenExists() {
        Optional<Language> result = repository.findById(english.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getCode()).isEqualTo("EN");
        assertThat(result.get().getName()).isEqualTo("English");
    }

    @Test
    void findById_shouldReturnEmptyWhenNotExists() {
        Optional<Language> result = repository.findById(9999L);

        assertThat(result).isEmpty();
    }

    @Test
    void findByCode_shouldReturnLanguageWhenExists() {
        Optional<Language> result = repository.findByCode("EN");

        assertThat(result).isPresent();
        assertThat(result.get().getCode()).isEqualTo("EN");
        assertThat(result.get().getName()).isEqualTo("English");
    }

    @Test
    void findByCode_shouldReturnEmptyWhenNotExists() {
        Optional<Language> result = repository.findByCode("XX");

        assertThat(result).isEmpty();
    }

    @Test
    void findAll_shouldReturnAllLanguages() {
        List<Language> result = repository.findAll();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Language::getCode).containsExactlyInAnyOrder("EN", "ES");
    }

    @Test
    void existsByCodeIgnoreCase_shouldReturnTrueWhenCodeExists() {
        boolean result = repository.existsByCodeIgnoreCase("en");

        assertThat(result).isTrue();
    }

    @Test
    void existsByCodeIgnoreCase_shouldReturnFalseWhenCodeNotExists() {
        boolean result = repository.existsByCodeIgnoreCase("XX");

        assertThat(result).isFalse();
    }

    @Test
    void existsByCodeIgnoreCaseAndIdNot_shouldReturnTrueWhenOtherEntityHasCode() {
        boolean result = repository.existsByCodeIgnoreCaseAndIdNot("ES", english.getId());

        assertThat(result).isTrue();
    }

    @Test
    void existsByCodeIgnoreCaseAndIdNot_shouldReturnFalseWhenSameEntity() {
        boolean result = repository.existsByCodeIgnoreCaseAndIdNot("EN", english.getId());

        assertThat(result).isFalse();
    }

    @Test
    void existsByCodeIgnoreCaseAndIdNot_shouldReturnFalseWhenCodeNotExists() {
        boolean result = repository.existsByCodeIgnoreCaseAndIdNot("XX", english.getId());

        assertThat(result).isFalse();
    }

    @Test
    void delete_shouldRemoveLanguage() {
        repository.delete(english);

        assertThat(repository.findById(english.getId())).isEmpty();
        assertThat(repository.findAll()).hasSize(1);
    }
}
