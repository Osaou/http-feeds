@import gg.jte.Content

@param String title
@param Content content

<!doctype html>

<html lang="en">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>${title}</title>
  <script src="https://unpkg.com/htmx.org@1.9.0" integrity="sha384-aOxz9UdWG0yBiyrTwPeMibmaoq07/d3a96GCbb9x60f3mOt5zwkjdbcHFnKH8qls" crossorigin="anonymous"></script>
  <style>
    main {
      margin: 15px;
      font-family: sans-serif;
      font-size: 14px;
    }
  </style>
</head>
<body>
  <main>
    ${content}
  </main>
</body>
</html>
