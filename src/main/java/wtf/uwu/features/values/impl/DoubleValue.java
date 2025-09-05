package wtf.uwu.features.values.impl;

import lombok.Getter;
import lombok.Setter;
import wtf.uwu.features.modules.Module;
import wtf.uwu.features.values.Value;
import wtf.uwu.utils.math.MathUtils;
import wtf.uwu.utils.math.RandomUtils;

import java.util.function.Supplier;

@Getter @Setter
public class DoubleValue extends Value {

    private final boolean dual;

    private double value;
    private double minValue;
    private double maxValue;

    private double minSliderValue;
    private double maxSliderValue;

    private double lastValue;
    private double lastMinSliderValue;
    private double lastMaxSliderValue;

    private final int decimalPlaces;

    private Runnable changeListener;

    // Single slider
    public DoubleValue(String name, Module module, double currentValue, double minValue, double maxValue, int decimalPlaces, Supplier<Boolean> visible) {
        super(name, module, visible);
        this.dual = false;
        this.value = currentValue;
        this.lastValue = currentValue;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.decimalPlaces = decimalPlaces;
    }

    // Dual slider
    public DoubleValue(String name, Module module, double minSliderValue, double maxSliderValue, double minValue, double maxValue, int decimalPlaces, Supplier<Boolean> visible) {
        super(name, module, visible);
        this.dual = true;
        this.minSliderValue = minSliderValue;
        this.maxSliderValue = maxSliderValue;
        this.lastMinSliderValue = minSliderValue;
        this.lastMaxSliderValue = maxSliderValue;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.decimalPlaces = decimalPlaces;
    }

    public void setChangeListener(Runnable changeListener) {
        this.changeListener = changeListener;
    }

    public void setValue(double value) {
        if (this.value != value) {
            this.value = value;
            if (changeListener != null) changeListener.run();
        }
    }

    public void setMinSliderValue(double minSliderValue) {
        if (this.minSliderValue != minSliderValue) {
            this.minSliderValue = minSliderValue;
            if (changeListener != null) changeListener.run();
        }
    }

    public void setMaxSliderValue(double maxSliderValue) {
        if (this.maxSliderValue != maxSliderValue) {
            this.maxSliderValue = maxSliderValue;
            if (changeListener != null) changeListener.run();
        }
    }

    public double[] getDualValue() {
        if (!dual) throw new IllegalStateException("Not a dual slider");
        return new double[]{minSliderValue, maxSliderValue};
    }

    public void clampDualValues() {
        if (dual) {
            if (minSliderValue > maxSliderValue) {
                double temp = minSliderValue;
                minSliderValue = maxSliderValue;
                maxSliderValue = temp;
            }
            minSliderValue = Math.max(minValue, Math.min(minSliderValue, maxValue));
            maxSliderValue = Math.max(minValue, Math.min(maxSliderValue, maxValue));
        }
    }

    public double getValue() {
        if (dual) {
            double min = Math.min(minSliderValue, maxSliderValue);
            double max = Math.max(minSliderValue, maxSliderValue);
            return MathUtils.roundToPlace(RandomUtils.secureDouble(min, max), decimalPlaces);
        } else {
            return value;
        }
    }

    public boolean hasChanged() {
        if (dual) {
            return minSliderValue != lastMinSliderValue || maxSliderValue != lastMaxSliderValue;
        } else {
            return value != lastValue;
        }
    }

    public void updateLast() {
        if (dual) {
            lastMinSliderValue = minSliderValue;
            lastMaxSliderValue = maxSliderValue;
        } else {
            lastValue = value;
        }
    }
}
