Pachyderm
=========

A Javascript micro web framework for the JVM.

```javascript
// app.js
app.get('/hello', function(req, res) {
    res.render({ text: 'hello' });
});
```

Run using Pachyderm:

```
./bin/pachyderm ~/app.js
```

View response at [http://localhost:8080/hello](http://localhost:8080/hello)

Rendering views with underscore.js
-----------

Web service code:

```javascript
app.get('/example/{number}', function(req, res) {
    var value = req.getParams().get("number");

    res.render({ view: "example.html", model: { someValue: value }});
});
```

View code (in /views directory):

```js+erb
<li><%= someValue %></li>
```

View response at [http://localhost:8080/example/12](http://localhost:8080/example/12)

Rendering JSON
--------------

```javascript
app.get('/example', function(req, res) {
    res.render({json: { value: "something", other: 12.0 }});
}
```

View response at [http://localhost:8080/example](http://localhost:8080/example)

Resolving Maven dependencies
----------------------------

Pachyderm can resolve Java dependencies at runtime:

```javascript
with(maven) {
	addDependency("commons-email:commons-email:1.1");
}

app.get('/email', function(req, res) {
	var email = new org.apache.commons.mail.SimpleEmail();

	email.setHostName("mail.myserver.com");
	email.addTo("jdoe@somewhere.org", "John Doe");
	email.setFrom("me@apache.org", "Me");
	email.setSubject("Test message");
	email.setMsg("This is a simple test of commons-email");
	email.send();
});
```