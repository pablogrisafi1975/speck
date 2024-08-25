#Commands.md

Display outdated dependencies
```
mvn versions:display-dependency-updates
```

Display outdated plugins
```
mvn versions:display-plugin-updates
```

Publish to Maven Central (takes a few days, remember to remove the -SNAPSHOT)
```
mvn deploy -DskipTests=true -Prelease
```
worked the first time at 2024-08-17 , by changing the user token
