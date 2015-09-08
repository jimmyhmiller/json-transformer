import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.generator.java.lang.IntegerGenerator;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

public class JsonObjectBuilderGenerator extends Generator<JsonObjectBuilder> {

    private Integer depth = 0;

    public JsonObjectBuilderGenerator() {
        super(JsonObjectBuilder.class);
    }

    public JsonObjectBuilderGenerator(Integer depth) {
        super(JsonObjectBuilder.class);
        this.depth = depth;
    }

    @Override
    public JsonObjectBuilder generate(SourceOfRandomness random, GenerationStatus status) {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        while (true) {
            Integer pickType = random.nextInt(0,5);
            Integer shouldEnd = random.nextInt(0,10);
            if (shouldEnd == 10 || depth > 5) {
                return builder;
            }
            if (pickType == 0) {
                builder.add(new ReadableStringGenerator().generate(random, status), new ReadableStringGenerator().generate(random, status));
            } else if (pickType == 1) {
                builder.add(new ReadableStringGenerator().generate(random, status), new IntegerGenerator().generate(random, status));
            } else if (pickType == 2) {
                builder.add(new ReadableStringGenerator().generate(random, status), random.nextBoolean());
            } else if (pickType == 3) {
                builder.add(new ReadableStringGenerator().generate(random, status), new JsonObjectBuilderGenerator(depth + 1).generate(random, status));
            }
            else if (pickType == 4) {
                builder.add(new ReadableStringGenerator().generate(random, status), new JsonArrayBuilderGenerator(depth + 1).generate(random, status));
            }
            else if (pickType == 5) {
                builder.add(new ReadableStringGenerator().generate(random, status), JsonValue.NULL);
            }
        }




    }

}
