import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;

import javax.json.JsonObject;

public class JsonObjectGenerator extends Generator<JsonObject> {
    public JsonObjectGenerator() {
        super(JsonObject.class);
    }

    @Override
    public JsonObject generate(SourceOfRandomness random, GenerationStatus status) {
        return new JsonObjectBuilderGenerator().generate(random, status).build();
    }

}
