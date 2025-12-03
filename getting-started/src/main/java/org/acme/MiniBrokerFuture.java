package org.acme;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.concurrent.CompletionStage;
import java.util.logging.Logger;

import org.acme.coisas.FutureHashMap;

import io.smallrye.mutiny.Uni;

// Faz o mesmo que o outro lado, só que usando
// o FutureHashMap, onde o get() retorna um CompletableFuture
@Path("/future")
@Produces(MediaType.TEXT_PLAIN)
@Consumes(MediaType.TEXT_PLAIN)
public class MiniBrokerFuture {
    private static final Logger log = Logger.getLogger(MiniBroker.class.getName());
    private static final FutureHashMap<String, String> map = new FutureHashMap<>();

    @GET
    public Response index(){
        log.info("hello future!");
        return Response.ok("hello world from the future!").build();
    }

    @POST
    @Path("/put/{key}")
    public Response put(
            @PathParam("key") String key, String value) {
        map.put(key, value);
        log.info("botando "+value+" em "+key);
        return Response.ok(key + "<-" + value).build();
    }

    // modo """reativo"""
    @GET
    @Path("/get/{key}")
    public Uni<Response> get(
        @PathParam("key") String key,
        @QueryParam("timeout") @DefaultValue("50000") long timeoutMs) {

        log.info("lendo "+key+" sem bloquear a thread");
        CompletionStage<String> future = map.get(key, timeoutMs);
        log.info("criando o Uni de um completion stage...");
        return Uni.createFrom().completionStage(future)
            .onItem().transform(value -> {
                if (value == null) {
                    log.info("timeout lendo "+key);
                    return Response.status(408).entity("timeout! " + key).build();
                }                
                log.info("li "+key+"="+value);
                return Response.ok(value).build();
            })
            .onFailure().recoverWithItem(error -> {
                log.severe("algum outro erro maluco aconteceu para " + key + ": " + error.getMessage());
                return Response.serverError().entity("erro maluco: " + error.getMessage()).build();
            });
    }
}

