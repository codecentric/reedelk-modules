package com.reedelk.esb.services.scriptengine.evaluator;

import com.reedelk.esb.exception.ScriptCompilationException;
import com.reedelk.esb.exception.ScriptExecutionException;
import com.reedelk.esb.pubsub.Action;
import com.reedelk.esb.pubsub.Event;
import com.reedelk.esb.pubsub.OnMessage;
import com.reedelk.esb.services.converter.DefaultConverterService;
import com.reedelk.esb.services.scriptengine.JavascriptEngineProvider;
import com.reedelk.esb.services.scriptengine.evaluator.function.FunctionDefinitionBuilder;
import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.content.utils.TypedPublisher;
import com.reedelk.runtime.api.script.ScriptBlock;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicValue;
import org.reactivestreams.Publisher;

import javax.script.ScriptException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.reedelk.esb.pubsub.Action.Module.Uninstalled;
import static com.reedelk.esb.services.scriptengine.evaluator.ValueProviders.STREAM_PROVIDER;

// TODO: Test also that  all resources and dynamic scripts are removedfrom the maps when a module is being uninstalled!!
abstract class AbstractDynamicValueEvaluator extends ScriptEngineServiceAdapter {

    private final Map<Long, List<String>> moduleIdFunctionNamesMap = new HashMap<>();

    AbstractDynamicValueEvaluator() {
        Event.operation.subscribe(Uninstalled, this);
    }

    <S, T> S execute(DynamicValue<T> dynamicValue, ValueProvider provider, FunctionDefinitionBuilder<DynamicValue> functionDefinitionBuilder, Object... args) {
        if (dynamicValue.isEmpty()) {
            return provider.empty();
        } else {
            Object evaluationResult = invokeFunction(dynamicValue, functionDefinitionBuilder, args);
            return convert(evaluationResult, dynamicValue.getEvaluatedType(), provider);
        }
    }

    <S> S convert(Object value, Class<?> targetClazz, ValueProvider provider) {
        if (value == null) {
            return provider.empty();

        } else if (value instanceof TypedPublisher<?>) {
            // Value is a typed stream
            TypedPublisher<?> typedPublisher = (TypedPublisher<?>) value;
            Object converted = converterService().convert(typedPublisher, targetClazz);
            return provider.from(converted);

        } else {
            // Value is NOT a typed stream
            Object converted = converterService().convert(value, targetClazz);
            return provider.from(converted);
        }
    }

    <T extends ScriptBlock> Object invokeFunction(T dynamicValue, FunctionDefinitionBuilder<T> functionDefinitionBuilder, Object... args) {
        try {

            return scriptEngine().invokeFunction(dynamicValue.functionName(), args);

        }  catch (ScriptException scriptException) {
            // We add some contextual information to the original exception such as
            // module if, flow id, flow title and script body which failed the execution.
            throw new ScriptExecutionException(dynamicValue, scriptException);

        } catch (NoSuchMethodException e) {
            // The function has not been compiled yet, optimistic invocation
            // failed. We compile the function and try to invoke it again.
            compile(dynamicValue, functionDefinitionBuilder);

            try {

                return scriptEngine().invokeFunction(dynamicValue.functionName(), args);

            }  catch (ScriptException scriptException) {
                // We add some contextual information to the original exception such as
                // module if, flow id, flow title and script body which failed the execution.
                throw new ScriptExecutionException(dynamicValue, scriptException);

            }  catch (NoSuchMethodException noSuchMethodException) {
                // If no such method exception was again thrown, it means
                // that something went wrong in the engine. In this case
                // there is nothing we can do to fix it and therefore
                // we rethrow the exception to the caller.
                throw new ESBException(noSuchMethodException);
            }
        }
    }

    private <T extends ScriptBlock> void compile(T scriptBlock, FunctionDefinitionBuilder<T> functionDefinitionBuilder) {
        synchronized (this) {

            long moduleId = scriptBlock.context().getModuleId();

            if (!moduleIdFunctionNamesMap.containsKey(moduleId)) {
                moduleIdFunctionNamesMap.put(moduleId, new ArrayList<>());
            }
            String functionName = scriptBlock.functionName();

            if (moduleIdFunctionNamesMap.get(moduleId).contains(functionName)) {
                // Already compiled by a previous call. This is needed because
                // compile might have been called by multiple Threads for the
                // same function, we prevent the function to be compiled twice.
                return;
            }

            String functionDefinition = functionDefinitionBuilder.from(functionName, scriptBlock);
            try {
                scriptEngine().compile(functionDefinition);
            } catch (ScriptException scriptCompilationException) {
                throw new ScriptCompilationException(scriptBlock, scriptCompilationException);
            }

            // Compilation was successful, we can add the function name
            // to the list of functions registered for the given module id.
            moduleIdFunctionNamesMap.get(moduleId).add(functionName);
        }
    }

    /**
     * Evaluate the payload without invoking the script engine. This is an optimization
     * since we can get the payload directly from Java without making an expensive call
     * to the script engine.
     */
    <T> TypedPublisher<T> evaluateMessagePayload(Class<T> targetType, Message message) {
        if (message.getContent().isStream()) {
            // We don't resolve the stream, but we still might need to
            // map its content from source type to a target type.
            TypedPublisher<?> stream = message.getContent().stream();
            return convert(stream, targetType, STREAM_PROVIDER);
        } else {
            Publisher<T> converted = convert(message.payload(), targetType, STREAM_PROVIDER);
            return TypedPublisher.from(converted, targetType);
        }
    }

    @OnMessage
    public void onModuleUninstalled(Action.Module.ActionModuleUninstalled action) {
        // No need to synchronize the access to 'moduleIdFunctionNamesMap' because
        // this method is called always AFTER a module has been completely stopped,
        // hence we are sure that none of its functions might be called.
        long moduleId = action.getMessage();
        if (moduleIdFunctionNamesMap.containsKey(moduleId)) {
            moduleIdFunctionNamesMap.get(moduleId).forEach(computedFunctionName ->
                    JavascriptEngineProvider.getInstance().undefineFunction(computedFunctionName));
        }
    }

    DefaultConverterService converterService() {
        return DefaultConverterService.getInstance();
    }

    ScriptEngineProvider scriptEngine() {
        return JavascriptEngineProvider.getInstance();
    }
}
