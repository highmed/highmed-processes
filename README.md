# HiGHmed Processes 

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

## Implementing new Processes
Instructions on how to implement a new process can be found in the HiGHmed DSF Wiki page with the name [Adding a new BPMN Process](https://github.com/highmed/highmed-dsf/wiki/Adding-BPMN-Processes).