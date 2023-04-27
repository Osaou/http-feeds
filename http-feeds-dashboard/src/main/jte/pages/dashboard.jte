
@template.layouts.page(title = "Http-feeds Dashboard", content = @`
  <h1>Traces in Dead-Letter Queue</h1>
  <section
    hx-get="/httpfeeds/dashboard/traces"
    hx-trigger="load"
    hx-swap="outerHTML">
  </section>
`)
