pachyderm
=========

A Javascript micro web framework for the JVM.

```javascript
// app.js
app.get('/hello', function(req, res) {
    res.render({ text: 'hello' }});
});
```

Run using Pachyderm:

```
./bin/pachyderm ~/app.js
```

View response at [http://localhost:8080/hello](http://localhost:8080/hello)