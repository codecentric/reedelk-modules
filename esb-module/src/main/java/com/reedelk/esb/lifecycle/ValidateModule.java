package com.reedelk.esb.lifecycle;

import com.reedelk.esb.module.DeserializedModule;
import com.reedelk.esb.module.Module;
import com.reedelk.runtime.api.commons.StringUtils;
import com.reedelk.runtime.api.exception.InvalidFlowException;
import com.reedelk.runtime.commons.JsonParser;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ValidateModule extends AbstractStep<Module, Module> {

    private static final Logger logger = LoggerFactory.getLogger(ValidateModule.class);

    private List<Validator> VALIDATORS = Arrays.asList(new ValidFlowIdValidator(), new UniqueFlowIdValidator());

    @Override
    public Module run(Module module) {

        DeserializedModule deserializedModule;
        try {
            deserializedModule = module.deserialize();
        } catch (Exception deserializationException) {
            logger.error("Module deserialization", deserializationException);
            module.error(deserializationException);
            return module;
        }

        Set<JSONObject> flows = deserializedModule.getFlows();
        try {
            VALIDATORS.forEach(validator -> validator.validate(flows));
        } catch (Exception validationException) {
            logger.error("Module validation", validationException);
            module.error(validationException);
            return module;
        }
        return module;
    }

    interface Validator {
        void validate(Set<JSONObject> flowsDefinition);
    }

    /**
     * Validates that all the items in the collection contain a property
     * value which is unique across all the elements in it.
     */
    class UniqueFlowIdValidator implements Validator {
        @Override
        public void validate(Set<JSONObject> flowsDefinition) {
            boolean test = flowsDefinition.stream()
                    .map(JsonParser.Flow::id)
                    .allMatch(new HashSet<>()::add);
            if (!test) {
                throw new InvalidFlowException("There are at least two flows with the same ID. Flows IDs must be unique.");
            }
        }
    }

    class ValidFlowIdValidator implements Validator {
        @Override
        public void validate(Set<JSONObject> flows) {
            flows.forEach(flowDefinition -> {
                if (!JsonParser.Flow.hasId(flowDefinition) ||
                        StringUtils.isBlank(JsonParser.Flow.id(flowDefinition))) {
                    throw new InvalidFlowException("\"id\" property must be defined in the flow definition");
                }
            });
        }
    }
}
