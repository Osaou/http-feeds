@import se.aourell.httpfeeds.tracing.core.ShelvedTrace
@import se.aourell.httpfeeds.util.PagedList

@param PagedList<ShelvedTrace> traces

<section id="traces">
  <%-- pagination --%>
  @if (traces.totalPages() > 1)
    <div class="paging">
      @if (traces.page() > 1)
        <button hx-get="/feed/dashboard/traces/?page=${traces.page() - 1}"
                hx-target="#traces"
                hx-swap="outerHTML">Previous</button>
      @endif

      <span>Page: ${traces.page()} / ${traces.totalPages()}</span>

      @if (traces.page() < traces.totalPages())
        <button hx-get="/feed/dashboard/traces/?page=${traces.page() + 1}"
                hx-target="#traces"
                hx-swap="outerHTML">Next</button>
      @endif

      <p>
        <i>Total count: ${traces.totalElements()}</i>
      </p>
    </div>
  @endif

  <%-- listed traces --%>
  @if (traces.list().isEmpty())
    <i>No traces...</i>
  @else
    @for (ShelvedTrace trace : traces.list())
      @template.components.trace(trace = trace, editing = false)
    @endfor
  @endif

  <style>
    #traces .paging button:first-child {
      margin-right: 10px;
    }
    #traces .paging button:last-child {
      margin-left: 10px;
    }

    #traces .paging i {
      color: #666;
    }
  </style>
</section>
