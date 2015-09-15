import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;

import javax.json.JsonValue;

public class JsonValueGenerator extends Generator<JsonValue> {
    public JsonValueGenerator() {
        super(JsonValue.class);
    }

    @Override
    public JsonValue generate(SourceOfRandomness random, GenerationStatus status) {
        Integer pickType = random.nextInt(0, 5);
        if (pickType == 0) {
            return new JsonNumberGenerator().generate(random, status);
        } else if (pickType == 1) {
            return new JsonStringGenerator().generate(random, status);
        } else if (pickType == 2) {
            return new JsonBooleanGenerator().generate(random, status);
        } else if (pickType == 3) {
            return new JsonObjectGenerator().generate(random, status);
        } else if (pickType == 4) {
            return new JsonArrayGenerator().generate(random, status);
        }
        return JsonValue.NULL;
    }

}
