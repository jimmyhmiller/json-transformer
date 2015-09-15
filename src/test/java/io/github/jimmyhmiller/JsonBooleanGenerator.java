import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;

import javax.json.JsonValue;

public class JsonBooleanGenerator extends Generator<JsonValue> {
    public JsonBooleanGenerator() {
        super(JsonValue.class);
    }

    @Override
    public JsonValue generate(SourceOfRandomness random, GenerationStatus status) {
        return createJsonBoolean(random.nextBoolean());
    }

    private static JsonValue createJsonBoolean(Boolean s) {
        if (s) {
            return JsonValue.TRUE;
        } else {
            return JsonValue.FALSE;
        }
    }
}




