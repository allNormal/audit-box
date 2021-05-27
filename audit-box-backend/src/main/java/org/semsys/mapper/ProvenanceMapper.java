package org.semsys.mapper;

import org.semsys.dto.ProvenanceListDto;
import org.semsys.entity.Provenance;

import java.util.Map;

public class ProvenanceMapper {

    public ProvenanceListDto entitiyToDto(Map<String, Provenance> provenenceMap) {
        return new ProvenanceListDto(provenenceMap);
    }
}
