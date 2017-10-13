# Release checklist

When any step fails, and there are code changes, go bakc to the 
beginning and start again.

**TODO:** automation for the whole process.

**TODO:** `mvn release:perform``` creates a tag for each release.
Would it be better to have a branch, at least for major/minor?
Does the ```maven-release-plugin` support this?

1. Replace SNAPSHOT versions with release versions of all 
dependencies. 

    **TODO:** shouldn't the release or versions plugins take care 
    of this?
    
2. (Maven)`mvn clean install`

    complete without errors; passes all 'unit' (actutests.
    
3. (clj) Significant scripts, if any, run successfully by hand.

    Judgment call whether there is anything to run. Basic idea is
    `mvn test` runs tests that are fast enough to run on every 
    build. However, really validating a release may require 
    running test scripts that take hours or days.
    The real difference between steps 2 and 3 is the length of
    time a test takes, not the difference between a 'unit' test
    and an `integration' test.
    
4. dependent projects also build and run successfully.

    This is a variation on step 3. Again, a judgment call which
    dependent projects to include here.
    
5. (Git) commit and push.

6. `mvn release:prepare`

7. `mvn release:perform`

8. Deploy codox and javadoc to Github pages

    See [Deploying to GitHub Pages](https://github.com/weavejester/codox/wiki/Deploying-to-GitHub-Pages).

    In a git bash window:
    ```
    rm -rf target/doc && mkdir -p target/doc
    git clone https://github.com/palisades-lakes/faster-multimethods.git target/doc
    cd target/doc
    git symbolic-ref HEAD refs/heads/gh-pages
    rm .git/index
    git clean -fdx
    git checkout gh-pages
    cd ../..
    ```
    Run 
    ```
    clj src\scripts\clojure\palisades\lakes\multimethods\codox.clj
    ```
    and 
    ```
    mvn javadoc:javadoc
    ```
    In a git bash window:
    ```
    cd target/doc
    git add .
    git commit -am "new documentation push."
    git push -u origin gh-pages
    cd ../..
    ```
    
[documentation](https://palisades-lakes.github.io/faster-multimethods)
