/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.internal.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;


/**
 * @author Clément Fournier
 * @since 6.11.0
 */
public final class IteratorUtil {

    private static final int MATCH_ANY = 0;
    private static final int MATCH_ALL = 1;
    private static final int MATCH_NONE = 2;

    private IteratorUtil() {

    }

    public static <T> Iterator<T> takeWhile(Iterator<T> iter, Predicate<? super T> predicate) {
        return new AbstractIterator<T>() {
            @Override
            protected void computeNext() {
                T next = iter.next();
                if (predicate.test(next)) {
                    setNext(next);
                } else {
                    done();
                }
            }
        };
    }

    public static <T> Iterator<T> reverse(Iterator<T> it) {
        List<T> tmp = toList(it);
        Collections.reverse(tmp);
        return tmp.iterator();
    }

    public static <T, R> Iterator<R> flatMap(Iterator<? extends T> iter, Function<? super T, ? extends Iterator<? extends R>> f) {
        return new AbstractIterator<R>() {
            private Iterator<? extends R> current = null;

            @Override
            protected void computeNext() {
                if (current != null && current.hasNext()) {
                    setNext(current.next());
                } else {
                    while (iter.hasNext()) {
                        Iterator<? extends R> next = f.apply(iter.next());
                        if (next != null && next.hasNext()) {
                            current = next;
                            setNext(current.next());
                            return;
                        }
                    }
                    done();
                }
            }
        };
    }

    public static <T> Iterator<@NonNull T> filterNotNull(Iterator<? extends T> it) {
        return filter(it, Objects::nonNull);
    }

    public static <T, R> Iterator<@NonNull R> mapNotNull(Iterator<? extends T> it, Function<@NonNull ? super T, @Nullable ? extends R> mapper) {
        return new AbstractIterator<R>() {
            @Override
            protected void computeNext() {
                while (it.hasNext()) {
                    T next = it.next();
                    if (next != null) {
                        R map = mapper.apply(next);
                        if (map != null) {
                            setNext(map);
                            return;
                        }
                    }
                }
                done();
            }
        };
    }

    public static <T> Iterator<T> filter(Iterator<? extends T> it, Predicate<? super T> filter) {
        return new AbstractIterator<T>() {
            @Override
            protected void computeNext() {
                while (it.hasNext()) {
                    T next = it.next();
                    if (filter.test(next)) {
                        setNext(next);
                        return;
                    }
                }
                done();
            }
        };
    }

    public static <T> Iterator<T> peek(Iterator<? extends T> iter, Consumer<? super T> action) {
        return map(iter, it -> {
            action.accept(it);
            return it;
        });
    }

    public static <T, R> Iterator<R> map(Iterator<? extends T> iter, Function<? super T, ? extends R> mapper) {
        return new Iterator<R>() {
            @Override
            public boolean hasNext() {
                return iter.hasNext();
            }

            @Override
            public R next() {
                return mapper.apply(iter.next());
            }
        };
    }

    public static <T> Iterator<T> concat(Iterator<? extends T> as, Iterator<? extends T> bs) {
        return new Iterator<T>() {

            @Override
            public boolean hasNext() {
                return as.hasNext() || bs.hasNext();
            }

            @Override
            public T next() {
                return as.hasNext() ? as.next() : bs.next();
            }
        };
    }

    public static <T> Iterator<T> distinct(Iterator<? extends T> iter) {
        Set<T> seen = new HashSet<>();
        return filter(iter, seen::add);
    }

    public static <T> List<T> toList(Iterator<T> it) {
        List<T> list = new ArrayList<>();
        while (it.hasNext()) {
            list.add(it.next());
        }
        return list;
    }

    public static <T> Iterable<T> toIterable(final Iterator<T> it) {
        return () -> it;
    }

    /** Counts the items in this iterator, exhausting it. */
    public static int count(Iterator<?> it) {
        int count = 0;
        while (it.hasNext()) {
            it.next();
            count++;
        }
        return count;
    }

    public static <T> @Nullable T last(Iterator<T> iterator) {
        T next = null;
        while (iterator.hasNext()) {
            next = iterator.next();
        }
        return next;
    }

    /**
     * Returns the nth element of this iterator, or null if the iterator
     * is shorter.
     *
     * @throws IllegalArgumentException If n is negative
     */
    public static <T> @Nullable T getNth(Iterator<T> iterator, int n) {
        advance(iterator, n);
        return iterator.hasNext() ? iterator.next() : null;
    }

    /** Advance {@code n} times. */
    public static void advance(Iterator<?> iterator, int n) {
        AssertionUtil.requireNonNegative("n", n);

        while (n > 0 && iterator.hasNext()) {
            iterator.next();
            n--;
        }
    }

    /** Limit the number of elements yielded by this iterator to the given number. */
    public static <T> Iterator<T> take(Iterator<T> iterator, final int n) {
        AssertionUtil.requireNonNegative("n", n);
        if (n == 0) {
            return Collections.emptyIterator();
        }

        return new AbstractIterator<T>() {
            private int yielded = 0;

            @Override
            protected void computeNext() {
                if (yielded >= n || !iterator.hasNext()) {
                    done();
                } else {
                    setNext(iterator.next());
                }
                yielded++;
            }
        };
    }

    /** Produce an iterator whose first element is the nth element of the given source. */
    public static <T> Iterator<T> drop(Iterator<T> source, final int n) {
        AssertionUtil.requireNonNegative("n", n);
        if (n == 0) {
            return source;
        }

        return new AbstractIterator<T>() {
            private int yielded = 0;

            @Override
            protected void computeNext() {
                while (yielded++ < n && source.hasNext()) {
                    source.next();
                }

                if (!source.hasNext()) {
                    done();
                } else {
                    setNext(source.next());
                }
            }
        };
    }

    /**
     * Returns whether some element match the predicate. If empty then {@code false}
     * is returned.
     */
    public static <T> boolean anyMatch(Iterator<? extends T> iterator, Predicate<? super T> pred) {
        return matches(iterator, pred, MATCH_ANY);
    }

    /**
     * Returns whether all elements match the predicate. If empty then {@code true}
     * is returned.
     */
    public static <T> boolean allMatch(Iterator<? extends T> iterator, Predicate<? super T> pred) {
        return matches(iterator, pred, MATCH_ALL);
    }

    /**
     * Returns whether no elements match the predicate. If empty then {@code true}
     * is returned.
     */
    public static <T> boolean noneMatch(Iterator<? extends T> iterator, Predicate<? super T> pred) {
        return matches(iterator, pred, MATCH_NONE);
    }

    private static <T> boolean matches(Iterator<? extends T> iterator, Predicate<? super T> pred, int matchKind) {
        final boolean kindAny = matchKind == MATCH_ANY;
        final boolean kindAll = matchKind == MATCH_ALL;

        while (iterator.hasNext()) {
            final T value = iterator.next();
            final boolean match = pred.test(value);
            if (match ^ kindAll) { // xor
                return kindAny && match;
            }
        }
        return !kindAny;
    }

    public static <T> Iterator<T> singletonIterator(T value) {
        class SingletonIterator implements Iterator<T> {
            private boolean done;

            @Override
            public boolean hasNext() {
                return !done;
            }

            @Override
            public T next() {
                if (done) {
                    throw new NoSuchElementException();
                }
                done = true;
                return value;
            }

            @Override
            public void forEachRemaining(Consumer<? super T> action) {
                action.accept(value);
            }
        }

        return new SingletonIterator();
    }

    public static <T> Iterable<T> asReversed(final List<T> lst) {

        return () -> new Iterator<T>() {

            ListIterator<T> li = lst.listIterator(lst.size());


            @Override
            public boolean hasNext() {
                return li.hasPrevious();
            }


            @Override
            public T next() {
                return li.previous();
            }


            @Override
            public void remove() {
                li.remove();
            }
        };
    }

    private abstract static class AbstractIterator<T> implements Iterator<T> {

        private State state = State.NOT_READY;
        private T next = null;


        @Override
        public boolean hasNext() {
            switch (state) {
            case DONE:
                return false;
            case READY:
                return true;
            default:
                state = null;
                computeNext();
                if (state == null) {
                    throw new IllegalStateException("Should have called done or setNext");
                }
                return state == State.READY;
            }
        }

        @Override
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            state = State.NOT_READY;
            return next;
        }

        protected final void setNext(T t) {
            next = t;
            state = State.READY;
        }

        protected final void done() {
            state = State.DONE;
        }

        protected abstract void computeNext();

        enum State {
            READY, NOT_READY, DONE
        }

    }
}
