package org.semsys;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.semsys.config.Config;
import org.semsys.controller.impl.controller;
import org.semsys.mapper.ProvenanceMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import spark.Filter;
import spark.Response;

import java.io.InputStream;

import static spark.Spark.*;

public class Service {

    private static final Logger log = LoggerFactory.getLogger(Service.class);
    private static String CONFIG = "config.yaml";
    private static org.semsys.controller.impl.controller controller = new controller();
    private ProvenanceMapper mapper = new ProvenanceMapper();
    public static Config config;

    public static void main(String[] args) {
        Yaml yaml = new Yaml(new Constructor(Config.class));
        InputStream configIS = Service.class.getClassLoader().getResourceAsStream(CONFIG);
        config = yaml.load(configIS);
        //PropertyConfigurator.configure(config.logFile);
        log.info("initialize audit-box services");
        Service service = new Service();
        service.establishRoutes();
        controller.init();
        log.info("audit-box initialized");
    }

    public void establishRoutes() {
        port(config.auditboxPort);
        initPingService();
        doExtraction();
        getAllAudit();
        doLoad();
        initCleanRepo();
        validate();
    }


    private void doExtraction() {
        post("/api/repo/extract", (request, response) -> {
            log.info("POST doExtraction" + request.headers());
            this.controller.doExtraction(request, response);
            return response;
        });
    }

    private void getAllAudit() {
        after((Filter) (request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Allow-Methods", "GET");
        });
        get("/allAudit", (request, response) -> {
            ObjectMapper mapper = new ObjectMapper();
            log.info("GET getAllAudit" + request.headers());
            return mapper.writeValueAsString(this.controller.getAllAudit(response));
        });
    }

    private void doLoad() {
        post("/api/repo/upload", (request, response) -> {
            log.info("doLoad" + request.headers());
            return response;
        });
    }


    private void initCleanRepo() {
        get("/api/reset", (request, response) -> {
            log.info("initCleanRepo" + request.headers());
            this.controller.initCleanRepo(response);
            return response;
        });
    }

    private void initPingService() {
        get("/api/ping", (request, response) -> {
            log.info("initPingService" + request.headers());
            return "hello!";
        });
    }

    private String defaultResponse(Response response) {
        response.status(200);
        response.type("application/json");
        return "{ status: 'query OK' }";
    }

    private void validate() {
        post("/api/validate", (request, response) -> {
            controller.validate(request);
            return "{status: 'validated'}";
        });
    }

}
