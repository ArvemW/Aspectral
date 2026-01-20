package arvem.aspectral.util;

/**
 * Comparison operators for numeric conditions.
 */
public enum Comparison {
    NONE(""),
    EQUAL("=="),
    NOT_EQUAL("!="),
    LESS_THAN("<"),
    LESS_THAN_OR_EQUAL("<="),
    GREATER_THAN(">"),
    GREATER_THAN_OR_EQUAL(">=");

    private final String symbol;

    Comparison(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    /**
     * Compare two double values using this comparison.
     */
    public boolean compare(double a, double b) {
        return switch (this) {
            case NONE -> true;
            case EQUAL -> a == b;
            case NOT_EQUAL -> a != b;
            case LESS_THAN -> a < b;
            case LESS_THAN_OR_EQUAL -> a <= b;
            case GREATER_THAN -> a > b;
            case GREATER_THAN_OR_EQUAL -> a >= b;
        };
    }

    /**
     * Compare two int values using this comparison.
     */
    public boolean compare(int a, int b) {
        return compare((double) a, (double) b);
    }

    /**
     * Get a comparison from its symbol.
     */
    public static Comparison fromSymbol(String symbol) {
        for (Comparison c : values()) {
            if (c.symbol.equals(symbol)) {
                return c;
            }
        }
        return NONE;
    }
}
