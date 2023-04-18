package se.aourell.httpfeeds.util;

import java.util.List;

public record PagedList<T>(
  List<T> list,
  int page,
  int totalPages,
  long totalElements
) { }
