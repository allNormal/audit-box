package org.semsys.dto;

import org.semsys.entity.Provenance;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ProvenanceListDto {

    private Map<String, Provenance> provenanceMap = new HashMap<>();

    public ProvenanceListDto() {
    }

    public ProvenanceListDto(Map<String, Provenance> provenanceMap) {
        this.provenanceMap = provenanceMap;
    }

    public Map<String, Provenance> getProvenanceMap() {
        return provenanceMap;
    }

    public void setProvenanceMap(Map<String, Provenance> provenanceMap) {
        this.provenanceMap = provenanceMap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProvenanceListDto that = (ProvenanceListDto) o;
        return Objects.equals(provenanceMap, that.provenanceMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(provenanceMap);
    }
}
