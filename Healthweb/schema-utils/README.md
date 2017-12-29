To compile the schemas:

```
xjc -d ../src/main/java -b bindings.xjb -extension -p fi.tietoallas.integration.healthweb.model ../src/main/resources/schemas/CDA_Fi.xsd
```

Note that the custom bindings defined in bindings.xjb rename a few of the original fields to avoid name collisions.  