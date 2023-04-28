@import se.aourell.httpfeeds.CloudEvent
@import se.aourell.httpfeeds.tracing.core.ShelvedTrace
@import java.time.format.DateTimeFormatter

@param ShelvedTrace trace

!{String hxRefreshUrl = null;}
!{String hxTrigger = null;}
!{String hxSwap = null;}

@if (trace.isRedelivering())
  !{hxRefreshUrl = "/feed/dashboard/traces/${trace.traceId()}";}
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
        @template.components.event(traceId = trace.traceId(), event = event, isEditing = false)
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

    #trace-${trace.traceId()} h3 {
      margin: 40px 0 20px;
    }

    #trace-${trace.traceId()} p:empty {
      display: none;
    }

    #trace-${trace.traceId()} button {
      margin: 10px 0;
    }

    #trace-${trace.traceId()} table {
      width: 100%;
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

    #trace-${trace.traceId()} .last-error {
      padding: 10px;
      background-color: #fee;
      overflow-x: auto;
    }
  </style>
</article>
