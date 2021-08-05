package edu.stanford.protege.webprotege.authorization;

import org.springframework.stereotype.Component;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2021-07-29
 */
@Component
@Retention(RetentionPolicy.RUNTIME)
public @interface ActionHandler {

}
