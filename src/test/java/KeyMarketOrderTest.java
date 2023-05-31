import com.worldplugins.vip.database.market.SellingKey;
import com.worldplugins.vip.database.player.model.VipType;
import com.worldplugins.vip.view.data.KeyMarketOrder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class KeyMarketOrderTest {
    @Test
    @DisplayName("Checks if the comparator sort the way it's expected, by their type.")
    void correctComparator() {
        final Collection<SellingKey> keys = createKeys();

        assertSortedEquals(keys, KeyMarketOrder.NONE, "1", "2", "3", "4");
        assertSortedEquals(keys, KeyMarketOrder.BETTER_TYPE, "2", "1", "3", "4");
        assertSortedEquals(keys, KeyMarketOrder.WORST_TYPE, "4", "3", "1", "2");
        assertSortedEquals(keys, KeyMarketOrder.EXPENSIVE, "1", "3", "2", "4");
        assertSortedEquals(keys, KeyMarketOrder.CHEAPER, "4", "2", "3", "1");
        assertSortedEquals(keys, KeyMarketOrder.MORE_USAGES, "4", "2", "3", "1");
        assertSortedEquals(keys, KeyMarketOrder.LESS_USAGES, "1", "3", "2", "4");
        assertSortedEquals(keys, KeyMarketOrder.LONGER_DURATION, "3", "1", "2", "4");
        assertSortedEquals(keys, KeyMarketOrder.SHORTER_DURATION, "4", "2", "1", "3");
        assertSortedEquals(keys, KeyMarketOrder.OLDER_POST, "2", "3", "1", "4");
        assertSortedEquals(keys, KeyMarketOrder.RECENT_POST, "4", "1", "3", "2");
    }

    void assertSortedEquals(Collection<SellingKey> keys, KeyMarketOrder order, String... codes) {
        assertTrue(
            sortedEquals(keys, order, codes),
            "The " + order.name() + " comparator does not work as expected."
        );
    }

    boolean sortedEquals(Collection<SellingKey> keys, KeyMarketOrder order, String... codes) {
        final Collection<SellingKey> sorted = keys.stream()
            .sorted(order.comparator())
            .collect(Collectors.toList());
        final Iterator<SellingKey> keyIterator = sorted.iterator();

        for (String code : codes) {
            if (!code.equals(keyIterator.next().code())) {
                return false;
            }
        }

        return true;
    }

    Collection<SellingKey> createKeys() {
        return Arrays.asList(
            createKey("1", 30d, 4, VipType.SERVER, 84600, 1, 60),
            createKey("2", 5d, 5, VipType.BASIC, 60, 4, 120),
            createKey("3", 15d, 1, VipType.PERMANENT, 3600, 2, 110),
            createKey("4", 4d, 0, VipType.ONLINE, 40, 5, 30)
        );
    }

    SellingKey createKey(
        String code,
        double price,
        int id,
        VipType type,
        int duration,
        int usages,
        int postSecondAgo
    ) {
        final UUID sellerId = UUID.randomUUID();
        final long postTimestamp = System.nanoTime() - TimeUnit.SECONDS.toNanos(postSecondAgo);
        return new SellingKey(code, sellerId, price, (byte) id, type, duration, (short) usages, postTimestamp);
    }
}
