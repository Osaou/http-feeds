@import se.aourell.httpfeeds.CloudEvent
@import se.aourell.httpfeeds.tracing.core.ShelvedTrace
@import se.aourell.httpfeeds.util.PagedList
@import java.time.format.DateTimeFormatter

@param PagedList<ShelvedTrace> traces

<div id="traces">
  <%-- pagination --%>
  @if (traces.totalPages() > 1)
    <div>
      @if (traces.page() > 1)
        <button hx-get="/httpfeeds/dashboard/traces?page=${traces.page() - 1}"
                hx-target="#traces"
                hx-swap="outerHTML">Previous</button>
      @endif

      <span>Page: ${traces.page()} / ${traces.totalPages()}</span>

      @if (traces.page() < traces.totalPages())
        <button hx-get="/httpfeeds/dashboard/traces?page=${traces.page() + 1}"
                hx-target="#traces"
                hx-swap="outerHTML">Next</button>
      @endif
    </div>
  @endif

  <%-- listed traces --%>
  @if (traces.list().isEmpty())
    <i>No traces...</i>
  @else
    @for (ShelvedTrace trace : traces.list())
      <article>
        <h2>Trace: ${trace.traceId()}</h2>

        <p>
          <b>Feed consumer:</b>
          ${trace.feedConsumerName()}
        </p>
        <p>
          <b>Shelving time:</b>
          ${trace.shelvedTime().format(DateTimeFormatter.RFC_1123_DATE_TIME)}
        </p>

        <button hx-get="/httpfeeds/dashboard/traces/redeliver/${trace.traceId()}">Re-introduce to event stream</button>
        <button>Edit content</button>

        <h3>Events</h3>
        <table>
          <thead>
            <tr>
              <th>Type</th>
              <th>ID</th>
              <th>Subject</th>
              <th>Time</th>
              <th>Data</th>
            </tr>
          </thead>
          <tbody>
            @for (CloudEvent event : trace.events())
              <tr>
                <td>${event.type()}</td>
                <td>${event.id()}</td>
                <td>${event.subject()}</td>
                <td>${event.time().format(DateTimeFormatter.RFC_1123_DATE_TIME)}</td>
                <td><pre>${event.data().toString()}</pre></td>
              </tr>
            @endfor
          </tbody>
        </table>

        @if (trace.lastKnownError() != null)
          <h3>Last known error</h3>
          <pre>${trace.lastKnownError()}</pre>
        @endif
      </article>
    @endfor
  @endif
</div>

<style id="traces-styles">
  #traces h3 {
    margin: 40px 0 20px;
  }

  #traces i {
    color: #666;
  }

  #traces article {
    border: 1px solid #ddb;
    padding: 20px;
    margin: 40px 0;
    overflow-x: auto;
    background-color: #ffe;
  }

  #traces th {
    text-align: left;
  }
  #traces th,
  #traces td {
    padding: 10px;
    outline: 1px solid #ddb;
  }
</style>
