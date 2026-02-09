package com.shifterizator.shifterizatorbackend.company.repository;

import com.shifterizator.shifterizatorbackend.company.model.Company;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;


@DataJpaTest
class CompanyRepositoryTest {

    @Autowired
    private CompanyRepository repository;

    private Company sampleCompany;
    private Optional<Company> result;
    private List<Company> resultSet;

    @BeforeEach
    void setup() {

        sampleCompany = new Company("Sample Company"
                , "Sample Company Legal"
                , "12345678T"
                , "sample@sample.com"
                , "+34932231515");

        repository.save(sampleCompany);

    }


    @Test
    void findByName_should_return_entity_when_name_exists() {

        result = repository.findByNameAndDeletedAtIsNull("Sample Company");

        assertTrue(result.isPresent());
        assertEquals("Sample Company", result.get().getName());


    }

    @Test
    void findByName_should_return_empty_when_name_does_not_exists() {

        result = repository.findByNameAndDeletedAtIsNull("Name that does  not exist");

        assertTrue(result.isEmpty());

    }

    @Test
    void findByTaxId_should_return_entity_when_taxid_exists() {

        result = repository.findByTaxIdAndDeletedAtIsNull("12345678T");

        assertTrue(result.isPresent());
        assertEquals("12345678T", result.get().getTaxId());

    }

    @Test
    void findByTaxId_should_return_empty_when_taxid_does_not_exists() {

        result = repository.findByTaxIdAndDeletedAtIsNull("00000000t");

        assertTrue(result.isEmpty());

    }

    @Test
    void findByEmail_should_return_entity_when_mail_exists() {

        result = repository.findByEmailAndDeletedAtIsNull("sample@sample.com");

        assertTrue(result.isPresent());
        assertEquals("sample@sample.com", result.get().getEmail());

    }

    @Test
    void findByEmail_should_return_empty_when_mail_does_not_exists() {

        result = repository.findByEmailAndDeletedAtIsNull("00000000t");

        assertTrue(result.isEmpty());

    }

    @Test
    void findByIsActive_should_return_entities_when_isactive_value_exists() {

        Company sampleCompany2 = new Company("Sample Company2"
                , "Sample Company Legal2"
                , "123456789T"
                , "sample2@sample2.com"
                , "+349322315152");

        repository.save(sampleCompany2);

        resultSet = repository.findByIsActiveAndDeletedAtIsNull(true);

        assertFalse(resultSet.isEmpty());
        assertEquals(2, resultSet.size());
        assertEquals("sample@sample.com", resultSet.get(0).getEmail());
        assertEquals("sample2@sample2.com", resultSet.get(1).getEmail());

    }

    @Test
    void findByIsActive_should_return_emptylist_when_isactive_value_does_not_exists() {

        Company sampleCompany2 = new Company("Sample Company2"
                , "Sample Company Legal2"
                , "123456789T"
                , "sample2@sample2.com"
                , "+349322315152");

        repository.save(sampleCompany2);

        resultSet = repository.findByIsActiveAndDeletedAtIsNull(false);

        assertTrue(resultSet.isEmpty());

    }

    @Test
    void findByNameContainingIgnoreCase_should_return_entities_when_containing_value_exists() {

        Company sampleCompany2 = new Company("Sample Company2"
                , "Sample Company Legal2"
                , "123456789T"
                , "sample2@sample2.com"
                , "+349322315152");

        repository.save(sampleCompany2);

        resultSet = repository.findByNameContainingIgnoreCaseAndDeletedAtIsNull("Mple");

        assertFalse(resultSet.isEmpty());
        assertEquals(2, resultSet.size());
        assertEquals("sample@sample.com", resultSet.get(0).getEmail());
        assertEquals("sample2@sample2.com", resultSet.get(1).getEmail());

    }

    @Test
    void findByNameContainingIgnoreCase_should_return_emptylist_when_containing_value_does_not_exists() {

        Company sampleCompany2 = new Company("Sample Company2"
                , "Sample Company Legal2"
                , "123456789T"
                , "sample2@sample2.com"
                , "+349322315152");

        repository.save(sampleCompany2);

        resultSet = repository.findByNameContainingIgnoreCaseAndDeletedAtIsNull("aMpz");

        assertTrue(resultSet.isEmpty());

    }

    @Test
    void findByNameContainingIgnoreCaseAndIsActive_should_return_entities_when_containing_and_is_active_values_exists() {

        Company sampleCompany2 = new Company("Sample Company2"
                , "Sample Company Legal2"
                , "123456789T"
                , "sample2@sample2.com"
                , "+349322315152");

        repository.save(sampleCompany2);

        resultSet = repository.findByNameContainingIgnoreCaseAndIsActiveAndDeletedAtIsNull("Mple", true);

        assertFalse(resultSet.isEmpty());
        assertEquals(2, resultSet.size());
        assertEquals("sample@sample.com", resultSet.get(0).getEmail());
        assertEquals("sample2@sample2.com", resultSet.get(1).getEmail());

    }

    @Test
    void findByNameContainingIgnoreCaseAndIsActive_should_return_emptylist_containing_or_is_active_values_does_not_exists() {

        Company sampleCompany2 = new Company("Sample Company2"
                , "Sample Company Legal2"
                , "123456789T"
                , "sample2@sample2.com"
                , "+349322315152");

        repository.save(sampleCompany2);

        resultSet = repository.findByNameContainingIgnoreCaseAndIsActiveAndDeletedAtIsNull("aMpz", true);
        assertTrue(resultSet.isEmpty());

        resultSet = repository.findByNameContainingIgnoreCaseAndIsActiveAndDeletedAtIsNull("Mple", false);
        assertTrue(resultSet.isEmpty());

        resultSet = repository.findByNameContainingIgnoreCaseAndIsActiveAndDeletedAtIsNull("aMpz", false);
        assertTrue(resultSet.isEmpty());


    }

    @Test
    void existsByNameIgnoreCaseAndIdNot_returns_true_when_different_id_and_name_exists() {

        boolean exist = repository.existsByNameIgnoreCaseAndIdNotAndDeletedAtIsNull("Sample Company", 985878L);

        assertTrue(exist);

    }

    @Test
    void existsByNameIgnoreCaseAndIdNot_returns_false_when_different_id_and_name_does_not_exist() {

        boolean exist = repository.existsByNameIgnoreCaseAndIdNotAndDeletedAtIsNull("Sample Company2", 985878L);

        assertFalse(exist);

    }


    @Test
    void existsByEmailIgnoreCaseAndIdNot_returns_true_when_different_id_and_email_exists() {

        boolean exist = repository.existsByEmailIgnoreCaseAndIdNotAndDeletedAtIsNull("sample@sample.com", 985878L);

        assertTrue(exist);

    }

    @Test
    void existsByEmailIgnoreCaseAndIdNot_returns_false_when_different_id_and_email_does_not_exist() {

        boolean exist = repository.existsByEmailIgnoreCaseAndIdNotAndDeletedAtIsNull("sample@sample.com2", 985878L);

        assertFalse(exist);

    }


    @Test
    void existsByTaxIdIgnoreCaseAndIdNot_returns_true_when_different_id_and_taxid_exists() {

        boolean exist = repository.existsByTaxIdIgnoreCaseAndIdNotAndDeletedAtIsNull("12345678T", 985878L);

        assertTrue(exist);

    }

    @Test
    void existsByTaxIdIgnoreCaseAndIdNot_returns_false_when_different_id_and_taxid_does_not_exist() {

        boolean exist = repository.existsByTaxIdIgnoreCaseAndIdNotAndDeletedAtIsNull("00000000T", 985878L);

        assertFalse(exist);

    }


}