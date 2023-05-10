# Tax History Frontend

Source code for the front end microservice for the Income Record Viewer service

### License

This code is open source software licensed under
the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")

### Running the application

In order to run the microservice, you must have SBT installed. You should then be able to start the application using:

```bash
sbt run
```

To run the tests for the application, you can run:

```bash 
sbt test
```

or to run tests+coverage+scalafmt+scalastyle run:

```bash
./run_all_tests.sh
```

Landing page URL for the service is: http://localhost:9996/tax-history/select-client

