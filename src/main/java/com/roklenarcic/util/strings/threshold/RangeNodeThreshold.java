package com.roklenarcic.util.strings.threshold;

public class RangeNodeThreshold implements Thresholder {

    private double exponent, linearFactor, maxValue, constantFactor;

    public RangeNodeThreshold() {
        this(1);
    }

    public RangeNodeThreshold(double exponent) {
        this(exponent, 1, 0.65, 2);
    }

    public RangeNodeThreshold(double exponent, double linearFactor, double maxValue, double constantFactor) {
        super();
        this.exponent = exponent;
        this.linearFactor = linearFactor;
        this.maxValue = maxValue;
        this.constantFactor = constantFactor;
    }

    public boolean isOverThreshold(int nodeSize, int nodeLevel, int keyIntervalSize) {
        if (keyIntervalSize <= 8) {
            return true;
        }
        int charArrayCost = (nodeSize / 4) + 3; // Char array costs 24 bytes + 2 bytes per char
        return nodeSize + charArrayCost > keyIntervalSize * (maxValue - linearFactor / Math.pow(constantFactor + nodeLevel, exponent));
    }

}
