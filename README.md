pachyderm
=========

A Javascript micro web framework for the JVM.

Simple Example
==============

```javascript
app.get('/value/{p}', function(req, res) {
    var value = req.getParams().get("p");

    res.renderJson({"value":value});
});
```