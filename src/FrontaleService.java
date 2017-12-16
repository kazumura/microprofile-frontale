package kzr.frontale;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.MediaType;
import javax.json.JsonArrayBuilder;
import javax.json.Json;

import javax.inject.Inject;
import javax.ejb.Stateless;

@Stateless
@Path("/")
public class FrontaleService {
  @Inject
  Frontale frontale;

  @GET
  @Path("list")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getList() {
    JsonArrayBuilder ja = Json.createArrayBuilder();
    frontale.getMap().keySet().forEach(e -> ja.add(e));
    return Response.ok(ja.build()).build();
  }

  @GET
  @Path("{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Player getPlayer(@PathParam("id") String id) {
    return frontale.getMap().get(id);
  }

}
