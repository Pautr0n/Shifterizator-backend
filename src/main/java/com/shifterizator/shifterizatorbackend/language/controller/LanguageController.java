package com.shifterizator.shifterizatorbackend.language.controller;

import com.shifterizator.shifterizatorbackend.language.dto.LanguageRequestDto;
import com.shifterizator.shifterizatorbackend.language.dto.LanguageResponseDto;
import com.shifterizator.shifterizatorbackend.language.mapper.LanguageMapper;
import com.shifterizator.shifterizatorbackend.language.model.Language;
import com.shifterizator.shifterizatorbackend.language.service.LanguageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/languages")
@RequiredArgsConstructor
@Tag(
        name = "Languages",
        description = "Endpoints for managing supported languages (e.g. for UI localization). " +
                "Typically restricted to SUPERADMIN."
)
public class LanguageController {

    private final LanguageService languageService;
    private final LanguageMapper languageMapper;

    @Operation(
            summary = "Create language",
            description = "Adds a new supported language (code and display name).",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Language created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PostMapping
    public ResponseEntity<LanguageResponseDto> create(@Valid @RequestBody LanguageRequestDto dto) {
        Language language = languageService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(languageMapper.toDto(language));
    }

    @Operation(
            summary = "Update language",
            description = "Updates an existing language.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Language updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "404", description = "Language not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<LanguageResponseDto> update(
            @Parameter(description = "Language ID", required = true) @PathVariable Long id,
            @Valid @RequestBody LanguageRequestDto dto) {
        Language language = languageService.update(id, dto);
        return ResponseEntity.ok(languageMapper.toDto(language));
    }

    @Operation(
            summary = "Delete language",
            description = "Deletes a language from the system.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Language deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Language not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Language ID", required = true) @PathVariable Long id) {
        languageService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Get language by ID",
            description = "Retrieves a language by its ID.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Language retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Language not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<LanguageResponseDto> findById(
            @Parameter(description = "Language ID", required = true) @PathVariable Long id) {
        Language language = languageService.findById(id);
        return ResponseEntity.ok(languageMapper.toDto(language));
    }

    @Operation(
            summary = "List all languages",
            description = "Returns all supported languages.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of languages")
    })
    @GetMapping
    public ResponseEntity<List<LanguageResponseDto>> findAll() {
        List<Language> languages = languageService.findAll();
        return ResponseEntity.ok(languages.stream().map(languageMapper::toDto).toList());
    }
}
