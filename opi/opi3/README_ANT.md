# Ant-сборка для лабораторной №3: JAR + Git + macOS

Этот вариант сделан под macOS и без отдельных `.sh`-скриптов. Вся логика находится в `build.xml` через Ant-теги.

## Что важно

- `compile`, `build`, `clean`, `test`, `doc`, `native2ascii`, `xml`, `alt`, `env` сделаны обычными Ant-задачами: `javac`, `jar`, `junit`, `javadoc`, `checksum`, `replace`, `xmlvalidate` и т.д.
- `team` и `history` теперь не вызывают `scripts/git-team.sh` и `scripts/git-history.sh`. Git-команды находятся прямо в `build.xml` через `<exec executable="git">`.
- `music` сделан под macOS через `afplay`.
- `report` и `diff` используют SVN через `<exec executable="svn">`, потому что по условию эти цели работают с SVN.

## Как запустить

Открой Terminal в папке проекта.

Собрать JAR:

```bat
ant clean build
```

Запустить JAR:

```bat
java -jar dist/opi3.jar
```

Запустить тесты:

```bat
ant test
```

Сгенерировать Javadoc и добавить MD5/SHA-1 в MANIFEST.MF:

```bat
ant doc
```

Проверить XML:

```bat
ant xml
```

Собрать альтернативную версию:

```bat
ant alt
```

Воспроизвести музыку после сборки:

```bat
ant music
```

Собрать 4 предыдущие Git-ревизии:

```bat
ant team
```

Найти последнюю компилирующуюся Git-ревизию и сохранить diff:

```bat
ant history
```

## Что поменять перед сдачей

В `build.properties` поменяй:

```properties
scp.user=s472548
scp.path=/home/studs/s472548/opi/lab3
svn.workdir=.
env.java17.home=/Users/artem/Library/Java/JavaVirtualMachines/graalvm-jdk-17.0.12/Contents/Home
env.java21.home=/Users/artem/Library/Java/JavaVirtualMachines/ms-21.0.10/Contents/Home
```

Если у тебя `ant` запускается не как `ant`, поменяй:

```properties
ant.executable=ant
```

## Ограничение history

Цель `history` проверяет текущую ревизию и предыдущие 20 ревизий: `HEAD` ... `HEAD~20`. Для учебной сдачи этого обычно хватает. Если у тебя больше сломанных коммитов подряд, можно добавить ещё блоки `-history-try-*` по аналогии.
