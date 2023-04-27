@import se.aourell.httpfeeds.tracing.core.ShelvedTrace
@import se.aourell.httpfeeds.util.PagedList

@param PagedList<ShelvedTrace> traces

<section id="traces">
  <%-- pagination --%>
  @if (traces.totalPages() > 1)
    <div>
      @if (traces.page() > 1)
        <button hx-get="/httpfeeds/dashboard/traces/?page=${traces.page() - 1}"
                hx-target="#traces"
                hx-swap="outerHTML">Previous</button>
      @endif

      <span>Page: ${traces.page()} / ${traces.totalPages()}</span>

      @if (traces.page() < traces.totalPages())
        <button hx-get="/httpfeeds/dashboard/traces/?page=${traces.page() + 1}"
                hx-target="#traces"
                hx-swap="outerHTML">Next</button>
      @endif
    </div>
  @endif

  <%-- listed traces --%>
  @if (traces.list().isEmpty())
    <i>No traces...</i>
  @else
    <i>Total count: ${traces.totalElements()}</i>
    @for (ShelvedTrace trace : traces.list())
      @template.components.trace(trace = trace, editing = false)
    @endfor
  @endif

  <style>
    #traces h3 {
      margin: 40px 0 20px;
    }

    #traces i {
      color: #666;
    }
  </style>
</section>
