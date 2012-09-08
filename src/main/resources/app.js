importPackage(org.tortiepoint.pachyderm);

app.get('/poop/{p}', function(req, res) {
    out.println(req.getParams());

    res.renderJson({"value":22});
});