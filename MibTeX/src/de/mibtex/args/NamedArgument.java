package de.mibtex.args;

import java.util.List;
import java.util.function.Consumer;

/**
 * A named argument for argument parsing.
 * A named argument has a long name and a short name (e.g., "help" vs "h" for an argument "--help" and "-h", respectively).
 * Each argument has an arity that denotes how many parameters can be passed to this argument.
 * For example, if you want an argument for an input or output file, you would choose an arity of ONE.
 * An argument may be specified as mandatory, which means that the argument parser issues an error if the user did not supply the respective argument.
 * For processing arguments, each NamedArgument has two callbacks:
 * - The definedCallback is invoked when the argument parser detects this named argument to be defined.
 * - Additionally, the parameterCallback is invoked when the argument parser finds an argument with a non-zero arity.
 *   The parameterCallback is invoked with all supplied parameters to this argument.
 * @author Paul Bittner
 */
public class NamedArgument {
    public enum Arity {
        ZERO,
        ONE,
        ANY
    }

    private final boolean mandatory;
    protected String longName, shortName, description;
    protected Arity arity;
    protected Runnable definedCallback;
    protected Consumer<List<String>> parameterCallback;

    public NamedArgument(String shortName, String longName, String description, Arity arity, boolean isMandatory, Runnable definedCallback, Consumer<List<String>> parameterCallback) {
        if (longName == null) {
            throw new NullPointerException("longName may be empty but cannot be null!");
        }
        if (shortName == null) {
            throw new NullPointerException("shortName may be empty but cannot be null!");
        }
        if (longName.isEmpty() && shortName.isEmpty()) {
            throw new IllegalArgumentException("longName and shortName are empty!");
        }
        if (longName.startsWith("-")) {
            throw new NullPointerException("longName " + longName + " starts with \"-\" but should not");
        }
        if (shortName.startsWith("-")) {
            throw new NullPointerException("shortName + " + shortName + " starts with \"-\" but should not");
        }

        if (arity == Arity.ZERO && parameterCallback != null) {
            throw new IllegalArgumentException("An argument with arity zero cannot have a parameter callback!");
        }

        if (longName.isEmpty()) {
            this.longName = "";
        } else {
            this.longName = "--" + longName;
        }

        if (shortName.isEmpty()) {
            this.shortName = "";
        } else {
            this.shortName = "-" + shortName;
        }

        this.description = description;
        this.arity = arity;
        this.mandatory = isMandatory;
        this.definedCallback = definedCallback;
        this.parameterCallback = parameterCallback;
    }

    public NamedArgument(char flag, String description, Runnable definedCallback) {
        this("" + flag, "", description, Arity.ZERO, false, definedCallback, null);
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public boolean matches(String strarg) {
        return longName.equals(strarg) || shortName.equals(strarg);
    }

    @Override
    public String toString() {
        if (longName.isEmpty()) {
            return shortName;
        } else {
            return longName;
        }
    }
}
