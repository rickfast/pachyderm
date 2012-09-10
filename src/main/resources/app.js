with(maven) {
    addDependency("io.netty", "netty", "jar", "3.5.7.Final");
}

app.get('/poop/{p}', function(req, res) {
    out.println(req.getParams());

    res.renderJson({"value":22});
});