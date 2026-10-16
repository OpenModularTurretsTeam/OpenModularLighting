package omtteam.openmodularlighting.core.light;

/**
 * The light block a beam uses, which decides what the beam does at each {@link Cell}.
 */
public enum LightType {
    /** Floodlights, fluorescent and grow lights. UV light blocks are passed without light (1.7.10 behaviour). */
    PHANTOM,
    /** UV floodlight. Phantom lights are taken over (replaced by UV light blocks). */
    UV;

    public enum Step {
        /** The beam lights this block (places a light block or joins the one there) and continues. */
        LIGHT,
        /** The beam continues without lighting this block. */
        PASS,
        /** The beam stops before this block. */
        BLOCK
    }

    public Step step(Cell cell) {
        return switch (cell) {
            case AIR, PHANTOM_LIGHT -> Step.LIGHT;
            case UV_LIGHT -> this == UV ? Step.LIGHT : Step.PASS;
            case TRANSPARENT -> Step.PASS;
            case OPAQUE, UNLOADED -> Step.BLOCK;
        };
    }
}
