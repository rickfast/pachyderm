importPackage(org.tortiepoint.pachyderm);

maven.addDependency("io.netty", "nettiy", "jar", "3.5.7.Final");

app.get('/poop/{p}', function(req, res) {
    out.println(req.getParams());

    res.renderJson({"value":22});
});