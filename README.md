# capashity

Capashity estimates how many records will be inserted into a database for a Web system.

It calls a web system's HTTP request  which create some records, then it measures the counts of records.

* suppots multiple databases
* only supports RDBMS

## Usage

First, edit the configuration files

* `databases.edn`
* `events.edn`

then, execute the command

```
$ lein run
```

## For development

Docker Compose and Mock Server are available.

Docker Compose is used for a database.

```
$ docker-compose up -d
```

Mock Server is a dummy HTTP server and it inserts records for databases.

```
$ lein repl
capashity.core=> (ns capashity.mock-server)
capashity.mock-server=> (start)
```

The request format of Mock Server is shown below.

```
GET http://localhost:3000/${db-name}/${table-name}
```

## License

Copyright © 2017 xorphitus

Distributed under the Eclipse Public License either version 1.0 or any later version.
