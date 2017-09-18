<p align="center">
<a href="https://7bridges.eu" title="7bridges.eu s.r.l.">
<img src="https://7bridges.eu/img/logo-inline.png" alt="7bridges clj-odbp"
width="500px" height="122px"/></a>
</p>

# carter

**carter** is an example project to show the usage of
[OrientDB](http://orientdb.com/orientdb/) binary protocol through
[7bridges](https://7bridges.eu) Clojure driver
[clj-odbp](https://github.com/7bridges-eu/clj-odbp).

## Self-hosted setup

If you to self-host the project, or intend to use the code as a base for a
personal project, create a `conf/config.edn` file in the root of the project
with the following format.

``` clojure
{:http {:port 8080}
 :orient {:host <your-orientdb-instance>
          :db-name <your-db-name>
          :properties {:user <your-user>
                       :password <your-password}}
 :twitter
 {:consumer-key <your-consumer-key>
  :consumer-secret <your-consumer-key-secret>}}
```

`consumer-key` and `consumer-secret` are the Consumer Key and Consumer Secret
you will find in “Keys and Access Tokens” in your Twitter application
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
