package io.github.jimmyhmiller;


import javax.json.*;
import javax.json.stream.JsonGenerator;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class JsonTransformer {

    private final JsonValue json;
    private final List<Function<JsonValue, JsonValue>> fns;

    public JsonTransformer(JsonValue json) {
        this.json = json;
        this.fns = new ArrayList<>();
    }

    private JsonTransformer(JsonValue json, List<Function<JsonValue, JsonValue>> fns) {
        this.json = json;
        this.fns = fns;
    }


    //This is a work around due to "same erasure" problems in Java.
    //See: http://benjiweber.co.uk/blog/2015/02/20/work-around-java-same-erasure-errors-with-lambdas/

    public interface FunctionFromIntegerToObject extends Function<Integer, Object> {}
    public interface FunctionFromStringToObject extends Function<String, Object> {}
    public interface FunctionFromBooleanToObject extends Function<Boolean, Object> {}
    public interface FunctionFromJsonObjectToObject extends Function<JsonObject, Object> {}
    public interface FunctionFromJsonArrayToObject extends Function<JsonArray, Object> {}
    public interface FunctionFromJsonObjectBuilderToObject extends Function<JsonObjectBuilder, Object> {}
    public interface BiFunctionFromStringAndStringToObject extends BiFunction<String, String, Object> {}
    public interface BiFunctionFromStringAndNumberToObject extends BiFunction<String, Integer, Object> {}
    public interface BiFunctionFromStringAndBooleanToObject extends BiFunction<String, Boolean, Object> {}
    public interface BiFunctionFromStringAndJsonObjectToObject extends BiFunction<String, JsonObject, Object> {}
    public interface BiFunctionFromStringAndJsonArrayToObject extends BiFunction<String, JsonArray, Object> {}
    public interface BiFunctionFromStringAndJsonObjectBuilderToObject extends BiFunction<String, JsonObjectBuilder, Object> {}

    /*
    * ## The Heart
    * This is the fundamental method that drives the application.
    * This is the method that I am astonished doesn't exist in any Java json library.
    * It recurses the json tree apply our function to element in the tree.
    * One thing to not is that the function must be from type JsonValue -> JsonValue.
    * Almost every other part of this library is dedicated to converting other function types
    * into functions from JsonValue -> JsonValue.
    * */
    private static JsonValue mapRecursive(JsonValue json, Function<JsonValue, JsonValue> f) {
        JsonValue applied = f.apply(json);
        if (isObject(applied)) {
            JsonObjectBuilder builder = Json.createObjectBuilder();
            JsonObject jsonO = (JsonObject) applied;
            jsonO.forEach((k, v) -> builder.add(k, mapRecursive(v, f)));
            return builder.build();
        }
        else if (isArray(applied)) {
            JsonArrayBuilder builder = Json.createArrayBuilder();
            JsonArray jsonA = (JsonArray) applied;
            jsonA.forEach(v -> builder.add(mapRecursive(v, f)));
            return builder.build();
        }
        else if (isPrimitive(applied)) {
            return applied;
        }
        return applied;
    }

    public static JsonObject map(JsonObject json, BiFunction<String, JsonValue, JsonValue> f) {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        json.forEach((k, v) -> builder.add(k, f.apply(k, v)));
        return builder.build();
    }

    public static JsonArray map(JsonArray json, Function<JsonValue, JsonValue> f) {
        JsonArrayBuilder builder = Json.createArrayBuilder();
        json.forEach((v) -> builder.add(f.apply(v)));
        return builder.build();
    }

    public JsonTransformer map(BiFunctionFromStringAndStringToObject f) {
        return this.addMapObject(convertFunctionType(f, JsonTransformer::isString, JsonTransformer::valueToString));
    }

    public JsonTransformer map(BiFunctionFromStringAndNumberToObject f) {
        return this.addMapObject(convertFunctionType(f, JsonTransformer::isNumber, JsonTransformer::valueToInteger));
    }

    public JsonTransformer map(BiFunctionFromStringAndBooleanToObject f) {
        return this.addMapObject(convertFunctionType(f, JsonTransformer::isBool, JsonTransformer::isTrue));
    }

    public JsonTransformer map(BiFunctionFromStringAndJsonObjectToObject f) {
        return this.addMapObject(convertFunctionType(f, JsonTransformer::isObject, JsonTransformer::valueToObject));
    }

    public JsonTransformer map(BiFunctionFromStringAndJsonArrayToObject f) {
        return this.addMapObject(convertFunctionType(f, JsonTransformer::isArray, JsonTransformer::valueToArray));
    }

    public JsonTransformer map(BiFunctionFromStringAndJsonObjectBuilderToObject f) {
        return this.addMapObject(convertFunctionType(f, JsonTransformer::isObject, JsonTransformer::valueToObjectBuilder));
    }

    public JsonTransformer mapIf(BiFunction<String, String, Boolean> pred, BiFunctionFromStringAndStringToObject f, BiFunctionFromStringAndStringToObject e) {
        return this.map((String key, String value) -> {
            if (pred.apply(key, value)) {
                return f.apply(key, value);
            }
            return e.apply(key, value);
        });
    }

    public JsonTransformer mapIf(BiFunction<String, Integer, Boolean> pred, BiFunctionFromStringAndNumberToObject f, BiFunctionFromStringAndNumberToObject e) {
        return this.map((String key, Integer value) -> {
            if (pred.apply(key, value)) {
                return f.apply(key, value);
            }
            return e.apply(key, value);
        });
    }

    public JsonTransformer mapIf(BiFunction<String, Boolean, Boolean> pred, BiFunctionFromStringAndBooleanToObject f, BiFunctionFromStringAndBooleanToObject e) {
        return this.map((String key, Boolean value) -> {
            if (pred.apply(key, value)) {
                return f.apply(key, value);
            }
            return e.apply(key, value);
        });
    }

    public JsonTransformer mapIf(BiFunction<String, JsonObject, Boolean> pred, BiFunctionFromStringAndJsonObjectToObject f, BiFunctionFromStringAndJsonObjectToObject e) {
        return this.map((String key, JsonObject value) -> {
            if (pred.apply(key, value)) {
                return f.apply(key, value);
            }
            return e.apply(key, value);
        });
    }

    public JsonTransformer mapIf(BiFunction<String, JsonArray, Boolean> pred, BiFunctionFromStringAndJsonArrayToObject f, BiFunctionFromStringAndJsonArrayToObject e) {
        return this.map((String key, JsonArray value) -> {
            if (pred.apply(key, value)) {
                return f.apply(key, value);
            }
            return e.apply(key, value);
        });
    }

    public JsonTransformer mapIf(BiFunction<String, JsonObject, Boolean> pred, BiFunctionFromStringAndJsonObjectBuilderToObject f, BiFunctionFromStringAndJsonObjectBuilderToObject e) {
        return this.map((String key, JsonObjectBuilder value) -> {
            JsonObject json = value.build();
            if (pred.apply(key, json)) {
                return f.apply(key, jsonObjectToBuilder(json)); // after you build value. It is set to null. That's why we reconvert.
            }
            return e.apply(key, value);
        });
    }

    public JsonTransformer mapIf(BiFunction<String, String, Boolean> pred, BiFunctionFromStringAndStringToObject f) {
        return this.mapIf(pred, f, (a, b) -> b);
    }

    public JsonTransformer mapIf(BiFunction<String, Integer, Boolean> pred, BiFunctionFromStringAndNumberToObject f) {
        return this.mapIf(pred, f, (a, b) -> b);
    }

    public JsonTransformer mapIf(BiFunction<String, Boolean, Boolean> pred, BiFunctionFromStringAndBooleanToObject f) {
        return this.mapIf(pred, f, (a, b) -> b);
    }

    public JsonTransformer mapIf(BiFunction<String, JsonObject, Boolean> pred, BiFunctionFromStringAndJsonObjectToObject f) {
        return this.mapIf(pred, f, (a, b) -> b);
    }

    public JsonTransformer mapIf(BiFunction<String, JsonArray, Boolean> pred, BiFunctionFromStringAndJsonArrayToObject f) {
        return this.mapIf(pred, f, (a, b) -> b);
    }

    public JsonTransformer mapIf(BiFunction<String, JsonObject, Boolean> pred, BiFunctionFromStringAndJsonObjectBuilderToObject f) {
        return this.mapIf(pred, f, (a, b) -> b);
    }

    public JsonTransformer map(FunctionFromStringToObject f) {
        return this.addMapArray(convertFunctionType(f, JsonTransformer::isString, JsonTransformer::valueToString));
    }

    public JsonTransformer map(FunctionFromIntegerToObject f) {
        return this.addMapArray(convertFunctionType(f, JsonTransformer::isNumber, JsonTransformer::valueToInteger));
    }

    public JsonTransformer map(FunctionFromBooleanToObject f) {
        return this.addMapArray(convertFunctionType(f, JsonTransformer::isBool, JsonTransformer::isTrue));
    }

    public JsonTransformer map(FunctionFromJsonObjectToObject f) {
        return this.addMapArray(convertFunctionType(f, JsonTransformer::isObject, JsonTransformer::valueToObject));
    }

    public JsonTransformer map(FunctionFromJsonArrayToObject f) {
        return this.addMapArray(convertFunctionType(f, JsonTransformer::isArray, JsonTransformer::valueToArray));
    }

    public JsonTransformer map(FunctionFromJsonObjectBuilderToObject f) {
        return this.addMapArray(convertFunctionType(f, JsonTransformer::isObject, JsonTransformer::valueToObjectBuilder));
    }

    public JsonTransformer mapIf(Function<String, Boolean> pred, FunctionFromStringToObject f, FunctionFromStringToObject e) {
        return this.map((String key, String value) -> {
            if (pred.apply(value)) {
                return f.apply(value);
            }
            return e.apply(value);
        });
    }

    public JsonTransformer mapIf(Function<Integer, Boolean> pred, FunctionFromIntegerToObject f, FunctionFromIntegerToObject e) {
        return this.map((String key, Integer value) -> {
            if (pred.apply(value)) {
                return f.apply(value);
            }
            return e.apply(value);
        });
    }

    public JsonTransformer mapIf(Function<Boolean, Boolean> pred, FunctionFromBooleanToObject f, FunctionFromBooleanToObject e) {
        return this.map((String key, Boolean value) -> {
            if (pred.apply(value)) {
                return f.apply(value);
            }
            return e.apply(value);
        });
    }

    public JsonTransformer mapIf(Function<JsonObject, Boolean> pred, FunctionFromJsonObjectToObject f, FunctionFromJsonObjectToObject e) {
        return this.map((String key, JsonObject value) -> {
            if (pred.apply(value)) {
                return f.apply(value);
            }
            return e.apply(value);
        });
    }

    public JsonTransformer mapIf(Function<JsonArray, Boolean> pred, FunctionFromJsonArrayToObject f, FunctionFromJsonArrayToObject e) {
        return this.map((String key, JsonArray value) -> {
            if (pred.apply(value)) {
                return f.apply(value);
            }
            return e.apply(value);
        });
    }

    public JsonTransformer mapIf(Function<JsonObject, Boolean> pred, FunctionFromJsonObjectBuilderToObject f, FunctionFromJsonObjectBuilderToObject e) {
        return this.map((String key, JsonObjectBuilder value) -> {
            JsonObject json = value.build();
            if (pred.apply(json)) {
                return f.apply(jsonObjectToBuilder(json)); // after you build value. It is set to null. That's why we reconvert.
            }
            return e.apply(value);
        });
    }

    public JsonTransformer mapIf(Function<String, Boolean> pred, FunctionFromStringToObject f) {
        return this.mapIf(pred, f, a -> a);
    }

    public JsonTransformer mapIf(Function<Integer, Boolean> pred, FunctionFromIntegerToObject f) {
        return this.mapIf(pred, f, a -> a);
    }

    public JsonTransformer mapIf(Function<Boolean, Boolean> pred, FunctionFromBooleanToObject f) {
        return this.mapIf(pred, f, a -> a);
    }

    public JsonTransformer mapIf(Function<JsonObject, Boolean> pred, FunctionFromJsonObjectToObject f) {
        return this.mapIf(pred, f, a -> a);
    }

    public JsonTransformer mapIf(Function<JsonArray, Boolean> pred, FunctionFromJsonArrayToObject f) {
        return this.mapIf(pred, f, a -> a);
    }

    public JsonTransformer mapIf(Function<JsonObject, Boolean> pred, FunctionFromJsonObjectBuilderToObject f) {
        return this.mapIf(pred, f, a -> a);
    }




    /*
    * This builds up our future transformations so that we can lazily apply them.
    * TODO: Use immutable lists
    *
    * */
    private JsonTransformer addTransformation(Function<JsonValue, JsonValue> f) {
        fns.add(f);
        return new JsonTransformer(json, fns);
    }
    private JsonTransformer addMapR(Function<JsonValue, JsonValue> f) {
        return addTransformation((JsonValue j) -> mapRecursive(j, f));
    }
    private JsonTransformer addMapObject(BiFunction<String, JsonValue, JsonValue> f) {
        return addTransformation((JsonValue j) -> {
            if (isObject(j)) {
                return map(valueToObject(j), f);
            }
            return j;
        });
    }

    private JsonTransformer addMapArray(Function<JsonValue, JsonValue> f) {
        return addTransformation((JsonValue j) -> {
            if (isArray(j)) {
                return map(valueToArray(j), f);
            }
            return j;
        });
    }

    public JsonTransformer mapRecursive(FunctionFromIntegerToObject f) {
        return this.addMapR(fromInteger(f));
    }
    public JsonTransformer mapRecursive(FunctionFromStringToObject f) {
        return this.addMapR(fromString(f));
    }
    public JsonTransformer mapRecursive(FunctionFromBooleanToObject f) {
        return this.addMapR(fromBoolean(f));
    }
    public JsonTransformer mapRecursive(FunctionFromJsonObjectToObject f) {
        return this.addMapR(fromJsonObject(f));
    }
    public JsonTransformer mapRecursive(FunctionFromJsonObjectBuilderToObject f) {
        return this.addMapR(fromJsonObjectBuilder(f));
    }
    public JsonTransformer mapRecursive(FunctionFromJsonArrayToObject f) {
        return this.addMapR(fromJsonArray(f));
    }

    //I would love to find a way to simplify these. But every time I do the types don't match.
    //The hack I had to do with the interfaces throws it off everytime.
    public JsonTransformer mapRecursiveIf(Function<Integer, Boolean> pred, FunctionFromIntegerToObject f, FunctionFromIntegerToObject e) {
        return this.mapRecursive((Integer i) -> {
            if (pred.apply(i)) {
                return f.apply(i);
            }
            return e.apply(i);
        });
    }
    public JsonTransformer mapRecursiveIf(Function<String, Boolean> pred, FunctionFromStringToObject f, FunctionFromStringToObject e) {
        return this.mapRecursive((String s) -> {
            if (pred.apply(s)) {
                return f.apply(s);
            }
            return e.apply(s);
        });
    }
    public JsonTransformer mapRecursiveIf(Function<Boolean, Boolean> pred, FunctionFromBooleanToObject f, FunctionFromBooleanToObject e) {
        return this.mapRecursive((Boolean b) -> {
            if (pred.apply(b)) {
                return f.apply(b);
            }
            return e.apply(b);
        });
    }
    public JsonTransformer mapRecursiveIf(Function<JsonObject, Boolean> pred, FunctionFromJsonObjectToObject f, FunctionFromJsonObjectToObject e) {
        return this.mapRecursive((JsonObject o) -> {
            if (pred.apply(o)) {
                return f.apply(o);
            }
            return e.apply(o);
        });
    }
    public JsonTransformer mapRecursiveIf(Function<JsonObject, Boolean> pred, FunctionFromJsonObjectBuilderToObject f, FunctionFromJsonObjectBuilderToObject e) {
        return this.mapRecursive((JsonObjectBuilder o) -> {
            JsonObject json = o.build();
            if (pred.apply(json)) {
                return f.apply(jsonObjectToBuilder(json)); // after you build value. It is set to null. That's why we reconvert.
            }
            return e.apply(o);
        });
    }
    public JsonTransformer mapRecursiveIf(Function<JsonArray, Boolean> pred, FunctionFromJsonArrayToObject f, FunctionFromJsonArrayToObject e) {
        return this.mapRecursive((JsonArray a) -> {
            if (pred.apply(a)) {
                return f.apply(a);
            }
            return e.apply(a);
        });
    }

    public JsonTransformer mapRecursiveIf(Function<Integer, Boolean> pred, FunctionFromIntegerToObject f) {
        return mapRecursiveIf(pred, f, a -> a);
    }
    public JsonTransformer mapRecursiveIf(Function<String, Boolean> pred, FunctionFromStringToObject f) {
        return mapRecursiveIf(pred, f, a -> a);
    }

    public JsonTransformer mapRecursiveIf(Function<Boolean, Boolean> pred, FunctionFromBooleanToObject f) {
        return mapRecursiveIf(pred, f, a -> a);
    }

    public JsonTransformer mapRecursiveIf(Function<JsonObject, Boolean> pred, FunctionFromJsonObjectToObject f) {
        return mapRecursiveIf(pred, f, a -> a);
    }

    public JsonTransformer mapRecursiveIf(Function<JsonArray, Boolean> pred, FunctionFromJsonArrayToObject f) {
        return mapRecursiveIf(pred, f, a -> a);
    }
    public JsonTransformer mapRecursiveIf(Function<JsonObject, Boolean> pred, FunctionFromJsonObjectBuilderToObject f) {
        return mapRecursiveIf(pred, f, a -> a);
    }


    //Types won't unify if made generic because JsonObjectBuilder::add is not generic.
    public JsonTransformer add(String key, String value) {
        return addTransformation((JsonValue j) -> {
            if(isObject(j)) {
                JsonObject jO = (JsonObject) j;
                return jsonObjectToBuilder(jO).add(key, value).build();
            }
            return j;
        });
    }

    public JsonTransformer add(String key, Integer value) {
        return addTransformation((JsonValue j) -> {
            if(isObject(j)) {
                JsonObject jO = (JsonObject) j;
                return jsonObjectToBuilder(jO).add(key, value).build();
            }
            return j;
        });
    }

    public JsonTransformer add(String key, Boolean value) {
        return addTransformation((JsonValue j) -> {
            if(isObject(j)) {
                JsonObject jO = (JsonObject) j;
                return jsonObjectToBuilder(jO).add(key, value).build();
            }
            return j;
        });
    }

    public JsonTransformer add(String key, JsonObjectBuilder value) {
        return addTransformation((JsonValue j) -> {
            if(isObject(j)) {
                JsonObject jO = (JsonObject) j;
                return jsonObjectToBuilder(jO).add(key, value).build();
            }
            return j;
        });
    }

    public JsonTransformer add(String key, JsonArray value) {
        return addTransformation((JsonValue j) -> {
            if(isObject(j)) {
                JsonObject jO = (JsonObject) j;
                return jsonObjectToBuilder(jO).add(key, value).build();
            }
            return j;
        });
    }



    public JsonTransformer print() {
        System.out.println(this.toJson());
        return this;
    }

    public JsonTransformer pprint() {
        StringWriter sw = new StringWriter();

        JsonReader jr = Json.createReader(new StringReader(this.toJson().toString()));
        JsonObject jobj = jr.readObject();

        Map<String, Object> properties = new HashMap<>(1);
        properties.put(JsonGenerator.PRETTY_PRINTING, true);
        JsonWriterFactory writerFactory = Json.createWriterFactory(properties);
        JsonWriter jsonWriter = writerFactory.createWriter(sw);
        jsonWriter.writeObject(jobj);
        jsonWriter.close();

        String prettyPrinted = sw.toString();
        System.out.println(prettyPrinted);
        return this;
    }
    public JsonValue toJson() {
        JsonValue result = this.json;
        for (Function<JsonValue, JsonValue> fn : fns) {
            result = fn.apply(result);
        }
        return result;
    }

    private static <T> Function<JsonValue, JsonValue> convertFunctionType(Function<T, Object> f,
                                                                          Function<JsonValue, Boolean> test,
                                                                          Function<JsonValue, T> converter) {
        return (JsonValue j) -> {
            if (test.apply(j)) {
                return coerceTypes(f.apply(converter.apply(j)));
            }
            return j;
        };
    }
    private static <T> BiFunction<String, JsonValue, JsonValue> convertFunctionType(BiFunction<String, T, Object> f,
                                                                          Function<JsonValue, Boolean> test,
                                                                          Function<JsonValue, T> converter) {
        return (String key, JsonValue j) -> {
            if (test.apply(j)) {
                return coerceTypes(f.apply(key, converter.apply(j)));
            }
            return j;
        };
    }


    private static Function<JsonValue, JsonValue> fromString(FunctionFromStringToObject f) {
        return convertFunctionType(f, JsonTransformer::isString, JsonTransformer::valueToString);
    }
    private static Function<JsonValue, JsonValue> fromBoolean(FunctionFromBooleanToObject f) {
        return convertFunctionType(f, JsonTransformer::isBool, JsonTransformer::isTrue);
    }
    private static Function<JsonValue, JsonValue> fromJsonObject(FunctionFromJsonObjectToObject f) {
        return convertFunctionType(f, JsonTransformer::isObject, JsonTransformer::valueToObject);
    }
    private static Function<JsonValue, JsonValue> fromJsonObjectBuilder(FunctionFromJsonObjectBuilderToObject f) {
        return convertFunctionType(f, JsonTransformer::isObject, JsonTransformer::valueToObjectBuilder);
    }
    private static Function<JsonValue, JsonValue> fromInteger(FunctionFromIntegerToObject f) {
        return convertFunctionType(f, JsonTransformer::isNumber, JsonTransformer::valueToInteger);
    }
    private  static Function<JsonValue, JsonValue> fromJsonArray(FunctionFromJsonArrayToObject f) {
        return convertFunctionType(f, JsonTransformer::isArray, JsonTransformer::valueToArray);
    }


    private static Integer valueToInteger(JsonValue json) {
        return ((JsonNumber) json).intValue();
    }

    private static String valueToString(JsonValue json) {
        return ((JsonString) json).getString();
    }

    private static JsonObject valueToObject(JsonValue json) {
        return (JsonObject) json;
    }

    private static JsonObjectBuilder valueToObjectBuilder(JsonValue json) {
        return jsonObjectToBuilder(((JsonObject) json));
    }

    private static JsonArray valueToArray(JsonValue json) {
        return (JsonArray) json;
    }

    public static Boolean isObject(JsonValue json) {
        return json.getValueType() == JsonValue.ValueType.OBJECT;
    }

    private static Boolean isArray(JsonValue json) {
        return json.getValueType() == JsonValue.ValueType.ARRAY;
    }

    private static Boolean isString(JsonValue json) {
        return json.getValueType() == JsonValue.ValueType.STRING;
    }

    private static Boolean isNumber(JsonValue json) {
        return json.getValueType() == JsonValue.ValueType.NUMBER;
    }

    private static Boolean isNull(JsonValue json) {
        return json.getValueType() == JsonValue.ValueType.NULL;
    }

    private static Boolean isTrue(JsonValue json) {
        return json.getValueType() == JsonValue.ValueType.TRUE;
    }

    private static Boolean isFalse(JsonValue json) {
        return json.getValueType() == JsonValue.ValueType.FALSE;
    }

    private static Boolean isBool(JsonValue json) {
        return isTrue(json) || isFalse(json);
    }

    private static Boolean isPrimitive(JsonValue json) {
        return isBool(json) || isNull(json) || isString(json) || isNumber(json);
    }

    public static JsonValue coerceTypes(Object o) throws RuntimeException {
        if (o == null) {
            return JsonValue.NULL;
        }
        try {
            return ((JsonObjectBuilder) o).build();
        }
        catch(ClassCastException ignored) {}
        try {
            return ((JsonArrayBuilder) o).build();
        }
        catch(ClassCastException ignored) {}
        try {
            return (JsonValue) o;
        }
        catch(ClassCastException ignored) {}
        try {
            return createJsonString((String) o);
        }
        catch(ClassCastException ignored) {}
        try {
            return createJsonNumber((Integer) o);
        }
        catch (ClassCastException ignored) {}
        try {
            return createJsonBoolean((Boolean) o);
        }
        catch (ClassCastException ignored) {}
        try {
            return ((JsonTransformer) o).toJson();
        }
        catch (ClassCastException ignored) {}
        throw new RuntimeException("There is some magic here. Because Java lacks union types, I have to coerce types." +
                "You need to return one of the following types for this to work. String, Boolean, Integer, JsonTransformer, JsonValue, JsonArrayBuilder, or JsonObjectBuilder");
    }



    public static JsonObjectBuilder jsonObjectToBuilder(JsonObject jo) {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        jo.forEach(builder::add);
        return builder;
    }


    private static JsonNumber createJsonNumber(int n) {
        return Json.createObjectBuilder()
                .add("n", n)
                .build().getJsonNumber("n");
    }

    private static JsonString createJsonString(String s) {
        return Json.createObjectBuilder()
                .add("s", s)
                .build().getJsonString("s");
    }

    private static JsonValue createJsonBoolean(Boolean s) {
        if (s) {
            return JsonValue.TRUE;
        } else {
            return JsonValue.FALSE;
        }
    }
}
