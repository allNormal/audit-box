package org.semsys.fileparser;

import com.taxonic.carml.engine.RmlMapper;
import com.taxonic.carml.logical_source_resolver.JsonPathResolver;
import com.taxonic.carml.model.TriplesMap;
import com.taxonic.carml.util.RmlMappingLoader;
import com.taxonic.carml.vocab.Rdf;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Set;

public class JSONFileParser extends FileParser{

    public JSONFileParser(String type) {
        super(type);
    }

    /**
     * save data using help from rml based on its type
     * @param inputStream input strea
     * @param rmlStream rml stream
     * @param outputStream
     * @param ns
     */
    @Override
    public void parse(InputStream inputStream, InputStream rmlStream, OutputStream outputStream,
                      Map<String, String> ns) {
        // load RML file and all supporting functions
        Set<TriplesMap> mapping = RmlMappingLoader.build().load(RDFFormat.TURTLE, rmlStream);

        RmlMapper mapper = null;
        //mapper = RmlMapper.newBuilder().setLogicalSourceResolver(Rdf.Ql.JsonPath, new JsonPathResolver())
        //        .addFunctions(new CarmlFunction()).build();
        mapper = RmlMapper.newBuilder().setLogicalSourceResolver(Rdf.Ql.JsonPath, new JsonPathResolver()).build();
        // load input file and convert it to RDF
        mapper.bindInputStream(inputStream);
        // write it out to an turtle file
        org.eclipse.rdf4j.model.Model sesameModel = mapper.map(mapping);
        ns.entrySet().forEach(entry -> sesameModel.setNamespace(entry.getKey(), entry.getValue()));
        // write to file
        Rio.write(sesameModel, outputStream, RDFFormat.TURTLE);// write mapping
    }

    @Override
    public boolean checkIfProv() {
        return false;
    }
}
