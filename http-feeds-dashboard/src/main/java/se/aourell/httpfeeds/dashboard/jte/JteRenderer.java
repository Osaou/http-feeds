package se.aourell.httpfeeds.dashboard.jte;

import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.TemplateOutput;
import gg.jte.output.StringOutput;

import java.util.Map;

public class JteRenderer {

  private final TemplateEngine jte;

  public JteRenderer() {
    jte = TemplateEngine.createPrecompiled(ContentType.Html);
  }

  public String view(String viewName) {
    return view(viewName, null);
  }

  public String view(String viewName, Map<String, Object> model) {
    final TemplateOutput output = new StringOutput();
    jte.render(viewName, model, output);

    return output.toString();
  }
}
