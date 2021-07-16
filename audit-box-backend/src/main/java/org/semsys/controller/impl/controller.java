package org.semsys.controller.impl;

import org.semsys.Service;
import org.semsys.controller.Icontroller;
import org.semsys.engine.Collector;
import org.semsys.entity.Provenance;
import org.semsys.exception.NoFileParserException;
import org.semsys.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import spark.Request;
import spark.Response;

import java.io.InputStream;
import java.util.Map;

public class controller implements Icontroller {

    private Config config;
    private Collector auditCollector;
    private static final Logger log = LoggerFactory.getLogger(Service.class);
    private static String CONFIG = "config.yaml";

    public controller() {
        Yaml yaml = new Yaml(new Constructor(Config.class));
        InputStream configIS = Service.class.getClassLoader().getResourceAsStream(CONFIG);
        config = yaml.load(configIS);
        this.auditCollector =  new Collector(config.provRepo, config.endpoint);
    }

    @Override
    public void init() {
        this.auditCollector.init(config.ep_plan, config.defaultGraph);
        this.auditCollector.init(config.sao, config.defaultGraph);
        this.auditCollector.init(config.provenance, config.defaultGraph);
    }

    /**
     * send api request to extract the information to the audit collector
     * check if audit collector works as intended
     * build response that will be send back
     * @param request request from the client
     * @param response response that will be send back to the client
     */
    @Override
    public void doExtraction(Request request, Response response) {
        System.out.println(request.body());
        try {
            if(request.contentType().contains("xml")) {
                auditCollector.storeDataInRepo(request.body(), config.xmlReceiver, "xml", config.defaultGraph, config.shapes);
            } else if(request.contentType().contains("json")) {
                auditCollector.storeDataInRepo(request.body(), config.jsonReceiver, "json", config.defaultGraph, config.shapes);
            } else if(request.contentType().contains("csv")) {
                auditCollector.storeDataInRepo(request.body(), config.csvReceiver, "csv", config.defaultGraph, config.shapes);
            } else {
                throw new NoFileParserException("file parser for " + response.type() + " not yet implemented");
            }
            response.status(201);
            response.body("{ message: 'success' }");
            log.info("success adding data to repository");
        }  catch (NoFileParserException e) {
            log.error(e.getMessage());
            response.body("{ error_message: '" + e.getMessage() + "'}");
            response.status(406);
        } catch (Exception e) {
            log.error(e.getMessage());
            response.body("{ error_message: '" + e.getMessage() + "'}");
            response.status(500);
        }

    }

    /**
     * send api request to clean the repository to the audit collector
     * @param response response that will be sended back to the client
     */
    @Override
    public void initCleanRepo(Response response) {
        auditCollector.clearRepo();
        response.status(200);
    }

    /**
     * send api request to get all available audit in repository to the audit collector
     * @param response response that will be send to the client
     * @return all available audit
     */
    @Override
    public Map<String, Provenance> getAllAudit(Response response) {
        response.type("application/json");
        Map<String, Provenance> allAudit = auditCollector.getAllProvenence();
        log.info("success retrieving all audit data from repository");
        return allAudit;
    }

}
