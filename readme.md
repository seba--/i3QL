Projects:

/idb - Incremental Database (engine using LMS as intermediate representation)
    /runtime - Runtime classes/engine (e.g. operators) for relations
    /syntax-iql - Syntax of the query language and transforming of queries into intermediate representation
    /intermediate-representation - Representation and optimization of queries as syntax trees. Uses LMS.
    /runtime-compiler - Transforms syntax trees to runnable "code".
    /integration-test - End-to-end user tests of queries.
    /schema-examples - University database example that is used for testing.
    /annotations - Implements custom annotations.
/bytecode-database - Concrete database for Java Bytecode
/analyses - Demo static analyses of Findbugs, Metrics


How to build me (SBT)

1.  To build this project you need to have SBT and GIT installed
    Download (SBT): http://www.scala-sbt.org/release/docs/Getting-Started/Setup.html
    Download (GIT): https://github.com

2.  Download and build the LMS project

        $ git clone https://github.com/TiarkRompf/virtualization-lms-core.git

    Go to the root directory and install the project using SBT. You need to be on the branch 'develop'

        $ cd virtualization-lms-core
        $ git checkout develop
        $ sbt publish-local

3.  Download and build the SAE project. The user credentials is your RBG username and password.

        $ git clone https://repository.st.informatik.tu-darmstadt.de/git/sae.git

    Go to the root directory and install the project using SBT. Currently you need to be on the branch 'lms'.

        $ cd sae
        $ git checkout lms
        $ sbt publish-local

4.  (Optional) You can automatically create project files for IntelliJ IDEA by using

        $ sbt gen-idea