package org.semsys.engine;

import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.semsys.Service;
import org.semsys.entity.Provenance;
import org.semsys.exception.NoFileParserException;
import org.semsys.fileparser.CSVFileParser;
import org.semsys.fileparser.JSONFileParser;
import org.semsys.fileparser.XMLFileParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.topbraid.shacl.validation.ValidationUtil;
import org.topbraid.shacl.vocabulary.SH;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class Collector {

    private EndpointV2 endpointV2;
    private static final Logger log = LoggerFactory.getLogger(Collector.class);
    private static final String FILELOCATION ="src/main/resources/";

    public Collector(String repoName, String managerAddr) {
        this.endpointV2 = new EndpointV2();
        this.endpointV2.init(repoName, managerAddr);
    }

    public void init(String provennance, String graph) {
        Path path = Paths.get(FILELOCATION + provennance);
        File file = new File(path.toString());
        this.endpointV2.storeDataInRepo(file, graph);
    }

    /**
     * send request to the graph db endpoint to store data into the repository
     * @param object data that want to be saved
     * @param rml rml that want to be used
     * @param serialization data type(xml/json/csv)
     * @param graph graph name
     * @throws Exception if fails throw an exception
     */
    public void storeDataInRepo(String object, String rml, String serialization, String graph, String shacl) throws Exception {
        File temp = storeData(object, rml, serialization, shacl);
        this.endpointV2.storeDataInRepo(temp, graph);
    }

    public void storeDataConsent(File consent, String namedGraph) {
        this.endpointV2.storeDataInRepo(consent, namedGraph);
    }

    /**
     * send request to the endpoint for graph db to clean the repository
     */
    public void clearRepo() {
        this.endpointV2.cleanRepo();
    }

    /**
     * using rml to convert received data into a .ttl file
     * @param object received data
     * @param rml rml location
     * @param serialization received data type
     * @return .ttl file
     * @throws Exception exception when something wrong happens
     */
    protected File storeData(String object, String rml, String serialization, String shacl) throws Exception {
        InputStream inputIS = IOUtils.toInputStream(object, "UTF-8");
        InputStream rmlIS = Collector.class.getClassLoader().getResourceAsStream(rml);
        InputStream shaclIS = Collector.class.getClassLoader().getResourceAsStream(shacl);
        Model shapes = ModelFactory.createDefaultModel();
        RDFDataMgr.read(shapes, shaclIS, Lang.TURTLE);

        File temp = File.createTempFile(Service.config.tempFile, Service.config.tempFileSuffix);
        FileOutputStream fos = new FileOutputStream(temp);
        temp.deleteOnExit();

        org.semsys.fileparser.FileParser fileParser = null;
        if(serialization.equals("xml")) {
            fileParser = new XMLFileParser(rml);
        } else if(serialization.equals("json")) {
            fileParser = new JSONFileParser(rml);
        } else if(serialization.equals("csv")) {
            fileParser = new CSVFileParser(rml);
        } else {
            throw new NoFileParserException("file parser for " + serialization + " not yet implemented");
        }

        fileParser.parse(inputIS,rmlIS, fos, new HashMap<>());
        FileInputStream tempIs = new FileInputStream(temp);
        Model data = ModelFactory.createDefaultModel();
        RDFDataMgr.read(data, tempIs, Lang.TURTLE);

        Resource validation = ValidationUtil.validateModel(data, shapes, false);
        boolean reportStatus = validation.getProperty(SH.conforms).getBoolean();
        if(!reportStatus) {
            log.warn("validation error " + validation.getModel().toString());
        }
        return temp;
    }

    /**
     * send request to graph db endpoint to get all available provenances in the repository
     * @return all available provenances in the repository
     */
    public Map<String, Provenance> getAllProvenence() {
        return this.endpointV2.getAllProvenences();
    }
}
