pachyderm
=========

A Javascript micro web framework for the JVM.

Simple Example
==============

The following ex

```javascript
app.get('/data', function(req, res) {
    res.render({ json: { data : "value" }})
});
```