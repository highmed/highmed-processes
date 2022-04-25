# HiGHmed Processes 

![Java CI Build with Maven](https://github.com/highmed/highmed-processes/workflows/Java%20CI%20Build%20with%20Maven/badge.svg)

In this repository you will find the processes of the HiGHmed consortia, that can be deployed on the [HiGHmed DSF](https://github.com/highmed/highmed-dsf).

## Development
Branching follows the git-flow model, for the latest development version see branch [develop](https://github.com/highmed/highmed-processes/tree/develop).

## License
All code of the HiGHmed processes is published under the [Apache-2.0 License](LICENSE).

## Wiki
A full documentation can be found in the [HiGHmed DSF Wiki](https://github.com/highmed/highmed-dsf/wiki).

## Building the Project
Prerequisite: Java 11, Maven >= 3.6

* Add the Github Package Registry server to your Maven `.m2/settings.xml`. Instructions on how to generate the `USERNAME` and `TOKEN` can be found in the HiGHmed DSF Wiki page with the name [Using the Github Maven Package Registry](https://github.com/highmed/highmed-dsf/wiki/Using-the-Github-Maven-Package-Registry).

    ```
    <servers>
        <server>
          <id>github</id>
          <username>USERNAME</username>
          <password>TOKEN</password>
        </server>
    </servers>
    ```

* Build the project from the root directory of this repository by executing the following command. If you want to copy the artifacts into the test folders of the **highmed-dsf** repository, make sure that the **highmed-processes** repository resides in the same folder as the **higmmed-dsf** repository and activate the profile `copy-to-highmed-dsf-process` in the build command.

  ```
  mvn clean install (-P copy-to-highmed-dsf-process)
  ```

## Executing the Processes
The HiGHmed DSF Wiki includes a section about [Manual Integration Testing](https://github.com/highmed/highmed-dsf/wiki/Build-and-Test-Project#manual-integration-testing-vms-for-docker-registry-3-medics-ttp), with detailed descriptions on how to run the processes in a simulated testing environment consisting of 3 MeDICs and 1 TTP. Example implementations to start a process can be found in the respective Maven sub-modules under `src/test/java` in the package `org.highmed.dsf.bpe.start`.


## Implementing new Processes
Instructions on how to implement a new process can be found in the HiGHmed DSF Wiki page with the name [Adding a new BPMN Process](https://github.com/highmed/highmed-dsf/wiki/Adding-BPMN-Processes).