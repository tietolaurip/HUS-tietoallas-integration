package fi.tietoallas.integration.staginstreamconsumer.utils;

import fi.tietoallas.integration.staginstreamconsumer.PseudoInformation;
import fi.tietoallas.integration.staginstreamconsumer.StaginStreamConsumer;
import fi.tietoallas.integration.staginstreamconsumer.util.MetaInformation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

@RunWith(JUnit4.class)
public class StagingStreamConsumerTest {

    private static final String PSEUDO_FIELD="this_field_is_hash";
    private static final String TEST_INTEGRATION = "TEST_INTEGRATION";
    private static final String TEST_TABLE = "TEST_TABLE";
    private static final String PASS_FIELD = "this_field_shuld_pass";
    private static final String NULL_FIELD="this_field_should_be_nulled";

    @Test
    public void testPseudoValue(){
        List<PseudoInformation> pseudoInformations = new ArrayList<>();
        pseudoInformations.add(new PseudoInformation(PSEUDO_FIELD,"HASH", TEST_INTEGRATION, TEST_TABLE));
        pseudoInformations.add(new PseudoInformation(PASS_FIELD,"PASS",TEST_INTEGRATION,TEST_TABLE));
        pseudoInformations.add(new PseudoInformation(NULL_FIELD,"NULL",TEST_INTEGRATION,TEST_TABLE));
        MetaInformation metaInformation = new MetaInformation(TEST_INTEGRATION, TEST_TABLE,"EMPTY_SCHEMA");
      //  List<String> pseudo = StaginStreamConsumer.getPseudo(pseudoInformations, metaInformation);
      //  assertThat(pseudo,hasSize(1));
       // assertThat(pseudo.get(0),is(PSEUDO_FIELD));


    }

}
