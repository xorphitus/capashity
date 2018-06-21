# capashity

[![Build Status](https://travis-ci.org/xorphitus/capashity.svg?branch=master)](https://travis-ci.org/xorphitus/capashity)

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

### events.edn

Events are expressed by an vector.
Each event is expressed by a map which has keys shown below.

* `method`
  * HTTP method
  * required
  * type: keyword (`:get`, `:post`, etc)
* `headers`
  * HTTP request headers
  * optional
  * type: map
    * key: header name (string)
    * value: header values (string)
* `url`
  * HTTP request url
  * required
  * type: string
* `param`
  * HTTP request body
  * optional
  * type: map
    * this map is converted to a JSON text
* `skip`
  * if true, the event is ignored
  * optional
  * type: boolean
* `decoy`
  * if true, the event is going to be fired, but not measured
  * optional
  * type: boolean
* `takeover`
  * if true, the event takes over its template parameter to the next event
  * optional
  * type: boolean

#### Template

TBC

#### Decoy event

TBC

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
