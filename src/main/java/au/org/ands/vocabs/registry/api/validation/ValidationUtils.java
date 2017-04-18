/** See the file "LICENSE" for the full license governing this code. */

package au.org.ands.vocabs.registry.api.validation;

import java.lang.invoke.MethodHandles;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintValidatorContext.ConstraintViolationBuilder;

import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.ands.vocabs.registry.enums.RelatedEntityRelation;
import au.org.ands.vocabs.registry.enums.RelatedEntityType;
import au.org.ands.vocabs.registry.utils.SlugGenerator;

/** Utility methods to support validation. */
public final class ValidationUtils {

    /** Logger for this class. */
    private static Logger logger = LoggerFactory.getLogger(
            MethodHandles.lookup().lookupClass());

    /** Private constructor for a utility class. */
    private ValidationUtils() {
    }

    /** The length of a String that is a date in "YYYY" format. */
    private static final int YYYY_LENGTH = 4;

    /** The length of a String that is a date in "YYYY-MM" format. */
    private static final int YYYY_MM_LENGTH = 7;

    /** The length of a String that is a date in "YYYY-MM-DD" format. */
    private static final int YYYY_MM_DD_LENGTH = 10;

    /** DateTimeFormatter that represents a value containing a year,
     * month, and day. */
    private static final DateTimeFormatter YYYY_MM_DD =
            DateTimeFormatter.ISO_LOCAL_DATE;
    /* Reuse ISO_LOCAL_DATE, which is already set to STRICT resolution. */

    /** Check that a field of a bean is not null. If it is in fact
     * null, register a constraint violation.
     * @param constraintInterfaceName The name of the interface
     *      for the constraint.
     * @param objectToTest The object that is required to be not null.
     * @param fieldName The name of the field that is being tested.
     * @param constraintContext The constraint context. If there is a
     *      violation, it is recorded here.
     * @param valid The state of validity, up to this point.
     * @return The updated validity state.
    */
    public static boolean requireFieldNotNull(
            final String constraintInterfaceName,
            final Object objectToTest,
            final String fieldName,
            final ConstraintValidatorContext constraintContext,
            final boolean valid) {
        return requireFieldNotNull(constraintInterfaceName,
                 objectToTest, fieldName, constraintContext, null,
                 valid);
    }

    /** Check that a field of a bean is not null. If it is in fact
     * null, register a constraint violation.
     * @param constraintInterfaceName The name of the interface
     *      for the constraint.
     * @param objectToTest The object that is required to be not null.
     * @param fieldName The name of the field that is being tested.
     * @param constraintContext The constraint context. If there is a
     *      violation, it is recorded here.
     * @param nodeModifier If null, the generated ConstraintViolationBuilder
     *      has {@code addPropertyNode(fieldName).addConstraintViolation()}
     *      invoked.
     *      If not null, this is taken to be a consumer of the
     *      ConstraintViolationBuilder
     *      to qualify the location of the error, and the caller is then
     *      responsible for specying a consumer that adds all location
     *      information, including,
     *      e.g., invoking {@code addPropertyNode()},
     *      and for invoking {@code addConstraintViolation()} at the end.
     * @param valid The state of validity, up to this point.
     * @return The updated validity state.
    */
    public static boolean requireFieldNotNull(
            final String constraintInterfaceName,
            final Object objectToTest,
            final String fieldName,
            final ConstraintValidatorContext constraintContext,
            final Consumer<ConstraintViolationBuilder> nodeModifier,
            final boolean valid) {
        boolean validToReturn = valid;
        if (objectToTest == null) {
            validToReturn = false;
            ConstraintViolationBuilder cvb = constraintContext.
                    buildConstraintViolationWithTemplate(
                    "{" + constraintInterfaceName + "." + fieldName + "}");
            if (nodeModifier != null) {
                nodeModifier.accept(cvb);
            } else {
                cvb.addPropertyNode(fieldName).addConstraintViolation();
            }
        }
        return validToReturn;
    }

    /** Check that a field of a bean is not an empty string.
     * If it is in fact an empty string, register a constraint violation.
     * @param constraintInterfaceName The name of the interface
     *      for the constraint.
     * @param stringToTest The String that is required to be not null.
     * @param fieldName The name of the field that is being tested.
     * @param constraintContext The constraint context. If there is a
     *      violation, it is recorded here.
     * @param valid The state of validity, up to this point.
     * @return The updated validity state.
    */
    public static boolean requireFieldNotEmptyString(
            final String constraintInterfaceName,
            final String stringToTest,
            final String fieldName,
            final ConstraintValidatorContext constraintContext,
            final boolean valid) {
        return requireFieldNotEmptyString(constraintInterfaceName,
                stringToTest, fieldName, constraintContext, null,
                valid);
    }

    /** Check that a field of a bean is not an empty string.
     * If it is in fact an empty string, register a constraint violation.
     * @param constraintInterfaceName The name of the interface
     *      for the constraint.
     * @param stringToTest The String that is required to be not null.
     * @param fieldName The name of the field that is being tested.
     * @param constraintContext The constraint context. If there is a
     *      violation, it is recorded here.
     * @param nodeModifier If null, the generated ConstraintViolationBuilder
     *      has {@code addPropertyNode(fieldName).addConstraintViolation()}
     *      invoked.
     *      If not null, this is taken to be a consumer of the
     *      ConstraintViolationBuilder
     *      to qualify the location of the error, and the caller is then
     *      responsible for specying a consumer that adds all location
     *      information, including,
     *      e.g., invoking {@code addPropertyNode()},
     *      and for invoking {@code addConstraintViolation()} at the end.
     * @param valid The state of validity, up to this point.
     * @return The updated validity state.
    */
    public static boolean requireFieldNotEmptyString(
            final String constraintInterfaceName,
            final String stringToTest,
            final String fieldName,
            final ConstraintValidatorContext constraintContext,
            final Consumer<ConstraintViolationBuilder> nodeModifier,
            final boolean valid) {
        boolean validToReturn = valid;
        if (stringToTest == null || stringToTest.isEmpty()) {
            validToReturn = false;
            ConstraintViolationBuilder cvb = constraintContext.
                    buildConstraintViolationWithTemplate(
                    "{" + constraintInterfaceName + "." + fieldName + "}");
            if (nodeModifier != null) {
                nodeModifier.accept(cvb);
            } else {
                cvb.addPropertyNode(fieldName).addConstraintViolation();
            }
        }
        return validToReturn;
    }

    /** Check that a field of a bean is not an empty string, and
     * also satisfies a property specified as a predicate.
     * If it is in fact an empty string, or does not satisfy the
     * property, register a constraint violation.
     * @param constraintInterfaceName The name of the interface
     *      for the constraint.
     * @param stringToTest The String that is required to be not null.
     * @param fieldName The name of the field that is being tested.
     * @param predicate The Predicate to be tested for the String.
     * @param constraintContext The constraint context. If there is a
     *      violation, it is recorded here.
     * @param valid The state of validity, up to this point.
     * @return The updated validity state.
    */
    public static boolean requireFieldNotEmptyStringAndSatisfiesPredicate(
            final String constraintInterfaceName,
            final String stringToTest,
            final String fieldName,
            final Predicate<String> predicate,
            final ConstraintValidatorContext constraintContext,
            final boolean valid) {
        return requireFieldNotEmptyStringAndSatisfiesPredicate(
                constraintInterfaceName, stringToTest, fieldName,
                predicate, constraintContext, null, valid);
    }

    /** Check that a field of a bean is not an empty string, and
     * also satisfies a property specified as a predicate..
     * If it is in fact an empty string, register a constraint violation.
     * @param constraintInterfaceName The name of the interface
     *      for the constraint.
     * @param stringToTest The String that is required to be not null.
     * @param fieldName The name of the field that is being tested.
     * @param predicate The Predicate to be tested for the String.
     * @param constraintContext The constraint context. If there is a
     *      violation, it is recorded here.
     * @param nodeModifier If null, the generated ConstraintViolationBuilder
     *      has {@code addPropertyNode(fieldName).addConstraintViolation()}
     *      invoked.
     *      If not null, this is taken to be a consumer of the
     *      ConstraintViolationBuilder
     *      to qualify the location of the error, and the caller is then
     *      responsible for specying a consumer that adds all location
     *      information, including,
     *      e.g., invoking {@code addPropertyNode()},
     *      and for invoking {@code addConstraintViolation()} at the end.
     * @param valid The state of validity, up to this point.
     * @return The updated validity state.
    */
    public static boolean requireFieldNotEmptyStringAndSatisfiesPredicate(
            final String constraintInterfaceName,
            final String stringToTest,
            final String fieldName,
            final Predicate<String> predicate,
            final ConstraintValidatorContext constraintContext,
            final Consumer<ConstraintViolationBuilder> nodeModifier,
            final boolean valid) {
        boolean validToReturn = valid;
        if (stringToTest == null || stringToTest.isEmpty()
                || !predicate.test(stringToTest)) {
            validToReturn = false;
            ConstraintViolationBuilder cvb = constraintContext.
                    buildConstraintViolationWithTemplate(
                    "{" + constraintInterfaceName + "." + fieldName + "}");
            if (nodeModifier != null) {
                nodeModifier.accept(cvb);
            } else {
                cvb.addPropertyNode(fieldName).addConstraintViolation();
            }
        }
        return validToReturn;
    }

    /** Check that a field of a bean is a valid date, according to
     * the supported formats: "YYYY", "YYYY-MM", and "YYYY-MM-DD".
     * If it is not valid, register a constraint violation.
     * @param constraintInterfaceName The name of the interface
     *      for the constraint.
     * @param dateToTest The String that is required to be a valid date.
     * @param fieldName The name of the field that is being tested.
     * @param mayBeEmpty If the field is allowed to be missing/empty.
     *      If this is true, validation passes if the field is null
     *      or an empty string. If this is false, a date value
     *      must be provided.
     * @param constraintContext The constraint context. If there is a
     *      violation, it is recorded here.
     * @param valid The state of validity, up to this point.
     * @return The updated validity state.
    */
    public static boolean requireFieldValidDate(
            final String constraintInterfaceName,
            final String dateToTest,
            final String fieldName,
            final boolean mayBeEmpty,
            final ConstraintValidatorContext constraintContext,
            final boolean valid) {
        return requireFieldValidDate(constraintInterfaceName,
                dateToTest, fieldName, mayBeEmpty, constraintContext, null,
                valid);
    }

    /** Check that a field of a bean is a valid date, according to
     * the supported formats: "YYYY", "YYYY-MM", and "YYYY-MM-DD".
     * If it is not valid, register a constraint violation.
     * @param constraintInterfaceName The name of the interface
     *      for the constraint.
     * @param dateToTest The String that is required to be a valid date.
     * @param fieldName The name of the field that is being tested.
     * @param mayBeEmpty If the field is allowed to be missing/empty.
     *      If this is true, validation passes if the field is null
     *      or an empty string. If this is false, a date value
     *      must be provided.
     * @param constraintContext The constraint context. If there is a
     *      violation, it is recorded here.
     * @param nodeModifier If null, the generated ConstraintViolationBuilder
     *      has {@code addPropertyNode(fieldName).addConstraintViolation()}
     *      invoked.
     *      If not null, this is taken to be a consumer of the
     *      ConstraintViolationBuilder
     *      to qualify the location of the error, and the caller is then
     *      responsible for specying a consumer that adds all location
     *      information, including,
     *      e.g., invoking {@code addPropertyNode()},
     *      and for invoking {@code addConstraintViolation()} at the end.
     * @param valid The state of validity, up to this point.
     * @return The updated validity state.
    */
    public static boolean requireFieldValidDate(
            final String constraintInterfaceName,
            final String dateToTest,
            final String fieldName,
            final boolean mayBeEmpty,
            final ConstraintValidatorContext constraintContext,
            final Consumer<ConstraintViolationBuilder> nodeModifier,
            final boolean valid) {
        /* validToReturn: what will be the return value of this method. */
        boolean validToReturn = valid;
        /* validDate: whether dateToTest is valid. */
        boolean validDate = true;
        /* Any extra help we get. For the moment, that means an error
         * message that resulting from parsing. */
        String extraErrorInfo = "";
        // First, basic tests of the format: a value must be provided,
        // and must match a regular expression.
        if (dateToTest == null || dateToTest.isEmpty()) {
            validDate = mayBeEmpty;
            // And that's all the checking we do in this case.
        } else {
            // A value was supplied. First, decide what format
            // has been provided; we do that by checking the value
            // against the three permitted lengths.
            String dateAsYYYYMMDD = dateToTest;
            if (dateAsYYYYMMDD.length() == YYYY_LENGTH) {
                dateAsYYYYMMDD += "-01";
                extraErrorInfo += "; (NB: no month was specified, so '-01' "
                        + "was temporarily inserted during validation)";
            }
            if (dateAsYYYYMMDD.length() == YYYY_MM_LENGTH) {
                dateAsYYYYMMDD += "-01";
                extraErrorInfo += "; (NB: no day was specified, so '-01' "
                        + "was temporarily inserted during validation)";
            }
            if (dateAsYYYYMMDD.length() != YYYY_MM_DD_LENGTH) {
                validDate = false;
                extraErrorInfo += "; value must be either "
                        + YYYY_LENGTH + ", "
                        + YYYY_MM_LENGTH + ", or "
                        + YYYY_MM_DD_LENGTH
                        + " characters long";
            }
            if (validDate) {
                try {
                    YYYY_MM_DD.parse(dateAsYYYYMMDD);
                } catch (DateTimeParseException dte) {
                    validDate = false;
                    extraErrorInfo += "; " + dte.getMessage();
                }
            }
        }
        if (!validDate) {
            validToReturn = false;
            ConstraintViolationBuilder cvb = constraintContext.
                    buildConstraintViolationWithTemplate(
                    "{" + constraintInterfaceName + "." + fieldName + "}"
                    + extraErrorInfo);
            if (nodeModifier != null) {
                nodeModifier.accept(cvb);
            } else {
                cvb.addPropertyNode(fieldName).addConstraintViolation();
            }
        }
        return validToReturn;
    }

    /** Determine if a user-specified slug is valid, i.e., has the
     * correct format. That means, it has only the allowed characters,
     * and is not too long.
     * @param slug The slug value to be tested.
     * @return true, if the slug value is valid.
     */
    public static boolean isValidSlug(final String slug) {
        if (slug == null || slug.isEmpty()) {
            return false;
        }
        // Slug generation is idempotent. So, the proposed slug
        // is valid iff it comes out of the slug generator unchanged.
        return slug.equals(SlugGenerator.generateSlug(slug));
    }

    /** Whitelist for jsoup to use to validate HTML. Initialized
     * in a static block to {@link Whitelist#basic()},
     * and customized further with regard to "a" tags. */
    private static Whitelist validWhitelist;

    static {
        validWhitelist = Whitelist.basic();
        validWhitelist.addEnforcedAttribute("a", "target", "_blank");
    }

    /** Utility method to determine if a String value contains
     * an HTML body fragment that parses correctly and contains
     * only the permitted tags/attributes.
     * @param stringToTest The String to be tested.
     * @return true, if stringToTest is valid.
     */
    public static boolean isValidHTML(final String stringToTest) {
        return Jsoup.isValid(stringToTest, validWhitelist);
    }

    /** Utility method to clean a a String value so that it
     * contains the required attributes.
     * @param stringToClean The String to be cleaned.
     * @return The cleaned string.
     */
    public static String cleanHTML(final String stringToClean) {
        return Jsoup.clean(stringToClean, validWhitelist);
    }

    /** Check that a field of a bean contains only acceptable HTML.
     * Here, "acceptable" means according to jsoup's "basic"
     * whitelist. A field value of null is also considered to
     * be "acceptable", so if the field is required, you must
     * test for this separately.
     * @param constraintInterfaceName The name of the interface
     *      for the constraint.
     * @param stringToTest The String that is required to have valid
     *      HTML. It must already have been checked to be a non-empty
     *      string. If null, or an empty string is passed in, return
     *      immediately with the value of valid.
     * @param fieldName The name of the field that is being tested.
     * @param constraintContext The constraint context. If there is a
     *      violation, it is recorded here.
     * @param valid The state of validity, up to this point.
     * @return The updated validity state.
    */
    public static boolean requireFieldValidHTML(
            final String constraintInterfaceName,
            final String stringToTest,
            final String fieldName,
            final ConstraintValidatorContext constraintContext,
            final boolean valid) {
        return requireFieldValidHTML(constraintInterfaceName,
                stringToTest, fieldName, constraintContext, null,
                valid);
    }

    /** Check that a field of a bean contains only acceptable HTML.
     * Here, "acceptable" means according to jsoup's "basic"
     * whitelist. A field value of null is also considered to
     * be "acceptable", so if the field is required, you must
     * test for this separately.
     * @param constraintInterfaceName The name of the interface
     *      for the constraint.
     * @param stringToTest The String that is required to have valid
     *      HTML. It must already have been checked to be a non-empty
     *      string. If null, or an empty string is passed in, return
     *      immediately with the value of valid.
     * @param fieldName The name of the field that is being tested.
     * @param constraintContext The constraint context. If there is a
     *      violation, it is recorded here.
     * @param nodeModifier If null, the generated ConstraintViolationBuilder
     *      has {@code addPropertyNode(fieldName).addConstraintViolation()}
     *      invoked.
     *      If not null, this is taken to be a consumer of the
     *      ConstraintViolationBuilder
     *      to qualify the location of the error, and the caller is then
     *      responsible for specying a consumer that adds all location
     *      information, including,
     *      e.g., invoking {@code addPropertyNode()},
     *      and for invoking {@code addConstraintViolation()} at the end.
     * @param valid The state of validity, up to this point.
     * @return The updated validity state.
    */
    public static boolean requireFieldValidHTML(
            final String constraintInterfaceName,
            final String stringToTest,
            final String fieldName,
            final ConstraintValidatorContext constraintContext,
            final Consumer<ConstraintViolationBuilder> nodeModifier,
            final boolean valid) {
        if (stringToTest == null || stringToTest.isEmpty()) {
            return valid;
        }
        boolean validToReturn = valid;

        if (!isValidHTML(stringToTest)) {
            validToReturn = false;
            ConstraintViolationBuilder cvb = constraintContext.
                    buildConstraintViolationWithTemplate(
                    "{" + constraintInterfaceName + "." + fieldName
                    + ".html}");
            if (nodeModifier != null) {
                nodeModifier.accept(cvb);
            } else {
                cvb.addPropertyNode(fieldName).addConstraintViolation();
            }
        }
        return validToReturn;
    }

    /** The set of allowed relations for related entities that are parties.
     * Initialized in a static block. */
    private static final HashSet<RelatedEntityRelation>
        ALLOWED_RELATIONS_FOR_PARTY = new HashSet<>();

    /** The set of allowed relations for related entities that are services.
     * Initialized in a static block. */
    private static final HashSet<RelatedEntityRelation>
        ALLOWED_RELATIONS_FOR_SERVICE = new HashSet<>();

    /** The set of allowed relations for related entities that are
     * vocabularies. Initialized in a static block. */
    private static final HashSet<RelatedEntityRelation>
        ALLOWED_RELATIONS_FOR_VOCABULARY = new HashSet<>();

    static {
        // Business rules as specified in:
        // https://intranet.ands.org.au/display/PROJ/
        //   Vocabularies+for+vocabulary+schema
        ALLOWED_RELATIONS_FOR_PARTY.add(RelatedEntityRelation.CONSUMER_OF);
        ALLOWED_RELATIONS_FOR_PARTY.add(RelatedEntityRelation.HAS_AUTHOR);
        ALLOWED_RELATIONS_FOR_PARTY.add(RelatedEntityRelation.HAS_CONTRIBUTOR);
        ALLOWED_RELATIONS_FOR_PARTY.add(RelatedEntityRelation.IMPLEMENTED_BY);
        ALLOWED_RELATIONS_FOR_PARTY.add(RelatedEntityRelation.POINT_OF_CONTACT);
        ALLOWED_RELATIONS_FOR_PARTY.add(RelatedEntityRelation.PUBLISHED_BY);

        ALLOWED_RELATIONS_FOR_SERVICE.add(
                RelatedEntityRelation.HAS_ASSOCIATION_WITH);
        ALLOWED_RELATIONS_FOR_SERVICE.add(RelatedEntityRelation.IS_USED_BY);
        ALLOWED_RELATIONS_FOR_SERVICE.add(
                RelatedEntityRelation.IS_PRESENTED_BY);

        ALLOWED_RELATIONS_FOR_VOCABULARY.add(RelatedEntityRelation.ENRICHES);
        ALLOWED_RELATIONS_FOR_VOCABULARY.add(
                RelatedEntityRelation.HAS_ASSOCIATION_WITH);
        ALLOWED_RELATIONS_FOR_VOCABULARY.add(
                RelatedEntityRelation.IS_DERIVED_FROM);
        ALLOWED_RELATIONS_FOR_VOCABULARY.add(RelatedEntityRelation.IS_PART_OF);
    }

    /** Decide whether a vocabulary may have a particular relation with
     * a related entity of a certain type, according to the business
     * rules about relations.
     * @param type The type of the related entity.
     * @param relation The relation being tested.
     * @return true, if the vocabulary is allowed to have the relation
     *      to the related entity.
     */
    public static boolean isAllowedRelation(final RelatedEntityType type,
            final RelatedEntityRelation relation) {
        switch (type) {
        case PARTY:
            return ALLOWED_RELATIONS_FOR_PARTY.contains(relation);
        case SERVICE:
            return ALLOWED_RELATIONS_FOR_SERVICE.contains(relation);
        case VOCABULARY:
            return ALLOWED_RELATIONS_FOR_VOCABULARY.contains(relation);
        default:
            // Can't happen.
            logger.error("Unknown RelatedEntityType!");
            return false;
        }
    }

}