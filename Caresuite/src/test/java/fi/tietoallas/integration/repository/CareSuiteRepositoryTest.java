package fi.tietoallas.integration.repository;


import fi.tietoallas.integration.domain.TableIdColumnInformation;
import fi.tietoallas.integration.domain.TableIdValuePair;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CareSuiteRepositoryTest {

    private static final String TEST_TABLE_1="SIMPLE_TABLE_1";
    @Autowired
    private CareSuiteRepository careSuiteRepository;

    @Test
    public void testSelect(){

        TableIdValuePair tableIdPairs = careSuiteRepository.getTablesWithUpdatingIdNotInDatalakeTable(TEST_TABLE_1,"DBOID");
        assertThat(tableIdPairs.idValue,notNullValue());
        assertThat(tableIdPairs.tableName,is(TEST_TABLE_1));
    }
}
