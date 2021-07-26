package org.semsys.engine;

import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.eclipse.rdf4j.RDF4JException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager;
import org.eclipse.rdf4j.repository.manager.RepositoryManager;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandler;
import org.eclipse.rdf4j.rio.Rio;
import org.semsys.Service;
import org.semsys.entity.Agent;
import org.semsys.entity.Entity;
import org.semsys.entity.Provenance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.topbraid.shacl.validation.ValidationUtil;
import org.topbraid.shacl.vocabulary.SH;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.jena.ontology.OntModelSpec.OWL_MEM;

public class EndpointV2 {

    private RepositoryManager manager;
    private Repository repository;
    private static final Logger log = LoggerFactory.getLogger(Service.class);
    private final String graphName = "http://vasqua.org/provenance/test";

    public EndpointV2() {
    }

    /**
     * initialize connection to graph db
     * @param repoName repository name
     * @param managerAddr graph db location
     */
    public void init(String repoName, String managerAddr) {
        log.info("initialize connection to the graph db");
        if(manager == null || repository == null) {
            manager = new RemoteRepositoryManager(managerAddr);
            manager.init();
            repository = manager.getRepository(repoName);
        }
    }

    /**
     * send request to graph db to clean the repository
     */
    public void cleanRepo() {
        log.info("clean all data in repository");
        try {
            RepositoryConnection repositoryConnection = repository.getConnection();
            try {
                repositoryConnection.clear();
            } finally {
                repositoryConnection.close();
            }
        } catch (RDF4JException e) {
            log.error(e.getMessage());
        }
    }

    public void storeDataInRepo(File file, String graph) {
        StoreData(file, graph);
    }

    /**
     * store a ttl file into the repository
     * @param file ttl file
     * @param graph graph name
     */
    private void StoreData(File file, String graph) {
        log.info("adding graph to repository");
        try {
            RepositoryConnection con = this.repository.getConnection();
            try {
                con.begin();
                IRI iri = this.repository.getValueFactory().createIRI(graph);
                con.add(file, null, RDFFormat.TURTLE, iri);
                con.commit();
            } finally {
                con.close();
            }
        } catch (RDF4JException e) {
            log.error(e.getMessage());
        } catch (java.io.IOException e) {
            log.error(e.getMessage());
        }
    }


    public void getDockerImage(Map<String, Provenance> provenenceMap) {
        Provenance provenance;
        Entity entity;
        try {
            RepositoryConnection con = this.repository.getConnection();
            String queryString = "PREFIX rr:     <http://www.w3.org/ns/r2rml#> \n" +
                    "prefix rml:    <http://semweb.mmlab.be/ns/rml#> \n" +
                    "prefix ql:     <http://semweb.mmlab.be/ns/ql#> \n" +
                    "prefix carml:  <http://carml.taxonic.com/carml/> \n" +
                    "prefix xsd:    <http://www.w3.org/2001/XMLSchema#> \n" +
                    "prefix dcterm: <http://purl.org/dc/terms/> \n" +
                    "prefix owl:    <http://www.w3.org/2002/07/owl#> \n" +
                    "prefix rdfs:   <http://www.w3.org/2000/01/rdf-schema#> \n" +
                    "prefix rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
                    "prefix prov:   <http://www.w3.org/ns/prov#> \n" +
                    "prefix : <http://www.semanticweb.org/43676/ontologies/2021/3/vasqua_auditbox#> \n" +
                    "\n" +
                    "SELECT DISTINCT ?uuid ?label ?gitCommitNumber ?gitDescription ?pipfreeze ?startedAt\n" +
                    "WHERE {\n" +
                    "    ?run :basedOfSnapshotFrom ?image .\n" +
                    "    ?run prov:startedAtTime ?startedAt .\n" +
                    "    ?image rdfs:label ?label .\n" +
                    "    ?image :gitCommitNumber ?gitCommitNumber .\n" +
                    "    ?image :gitDescription ?gitDescription .\n" +
                    "    ?image :pipfreeze ?pipfreeze .\n" +
                    "    ?run :uuid ?uuid .\n" +
                    "}\n";
            try {
                String keyTemp = "";
                TupleQuery query = con.prepareTupleQuery(queryString);
                TupleQueryResult queryResult = query.evaluate();
                for(BindingSet binding: queryResult) {
                    String keyQuery = binding.getValue("uuid").stringValue();
                    String label = binding.getValue("label").stringValue();
                    String gitCommitNumber = binding.getValue("gitCommitNumber").stringValue();
                    String gitDescription = binding.getValue("gitDescription").stringValue();
                    String pipfreeze = binding.getValue("pipfreeze").stringValue();
                    String startedAt = binding.getValue("startedAt").stringValue();
                    if(!keyTemp.equals(keyQuery)) {
                        entity = new Entity();
                        provenance = new Provenance();
                        entity.setName(label);
                        entity.addValue("gitCommitNumber", gitCommitNumber);
                        entity.addValue("pipfreeze", pipfreeze);
                        entity.addValue("gitDescription", gitDescription);
                        provenance.setTime(startedAt);
                        provenance.addEntity(entity);
                        provenenceMap.put(keyQuery, provenance);
                    }
                }
            } finally {
                con.close();
            }
        } catch (RDF4JException e) {
            log.error(e.getMessage());
        }
    }

    public void validate(String uuid) {
        RepositoryConnection con = this.repository.getConnection();
        IRI context = this.repository.getValueFactory().createIRI(this.graphName + "-" + uuid);
        File temp = null;
        InputStream shaclIS = Collector.class.getClassLoader().getResourceAsStream(Service.config.shapes);
        Model shapes = ModelFactory.createDefaultModel();
        RDFDataMgr.read(shapes, shaclIS, Lang.TURTLE);

        try {
            temp = File.createTempFile(Service.config.tempFile, Service.config.tempFileSuffix);
            FileOutputStream fos = new FileOutputStream(temp);
            RDFHandler writer = Rio.createWriter(RDFFormat.TURTLE, fos);
            con.export(writer, context);

            FileInputStream tempIs = new FileInputStream(temp);
            Model data = ModelFactory.createDefaultModel();
            RDFDataMgr.read(data,tempIs , Lang.TURTLE);

            Resource validation = ValidationUtil.validateModel(data, shapes, false);
            boolean reportStatus = validation.getProperty(SH.conforms).getBoolean();
            StmtIterator iterator = data.listStatements();
            RDFNode rdfNode = null;
            Property property = null;
            while (iterator.hasNext()) {
                Statement statement = iterator.nextStatement();
                if (statement.getPredicate().toString().equals("http://www.semanticweb.org/43676/ontologies/2021/3/vasqua_auditbox#status")) {
                    rdfNode = statement.getObject();
                    property = statement.getPredicate();
                    con.prepareUpdate(
                            "PREFIX : <http://www.semanticweb.org/43676/ontologies/2021/3/vasqua_auditbox#>" + "\n"
                            + "DELETE DATA { \n"
                            + "<http://www.semanticweb.org/43676/ontologies/2021/2/Vasqua-Auditbox/ExecutionTraceBundle-" + uuid + "> :status  'Not Validated' . \n"
                            + "}"
                    ).execute();
                    break;
                }
            }
            tempIs = new FileInputStream(temp);
            OntModel model = ModelFactory.createOntologyModel(OWL_MEM);
            RDFDataMgr.read(model,tempIs , Lang.TURTLE);
            //DatatypeProperty status = model.getDatatypeProperty("http://www.semanticweb.org/43676/ontologies/2021/3/vasqua_auditbox#status");
            Individual bundle = model.getIndividual("http://www.semanticweb.org/43676/ontologies/2021/2/Vasqua-Auditbox/ExecutionTraceBundle-" + uuid);

            if(!reportStatus) {
                bundle.removeProperty(property, rdfNode);
                bundle.addProperty(property, " Not Complete");
            } else {
                bundle.removeProperty(property, rdfNode);
                bundle.addProperty(property, " Complete");
            }
            File file = File.createTempFile(Service.config.tempFile, Service.config.tempFileSuffix);
            FileOutputStream out = new FileOutputStream(file);
            model.write(out, "TTL");
            storeDataInRepo(file, this.graphName + "-" + uuid);
            file.deleteOnExit();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            con.close();
        }
        /*
        RepositoryConnection con = this.repository.getConnection();
        FileInputStream tempIs = new FileInputStream(temp);
        Model data = ModelFactory.createDefaultModel();
        RDFDataMgr.read(data, , Lang.TURTLE);

        Resource validation = ValidationUtil.validateModel(data, shapes, false);
        boolean reportStatus = validation.getProperty(SH.conforms).getBoolean();
        if(!reportStatus) {
            log.warn("validation error " + validation.getModel().toString());
        }

         */
    }

    public Map<String, Provenance> getAllProvenences() {
        Map<String, Provenance> result = new HashMap<>();
        getDockerImage(result);
        for(Map.Entry<String, Provenance> prov : result.entrySet()) {
            addJNBData(prov.getKey(), prov.getValue());
            addWebsiteRAWData(prov.getKey(), prov.getValue());
            addJsonData(prov.getKey(), prov.getValue());
        }
        return result;
    }

    public Map<String, Provenance> getAllProvenence() {
        Map<String, Provenance> result = new HashMap<>();
        Provenance provenance = new Provenance();
        Entity entity = new Entity();
        try {
            String key = "";
            RepositoryConnection con = this.repository.getConnection();
            String queryString = "PREFIX rr:     <http://www.w3.org/ns/r2rml#> \n" +
                    "prefix rml:    <http://semweb.mmlab.be/ns/rml#> \n" +
                    "prefix ql:     <http://semweb.mmlab.be/ns/ql#> \n" +
                    "prefix carml:  <http://carml.taxonic.com/carml/> \n" +
                    "prefix xsd:    <http://www.w3.org/2001/XMLSchema#> \n" +
                    "prefix dcterm: <http://purl.org/dc/terms/> \n" +
                    "prefix owl:    <http://www.w3.org/2002/07/owl#> \n" +
                    "prefix rdfs:   <http://www.w3.org/2000/01/rdf-schema#> \n" +
                    "prefix rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
                    "prefix prov:   <http://www.w3.org/ns/prov#> \n" +
                    "prefix : <http://www.semanticweb.org/43676/ontologies/2021/2/Vasqua-Auditbox#> \n" +
                    "\n" +
                    "SELECT DISTINCT ?uuid ?label ?gitCommitNumber ?pipfreeze ?startedAt\n" +
                    "WHERE {\n" +
                    "    ?run :basedOfSnapshotFrom ?image .\n" +
                    "    ?image rdfs:label ?label .\n" +
                    "    ?image :gitCommitNumber ?gitCommitNumber .\n" +
                    "    ?image :pipfreeze ?pipfreeze .\n" +
                    "    ?image prov:startedAtTime ?startedAt .\n" +
                    "    ?image :uuid ?uuid .\n" +
                    "}\n";
            try {
                TupleQuery query = con.prepareTupleQuery(queryString);
                TupleQueryResult queryResult = query.evaluate();
                List<BindingSet> bindingSetList;

                for(BindingSet binding: queryResult) {
                    String keyQuery = binding.getValue("uuid").stringValue();
                    String label = binding.getValue("label").stringValue();
                    String gitCommitNumber = binding.getValue("gitCommitNumber").stringValue();
                    String pipfreeze = binding.getValue("pipfreeze").stringValue();
                    String startedAt = binding.getValue("startedAt").stringValue();
                    if(key.equals("")) {
                        key = keyQuery;
                        entity.setName(label);
                        entity.addValue("gitCommitNumber", gitCommitNumber);
                        entity.addValue("pipfreeze", pipfreeze);
                        provenance.setTime(startedAt);
                        provenance.addEntity(entity);
                        addAuditData(key, provenance);
                        result.put(key, provenance);
                    } else if(!key.equals(keyQuery)){
                        key = keyQuery;
                        entity = new Entity();
                        provenance = new Provenance();
                        entity.setName(label);
                        entity.addValue("gitCommitNumber", gitCommitNumber);
                        entity.addValue("pipfreeze", pipfreeze);
                        provenance.addEntity(entity);
                        provenance.setTime(startedAt);
                        addAuditData(key, provenance);
                        result.put(key, provenance);
                    }
                }
            } finally {
                con.close();
            }
        } catch (RDF4JException e) {
            e.printStackTrace();
        }
        return result;
    }

    private void addAuditData(String key, Provenance provenance) {
        addJNBData(key, provenance);
        addWebsiteRAWData(key, provenance);
    }

    private void addJNBData(String key, Provenance provenance) {
        try {
            RepositoryConnection con = this.repository.getConnection();
            String queryString = "PREFIX rr:     <http://www.w3.org/ns/r2rml#> \n" +
                    "prefix rml:    <http://semweb.mmlab.be/ns/rml#> \n" +
                    "prefix ql:     <http://semweb.mmlab.be/ns/ql#> \n" +
                    "prefix carml:  <http://carml.taxonic.com/carml/> \n" +
                    "prefix xsd:    <http://www.w3.org/2001/XMLSchema#> \n" +
                    "prefix dcterm: <http://purl.org/dc/terms/> \n" +
                    "prefix owl:    <http://www.w3.org/2002/07/owl#> \n" +
                    "prefix rdfs:   <http://www.w3.org/2000/01/rdf-schema#> \n" +
                    "prefix rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
                    "prefix prov:   <http://www.w3.org/ns/prov#> \n" +
                    "prefix : <http://www.semanticweb.org/43676/ontologies/2021/2/Vasqua-Auditbox#> \n" +
                    "\n" +
                    "SELECT DISTINCT ?uuid ?jnbName ?label ?hash\n" +
                    "WHERE{\n" +
                    "    \t?dockerContainer :contain ?jnbName .\n" +
                    "    \t?jnbName :hash ?hash .\n" +
                    "    \t?jnbName rdfs:label ?label.\n" +
                    "    \t?dockerContainer :uuid ?uuid .\n" +
                    "}";
            try {
                TupleQuery query = con.prepareTupleQuery(queryString);
                TupleQueryResult queryResult = query.evaluate();
                for(BindingSet binding: queryResult) {
                    String keyQuery = binding.getValue("uuid").stringValue();
                    String label = binding.getValue("label").stringValue();
                    String hash = binding.getValue("hash").stringValue();
                    if(key.equals(keyQuery)) {
                        Agent agent = new Agent();
                        agent.setName(label);
                        agent.addValue("hash",hash);
                        provenance.addAgent(agent);
                        break;
                    }

                }
            } finally {
                con.close();
            }
        } catch (RDF4JException e) {
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void addWebsiteRAWData(String key, Provenance provenance) {
        try {
            RepositoryConnection con = this.repository.getConnection();
            String queryString = "PREFIX rr:     <http://www.w3.org/ns/r2rml#> \n" +
                    "prefix rml:    <http://semweb.mmlab.be/ns/rml#> \n" +
                    "prefix ql:     <http://semweb.mmlab.be/ns/ql#> \n" +
                    "prefix carml:  <http://carml.taxonic.com/carml/> \n" +
                    "prefix xsd:    <http://www.w3.org/2001/XMLSchema#> \n" +
                    "prefix dcterm: <http://purl.org/dc/terms/> \n" +
                    "prefix owl:    <http://www.w3.org/2002/07/owl#> \n" +
                    "prefix rdfs:   <http://www.w3.org/2000/01/rdf-schema#> \n" +
                    "prefix rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
                    "prefix prov:   <http://www.w3.org/ns/prov#> \n" +
                    "prefix : <http://www.semanticweb.org/43676/ontologies/2021/2/Vasqua-Auditbox#> \n" +
                    "\n" +
                    "SELECT DISTINCT ?rawDataUUID ?label ?airportName ?hash ?startedAt ?endedAt\n" +
                    "WHERE{\n" +
                    "    \t?dockerContainer :contain ?jnbName .\n" +
                    "    \t?jnbName :sendRequest ?get_website_raw .\n" +
                    "    \t?get_website_raw prov:startedAtTime ?startedAt .\n" +
                    "    \t?get_website_raw prov:endedAtTime ?endedAt .\n" +
                    "    \t?get_website_raw :response ?rawData .\n" +
                    "    \t?rawData :hash ?hash .\n" +
                    "    \t?rawData rdfs:label ?label.\n" +
                    "    \t?rawData :uuid ?rawDataUUID .\n" +
                    "    \t?rawData :airportName ?airportName .\n" +
                    "} ";
            try {
                TupleQuery query = con.prepareTupleQuery(queryString);
                TupleQueryResult queryResult = query.evaluate();
                List<BindingSet> bindingSetList;
                for(BindingSet binding:queryResult){
                    String keyQuery = binding.getValue("rawDataUUID").stringValue();
                    String label = binding.getValue("label").stringValue();
                    String hash = binding.getValue("hash").stringValue();
                    String startedAt = binding.getValue("startedAt").stringValue();
                    String endedAt = binding.getValue("endedAt").stringValue();
                    String airportName = binding.getValue("airportName").stringValue();
                    if(key.equals(keyQuery)) {
                        Entity entity = new Entity();
                        entity.setName(label);
                        entity.addValue("hash",hash);
                        entity.addValue("get_website_raw_start", startedAt);
                        entity.addValue("get_website_raw_end", endedAt);
                        entity.addValue("airportName", airportName);
                        provenance.addEntity(entity);
                        break;
                    }

                }
            } finally {
                con.close();
            }
        } catch (RDF4JException e) {
            e.printStackTrace();
        }
    }

    private void addJsonData(String key, Provenance provenance) {
        try {
            RepositoryConnection con = this.repository.getConnection();
            String queryString = "PREFIX rr:     <http://www.w3.org/ns/r2rml#> \n" +
                    "prefix rml:    <http://semweb.mmlab.be/ns/rml#> \n" +
                    "prefix ql:     <http://semweb.mmlab.be/ns/ql#> \n" +
                    "prefix carml:  <http://carml.taxonic.com/carml/> \n" +
                    "prefix xsd:    <http://www.w3.org/2001/XMLSchema#> \n" +
                    "prefix dcterm: <http://purl.org/dc/terms/> \n" +
                    "prefix owl:    <http://www.w3.org/2002/07/owl#> \n" +
                    "prefix rdfs:   <http://www.w3.org/2000/01/rdf-schema#> \n" +
                    "prefix rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
                    "prefix prov:   <http://www.w3.org/ns/prov#> \n" +
                    "prefix : <http://www.semanticweb.org/43676/ontologies/2021/2/Vasqua-Auditbox#> \n" +
                    "\n" +
                    "SELECT DISTINCT ?jsonUUID ?label ?airportName ?hash\n" +
                    "WHERE{\n" +
                    "    ?airportJSON :data ?sendJSON .\n" +
                    "    ?airportJSON :hash ?hash .\n" +
                    "    ?airportJSON rdfs:label ?label .\n" +
                    "    ?airportJSON :uuid ?jsonUUID .\n" +
                    "    ?airportJSON :airportName ?airportName .\n" +
                    "} ";
            try {
                TupleQuery query = con.prepareTupleQuery(queryString);
                TupleQueryResult queryResult = query.evaluate();
                for(BindingSet binding: queryResult) {
                    String keyQuery = binding.getValue("jsonUUID").stringValue();
                    String label = binding.getValue("label").stringValue();
                    String airportName =  binding.getValue("airportName").stringValue();
                    String hash = binding.getValue("hash").stringValue();
                    if(key.equals(keyQuery)) {
                        Entity entity = new Entity();
                        entity.setName(label);
                        entity.addValue("hash",hash);
                        entity.addValue("airportName", airportName);
                        provenance.addEntity(entity);
                        break;
                    }

                }
            } finally {
                con.close();
            }
        } catch (RDF4JException e) {
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
