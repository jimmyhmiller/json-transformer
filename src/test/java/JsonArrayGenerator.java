import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;

import javax.json.JsonArray;

public class JsonArrayGenerator extends Generator<JsonArray> {
    public JsonArrayGenerator() {
        super(JsonArray.class);
    }

    @Override
    public JsonArray generate(SourceOfRandomness random, GenerationStatus status) {
        return new JsonArrayBuilderGenerator().generate(random, status).build();
    }

}
