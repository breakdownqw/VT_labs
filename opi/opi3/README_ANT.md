# Ant-сборка для лабораторной №3

Этот вариант сделан под macOS. Вся логика находится в `build.xml` через Ant-теги.

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

