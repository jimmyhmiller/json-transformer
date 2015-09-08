import com.pholser.junit.quickcheck.ForAll;
import com.pholser.junit.quickcheck.From;
import json.JsonTransformer;
import org.junit.contrib.theories.Theories;
import org.junit.contrib.theories.Theory;
import org.junit.runner.RunWith;

import javax.json.JsonObject;
import javax.json.JsonValue;
import java.util.function.Function;




// JsonTransformer really is a functor(ish thing). So the functor laws should hold for it.
// mapRecursive(identity) = identity
// mapRecursive(compose(f, g)) = compose(mapRecursive(f), mapRecursive(g))

@RunWith(Theories.class)
public class JsonTransformerTest {
    @Theory public void mapIdentityString(@ForAll @From(JsonValueGenerator.class) JsonValue s1) {
        Function<JsonValue, JsonValue> identity = (a -> a);
        assert(new JsonTransformer(s1).mapRecursive((String a) -> a).toJson().equals(identity.apply(s1)));
    }
    @Theory public void mapIdentityInteger(@ForAll @From(JsonValueGenerator.class) JsonValue s1) {
        Function<JsonValue, JsonValue> identity = (a -> a);
        assert(new JsonTransformer(s1).mapRecursive((Integer a) -> a).toJson().equals(identity.apply(s1)));
    }
    @Theory public void mapIdentityJsonObject(@ForAll @From(JsonValueGenerator.class) JsonValue s1) {
        Function<JsonValue, JsonValue> identity = (a -> a);
        assert(new JsonTransformer(s1).mapRecursive((JsonObject a) -> a).toJson().equals(identity.apply(s1)));
    }
    @Theory public void mapIdentityBoolean(@ForAll @From(JsonValueGenerator.class) JsonValue s1) {
        Function<JsonValue, JsonValue> identity = (a -> a);
        assert(new JsonTransformer(s1).mapRecursive((Boolean a) -> a).toJson().equals(identity.apply(s1)));
    }
    @Theory public void mapComposeInteger(@ForAll @From(JsonValueGenerator.class) JsonValue s1) {
        JsonTransformer.FunctionFromIntegerToObject f = (Integer i) -> i + 2;
        JsonTransformer.FunctionFromIntegerToObject g = (Integer i) -> i * 3;
        assert(new JsonTransformer(s1).mapRecursive(f).mapRecursive(g).toJson().equals(new JsonTransformer(s1).mapRecursive((Integer i) -> g.apply((Integer) f.apply(i))).toJson()));
    }
}
