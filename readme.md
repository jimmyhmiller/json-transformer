#Json Transformer

A clean functional way to transform JSON in Java 8.

```java
JsonValue json = Json.createObjectBuilder()
        .add("test", "change me")
        .add("add2", 3)
        .add("add2ToAll", Json.createArrayBuilder().add(2).add(3).add(4))
        .add("nested", Json.createObjectBuilder()
            .add("test2", "change me"))
        .build();

JsonValue transformed = new JsonTransformer(json)
        .mapRIf((String s) -> s.equals("change me"), (String s) -> "changed")
        .mapIf((String key, Integer i) -> key.equals("add2"), (String key, Integer i) -> i + 2)
        .mapR((JsonArray a) -> new JsonTransformer(a).map((Integer i) -> i + 2).toJson())
        .pprint()
        .toJson();
 
/* console output
{
    "test":"changed",
    "add2":5,
    "add2ToAll":[
        4,
        5,
        6
    ],
    "nested":{
        "test2":"changed"
    }
}
*/
```

##Installation
Add the following to your build.gradle
```groovy
repositories {
    mavenCentral()
    maven {
        url "https://oss.sonatype.org/content/repositories/snapshots/"
    }

}

dependencies {
    compile 'io.github.jimmyhmiller:json-transformer:0.1.0-SNAPSHOT'
}
```



##Motivation
Performing transformations to JSON as JSON in Java is typically a pain. Lots of type checking and casting typically occurs. No libraries have builtin support for structural recursion. Code becomes increasingly imperative. 

JsonTransformer lets you simple declare your transformation using Map and MapRecursive. It also gets rid of (hides) all the type conversion you need to do by simply allowing you to specify your type in the lambda expression. 

##Limitations
JsonTransformer is still in the earlier stages and as such is not feature complete nor provides rich documentation. But what has been released is a useable core. Currently map is the only function type offered. Filter, remove, and possibly reduce are planned, though what their behavior out to be exactly still needs to be figured out.

Type information is a little wonky when writing your lambdas. This is due to a required work around for Java's "same erasure" issue. You will notice types like "FunctionFromStringToObject", this quite literally means, "Function<String, Object>" but we need this dummy value in order to appease Java. This means also that you must specify your types explicitly in your lambdas.

##Planned Features

* Add filter and filterRecursive
* Add reduce and reduceRecursive
* Add full JavaDoc
* Add transform (this will be a method that takes another JSON transformer)
* Add more complete testing suite