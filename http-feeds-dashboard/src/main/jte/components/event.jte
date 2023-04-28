@import se.aourell.httpfeeds.CloudEvent
@import java.time.format.DateTimeFormatter
@import static se.aourell.httpfeeds.dashboard.http.HttpFeedsDashboardController.serializeEventDataForEditing

@param String traceId
@param CloudEvent event
@param boolean isEditing

<tr id="event-${event.id()}">
  <td>${event.id()}</td>
  <td>${event.type()}</td>
  <td>${event.subject()}</td>
  <td>${event.time().format(DateTimeFormatter.RFC_1123_DATE_TIME)}</td>
  <td>
    @if (isEditing)
      <form>
        <textarea name="data">${serializeEventDataForEditing(event.data())}</textarea>
        <button type="button"
                hx-put="/feed/dashboard/traces/${traceId}/events/${event.id()}"
                hx-trigger="click"
                hx-target="#event-${event.id()}"
                hx-swap="outerHTML">
          Save
        </button>
      </form>
    @else
      <pre>${event.data().toString()}</pre>
      <button type="button"
              hx-get="/feed/dashboard/traces/${traceId}/events/${event.id()}/?edit=true"
              hx-trigger="click"
              hx-target="#event-${event.id()}"
              hx-swap="outerHTML">
        Edit
      </button>
    @endif
  </td>

  <style>
    #event-${event.id()} td {
      vertical-align: top;
    }

    #event-${event.id()} textarea {
      padding: 10px;
      width: calc(100% - 20px);
      min-width: 300px;
      min-height: 100px;
    }
  </style>
</tr>
