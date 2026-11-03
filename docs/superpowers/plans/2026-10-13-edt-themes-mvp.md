# EDT Themes MVP Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans for native execution, or superpowers:subagent-driven-development if the user chooses delegation. Steps use checkbox syntax. Do not use Git before MVP.

**Goal:** Поставить локальный p2 ZIP плагина Darcula/Monokai для EDT 2026.1.3 с согласованной палитрой интерфейса и BSL, сохранением выбора и восстановлением исходных настроек.

**Architecture:** Один UI bundle с каталогом палитр, адаптером штатных цветов EDT, сохранением снимка и страницей настроек. Профили работают поверх штатной Dark, сохраняя стандартный CSS theme ID и dark icons. Сначала проверить приоритеты настроек и восстановление на одном профиле; затем расширить покрытие и упаковать обе палитры.

**Tech Stack:** Windows x64, Eclipse 4.30.0, Java 17, Maven 3.10.0, Tycho 4.0.13, SWT/JFace, Eclipse preferences. Unit tests — обычный Maven-модуль с JUnit 4.13.2 для Java-кода без зависимости от workbench; GUI-проверки — в тестовой EDT.

**Spec:** [Согласованный подход](../../research/2026-10-13-edt-themes.md), [локальная платформа](../../research/2026-10-13-installed-platform.md). Пользователь подтвердил этот план 13 октября 2026 года и поручил самостоятельное исполнение без субагентов. Исполнение перенесено в новый чат по просьбе пользователя. Реализация и доступная приёмка MVP выполнены 13–14 октября 2026. Фактические результаты и явно непроверенные экраны — docs/testing/mvp-validation.md и docs/testing/progress.md.

**Статус исполнения:** checkbox задач 6 по DPI и исключённым макетам означает, что выполнена предусмотренная самим пунктом передача ручной матрицы/фиксация непроверенного экрана. Полная визуальная приёмка этих экранов не заявлена. Проверены публичные API и настоящий BSL editor, Shell, Apply/Cancel, перезапуск, rollback/conflicts и p2 установка/удаление. Для сборки по результату Task 1 пользователь установил Maven 3.9.11; исходный выбор 3.10.0 ниже оставлен как история утверждённого плана.

## Global Constraints

- Цель: EDT 2026.1.3, Windows x64; Eclipse 4.30.0, JavaSE-17.
- JDK: `C:\Program Files\Axiom\AxiomJDK-Pro-17-Full`.
- Maven: `C:\apps\apache-maven-3.10.0\bin\mvn.cmd`. Не искать Maven/Java через PATH.
- EDT target: `C:\Program Files\1C\1CE\components\1c-edt-2026.1.3+25-x86_64`.
- Глобальные переменные среды и файлы установленной EDT не менять. Проверки выполнения плагина — в копии EDT и отдельной рабочей области внутри проекта.
- Git до MVP не использовать. Последние шаги задач — проверка и отметка результата, без коммитов.
- Стандартный CSS theme ID: `org.eclipse.e4.ui.css.theme.e4_dark`; не выдавать собственную CSS-тему за штатную.
- Не менять содержимое предпросмотра формы, табличного документа, графической схемы и макета оформления СКД.
- Не менять шрифты, клавиши, форматирование и кодировку. Палитры меняют только явно перечисленные цвета и необходимые флаги SystemDefault редактора.
- ZIP содержит только собственный bundle/feature и p2 metadata; базовые bundles Eclipse/EDT не включать.
- Не устанавливать дополнительное системное ПО автоматически. Обычное получение зависимостей сборки Maven сохранять в локальном кэше проекта; при недоступности сети сообщить конкретный недостающий артефакт.

## Review Focus

1. Исходное значение не задано явно: после восстановления значение снова отсутствует, а не превращается в копию текущего default.
2. Пользователь меняет цвет после включения профиля: автоматический выход сохраняет такую правку; принудительное восстановление выполняется только явным действием.
3. Ошибка записи/сохранения на середине применения: исходный снимок остаётся доступен, частичные изменения откатываются, активный профиль не объявляется успешным.
4. Смена штатной темы во время активного профиля: управление прекращается без возвращения пользователя обратно в Dark и без циклических событий.
5. Перезапуск и многократное Darcula → Monokai: исходный снимок не перезаписывается; штатный CSS не сбрасывает профиль после старта или открытия редактора.

Первые три условия покрываются задачей 3, четвёртое и пятое — задачей 4 и GUI-матрицей задачи 6.

## Структура файлов

- `pom.xml` — Tycho reactor: bundle, feature, repository; версия проекта `0.1.0-SNAPSHOT`.
- `targets/edt-2026.1.target` — Directory location установленного target, путь передаётся свойством сборки; только Windows x64.
- `scripts/build.ps1` — параметры `MavenHome`, `JavaHome`, `EdtHome`, действия `Check`, `Test`, `Verify`; явный запуск и проверка exit codes.
- `scripts/prepare-test-edt.ps1` — копия целевой EDT в `.work/edt-test`, отдельные configuration/p2/workspace, без запуска рабочей EDT пользователя.
- `bundles/ru.litonchik.edt.themes/` — `META-INF/MANIFEST.MF`, `plugin.xml`, `plugin.properties`, `build.properties`, `pom.xml`, `src/ru/litonchik/edt/themes/`.
- `.../model/` — `Palette.java`, `PaletteCatalog.java`, `SettingKey.java`, `OriginalValue.java`, `SettingAccess.java`, `SettingsSnapshot.java`, `SnapshotService.java`, `RestoreResult.java`, `ProfileState.java`; Java без Eclipse imports.
- `.../edt/` — `EdtSettingsAdapter.java`, `EdtCompatibility.java`; адаптация реальных keys/scopes и проверка платформы.
- `.../runtime/` — `ThemeCoordinator.java`, `SnapshotPreferences.java`, `ThemeStartup.java`; lifecycle, сохранение и события.
- `.../ui/ThemePreferencePage.java` — выбор, BSL checkbox, Apply/OK/Cancel, восстановление.
- `.../resources/palettes/{darcula,monokai}.properties` — исходные RGB по смысловым ролям.
- `.../resources/edt-2026.1-settings.properties` — проверенные targets: роль → scope/node/key, исходное семейство, ссылка на локальный источник.
- `tests/unit/` — самостоятельный Maven test module; компилирует только `model/` из bundle и содержит unit tests. Исключён из Tycho reactor, вызывается скриптом отдельно.
- `features/ru.litonchik.edt.themes.feature/{pom.xml,feature.xml}` — пользовательский компонент.
- `repositories/ru.litonchik.edt.themes.repository/{pom.xml,category.xml}` — p2 ZIP.
- `docs/reference/edt-2026.1-settings.md` — карта проверенных настроек и области покрытия.
- `docs/testing/mvp-validation.md` — выполненные проверки, остаточные ограничения, скриншоты.
- `docs/user-guide.md` — установка, выбор профиля, перезапуск, восстановление, удаление.

`.cache/m2` — зависимости сборки; `.work` — экспериментальный экземпляр EDT и workspace; `dist` — готовый ZIP. Все эти каталоги внутри проекта. Скрипт подготовки не удаляет существующую копию рекурсивно: при уже занятом каталоге завершает работу с указанием пути.

## Начальные палитры для рассмотрения

Названия описывают адаптацию цветовых семейств к EDT, а не перенос всей внешней системы тем.

| Роль | Darcula | Monokai |
|---|---|---|
| Фон редактора/основной области | `#2B2B2B` | `#272822` |
| Основной текст | `#A9B7C6` | `#F8F8F2` |
| Фон панелей/контролов | `#3C3F41` | `#30312B` |
| Граница | `#555555` | `#49483E` |
| Фон выделения | `#214283` | `#49483E` |
| Текст выделения | `#FFFFFF` | `#F8F8F2` |
| Текущая строка | `#323232` | `#32332D` |
| Комментарии | `#808080` | `#75715E` |
| Ключевые слова | `#CC7832` | `#F92672` |
| Строки | `#6A8759` | `#E6DB74` |
| Числа | `#6897BB` | `#AE81FF` |
| Препроцессор/директивы | `#BBB529` | `#A6E22E` |
| Ссылки/акцент | `#589DF6` | `#66D9EF` |

Ошибки, предупреждения, фокус, hover и disabled должны сохранять различимость. Их значения и реально поддерживаемые targets фиксируются в карте при реализации; не приравнивать разные состояния к одному цвету только ради формального покрытия.

## Task 1: Проверяемая сборка и карта target

**Files:** создать `pom.xml`, `targets/edt-2026.1.target`, `scripts/build.ps1`, metadata bundle/feature/repository, `docs/reference/edt-2026.1-settings.md`; дополнить `docs/development-environment.md`.

**Interfaces:** `build.ps1 -Action Check|Test|Verify -MavenHome <directory> -JavaHome <directory> -EdtHome <directory>`. Значения по умолчанию — пути Global Constraints. `Check` не запускает EDT и не меняет настройки. `Verify` сначала выполняет unit tests, затем Tycho reactor.

- [x] Проверить обязательные исполняемые файлы и Java major 17. На неверном пути/Java major завершаться ненулевым кодом до Maven.
- [x] Скрипт сохраняет прежние process-local `JAVA_HOME`, `MAVEN_BASEDIR`, `MAVEN_SKIP_RC`, устанавливает Java 17, каталог проекта и запрет user mavenrc на время Maven; восстанавливает их в `finally`. Не менять PATH и пользовательские/машинные переменные.
- [x] Настроить Tycho 4.0.13 и `JavaSE-17`, локальный Directory target из установленной EDT. Использовать `${edt.home}`/генерацию target в `.work`, если интерполяция Directory location не поддерживается выбранным resolver. Конечная проверка — компиляция против точных локальных bundles, без target latest.
- [x] Прочитать `DtColorConstants`, theming CSS и `plugin.xml`, проверить scope/node/key для основных UI цветов, general editor, BSL, console и hints. Записать карту с источниками. BSL prefix уже подтверждён: `com._1c.g5.v8.dt.bsl.Bsl.syntaxColorer.tokenStyles.`; node-кандидат `com._1c.g5.v8.dt.bsl.ui` требуется проверить.
- [x] Создать пустую feature/bundle/repository, собрать: `& .\scripts\build.ps1 -Action Verify`. Ожидается BUILD SUCCESS, разрешение зависимостей по локальному target, p2 ZIP без базовых библиотек вендора. Проверку class-file major 61 выполнить после появления Java-кода в задаче 2. На первом шаге unit module ещё не добавлен; скрипт проверяет его наличие перед запуском.
- [x] Если Maven/Tycho 4.0.13 несовместимы фактически, зафиксировать конкретную ошибку и минимальное изменение версии; не переключать Java 17 на 25. Если сетевой доступ закрыт, указать необходимые зависимости, без установки другого Maven автоматически.

## Task 2: Две палитры и проверка применения одного профиля

**Files:** `model/{Palette,PaletteCatalog,SettingKey,OriginalValue,SettingAccess}.java`, `resources/palettes/*.properties`, `resources/edt-2026.1-settings.properties`, `edt/{EdtSettingsAdapter,EdtCompatibility}.java`, `tests/unit/pom.xml`, `tests/unit/src/test/java/ru/litonchik/edt/themes/PaletteCatalogTest.java`, `scripts/prepare-test-edt.ps1`.

**Interfaces:** `PaletteCatalog.load(String profileId): Palette`; `Palette(String id, Map<String,String> colors)`; `SettingKey(String scope, String node, String key)`; `OriginalValue(boolean present, String value)` — при `present=false` value отсутствует; `SettingAccess.read(SettingKey): OriginalValue`, `write(SettingKey,OriginalValue): void`, `flush(): void`. `EdtCompatibility.verify(): void` с понятной ошибкой при неподдерживаемой платформе; `EdtSettingsAdapter.plan(Palette palette, boolean includeBsl): Map<SettingKey,OriginalValue>` — значения для записи, без побочных эффектов. `EdtSettingsAdapter` реализует `SettingAccess`.

- [x] Написать тесты `darculaUsesApprovedEditorColors`, `monokaiUsesApprovedEditorColors`, `unknownProfileFails`, `invalidRgbFails`, `missingRequiredRoleFails`. Assert для Darcula: `#2B2B2B`, `#A9B7C6`; Monokai: `#272822`, `#F8F8F2`; неверная роль/цвет дают `IllegalArgumentException`.
- [x] Выполнить `& .\scripts\build.ps1 -Action Test`; подтвердить, что новые тесты падают из-за отсутствующей реализации.
- [x] Реализовать каталог из Properties, валидацию RGB и преобразование в `r,g,b`. Никаких внешних библиотек палитр, произвольного импорта или динамического исполнения CSS.
- [x] Выполнить tests до PASS; запустить Verify для компиляции адаптера на target и проверить class-file major 61 собственного кода.
- [x] Подготовить `.work/edt-test` и `.work/workspace-probe`, установить экспериментальную feature только в копию. Подготовка проверяет, что writable destination находится внутри проекта и не совпадает с исходной EDT. Не переносить рабочие проекты пользователя.
- [x] В тестовой EDT проверить один профиль: UI фон/текст, BSL keywords/strings/comments, повторное открытие редактора и перезапуск. Сравнить реально выбранную JVM с Java 17.
- [x] Установить путь записи: предпочитать instance preferences, штатный store и уведомления registry. Если стандартный CSS восстанавливает значения, исследовать порядок событий/адаптер; не добавлять циклический таймер перезаписи. Зафиксировать результат в карте. До устойчивого применения не расширять оформление всех экранов.

## Task 3: Снимок, откат и пользовательские правки

**Files:** `model/{SettingsSnapshot,SnapshotService,RestoreResult}.java`, `runtime/SnapshotPreferences.java`; tests `SnapshotServiceTest.java`, `SnapshotEncodingTest.java` в unit module.

**Interfaces:** потребляет `SettingKey`, `OriginalValue`, `SettingAccess` из задачи 2. Создаёт `SnapshotService.apply(Map<SettingKey,OriginalValue>): SettingsSnapshot`, `restore(boolean force): RestoreResult`. `RestoreResult` содержит восстановленные keys, сохранённые пользовательские конфликты и ошибки. `SnapshotPreferences` сохраняет исходный снимок, last-applied и состояние незавершённой операции в собственном node `ru.litonchik.edt.themes`.

- [x] Написать тесты: `unsetValueRestoresToUnset`, `switchingProfilesKeepsFirstSnapshot`, `userEditSurvivesAutomaticRestore`, `forceRestoreUsesOriginalSnapshot`, `writeFailureRollsBackPreviousWrites`, `flushFailureKeepsRecoverableJournal`, `snapshotRoundTripPreservesUnicodeAndSpaces`.
- [x] В `unsetValueRestoresToUnset` исходное `present=false`; после apply/restore assert `present=false`. В пользовательском конфликте original=`10,10,10`, last-applied=`43,43,43`, пользователь=`60,60,60`: restore(false) оставляет `60,60,60`; restore(true) возвращает `10,10,10`.
- [x] Запустить tests и зафиксировать FAIL, затем реализовать снимок и алгоритм. Снимок записывать и сохранять до первой мутации, исходный снимок не заменять при смене профиля. Удаление явного значения через adapter восстанавливает отсутствие override.
- [x] При ошибке применять компенсационный откат только затронутых keys; сохранять журнал при неудавшемся откате. Профиль помечается активным только после сохранения всех изменений.
- [x] Реализовать сериализацию без внешнего JSON serializer: отдельные preference child nodes, безопасное кодирование SettingKey, версия формата `1`. Повреждённый/неизвестный формат не применять и не удалять; сообщать ошибку восстановления.
- [x] Выполнить tests до PASS и Verify. В тестовой EDT проверить восстановление на чистых defaults и на нескольких заданных пользователем цветах.

## Task 4: Lifecycle и выбор профиля

**Files:** `runtime/{ThemeCoordinator,ThemeStartup}.java`, `ui/ThemePreferencePage.java`, `plugin.xml`, `plugin.properties`, `model/ProfileState.java`; `tests/unit/src/test/java/ru/litonchik/edt/themes/ProfileStateTest.java`.

**Interfaces:** `ThemeCoordinator.apply(String profileId, boolean includeBsl): void`, `restore(boolean force): RestoreResult`, `onThemeChanged(String cssThemeId): void`, `close(): void`. Чистая `ProfileState` хранит выбранный профиль, includeBsl и признак собственного apply; методы `onThemeChanged(String): boolean` (нужно ли освобождать профиль), `beginApply(): boolean` (разрешён ли вход), `endApply(): void`, `activeProfile(): String` используются coordinator и unit tests. `ThemePreferencePage` использует coordinator только при Apply/OK или явном подтверждённом восстановлении. `ThemeStartup` восстанавливает состояние через UI thread после инициализации штатного theme engine; снимает listeners при остановке bundle.

- [x] Написать tests `externalLightThemeDoesNotSwitchBackToDark`, `ownApplyDoesNotReenterCoordinator`, `restartUsesSavedProfileWithoutReplacingSnapshot`, `disableBslRestoresOnlyManagedBslKeys`. Запустить FAIL перед чистой state-machine, затем PASS после реализации.
- [x] Обработать события изменения native theme через публичный API; защита от рекурсивного apply и против записи при неизвестном target. На чужой CSS theme восстановить управляемые keys с сохранением конфликтов и прекратить профиль, не возвращать прежний CSS ID.
- [x] Страница «Темы EDT» под General → Appearance: Darcula/Monokai, «Применять палитру BSL», Apply/OK, «Восстановить исходное оформление». Cancel без Apply не меняет настройки; после Apply Cancel не отменяет уже подтверждённое применение.
- [x] При первом применении вне Dark сохранить исходные CSS/legacy IDs, переключить штатную Dark и затем палитру. При явном полном восстановлении вернуть исходные IDs; при внешней смене темы сохранить выбор пользователя.
- [x] Обычный shutdown не восстанавливает preferences. На startup не перезаписывать исходный снимок. При незавершённом журнале сначала выполнить безопасное восстановление/сообщить конфликт, не объявлять успешный профиль.
- [x] Обрабатывать включение/выключение BSL при уже активном профиле: вернуть только управляемые BSL overrides, сохранить остальные цвета и исходный общий снимок.
- [x] Выполнить Verify и GUI-проверки Cancel, Apply, переключения Dark/Light, повторного старта. Повторить десять смен профиля, проверить отсутствие циклов и ошибок SWT в журнале. **Результат:** десять смен и assertions прошли; shutdown диагностические сообщения штатных EDT служб сохраняются в логах, полная чистота vendor log не заявлена.

## Task 5: Покрытие экранов и p2 ZIP

**Files:** дополнить palette/settings resources, metadata feature/repository, `docs/reference/edt-2026.1-settings.md`; добавить проверки `PaletteCoverageTest.java`.

**Interfaces:** стабильные ID профилей `darcula`, `monokai`; обе палитры имеют одинаковый набор обязательных ролей. BSL checkbox управляет только BSL targets. ZIP feature ID `ru.litonchik.edt.themes.feature`, bundle ID `ru.litonchik.edt.themes`, release `0.1.0` с build qualifier без Git.

- [x] Добавить failing coverage tests: обе палитры имеют совпадающие roles, selection foreground отличается от background, обязательные targets присутствуют в проверенной карте, targets не включают font/keybinding/formatter/model-data settings.
- [x] Настроить панели навигации, свойства, вкладки, ошибки, основные диалоги, hints и консоль. Для query inside BSL проверить реальные категории; не заявлять отдельную палитру query editor, если она не реализована.
- [x] Выполнить tests до PASS, Verify до BUILD SUCCESS. Проверить p2 metadata и состав ZIP; никаких `org.eclipse.*` или `com._1c.*` binary bundles внутри репозитория.
- [x] Сохранить проверенный ZIP в `dist/edt-themes-0.1.0.zip`; предусмотреть генерацию контрольной SHA-256 суммы. Отметить, какие экраны следуют палитре полностью и где осталась штатная Dark.

## Task 6: Приёмка и инструкция

**Files:** `docs/testing/mvp-validation.md`, `docs/testing/screenshots/`, `docs/user-guide.md`, обновить `README.md`, AGENTS.md и документ среды по фактическому состоянию.

- [x] Установить итоговый ZIP в чистую тестовую копию EDT. Проверить, что p2 не требует замены базовых bundles. Показать путь проверяемой копии в отчёте.
- [x] Проверить обе палитры на основных экранах и BSL с русскими/английскими ключевыми словами, комментариями, строками, числами, directives и запросом внутри строки.
- [x] Проверить обычный и повышенный масштаб Windows; selection, focus, hover, disabled, error и warning states. Если доступное средство автоматизации не управляет native SWT, явно передать пользователю ручную матрицу проверки; не заявлять визуальную приёмку по одной компиляции.
- [x] Проверить предпросмотр формы, MXL, графическую схему и макет СКД на небольшом тестовом проекте. Если соответствующий экран невозможно проверить, отметить его как непроверенный, а не засчитывать автоматически.
- [x] Проверить десять переключений, открытие нового окна/редактора, перезапуск, возврат исходной темы, конфликт пользовательского цвета, выключение BSL и Cancel.
- [x] Проверить удаление: сначала восстановление, затем uninstall feature; перезапуск не оставляет настройки плагина активными. В пользовательской инструкции указать этот порядок.
- [x] Финальный `& .\scripts\build.ps1 -Action Verify`, проверка содержимого ZIP и сумм. Сохранить конкретные результаты tests/build/GUI и ограничения. Передать ZIP и инструкцию; не устанавливать его в рабочую EDT пользователя автоматически.

## Самопроверка плана

- Все согласованные функции имеют задачи: сборка/target — 1; палитры/BSL — 2 и 5; восстановление — 3; страница/lifecycle — 4; p2 — 5; приёмка/инструкция — 6.
- Java 17, native Dark, исключения предпросмотра, запрет Git и явные пути инструментов сохранены.
- Конкретные runtime scopes и реакция CSS вынесены в раннюю проверку; это проверяемый результат задачи 1–2, от которого зависят последующие adapters.
- Дополнительный CSS разрешён только после проверки необходимости; основной способ MVP — настройка штатных цветовых механизмов.
- Независимые agents не запускались. Пользователь выбрал самостоятельную реализацию без субагентов; исполнять план в новом рабочем чате, начиная с задачи 1.

## Источники выбора сборки

В [документации Tycho 4.0.13](https://tycho.eclipseprojects.io/doc/4.0.13/target-platform-configuration/plugin-info.html) указаны minimum Maven 3.9.0 и JDK 17. [Target platform](https://tycho.eclipseprojects.io/doc/4.0.13/TargetPlatform.html) описывает разделение target и окружения сборки. Это подтверждает выбор версии для пробы; фактическая совместимость с Maven 3.10.0 подтверждается только задачей 1.
