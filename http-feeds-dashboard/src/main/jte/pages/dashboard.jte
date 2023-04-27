
@template.layouts.page(title = "Http-feeds Dashboard", content = @`
  <h1>Traces in Dead-Letter Queue</h1>
  <section
    hx-get="/feed/dashboard/traces"
    hx-trigger="load"
    hx-swap="outerHTML">
  </section>
`)
