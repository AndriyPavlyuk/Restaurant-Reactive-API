package it.discovery.rxjava3;

import io.reactivex.rxjava3.core.Observable;

public class RxJavaStarter {
    public static void main(String[] args) {
//        Observable.range('a', 'z' - 'a' + 1)
//        .map(Character::toChars)
//        .subscribe(System.out::println);
//        Observable.generate(() -> 'a',
//                (v, emitter) -> {
//                    emitter.onNext(v);
//                    if(v < 'z') {
//                        v++;
//                    } else {
//                        emitter.onComplete();
//                    }
//                    return v;
//                }).subscribe(System.out::println);
        Observable.just('i', 't')
                .map(String::valueOf)
                .reduce("", (s1, s2) -> s1 + s2)
                .subscribe(System.out::println);
    }
}
