package edu.stanford.protege.webprotege.authorization.config;


import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import edu.stanford.protege.webprotege.authorization.*;
import edu.stanford.protege.webprotege.authorization.handlers.*;
import edu.stanford.protege.webprotege.ipc.CommandExecutor;
import edu.stanford.protege.webprotege.ipc.impl.CommandExecutorImpl;
import edu.stanford.protege.webprotege.jackson.WebProtegeJacksonApplication;
import org.springframework.context.annotation.*;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.util.concurrent.locks.*;

@Configuration
public class ApplicationBeans {


    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new WebProtegeJacksonApplication().objectMapper(new OWLDataFactoryImpl());
        SimpleModule module = new SimpleModule("authorizationModule");
        objectMapper.registerModule(module);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        return objectMapper;
    }

    @Bean
    CommandExecutor<GetMatchingCriteriaRequest, GetMatchingCriteriaResponse> getMatchingCriteriaExecutorCommand() {
        return new CommandExecutorImpl<>(GetMatchingCriteriaResponse.class);
    }


    @Bean
    public ReadWriteLock readWriteLock() {
        return new ReentrantReadWriteLock(true);
    }
}
