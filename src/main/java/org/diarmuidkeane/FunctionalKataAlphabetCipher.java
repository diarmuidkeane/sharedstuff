package org.diarmuidkeane;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.*;

/**
 * Created by diarmuidkeane on 28/09/2016.
 */
public class FunctionalKataAlphabetCipher {
    final static int cipherPeriod = 26;
    final static String keyword="scones";

    final static String testMessage ="meetmebythetree";
    final static String encryptedTestMessage ="egsgqwtahuiljgs";

    private char int2char(int i){
        return (char)((int)'a'+i);
    }

    private int normalize(int charValue){
        return charValue-(int)'a';
    }
    
    private int char2int(char a){
        return normalize((int)a);
    }

    /**
     * generates an unevaluated cyclical infinite stream of the characters of the string repeated infinitely
     * ( or as long as you want them )
     */
    private Stream<Character> string2LoopingStream(String string){
        final int stringLength = string.length();

        return Stream.generate(new Supplier<Character>() {
            int index=0;
            @Override
            public Character get() {
                return string.charAt(index++%stringLength);
            }
        });
    }

    private String encode(String input){
        return zipStreams(input , (x, y) -> (x+y)% cipherPeriod).map(Object::toString).collect(Collectors.joining(""));
    }

    private String decode(String input){
        return zipStreams(input,(x, y) -> (-x+y+cipherPeriod)% cipherPeriod).map(Object::toString).collect(Collectors.joining(""));
    }

    private Stream<Character> zipStreams(String message, BiFunction<Integer,Integer,Integer> lambda) {
        final IntStream keyStream = string2LoopingStream(keyword).map(this::char2int).mapToInt(Integer::intValue);
        final IntStream messageStream = message.chars().map(this::normalize);
        final Stream<Character> outputStream = zip(keyStream, messageStream, lambda).map(this::int2char);
        return outputStream;
    }

    public static void main(String[] args) {
        FunctionalKataAlphabetCipher fk = new FunctionalKataAlphabetCipher();
        System.out.println("encoding of "+ testMessage + " is "+fk.encode(testMessage));
        System.out.println("decoding of "+ encryptedTestMessage + " is "+fk.decode(encryptedTestMessage));

    }

    /**
     * zip for Java8 based on Jubobs' solution to stack overflow question
     * http://stackoverflow.com/questions/17640754/zipping-streams-using-jdk8-with-lambda-java-util-stream-streams-zip
     */
    private <A, B, C> Stream<Integer> zip(IntStream a,
                                  IntStream b,
                                  BiFunction<? super Integer, ? super Integer, ? extends Integer> zipper) {
        Spliterator<? extends Integer> aSpliterator = Objects.requireNonNull(a).spliterator();
        Spliterator<? extends Integer> bSpliterator = Objects.requireNonNull(b).spliterator();

        // Zipping looses DISTINCT and SORTED characteristics
        int characteristics = aSpliterator.characteristics() & bSpliterator.characteristics() &
                ~(Spliterator.DISTINCT | Spliterator.SORTED);

        long zipSize = ((characteristics & Spliterator.SIZED) != 0)
                ? Math.min(aSpliterator.getExactSizeIfKnown(), bSpliterator.getExactSizeIfKnown())
                : -1;

        Iterator<Integer> aIterator = Spliterators.iterator(aSpliterator);
        Iterator<Integer> bIterator = Spliterators.iterator(bSpliterator);
        Iterator<Integer> cIterator = new Iterator<Integer>() {
            @Override
            public boolean hasNext() {
                return aIterator.hasNext() && bIterator.hasNext();
            }

            @Override
            public Integer next() {
                return zipper.apply(aIterator.next(), bIterator.next());
            }
        };

        Spliterator<Integer> split = Spliterators.spliterator(cIterator, zipSize, characteristics);
        return StreamSupport.stream(split, false);
    }
}
