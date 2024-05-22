# Tax History Frontend

Source code for the front end microservice for the Income Record Viewer service

## Running the application

Before running this frontend, start all dependant services in Service Manager 2:

```bash
sm2 --start TAX_HISTORY_ALL
sm2 --stop TAX_HISTORY_FRONTEND
```

In order to run the microservice, you must have SBT installed. You should then be able to start the application using:

```bash
sbt run
```

To run the tests for the application, you can run:

```bash 
sbt test
```

or to format and check the code style, compile the code, runs tests with coverage, performs accessibility tests with report, generate a coverage report, and check for dependency updates:

```bash
./run_all_tests.sh
```

## Accessibility Tests

### Prerequisites
Have node installed on your machine

### Execute accessibility tests
To run the ally tests locally, simply run:
```bash
sbt clean A11y/test
```

The landing page URL for the service is: http://localhost:9996/tax-history/select-client [[staging](https://www.staging.tax.service.gov.uk/tax-history/select-client)]

You will need an Agent account to sign-in [see Create Agent user](#create-agent-user).

## Manually adding test data

This frontend allows an Agent to view Income record details about a client therefore a relationship between the two needs to be formed.

### Create Agent user

To create an agent head to http://localhost:9553/agents-external-stubs/gg/sign-in [[staging](https://www.staging.tax.service.gov.uk/agents-external-stubs/gg/sign-in)] and complete the sign-in form filling any UserID and Planet.

This redirects to the user creation page. Set the "Affinity group" to **Agent** which populates "Principal enrolment" with the value **HMRC-AS-AGENT**.
Press Create.

This automatically generates sample data for this Agent.
Set the "Credential Strength" to **Strong** and record the **ARN** number (AgentReferenceNumber) under the heading "Principal enrolments" assigned to this user.
```example: QARN8691038```

Press Update to continue.

### Create Client user

If signed in with the Agent user, create the client user using the form in the panel or using
[http://localhost:9553/agents-external-stubs/user/create?userId={InsertClientID}](http://localhost:9553/agents-external-stubs/user/create?userId=SampleClient) [[staging](https://www.staging.tax.service.gov.uk/agents-external-stubs/user/create?userId=SampleClient)].

Being signed in as the Agent from the creation step ensures this second user is stored within the same _Planet_.

Set the "Affinity group" to **Individual** which populates "Principal enrolment" with the value **HMRC-MTD-IT**.
Press Create.

This automatically generates sample data for this Client.
Set the "Credential Strength" to **Strong** and record the **NINO** (or edit to a custom NINO to include more test data [see here](#checking-the-added-client-data)).
```example: AM242413B```

Press Update to continue. 


### Create the relationship between Agent and Client

To create a relationship head to http://localhost:9448/invitations/test-only/relationships/create [[staging](https://www.staging.tax.service.gov.uk/invitations/test-only/relationships/create)].

Fill in the fields
- ARN should match the **ARN** recorded from the **Agent** in the creation step.
- Service should be set to **PERSONAL-INCOME-RECORD**
- Client ID should match the **NINO** recorded from the **Client** in the creation step.

Press Create.

_Note - the relationship url in staging can be inconsistent. If wanting to test in staging, run the [performance tests](https://github.com/hmrc/tax-history-performance-tests) and test with NINO `AM242413B` which contains all varieties of tax data for different years._

### Checking the added client data

Go to http://localhost:9996/tax-history/select-client [[staging](https://www.staging.tax.service.gov.uk/tax-history/select-client)]. Sign in as the Agent created before.

When entering the NINO the name of the client created should be found.

Currently, a new client will have no data assigned to them relating to tax years.
This data is retrieved using [tax-history](https://github.com/hmrc/tax-history) and [citizen-details](https://github.com/hmrc/citizen-details) in production.

Using the NINO's available in [stubbed data](https://github.com/hmrc/tax-history-stubs/tree/main/conf/resources/data)
at client creation ensures there are details for a clients specific tax years.
Otherwise, run the stub repository locally and create custom data in the same format for the generated client.

### Troubleshooting

If the expected user does not sign in correctly. Sign in with a new user on the same _Planet_.

Go to "Test Users" http://localhost:9553/agents-external-stubs/users [[staging](https://www.staging.tax.service.gov.uk/agents-external-stubs/users)] and remove the problematic user and reform the relationship.

If there are still issues, press _**End this planet and destroy the data**_ and start over.

## License

This code is open source software licensed under
the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")
