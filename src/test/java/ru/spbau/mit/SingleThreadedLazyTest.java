package ru.spbau.mit;

import ru.spbau.mit.utils.AbstractLazyFactory;
import ru.spbau.mit.utils.TestSupplier;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class SingleThreadedLazyTest {
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {(AbstractLazyFactory) LazyFactory::createSingleThreadedLazy},
                {(AbstractLazyFactory) LazyFactory::createSingletonLazy},
                {(AbstractLazyFactory) LazyFactory::createLockFreeLazy}
        });
    }

    private final AbstractLazyFactory factory;

    public SingleThreadedLazyTest(AbstractLazyFactory factory) {
        this.factory = factory;
    }

    private void test(TestSupplier supplier, Integer expected) {
        Lazy<Integer> comp = factory.createLazy(supplier);
        assertEquals(0, supplier.getCallsCount());
        assertEquals(expected, comp.get());
        assertEquals(1, supplier.getCallsCount());
        assertEquals(expected, comp.get());
        assertEquals(1, supplier.getCallsCount());
        assertEquals(expected, comp.get());
        assertEquals(1, supplier.getCallsCount());
    }

    @Test
    public void testComputation() {
        test(new TestSupplier(), 1);
    }

    @Test
    public void testNullFirst() {
        test(new TestSupplier(1), null);
    }
}
