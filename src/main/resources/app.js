with(maven) {
    addDependency("io.netty", "netty", "jar", "3.5.7.Final");
}

app.get('/poop/{p}', function(req, res) {
    out.println(req.getParams());

    res.renderJson({"value":22});
});

app.get('/pupe/{x}', function(req, res) {
    res.renderXml({"value": req.getParams().get("x")});
});

app.get('/render', function(req, res) {
    res.renderTemplate("sample.template", {poop: 'powpers'});
});