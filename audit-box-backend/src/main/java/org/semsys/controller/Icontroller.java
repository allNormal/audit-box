package org.semsys.controller;

import org.semsys.entity.Provenance;
import spark.Request;
import spark.Response;

import java.util.Map;

public interface Icontroller {

    void init();

    void doExtraction(Request request, Response response);

    void initCleanRepo(Response response);

    //void addNewAudit(Request request, Response response);

    Map<String, Provenance> getAllAudit(Response response);

    void validate(Request request);

}
