package edu.stanford.protege.webprotege.authorization.handlers;

import com.fasterxml.jackson.annotation.*;
import edu.stanford.protege.webprotege.common.*;
import edu.stanford.protege.webprotege.criteria.CompositeRootCriteria;
import org.semanticweb.owlapi.model.IRI;

import java.util.*;


@JsonTypeName(GetMatchingCriteriaRequest.CHANNEL)
public record GetMatchingCriteriaRequest(@JsonProperty("criteriaMap") Map<String, List<CompositeRootCriteria>> criteriaMap,
                                        @JsonProperty("projectId") ProjectId projectId,
                                        @JsonProperty("entityIri") IRI entitiyIri) implements Request<GetMatchingCriteriaResponse> {

    public static final String CHANNEL = "webprotege.entities.GetMatchingCriteria";

    @Override
    public String getChannel() {
        return CHANNEL;
    }
}
