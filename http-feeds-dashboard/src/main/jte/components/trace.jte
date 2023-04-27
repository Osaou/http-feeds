@import se.aourell.httpfeeds.CloudEvent
@import se.aourell.httpfeeds.tracing.core.ShelvedTrace
@import se.aourell.httpfeeds.util.PagedList
@import java.time.format.DateTimeFormatter

@param ShelvedTrace trace
@param boolean editing

!{String hxRefreshUrl = null;}
!{String hxTrigger = null;}
!{String hxSwap = null;}

@if (trace.isRedelivering())
  !{hxRefreshUrl = "/feed/dashboard/traces/${trace.traceId()}/status";}
  !{hxTrigger = "load delay:10s";}
  !{hxSwap = "outerHTML";}
@endif

<article id="trace-${trace.traceId()}"
         hx-get="${hxRefreshUrl}"
         hx-trigger="${hxTrigger}"
         hx-swap="${hxSwap}">
  <h2>Trace: ${trace.traceId()}</h2>

  <p>
    <b>Feed consumer:</b>
    ${trace.feedConsumerName()}
  </p>
  <p>
    <b>Shelving time:</b>
    ${trace.shelvedTime().format(DateTimeFormatter.RFC_1123_DATE_TIME)}
  </p>

  <button type="button"
          hx-post="/feed/dashboard/traces/${trace.traceId()}/redeliver"
          hx-trigger="click"
          hx-target="next p">
    Re-introduce to event stream
  </button>

  @if (editing)
    <button type="button"
            hx-post="/feed/dashboard/traces/${trace.traceId()}/status"
            hx-trigger="click"
            hx-target="#trace-${trace.traceId()}"
            hx-swap="outerHTML">
      Save event data
    </button>
  @else
    <button type="button"
            hx-get="/feed/dashboard/traces/${trace.traceId()}/status/?editing=true"
            hx-trigger="click"
            hx-target="#trace-${trace.traceId()}"
            hx-swap="outerHTML">
      Edit event data
    </button>
  @endif

  <p></p>

  <h3>Events</h3>
  <table>
    <thead>
    <tr>
      <th>ID</th>
      <th>Type</th>
      <th>Subject</th>
      <th>Time</th>
      <th>Data</th>
    </tr>
    </thead>
    <tbody>
      @for (CloudEvent event : trace.events())
        <tr>
          <td>${event.id()}</td>
          <td>${event.type()}</td>
          <td>${event.subject()}</td>
          <td>${event.time().format(DateTimeFormatter.RFC_1123_DATE_TIME)}</td>
          <td>
            @if (editing)
              <textarea>${event.data().toString()}</textarea>
            @else
              <pre>${event.data().toString()}</pre>
            @endif
          </td>
        </tr>
      @endfor
    </tbody>
  </table>

  @if (trace.lastKnownError() != null)
    <h3>Last known error</h3>
    <pre class="last-error">${trace.lastKnownError()}</pre>
  @endif

  <style>
    #trace-${trace.traceId()} {
      border: 1px solid #ddb;
      padding: 20px;
      margin: 40px 0;
      background-color: #ffe;
    }

    #trace-${trace.traceId()} tbody tr:first-child td {
      background-color: #fee;
    }

    #trace-${trace.traceId()} th {
      text-align: left;
    }

    #trace-${trace.traceId()} th,
    #trace-${trace.traceId()} td {
      padding: 10px;
      outline: 1px solid #ddb;
    }

    #trace-${trace.traceId()} textarea {
      padding: 10px;
      width: 100%;
      height: 200px;
    }

    #trace-${trace.traceId()} .last-error {
      padding: 10px;
      background-color: #fee;
      overflow-x: auto;
    }
  </style>
</article>
