package org.acme;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.logging.Logger;

import org.acme.coisas.BlockingHashMap;

// O que isso faz é o seguinte:
// em    GET /get/{xxxxx} eu leio o valor de {xxxxx}
// e em POST /put/{xxxxx} eu coloco o valor de {xxxxx}

@Path("/")
@Produces(MediaType.TEXT_PLAIN)
@Consumes(MediaType.TEXT_PLAIN)
public class MiniBroker {
    private static final Logger log = Logger.getLogger(MiniBroker.class.getName());
    private static final BlockingHashMap<String, String> map = new BlockingHashMap<>();

    @GET
    public Response index(){
        log.info("hello!");
        return Response.ok("hello world!").build();
    }

    @POST
    @Path("/put/{key}")
    public Response put(
            @PathParam("key") String key, String value) {

        map.put(key, value);
        log.info("botando "+value+" em "+key);
        return Response.ok(key + "<-" + value).build();
    }

    @GET
    @Path("/get/{key}")
    public Response get(
        @PathParam("key") String key,
        @QueryParam("timeout") @DefaultValue("50000") long timeoutMs) {

        try {
            log.info("lendo "+key+". isso pode demorar...");
            String value = map.get(key, timeoutMs);
            if (value == null) {
                return Response.status(408).entity("timeout! " + key).build();
            }
            log.info("li "+key+"="+value);
            return Response.ok(value).build();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Response.serverError().entity("interrupted").build();
        }
    }
}
