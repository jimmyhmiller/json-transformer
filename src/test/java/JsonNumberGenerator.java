import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.generator.java.lang.IntegerGenerator;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;

import javax.json.Json;
import javax.json.JsonNumber;

public class JsonNumberGenerator extends Generator<JsonNumber> {
    public JsonNumberGenerator() {
        super(JsonNumber.class);
    }

    @Override
    public JsonNumber generate(SourceOfRandomness random, GenerationStatus status) {
        return createJsonNumber(new IntegerGenerator().generate(random, status));
    }

    private static JsonNumber createJsonNumber(int n) {
        return Json.createObjectBuilder()
                .add("n", n)
                .build().getJsonNumber("n");
    }
}
