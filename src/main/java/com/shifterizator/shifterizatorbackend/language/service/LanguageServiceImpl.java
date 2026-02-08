package com.shifterizator.shifterizatorbackend.language.service;

import com.shifterizator.shifterizatorbackend.language.dto.LanguageRequestDto;
import com.shifterizator.shifterizatorbackend.language.exception.LanguageNotFoundException;
import com.shifterizator.shifterizatorbackend.language.mapper.LanguageMapper;
import com.shifterizator.shifterizatorbackend.language.model.Language;
import com.shifterizator.shifterizatorbackend.language.repository.LanguageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class LanguageServiceImpl implements LanguageService {

    private final LanguageRepository languageRepository;
    private final LanguageMapper languageMapper;

    @Override
    public Language create(LanguageRequestDto dto) {
        Language language = languageMapper.toEntity(dto);
        return languageRepository.save(language);
    }

    @Override
    public Language update(Long id, LanguageRequestDto dto) {
        Language language = languageRepository.findById(id)
                .orElseThrow(() -> new LanguageNotFoundException("Language not found"));

        language.setCode(dto.code());
        language.setName(dto.name());
        return language;
    }

    @Override
    public void delete(Long id) {
        Language language = languageRepository.findById(id)
                .orElseThrow(() -> new LanguageNotFoundException("Language not found"));
        languageRepository.delete(language);
    }

    @Override
    @Transactional(readOnly = true)
    public Language findById(Long id) {
        return languageRepository.findById(id)
                .orElseThrow(() -> new LanguageNotFoundException("Language not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Language> findAll() {
        return languageRepository.findAll();
    }
}
