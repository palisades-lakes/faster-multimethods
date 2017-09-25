# Release checklist

When any step fails, and there are code changes, go bakc to the 
beginning and start again.

**TODO:** automation for the whole process.

**TODO:** ```mvn release:perform``` creates a tag for each release.
Would it be better to have a branch, at least for major/minor?
Does the ```maven-release-plugin``` support this?

1. Replace SNAPSHOT versions with release versions of all 
dependencies. 

    **TODO:** shouldn't the release or versions plugins take care 
    of this?
    
2. (Maven)```mvn clean install```

    complete without errors; passes all 'unit' (actutests.
    
3. (clj) Significant scripts, if any, run successfully by hand.

    Judgment call whether there is anything to run. Basic idea is
    ```mvn test``` runs tests that are fast enough to run on every 
    build. However, really validating a release may require 
    running test scripts that take hours or days.
    The real difference between steps 2 and 3 is the length of
    time a test takes, not the difference between a 'unit' test
    and an `integration' test.
    
4. dependent projects also build and run successfully.

    This is a variation on step 3. Again, a judgment call which
    dependent projects to include here.
    
5. (clj) update ```src\scripts\clojure\doc\codox.clj``` with the
    release version. Run it (with ```clj.bat``` or ```clj.sh```,
    or however you like) 
    
    **TODO:** FIgure out how to do this from maven, integrating
    with ```mvn release:prepare```.
    
6. ```mvn javadoc:javadoc```
....
    **TODO:** FIgure out how to do this from maven, integrating
    with ```mvn release:prepare```.
    
    
5. (Git) commit and push.

6. ```mvn release:prepare```

7. ```mvn release:perform```