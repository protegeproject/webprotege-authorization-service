package edu.stanford.protege.webprotege.authorization.handlers;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.stanford.protege.webprotege.common.Response;

import java.util.List;

public record GetMatchingCriteriaResponse(@JsonProperty("matchingKeys")List<String> matchingKeys) implements Response {
}
