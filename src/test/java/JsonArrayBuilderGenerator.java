import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.generator.java.lang.IntegerGenerator;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;

import javax.json.Json;
import javax.json.JsonArrayBuilder;

public class JsonArrayBuilderGenerator extends Generator<JsonArrayBuilder> {

    private Integer depth = 0;

    public JsonArrayBuilderGenerator() {
        super(JsonArrayBuilder.class);
    }

    public JsonArrayBuilderGenerator(Integer depth) {
        super(JsonArrayBuilder.class);
        this.depth = depth;
    }

    @Override
    public JsonArrayBuilder generate(SourceOfRandomness random, GenerationStatus status) {
        JsonArrayBuilder builder = Json.createArrayBuilder();
        while (true) {
            Integer pickType = random.nextInt(0,5);
            Integer shouldEnd = random.nextInt(0,10);
            if (shouldEnd == 10 || depth > 5) {
                return builder;
            }
            if (pickType == 0) {
                builder.add(new ReadableStringGenerator().generate(random, status));
            } else if (pickType == 1) {
                builder.add(new IntegerGenerator().generate(random, status));
            } else if (pickType == 2) {
                builder.add( random.nextBoolean());
            } else if (pickType == 3) {
                builder.add(new JsonObjectBuilderGenerator(depth + 1).generate(random, status));
            }
            else if (pickType == 4) {
                builder.add(new JsonArrayBuilderGenerator(depth + 1).generate(random, status));
            }
        }




    }

}
