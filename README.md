pachyderm
=========

A Javascript micro web framework for the JVM.

Simple Example
==============

```javascript
app.get('/data', function(req, res) {
    res.render({ json: { data : "value" }})
});
```