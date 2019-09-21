package rest;

import entities.Movie;
import static entities.Movie_.name;
import utils.EMF_Creator;
import io.restassured.RestAssured;
import static io.restassured.RestAssured.given;
import io.restassured.http.ContentType;
import io.restassured.parsing.Parser;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.ws.rs.core.UriBuilder;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import static org.hamcrest.Matchers.equalTo;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import utils.EMF_Creator.DbSelector;
import utils.EMF_Creator.Strategy;

//Uncomment the line below, to temporarily disable this test
@Disabled
public class MovieResourceTest {

    private static final int SERVER_PORT = 7777;
    private static final String SERVER_URL = "http://localhost/api";
//    Read this line from a settings-file  since used several places
    private static final String TEST_DB = "jdbc:mysql://localhost:3307/movie_test";

    static final URI BASE_URI = UriBuilder.fromUri(SERVER_URL).port(SERVER_PORT).build();
    private static HttpServer httpServer;
    private static EntityManagerFactory emf;
    private static Movie movie1;
    private static Movie movie2;



    static HttpServer startServer() {
        ResourceConfig rc = ResourceConfig.forApplication(new ApplicationConfig());
        return GrizzlyHttpServerFactory.createHttpServer(BASE_URI, rc);
    }

    @BeforeAll
    public static void setUpClass() {
        emf = EMF_Creator.createEntityManagerFactory(DbSelector.TEST, Strategy.CREATE);

//        NOT Required if you use the version of EMF_Creator.createEntityManagerFactory used above        
//        System.setProperty("IS_TEST", TEST_DB);
//        We are using the database on the virtual Vagrant image, so username password are the same for all dev-databases
//        httpServer = startServer();
//
//        Setup RestAssured
//        RestAssured.baseURI = SERVER_URL;
        RestAssured.port = SERVER_PORT;

        RestAssured.defaultParser = Parser.JSON;
    }

    @AfterAll
    public static void closeTestServer() {
//        System.in.read();
        httpServer.shutdownNow();
    }

//     Setup the DataBase (used by the test-server and this test) in a known state BEFORE EACH TEST
//    TODO -- Make sure to change the script below to use YOUR OWN entity class
    @BeforeEach
    public void setUp() {
        EntityManager em = emf.createEntityManager();
        movie1 = new Movie(2000, "King Kong", "Kong");
        movie2 = new Movie(2004, "Spiderman", "Toby");
        try {
            em.getTransaction().begin();
            em.createNamedQuery("Movie.deleteAllRows").executeUpdate();
            em.persist(movie1);
            em.persist(movie2);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    @Test
    public void testServerIsUp() {
        System.out.println("Testing is server UP");
        given().contentType("application/json").
                get("/movie").
                then()
                .statusCode(200);
    }

    @Test
    public void GetMoviesCount() {
        System.out.println("Testing number of movies");
        given().contentType("application/json")
                .get("/movie/count").
                then()
                .statusCode(200)
                .body("movies", equalTo(2));
    }

    @Test
    public void getAllMovies() {
        System.out.println("Testing getting all movies");
        List<String> name = new ArrayList<>();
        name.add(movie1.getName());
        given().contentType("application/json")
                .get("/movie/all").then().statusCode(200)
                .body("Movie", equalTo(movie1.getActors()));

    }
    @Test
    public void getMovieName() {
        System.out.println("Test get Movie name");
        given().
                contentType("application/json").
                get("/movie/name/{name}", "Kong").
                then().log().body().assertThat().
                statusCode(HttpStatus.OK_200.getStatusCode()).
                body("movie", equalTo(movie1.getActors()));
    }
    
    
}


