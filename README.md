<p align="center">
    <img src="https://github.com/7bridges-eu/carter/blob/master/resources/logo.png" alt="7bridges carter"
    width="500px" height="122px"/>
</p>

# carter

**carter** is an example project to show the usage of
[OrientDB](http://orientdb.com/orientdb/) binary protocol through
[7bridges](https://7bridges.eu) Clojure driver
[clj-odbp](https://github.com/7bridges-eu/clj-odbp).

Give it a try: https://carter.7bridges.eu/

## Self-hosted setup

If you want to self-host the project, or intend to use the code as a base for a
personal project, create a `conf/config.edn` file in the root of the project
with the following format.

``` clojure
{:http {:port 8080}
 :orient {:host <your-orientdb-instance>
          :port <your-orientdb-port>
          :db-name <your-db-name>
          :properties {:user <your-user>
                       :password <your-password}}
 :twitter
 {:consumer-key <your-consumer-key>
  :consumer-secret <your-consumer-key-secret>}}
```

`consumer-key` and `consumer-secret` are the Consumer Key and Consumer Secret
you will find in “Keys and Access Tokens” of your Twitter application
management.

Then run:

``` shell
$ lein init-db
$ lein cljsbuild once
$ lein run
```

Now point your browser to `http://localhost:8080/` and have fun.

## License

Copyright © 2017 7bridges

Distributed under the Apache License 2.0.
