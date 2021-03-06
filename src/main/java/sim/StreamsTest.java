package sim;

import com.google.common.base.Preconditions;
import dto.Cancellation;
import dto.Placement;
import dto.Side;
import state.Limit;
import state.LimitOrderBook;

import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;


/**]
 * Sandbox for simulation
 */
public class StreamsTest {

    private static final Random RND = new Random();
    private static int[] nPlacementPicker = {4, 8, 16, 32, 64}; // Mesh to pick n of placements per limit from;
    private static int[] vPlacementPicker = {128, 256, 512, 1024, 2048}; // Mesh to pick volume of placements from;
    private static final LimitOrderBook BOOK = getRandomizedOrderBook(50, 10, 1000);

    static LimitOrderBook getRandomizedOrderBook(int limitsPerSide, int tickSize, long mid){
        Preconditions.checkState(mid - limitsPerSide * tickSize >= 0, "Cannot have negative prices");
        LimitOrderBook limitOrderBook = LimitOrderBook.empty();
        long midTick = mid * tickSize;
        LongStream.range(mid - limitsPerSide, mid + limitsPerSide)
                .forEach(price -> {
                    int nps = nPlacementPicker[RND.nextInt(nPlacementPicker.length)];
                    long p = price * tickSize;
                    if(p < midTick){
                        IntStream.range(0, nps).forEach(i -> {
                            int amount = vPlacementPicker[RND.nextInt(vPlacementPicker.length)];
                            Placement placement = new Placement(Side.BID, p, amount);
                            limitOrderBook.place(placement);
                        });} else {
                        IntStream.range(0, nps).forEach(i -> {
                            int amount = vPlacementPicker[RND.nextInt(vPlacementPicker.length)];
                            Placement placement = new Placement(Side.OFFER, p, amount);
                            limitOrderBook.place(placement);
                        });
                    }
                });
        return limitOrderBook;
    }

    static Stream<Placement> placementStream(int limitsPerSide, int tickSize, long mid, double probMkt){
        long midTick = mid * tickSize;
        return RND.longs(mid - limitsPerSide, mid + limitsPerSide)
                .mapToObj(price -> {
                    long p = price * tickSize;
                    int amount = vPlacementPicker[RND.nextInt(vPlacementPicker.length)];
                    if(Math.random() <= 1 - probMkt){
                        if(p < midTick){
                            return new Placement(Side.BID, p, amount);
                        } else {
                            Limit limit = new Limit(Side.OFFER, p);
                            return new Placement(Side.OFFER, p, amount);
                        }
                    } else {
                        //Issue market order
                        if(p < midTick){
                            return new Placement(Side.OFFER, midTick - tickSize, amount);
                        } else {
                            return new Placement(Side.BID,  midTick + tickSize, amount);
                        }
                    }
                });
    }

    static Stream<Cancellation> cancellationStream(int limitsPerSide, int tickSize, long mid, double probCncl){
        long midTick = mid * tickSize;
        return RND.longs(mid - limitsPerSide, mid + limitsPerSide)
                .mapToObj(price -> {
                   long p = price * tickSize;
                    int amount = vPlacementPicker[RND.nextInt(vPlacementPicker.length)];
                    if(Math.random() <= 1 - probCncl) {
                        return null;
                    } else {
                        return null;
                    }
                });
    }

    public static void main(String[] args){
        placementStream(50, 10, 1000, 0.35)
                .forEach(p -> {
                    BOOK.place(p);
                    System.out.println(BOOK.bestBidOffer());
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
    }
}
