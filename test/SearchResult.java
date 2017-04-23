import java.util.List;

public class SearchResult<T> {
  private final List<T> found;
  private final List<T> notFound;

  SearchResult(List<T> found, List<T> notFound) {
    this.found = found;
    this.notFound = notFound;
  }

  public List<T> getFound() {
    return found;
  }

  public List<T> getNotFound() {
    return notFound;
  }
}
