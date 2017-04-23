import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public enum OperationType {
  INSERT,
  SEARCH,
  DELETE;

  private static final List<OperationType> values =
      Collections.unmodifiableList(Arrays.asList(values()));

  public static OperationType randomType() {
    return values.get(ThreadLocalRandom.current().nextInt(values.size()));
  }
}
