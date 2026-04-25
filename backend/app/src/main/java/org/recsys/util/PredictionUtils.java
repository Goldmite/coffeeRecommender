package org.recsys.util;

public class PredictionUtils {

    public static final long FOUR_MONTHS_SECONDS = 10368000; // bin period
    public static final float SECONDS_IN_DAY = 86400.0f;
    public static final int TOTAL_BINS = 6; // 2 years / 4 months

    /**
     * <p>
     * Rui = μ + Bu(t) + Bi(t) + (Pu · Qi)
     * </p>
     * <P>
     * Bu(t) = Bu + Au * dev_u
     * </p>
     * <p>
     * Bi(t) = Bi + Bi_bin
     * </p>
     * 
     * @param mean   - μ
     * @param Bu     - user bias
     * @param Au     - user alhpa
     * @param dev_u
     * @param Bi
     * @param Bi_bin
     * @param PuQi   - dot product
     * @return Rui - score prediction
     */
    public static float calculatePrediction(float mean, float Bu, float Au, float dev_u, float Bi, float Bi_bin,
            float PuQi) {
        return mean + (Bu + Au * dev_u) + (Bi + Bi_bin) + PuQi;
    }

    /**
     * Calculates the continuous drift deviation.
     * Uses days as the unit to prevent gradient explosion.
     * 
     * @param t    - timestamp
     * @param Tu   - user timestamp mean
     * @param beta - constant time exponent
     * @return user deviation
     */
    public static float calculateUserDev(long t, float Tu, double beta) {
        float timeDiffDays = (t - Tu) / SECONDS_IN_DAY;
        return (float) (Math.signum(timeDiffDays) * Math.pow(Math.abs(timeDiffDays), beta));
    }

    /**
     * Maps a timestamp to a specific discrete bin index.
     */
    public static int calculateBinIndex(long timestamp, long minTimestamp) {
        int binIdx = (int) ((timestamp - minTimestamp) / FOUR_MONTHS_SECONDS);
        return Math.max(0, Math.min(binIdx, TOTAL_BINS - 1));
    }

    /**
     * Calculates the 1D array offset for the coffee-bin bias matrix.
     */
    public static int getCoffeeBinOffset(int coffeeIdx, int binIdx) {
        return (coffeeIdx * TOTAL_BINS) + binIdx;
    }
}
