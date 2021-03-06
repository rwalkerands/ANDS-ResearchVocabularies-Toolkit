/** See the file "LICENSE" for the full license governing this code. */
package au.org.ands.vocabs.toolkit.restlet;

//Disable LineLength check just for Eclipse-maintained imports.
//CHECKSTYLE:OFF: LineLength
import java.lang.invoke.MethodHandles;
import java.util.HashMap;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.ands.vocabs.toolkit.provider.harvest.PoolPartyHarvestProvider;
import au.org.ands.vocabs.toolkit.provider.transform.GetMetadataTransformProvider;
import au.org.ands.vocabs.toolkit.tasks.TaskStatus;
//CHECKSTYLE:ON: LineLength

/** Restlets for getting vocabulary metadata. */
@Path("getMetadata")
public class GetMetadata {

    /** Logger for this class. */
    private Logger logger = LoggerFactory.getLogger(
            MethodHandles.lookup().lookupClass());

    /** Injected servlet context. */
    @Context
    private ServletContext context;

    /** Get metadata for a PoolParty project.
     * @param pPProjectId PoolParty project id
     * @return The metadata for this PoolParty project, in JSON format,
     * as returned by PoolParty. */
    @Path("poolParty/{project_id}")
    @Produces(MediaType.APPLICATION_JSON)
    @GET
    public final HashMap<String, Object> getInfoPoolParty(
            @PathParam("project_id")
            final String pPProjectId) {
        HashMap<String, Object> result =
                new HashMap<String, Object>();
        logger.info("called getMetadata/poolParty " + pPProjectId);
        result.putAll(new PoolPartyHarvestProvider().getMetadata(pPProjectId));
        if (result.containsKey(TaskStatus.ERROR)
                || result.containsKey(TaskStatus.EXCEPTION)) {
            // There was a problem getting the data from PoolParty,
            // so stop here.
            return result;
        }
        result.putAll(new GetMetadataTransformProvider().
                extractMetadata(pPProjectId));
        return result;
    }
}
