package software.bernie.geckolib.loading.math.function.misc;

import software.bernie.geckolib.loading.math.MathValue;
import software.bernie.geckolib.loading.math.function.MathFunction;

/**
 * {@link MathFunction} value supplier
 *
 * <p>
 * <b>Contract:</b>
 * <br>
 * Converts the input value to degrees
 */
public final class ToDegFunction extends MathFunction {
    private final MathValue value;

    public ToDegFunction(MathValue... values) {
        super(values);

        this.value = values[0];
    }

    @Override
    public String getName() {
        return "math.to_deg";
    }

    @Override
    public double compute() {
        return Math.toDegrees(this.value.get());
    }

    @Override
    public int getMinArgs() {
        return 1;
    }

    @Override
    public MathValue[] getArgs() {
        return new MathValue[] {this.value};
    }
}
