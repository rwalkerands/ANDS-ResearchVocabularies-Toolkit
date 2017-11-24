/** See the file "LICENSE" for the full license governing this code. */

package au.org.ands.vocabs.toolkit.test.arquillian;

import static au.org.ands.vocabs.toolkit.test.utils.DatabaseSelector.REGISTRY;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.dbunit.DatabaseUnitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import au.org.ands.vocabs.registry.api.validation.CheckVocabulary;
import au.org.ands.vocabs.registry.schema.vocabulary201701.Vocabulary;
import au.org.ands.vocabs.toolkit.test.utils.RegistrySchemaValidationHelper;

/** Tests of the validation of vocabulary data provided to API methods. */
@Test
public class RegistryCheckVocabularyTests extends ArquillianBaseTest {

    /** Logger for this class. */
    private Logger logger = LoggerFactory.getLogger(
            MethodHandles.lookup().lookupClass());

    /** Test of the CheckVocabulary validator.
     * @throws DatabaseUnitException If a problem with DbUnit.
     * @throws IOException If a problem getting test data for DbUnit,
     *          or reading JSON from the correct and test output files.
     * @throws SQLException If DbUnit has a problem performing
     *           performing JDBC operations.
     * @throws JAXBException If there is an error configuring or
     *      reading XML data.
     */
    @Test
    public final void testCheckVocabulary1() throws
    DatabaseUnitException, IOException, SQLException, JAXBException {
        ArquillianTestUtils.clearDatabase(REGISTRY);
        Vocabulary newVocabulary;

        InputStream is = ArquillianTestUtils.getResourceAsInputStream(
                "test/tests/au.org.ands.vocabs.toolkit."
                + "test.arquillian.AllArquillianTests."
                + "testCheckVocabulary1/"
                + "test-validation-1.xml");
        JAXBContext jaxbContext = JAXBContext.newInstance(Vocabulary.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        newVocabulary = (Vocabulary) jaxbUnmarshaller.unmarshal(is);

        Set<ConstraintViolation<RegistrySchemaValidationHelper>>
        errors = RegistrySchemaValidationHelper.getNewVocabularyValidation(
                newVocabulary);

        List<ValidationSummary> actualErrors = new ArrayList<>();
        for (ConstraintViolation<RegistrySchemaValidationHelper> oneError
                : errors) {
            actualErrors.add(new ValidationSummary(
                    oneError.getMessageTemplate(),
                    oneError.getPropertyPath().toString()));
        }

        List<ValidationSummary> expectedErrors = new ArrayList<>();
        String pathPrefix = "testNewVocabulary.arg0.";
        expectedErrors.add(new ValidationSummary(
                "{" + CheckVocabulary.INTERFACE_NAME
                + ".relatedEntityRef.noPublisher}",
                pathPrefix + "relatedEntityRef"));
        expectedErrors.add(new ValidationSummary(
                "{" + CheckVocabulary.INTERFACE_NAME
                + ".relatedEntityRef.unknown}",
                pathPrefix + "relatedEntityRef[0]"));
        expectedErrors.add(new ValidationSummary(
                "{" + CheckVocabulary.INTERFACE_NAME
                + ".version.creationDate}; "
                + "value must be either 4, 7, or 10 characters long",
                pathPrefix + "version[0].releaseDate"));
        expectedErrors.add(new ValidationSummary(
                "{" + CheckVocabulary.INTERFACE_NAME
                + ".poolpartyProject.serverId}",
                pathPrefix + "poolpartyProject.serverId"));
        expectedErrors.add(new ValidationSummary(
                "{" + CheckVocabulary.INTERFACE_NAME
                + ".subject.source}",
                pathPrefix + "subject[0].source"));
        expectedErrors.add(new ValidationSummary(
                "{" + CheckVocabulary.INTERFACE_NAME
                + ".version.id}",
                pathPrefix + "version[0].id"));
        expectedErrors.add(new ValidationSummary(
                "{" + CheckVocabulary.INTERFACE_NAME
                + ".subject.noAnzsrcFor}",
                pathPrefix + "subject"));
//        expectedErrors.add(new ValidationSummary(
//                "{" + CheckVocabulary.INTERFACE_NAME
//                + "}",
//                pathPrefix + ""));

        Assert.assertEqualsNoOrder(actualErrors.toArray(),
                expectedErrors.toArray(),
                "Set of validation errors does not match");
    }

    /** Static nested class to aid comparison between actual and expected
     * sets of constraint violations. An instance of this class represents
     * one violation simplified to its essentials. */
    private static class ValidationSummary {

        /** The message template of the violation. */
        private String messageTemplate;

        /** The property path of the violation, as a String. */
        private String propertyPath;

        /** Constructor.
         * @param aMessageTemplate The message template of the violation.
         * @param aPropertyPath The property path of the violation, as a String.
         */
        ValidationSummary(final String aMessageTemplate,
                final String aPropertyPath) {
            messageTemplate = aMessageTemplate;
            propertyPath = aPropertyPath;
        }

        /** {@inheritDoc} */
        @Override
        public String toString() {
            return "messageTemplate: " + messageTemplate
                    + "; propertyPath:" + propertyPath;
        }

        /** {@inheritDoc}
         * Equality test based on messageTemplate and propertyPath.
         */
        @Override
        public boolean equals(final Object other) {
            if (other == null
                    || !(other instanceof ValidationSummary)) {
                return false;
            }
            ValidationSummary vsOther =
                    (ValidationSummary) other;
            return messageTemplate.equals(vsOther.messageTemplate)
                    && propertyPath.equals(vsOther.propertyPath);
        }

        /** {@inheritDoc}
         * The hash code returned is that of the messageTemplate.
         */
        @Override
        public int hashCode() {
            return messageTemplate.hashCode();
        }

    }

}