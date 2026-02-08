package com.shifterizator.shifterizatorbackend.language.service;

import com.shifterizator.shifterizatorbackend.language.dto.LanguageRequestDto;
import com.shifterizator.shifterizatorbackend.language.model.Language;

import java.util.List;

public interface LanguageService {

    Language create(LanguageRequestDto dto);

    Language update(Long id, LanguageRequestDto dto);

    void delete(Long id);

    Language findById(Long id);

    List<Language> findAll();
}
