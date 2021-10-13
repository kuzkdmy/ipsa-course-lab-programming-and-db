#### This is a mix for 2 course labs in 'National Technical University of Ukraine “Igor Sikorsky Kyiv Polytechnic Institute”'

- OOP programming(done in Scala, app is just simple CRUD(storage in db),
- Databases work and design

#### Core libraries used

- [zio](https://zio.dev/)
- [zio-http](https://dream11.github.io/zio-http/)
- [tapir](https://tapir.softwaremill.com/en/latest/index.html)
- [doobie](https://tpolecat.github.io/doobie/)
- [tofu](tf.tofu)

#### How to check work(test/run)

- ```docker-compose up -d``` <br>
  will pull required docker images to run application <br>
  automatically await postgres db start and apply database migrations
- verification via test <br>
  ```sbt test``` <br>
  or install ZIO plugin and run as regular test(note Intellij plugin ignore zio-test aspects)
- manual run `MainApp` all configs target to local infrastructure, so it can start without any tricks with env variables

#### Requirements RU/UA link

- [oop course link](README_OOP_UA.md)
- [db course link](README_DB_UA.md)

#### Requirements US link

- todo, can be done with Google Translate from Ukrainian to English

#### Bonus level programming

- `x-request-id` middleware
- MDC logging with `x-request-id` <br>
  (wait for ZIO2, if ZIO.log(...) will be able to work with some context)

#### TODO

- prometheus metrics export endpoint(enrich docker-compose with grafana and prometheus)
- doobie tofu logs, introduce better logging on errors
- doobie newType don't derive List[NewTypeClass] need to create explicit List read/write
- json web token integration
- http client research forced by contract x-request-id(zio-grpc is good it has layer to force context)
- double check doobie blocking/nonBlocking pools
- api converters from api to domain classes should convert to ZIO[_ , ValidationError, T] (maybe `wix accord`, but they
  don't work by F[_])
- check how tapir works when there will be > 22 filters, they are tuples
- introduce docker-containers in tests
