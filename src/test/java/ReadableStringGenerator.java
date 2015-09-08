import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;

public class ReadableStringGenerator extends Generator<String> {
    public ReadableStringGenerator() {
        super(String.class);
    }

    @Override
    public String generate(SourceOfRandomness random, GenerationStatus status) {
        return generateRandomWords(random);
    }

    public static String generateRandomWords(SourceOfRandomness random) {
        char[] word = new char[random.nextInt(3, 20)];
        for (int j = 0; j < word.length; j++) {
            word[j] = (char) ('a' + random.nextInt(26));
        }
        return new String(word);
    }

}
