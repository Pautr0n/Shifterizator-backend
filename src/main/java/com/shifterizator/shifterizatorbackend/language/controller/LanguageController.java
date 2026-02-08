package com.shifterizator.shifterizatorbackend.language.controller;

import com.shifterizator.shifterizatorbackend.language.dto.LanguageRequestDto;
import com.shifterizator.shifterizatorbackend.language.dto.LanguageResponseDto;
import com.shifterizator.shifterizatorbackend.language.mapper.LanguageMapper;
import com.shifterizator.shifterizatorbackend.language.model.Language;
import com.shifterizator.shifterizatorbackend.language.service.LanguageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/languages")
@RequiredArgsConstructor
public class LanguageController {

    private final LanguageService languageService;
    private final LanguageMapper languageMapper;

    @PostMapping
    public ResponseEntity<LanguageResponseDto> create(@Valid @RequestBody LanguageRequestDto dto) {
        Language language = languageService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(languageMapper.toDto(language));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LanguageResponseDto> update(@PathVariable Long id, @Valid @RequestBody LanguageRequestDto dto) {
        Language language = languageService.update(id, dto);
        return ResponseEntity.ok(languageMapper.toDto(language));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        languageService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<LanguageResponseDto> findById(@PathVariable Long id) {
        Language language = languageService.findById(id);
        return ResponseEntity.ok(languageMapper.toDto(language));
    }

    @GetMapping
    public ResponseEntity<List<LanguageResponseDto>> findAll() {
        List<Language> languages = languageService.findAll();
        return ResponseEntity.ok(languages.stream().map(languageMapper::toDto).toList());
    }
}
