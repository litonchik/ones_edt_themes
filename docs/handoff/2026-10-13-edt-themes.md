# Handoff: MVP тем для 1С:EDT 2026.1.3

Дата: 13 октября 2026 года. Проект: `C:\Users\litonchik\Documents\Codex\ones_edt_theme_plugin`.

## Запрос пользователя и разрешение на исполнение

Создать плагин с привычными палитрами IDE: Darcula и Monokai для интерфейса и BSL. Первоначально пользователь поручил только анализ, затем согласовал вариант и план.

Последнее указание пользователя:

> план подтверждаю. выполняй сам, субагентов не надо. Давай только перейдем в другой чат, составь handoff в своем каталоге codex и напиши мне промт для нового чата

**План утверждён, исполнение разрешено. Начинать с Task 1, без повторного brainstorming/согласования плана. Выполнять самостоятельно, без субагентов.** По просьбе пользователя реализация переносится в новый чат; здесь создан только handoff.

## Что уже сделано

- Изучены руководство разработчика плагинов EDT и первичная документация Eclipse/Xtext.
- Прочитаны manifests, plugin.xml, CSS и отдельные публичные классы установленной EDT через `javap`. EDT не запускалась.
- Найден установленный JDK 17; `java -version` подтверждает Axiom 17.0.18+10-LTS.
- Проверен Maven 3.10.0 по полному пути с Java 17.0.18: `--version`, exit code 0.
- Подготовлены исследование, инструкция по среде, факты локального target, AGENTS.md и план из шести задач. Пользователь просмотрел документы и утвердил план.

**Нет исходников, pom.xml, сборочного скрипта, тестов, установленного плагина или p2 ZIP. Сборка не выполнялась. Все checkbox плана ещё не выполнены. Git не инициализирован.**

## Прочитать перед реализацией

1. [AGENTS.md](C:/Users/litonchik/Documents/Codex/ones_edt_theme_plugin/AGENTS.md).
2. [Утверждённый план](C:/Users/litonchik/Documents/Codex/ones_edt_theme_plugin/docs/superpowers/plans/2026-10-13-edt-themes-mvp.md).
3. [Факты установленной платформы](C:/Users/litonchik/Documents/Codex/ones_edt_theme_plugin/docs/research/2026-10-13-installed-platform.md).
4. [Среда разработки](C:/Users/litonchik/Documents/Codex/ones_edt_theme_plugin/docs/development-environment.md).
5. [Анализ и архитектура](C:/Users/litonchik/Documents/Codex/ones_edt_theme_plugin/docs/research/2026-10-13-edt-themes.md).

Не требуется повторно собирать сведения, уже подтверждённые в этих файлах. При новых фактах корректировать документы, не считать статический анализ доказательством GUI-работоспособности.

## Точные пути инструментов

| Что | Путь |
|---|---|
| Рабочий каталог | `C:\Users\litonchik\Documents\Codex\ones_edt_theme_plugin` |
| Установленная EDT | `C:\Program Files\1C\1CE\components\1c-edt-2026.1.3+25-x86_64` |
| JDK 17 | `C:\Program Files\Axiom\AxiomJDK-Pro-17-Full` |
| Maven | `C:\apps\apache-maven-3.10.0\bin\mvn.cmd` |

Пользователь сообщил, что PATH содержит конфликтующие Java, и попросил не вызывать Maven через поиск в PATH. Использовать точные пути. Для Maven задавать `JAVA_HOME` только на время процесса сборки; глобальные PATH/JAVA_HOME не менять.

Проверенный диагностический запуск PowerShell:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Axiom\AxiomJDK-Pro-17-Full'
$env:MAVEN_BASEDIR = 'C:\Users\litonchik\Documents\Codex\ones_edt_theme_plugin'
& 'C:\apps\apache-maven-3.10.0\bin\mvn.cmd' --version
```

Команда запускалась в отдельном процессе shell. В будущем постоянном скрипте сохранить исходные process-local переменные и восстановить в `finally`. План также предусматривает `MAVEN_SKIP_RC`, чтобы user mavenrc не изменял выбранную Java.

Без явного `MAVEN_BASEDIR` Maven запускался, но сначала печатал `Access is denied`. В batch launcher найден поиск `.mvn` вверх по каталогам; при заданном basedir сообщение исчезло. Не пытаться исправлять это изменением глобальных прав или запуском от администратора.

## Подтверждённая целевая платформа

- Eclipse Platform 4.30.0, SWT 3.124.200, Xtext UI 2.33.0.
- `1cedt.ini`: `osgi.requiredJavaVersion=17`; Eclipse/theming manifests требуют Java 17.
- EDT BSL UI: `23.0.1.v202609041212`; EDT theming UI: `2.1.0.v202609041212`.
- Target bundles физически находятся в `plugins` указанной EDT.
- Для сборки выбран Tycho 4.0.13. Его документация требует минимум Maven 3.9.0 / JDK 17; полная работа с Maven 3.10.0 ещё не проверена.
- Текущий master примера 1С уже ориентирован на EDT 2026.2/Java 25. Не переносить его зависимости в наш проект без проверки.

## Ключевой архитектурный факт

`DtThemeUtil.isDarkThemeActive()` сравнивает CSS theme ID с точной строкой `org.eclipse.e4.ui.css.theme.e4_dark`; `getIconPath` опирается на этот результат. CSS contributions многих bundles EDT тоже привязаны к стандартному dark ID.

Поэтому утверждённый MVP — **профили Darcula и Monokai поверх штатной Dark EDT**, с выбором на собственной странице «Темы EDT». Стандартный selector Eclipse продолжает показывать Dark. Не создавать самостоятельные CSS theme IDs в первом MVP и не заменять стандартную тему собственной реализацией.

Основной путь — именованные цвета/реестр EDT плюс проверенные BSL preferences. Дополнительный CSS только при доказанной необходимости. Не менять парсер/semantic highlighting calculator, не патчить JAR вендора и не обходить контракты reflection.

Публичный класс с константами UI: `com._1c.g5.v8.dt.theming.ui.colors.DtColorConstants`. Идентификаторы цветов там подтверждены `javap -constants`; не использовать выдуманное имя `DtColors`.

BSL CSS содержит prefix `com._1c.g5.v8.dt.bsl.Bsl.syntaxColorer.tokenStyles.` и суффиксы `Builtin function.color`, `BSL_Keywords.color`, `Comment.color`, `Operators.color`, `Others.color`, `Preprocessor.color`, `Brackets.color`, `Strings.color`, `Numbers.color`. Node-кандидат — `com._1c.g5.v8.dt.bsl.ui`; фактические scopes, приоритеты и применение через API нужно проверить в начале реализации.

## Ограничения пользователя

- Git не использовать до получения MVP: без init, commits, branches и worktrees.
- Субагентов не запускать.
- Документацию вести в проекте на русском.
- Дополнительное системное ПО пользователь скачивает и устанавливает сам. Сейчас Maven и JDK есть; Eclipse PDE не объявлен необходимым условием старта.
- Рабочую EDT, её настройки и рабочие проекты не менять. Проверки — в копии EDT и отдельной workspace внутри проекта.
- Предпросмотр формы и прикладное содержимое MXL, графической схемы и макета оформления СКД сохраняют исходный пользовательский вид.
- Восстанавливать исходные цвета, включая отсутствие явного override. При смене наших профилей не перезаписывать первичный снимок; автоматически сохранять ручные правки пользователя.
- Итог — feature и локальный p2 ZIP только с собственными bundles, без обновления базовой платформы.

## С чего продолжить

Выполнить утверждённый план с Task 1: скрипт сборки с точными путями, локальный target, Tycho reactor, карта штатных settings и первая сборка. Далее ранняя проверка одной палитры и восстановления, затем полноценные обе палитры, страница/lifecycle, упаковка и приёмка.

Включить применимые implementation/verification навыки, но не повторять уже завершённые approval gates. User instructions про отсутствие Git и субагентов выше соответствующих skill defaults.

Сеть в shell ранее была ограничена: Invoke-WebRequest к внешним хостам не разрешал DNS; web tool работал, часть URL давала cache miss. Доступ Maven Central/Tycho пока не проверялся. Если зависимости нельзя получить, перечислить конкретные недостающие артефакты и продолжить независимую работу, не объявлять сборку успешной.

Native SWT GUI automation через доступный CUA инструмент отключена. Компиляцию, unit tests и p2 metadata можно проверять программно; если визуальную проверку выполнить нельзя, дать пользователю конкретную ручную матрицу и честно отметить непроверенные экраны. Не выдавать compile success за принятую GUI-палитру.
