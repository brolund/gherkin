package gherkin.formatter;

import gherkin.formatter.model.*;
import net.iharder.Base64;
import org.json.simple.JSONValue;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JSONFormatter implements Reporter, Formatter {
    private final Writer out;
    private Map<Object, Object> featureHash;

    public JSONFormatter(Writer out) {
        this.out = out;
    }

    public JSONFormatter(OutputStream out) throws UnsupportedEncodingException {
        this(new OutputStreamWriter(out, "UTF-8"));
    }

    @Override
    public void uri(String uri) {
    }

    @Override
    public void feature(Feature feature) {
        featureHash = feature.toMap();
    }

    @Override
    public void background(Background background) {
        getFeatureElements().add(background.toMap());
    }

    @Override
    public void scenario(Scenario scenario) {
        getFeatureElements().add(scenario.toMap());
    }

    @Override
    public void scenarioOutline(ScenarioOutline scenarioOutline) {
        getFeatureElements().add(scenarioOutline.toMap());
    }

    @Override
    public void examples(Examples examples) {
        getAllExamples().add(examples.toMap());
    }

    @Override
    public void step(Step step) {
        getSteps().add(step.toMap());
    }

    @Override
    public void match(Match match) {
        getLastStep().put("match", match.toMap());
    }

    @Override
    public void result(Result result) {
        getLastStep().put("result", result.toMap());
    }

    @Override
    public void embedding(final String mimeType, final byte[] data) {
        final Map<String,String> embedding = new HashMap<String,String>(){{
            put("mime_type", mimeType);
            put("data", Base64.encodeBytes(data));
        }};
        getEmbeddings().add(embedding);
    }

    @Override
    public void eof() {
        try {
            out.write(JSONValue.toJSONString(featureHash));
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException("Failed to write JSON to " + out, e);
        }
    }

    @Override
    public void syntaxError(String state, String event, List<String> legalEvents, String uri, int line) {
        throw new UnsupportedOperationException();
    }

    private List<Object> getFeatureElements() {
        List<Object> featureElements = (List) featureHash.get("elements");
        if (featureElements == null) {
            featureElements = new ArrayList<Object>();
            featureHash.put("elements", featureElements);
        }
        return featureElements;
    }

    private Map<Object,List<Object>> getFeatureElement() {
        return (Map) getFeatureElements().get(getFeatureElements().size() - 1);
    }

    private List<Object> getAllExamples() {
        List<Object> allExamples = getFeatureElement().get("examples");
        if (allExamples == null) {
            allExamples = new ArrayList<Object>();
            getFeatureElement().put("examples", allExamples);
        }
        return allExamples;
    }

    private List<Object> getSteps() {
        List<Object> steps = getFeatureElement().get("steps");
        if (steps == null) {
            steps = new ArrayList<Object>();
            getFeatureElement().put("steps", steps);
        }
        return steps;
    }

    private Map<Object,Object> getLastStep() {
        return (Map) getSteps().get(getSteps().size() - 1);
    }

    private List getEmbeddings() {
        List<Object> embeddings = (List<Object>) getLastStep().get("embeddings");
        if(embeddings == null) {
            embeddings = new ArrayList<Object>();
            getLastStep().put("embeddings", embeddings);
        }
        return embeddings;
    }
}
