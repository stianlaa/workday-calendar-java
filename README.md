# Workday calendar

This workday calendar application is an assignment answer. From a point in time it determines a date and time a desired number of working days away.

## Usage

The application was created for Java 11. The behaviour can be seen by running the test by the maven command:
```
mvn clean verify
```
from the project root directory. The application can also be run by in any IDE with java 11 setup.

Manual testing of the program is possible by first building the project using
```
mvn clean install
```
Then in the folder ./target, execute the .jar file by using
```
java -jar workday-calendar-0.0.1-SNAPSHOT.jar
```
The program should instruct you on how to test the project.