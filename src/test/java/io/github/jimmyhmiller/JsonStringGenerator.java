import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;

import javax.json.Json;
import javax.json.JsonString;

public class JsonStringGenerator extends Generator<JsonString> {
    public JsonStringGenerator() {
        super(JsonString.class);
    }

    @Override
    public JsonString generate(SourceOfRandomness random, GenerationStatus status) {
        return createJsonString(new ReadableStringGenerator().generate(random, status));
    }

    private static JsonString createJsonString(String s) {
        return Json.createObjectBuilder()
                .add("s", s)
                .build().getJsonString("s");
    }
}
