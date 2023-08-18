package de.mibtex.args;

import java.util.function.Consumer;

public record NamelessArgument(String description, Consumer<String> definedCallback) {
}
