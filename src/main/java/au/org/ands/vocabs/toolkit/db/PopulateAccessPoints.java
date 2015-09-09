/** See the file "LICENSE" for the full license governing this code. */
package au.org.ands.vocabs.toolkit.db;

import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

import javax.json.Json;
import javax.json.JsonObjectBuilder;

import au.org.ands.vocabs.toolkit.db.model.AccessPoints;
import au.org.ands.vocabs.toolkit.db.model.Versions;
import au.org.ands.vocabs.toolkit.restlet.Download;
import au.org.ands.vocabs.toolkit.utils.ToolkitProperties;

import com.fasterxml.jackson.databind.JsonNode;

/** Populate the access_points table based on the content of the versions
 * table. This works on the "original" version of the versions table,
 * in which the "data" attribute contains the access points directly.
 * This is to be used for upgrading a Release 16 database to Release 17. */
public final class PopulateAccessPoints {

    /** Access to the Toolkit properties. */
    protected static final Properties PROPS = ToolkitProperties.getProperties();

    /** URL that is a prefix to all our SPARQL endpoints. */
    private static String sparqlPrefixProperty =
            PROPS.getProperty("SesameImporter.sparqlPrefix") + "/";

    /** URL that is a prefix to Sesame endpoints. */
    private static String sesamePrefixProperty =
            PROPS.getProperty("SesameImporter.serverUrl");

    /** URL that is a prefix to download endpoints. */
    private static String downloadPrefixProperty =
            PROPS.getProperty("Toolkit.downloadPrefix");

    /** Private constructor for a utility class. */
    private PopulateAccessPoints() {
    }

    /**
     * Main program.
     * @param args Command-line arguments
     */
    public static void main(final String[] args) {
        // Create prefixes that both end with a slash, so that
        // they can be substituted for each other.
        String sparqlPrefix = sparqlPrefixProperty;
        if (!sparqlPrefix.endsWith("/")) {
            sparqlPrefix += "/";
        }
        String sesamePrefix = sesamePrefixProperty;
        if (!sesamePrefix.endsWith("/")) {
            sesamePrefix += "/";
        }
        sesamePrefix += "repositories/";
        System.out.println("sparqlPrefix: " + sparqlPrefix);
        System.out.println("sesamePrefix: " + sesamePrefix);
        List<Versions> versions = VersionsUtils.getAllVersions();
        for (Versions version: versions) {
            System.out.println(version.getId());
            System.out.println(version.getTitle());
            String data = version.getData();
            System.out.println(data);
            JsonNode dataJson = TasksUtils.jsonStringToTree(data);
            JsonNode accessPoints = dataJson.get("access_points");
            if (accessPoints != null) {
                System.out.println(accessPoints);
                System.out.println(accessPoints.size());
                for (JsonNode accessPoint: accessPoints) {
                    System.out.println(accessPoint);
                    AccessPoints ap = new AccessPoints();
                    ap.setVersionId(version.getId());
                    String type = accessPoint.get("type").asText();
                    JsonObjectBuilder jobPortal = Json.createObjectBuilder();
                    JsonObjectBuilder jobToolkit = Json.createObjectBuilder();
                    String uri;
                    switch (type) {
                    case AccessPoints.FILE_TYPE:
                       ap.setType(type);
                       // Get the path from the original access point.
                       String filePath = accessPoint.get("uri").asText();
                       // Save the last component of the path to use
                       // in the portal URI.
                       String downloadFilename = Paths.get(filePath).
                               getFileName().toString();
                       if (!filePath.startsWith("/")) {
                           // Relative path that we need to fix up manually.
                           filePath = "FIXME " + filePath;
                       }
                       jobToolkit.add("path", filePath);
                       ap.setPortalData("");
                       ap.setToolkitData(jobToolkit.build().toString());
                       // Persist what we have ...
                       AccessPointsUtils.saveAccessPoint(ap);
                       // ... so that now we can get access to the
                       // ID of the persisted object with ap2.getId().
                       String format;
                       if (downloadFilename.endsWith(".trig")) {
                           // Force TriG. This is needed for some legacy
                           // cases where the filename is ".trig" but
                           // the format has been incorrectly recorded
                           // as RDF/XML.
                           format = "TriG";
                       } else {
                           format = accessPoint.get("format").asText();
                       }
                       jobPortal.add("format", format);
                       jobPortal.add("uri",
                               downloadPrefixProperty + ap.getId()
                               + "/" + downloadFilename);
                       ap.setPortalData(jobPortal.build().toString());
                       AccessPointsUtils.updateAccessPoint(ap);
                       break;
                    case AccessPoints.API_SPARQL_TYPE:
                        ap.setType(type);
                        uri = accessPoint.get("uri").asText();
                        jobPortal.add("uri", uri);
                        if (uri.startsWith(sparqlPrefix)) {
                            // One of ours, so also add a sesameDownload
                            // endpoint.
                            AccessPoints ap2 = new AccessPoints();
                            ap2.setVersionId(version.getId());
                            ap2.setType(AccessPoints.SESAME_DOWNLOAD_TYPE);
                            ap2.setPortalData("");
                            // Persist what we have ...
                            AccessPointsUtils.saveAccessPoint(ap2);
                            // ... so that now we can get access to the
                            // ID of the persisted object with ap2.getId().
                            JsonObjectBuilder job2Portal =
                                    Json.createObjectBuilder();
                            JsonObjectBuilder job2Toolkit =
                                    Json.createObjectBuilder();
                            job2Portal.add("uri",
                                    downloadPrefixProperty + ap2.getId()
                                    + "/"
                                    + Download.downloadFilename(ap2,
                                            ""));
                            job2Toolkit.add("uri",
                                    uri.replaceFirst(sparqlPrefix,
                                            sesamePrefix));
                            ap2.setPortalData(job2Portal.build().toString());
                            ap2.setToolkitData(job2Toolkit.build().toString());
                            AccessPointsUtils.updateAccessPoint(ap2);
                            jobToolkit.add("source", "local");
                        } else {
                            jobToolkit.add("source", "remote");
                        }
                        ap.setPortalData(jobPortal.build().toString());
                        ap.setToolkitData(jobToolkit.build().toString());
                        AccessPointsUtils.saveAccessPoint(ap);
                        break;
                    case AccessPoints.WEBPAGE_TYPE:
                        uri = accessPoint.get("uri").asText();
                        if (uri.endsWith("concept/topConcepts")) {
                            ap.setType(AccessPoints.SISSVOC_TYPE);
                            jobToolkit.add("source", "local");
                            jobPortal.add("uri", uri.
                                    replaceFirst("/concept/topConcepts$", ""));
                        } else {
                            ap.setType(type);
                            jobPortal.add("uri", uri);
                        }
                        ap.setPortalData(jobPortal.build().toString());
                        ap.setToolkitData(jobToolkit.build().toString());
                        AccessPointsUtils.saveAccessPoint(ap);
                        break;
                    default:
                    }
                    System.out.println("type is: " + ap.getType());
                    System.out.println("portal_data: " + ap.getPortalData());
                    System.out.println("toolkit_data: " + ap.getToolkitData());
                }
            }
        }
    }


}