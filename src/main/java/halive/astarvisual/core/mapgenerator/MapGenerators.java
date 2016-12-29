package halive.astarvisual.core.mapgenerator;

import java.util.Random;

public enum MapGenerators {
    WHITE_NOISE("Weisses Rauschen", new WhiteNoiseGenerator(new Random(Constants.seed))),
    SIMPLEX_ABSOULTE("Simplex Rauschen (Absolut)", new SimplexNoiseGenerator(new Random(Constants.seed),
            SimplexNoiseGenerator.ValueConverter.ABSOULTE));

    private String name;
    private MapGenerator generator;

    MapGenerators(String s, MapGenerator generator) {
        this.name = s;
        this.generator = generator;
    }

    @Override
    public String toString() {
        return name;
    }

    public MapGenerator getGenerator() {
        return generator;
    }

    private static class Constants {

        private static final long seed = System.currentTimeMillis();
    }
}
