package de.mibtex.args;

import java.util.function.Consumer;

/**
 * The last argument supplied on the command line interface.
 * This argument has no name but has a description for a help text and error messages.
 * The definedCallback is invoked upon parsing with the supplied single string argument.
 * @author Paul Bittner
 */
public record NamelessArgument(String description, Consumer<String> definedCallback) {
}
