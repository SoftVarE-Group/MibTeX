package de.mibtex.args;

import java.util.ArrayList;
import java.util.List;

public class ArgParser {

    private final List<NamedArgument> arguments;
    private final NamelessArgument namelessArgument;

    public ArgParser(final NamelessArgument namelessArgument, final NamedArgument... args) {    
        this.arguments = new ArrayList<>(args.length + 1);
        this.arguments.addAll(List.of(args));
        this.arguments.add(
                new NamedArgument(
                        "h", "help",
                        "Show this help page.",
                        NamedArgument.Arity.ZERO,
                        false,
                        () -> {
                            final StringBuilder b = new StringBuilder();
                            help(b);
                            System.out.println(b);
                            System.exit(0);
                        },
                        null
                )
        );
        this.namelessArgument = namelessArgument;
    }
    
    private static void runArg(final NamedArgument arg, final List<String> params) {
        if (arg != null) {
            switch (arg.arity) {
                case ZERO -> {
                    if (!params.isEmpty()) {
                        throw new IllegalArgumentException("Argument " + arg + " expects zero arguments but got " + params);
                    }
                }
                case ONE -> {
                    if (params.isEmpty()) {
                        throw new IllegalArgumentException("Argument " + arg + " expects exactly one argument but none!");
                    }
                    if (params.size() > 1) {
                        throw new IllegalArgumentException("Argument " + arg + " expects exactly one argument but got " + params);
                    }
                    arg.parameterCallback.accept(params);
                }
                case ANY -> arg.parameterCallback.accept(params);
            }
        }
    }

    public void parse(String[] args) {
        NamedArgument currentArgument = null;
        List<String> currentArgs = null;
                
        final List<NamedArgument> handledArgs = new ArrayList<>();
        final List<String> handledStrargs = new ArrayList<>();
                
        boolean invokedNamelessArg = namelessArgument == null;
        
        for (int i = 0; i < args.length; ++i) {
            final String strarg = args[i];
            
            if (strarg.startsWith("-")) {
                runArg(currentArgument, currentArgs);
                
                if (handledStrargs.contains(strarg)) {
                    throw new IllegalArgumentException("Duplicate specification of argument \"" + strarg + "\"!");
                }
                
                currentArgs = new ArrayList<>();
                
                NamedArgument matchingArgument = null;
                for (final NamedArgument potentialArgument : arguments) {
                    if (potentialArgument.matches(strarg)) {
                        matchingArgument = potentialArgument;
                        break;
                    }
                }

                if (matchingArgument != null) {
                    currentArgument = matchingArgument;
                    handledStrargs.add(strarg);
                    handledArgs.add(currentArgument);

                    if (currentArgument.definedCallback != null) {
                        currentArgument.definedCallback.run();
                    }
                } else {
                    final StringBuilder errorMsg = new StringBuilder("Unknown argument \"" + strarg + "\" given!\nAvailable arguments are:");
                    help(errorMsg);
                    throw new IllegalArgumentException(errorMsg.toString());
                }
            } else if ((i == 0 || i == args.length - 1) && !invokedNamelessArg) {
                namelessArgument.definedCallback().accept(strarg);
                invokedNamelessArg = true;
            } else {
                if (currentArgument != null && currentArgument.parameterCallback != null) {
                    currentArgs.add(strarg);
                } else {
                    throw new IllegalArgumentException("Unknown parameter \"" + strarg + "\" given! An argument (beginning with \"-\" or \"--\") has to be specified first");
                }
            }
        }
        
        runArg(currentArgument, currentArgs);
        
        if (!invokedNamelessArg) {
            throw new IllegalArgumentException("Missing mandatory last argument: " + namelessArgument.description());
        }

        for (final NamedArgument a : this.arguments) {
            if (a.isMandatory() && !handledArgs.contains(a)) {
                throw new IllegalArgumentException("Argument \"" + a + "\" is mandatory but not specified!");
            }
        }
    }
    
    public void help(final StringBuilder msg) {
        msg.append("\n").append("Available arguments:");
        for (final NamedArgument arg : arguments) {
            msg
                    .append("\n")
                    .append("  ").append(arg.longName).append(" / ").append(arg.shortName)
                    .append("\n")
                    .append("    [").append(arg.isMandatory() ? "MANDATORY" : "OPTIONAL").append("]")
                    .append("\n")
                    .append("    ").append(arg.description)
                    .append("\n")
                    ;
        }
    }
}
