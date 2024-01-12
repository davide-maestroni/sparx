package sparx.concurrent.printer;

import java.io.PrintStream;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.Receiver;
import sparx.logging.LogMessage;
import sparx.logging.LogTemplate;
import sparx.logging.LogTemplate.VariableResolver;

public class ConsoleLogPrinter implements Receiver<LogMessage> {

  private static final String PROP_PREFIX = ConsoleLogPrinter.class.getName();
  public static final String MESSAGE_TEMPLATE_PROP = PROP_PREFIX + ".template";
  public static final String VAR_PREFIX_PROP = PROP_PREFIX + ".varPrefix";
  public static final String VAR_SUFFIX_PROP = PROP_PREFIX + ".varSuffix";
  public static final String STREAM_PROP = PROP_PREFIX + ".stream";

  private static final String DEFAULT_TEMPLATE = "%1$siso_datetime%2$s [%1$slevel%2$s] [%1$stag_name%2$s]: %1$smessage%2$s";

  private final PrintStream printStream;
  private final Map<String, VariableResolver> resolvers = LogTemplate.defaultResolvers();
  private final String template;
  private final String varPrefix;
  private final String varSuffix;

  public ConsoleLogPrinter(@NotNull final Properties properties) {
    varPrefix = properties.getProperty(VAR_PREFIX_PROP, "{");
    varSuffix = properties.getProperty(VAR_SUFFIX_PROP, "}");
    template = properties.getProperty(MESSAGE_TEMPLATE_PROP,
        String.format(DEFAULT_TEMPLATE, varPrefix, varSuffix));
    final String stream = properties.getProperty(STREAM_PROP, "out");
    if ("out".equals(stream)) {
      printStream = System.out;
    } else if ("err".equals(stream)) {
      printStream = System.err;
    } else {
      throw new IllegalArgumentException("Unknown stream: " + stream);
    }
  }

  @Override
  public boolean fail(@NotNull Exception error) {
    return false;
  }

  @Override
  public void set(final LogMessage value) {
    printStream.println(LogTemplate.fillTemplate(template, varPrefix, varSuffix, resolvers, value));
  }

  @Override
  public void setBulk(@NotNull final Collection<LogMessage> values) {
    for (final LogMessage message : values) {
      set(message);
    }
  }

  @Override
  public void close() {
  }
}
