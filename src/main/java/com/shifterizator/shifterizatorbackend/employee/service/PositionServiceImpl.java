package com.shifterizator.shifterizatorbackend.employee.service;

import com.shifterizator.shifterizatorbackend.company.exception.CompanyNotFoundException;
import com.shifterizator.shifterizatorbackend.company.model.Company;
import com.shifterizator.shifterizatorbackend.company.repository.CompanyRepository;
import com.shifterizator.shifterizatorbackend.employee.dto.PositionDto;
import com.shifterizator.shifterizatorbackend.employee.exception.PositionAlreadyExistsException;
import com.shifterizator.shifterizatorbackend.employee.exception.PositionNotFoundException;
import com.shifterizator.shifterizatorbackend.employee.mapper.PositionMapper;
import com.shifterizator.shifterizatorbackend.employee.model.Position;
import com.shifterizator.shifterizatorbackend.employee.repository.PositionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PositionServiceImpl implements PositionService {

    private final PositionRepository positionRepository;
    private final CompanyRepository companyRepository;
    private final PositionMapper positionMapper;

    @Override
    public PositionDto create(String name, Long companyId) {

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new CompanyNotFoundException("Company not found"));

        ValidatePositionExistsByNameAndCompanyId(name, companyId);

        Position position = Position.builder()
                .name(name)
                .company(company)
                .build();

        positionRepository.save(position);

        return positionMapper.toDto(position);
    }

    @Override
    public PositionDto update(Long id, String name) {

        Position position = positionRepository.findById(id)
                .orElseThrow(() -> new PositionNotFoundException("Position not found"));

        ValidatePositionExistsByNameAndCompanyId(name, position.getCompany().getId());

        position.setName(name);

        return positionMapper.toDto(position);
    }

    @Override
    public void delete(Long id) {
        Position position = positionRepository.findById(id)
                .orElseThrow(() -> new PositionNotFoundException("Position not found"));

        positionRepository.delete(position);
    }

    @Override
    public PositionDto findById(Long id) {
        Position position = positionRepository.findById(id)
                .orElseThrow(() -> new PositionNotFoundException("Position not found"));

        return positionMapper.toDto(position);
    }

    @Override
    public List<PositionDto> findByCompany(Long companyId) {
        return positionRepository.findAll().stream()
                .filter(p -> p.getCompany().getId().equals(companyId))
                .map(positionMapper::toDto)
                .toList();
    }

    private void ValidatePositionExistsByNameAndCompanyId (String name, Long companyId) {
        if (positionRepository.existsByNameAndCompany_Id(name, companyId)) {
            throw new PositionAlreadyExistsException("Position already exists for this company");
        }
    }

}
