# Ayu Light (Bordered) — локальный кандидат 0.1.20

Дата: **04.11.2026**. Добавлена одна палитра `ayu-light`, группа LIGHT.
В группе «Светлые» теперь четыре схемы: GitHub Light Default,
Winter is Coming (Light), Night Owl Light, Ayu Light (Bordered).
Прежние 14 палитр и общий механизм 0.1.19 сохранены.
[Первичный источник, адаптация 17 ролей и контраст](../research/2026-11-04-ayu-light.md).
MIT Ayu побайтово совпала с уже включённым notice; атрибуция расширена на Light.

## Проверки

**97 unit tests**, 0 failures/errors; Maven 3.9.11 / Tycho 4.0.13 / Java 17.
ZIP verify: только собственные bundle/feature, 38 classes major 61, 15 палитр,
лицензии и p2 metadata. Подготовка локального p2-сайта — PASS, без публикации.
37 из 38 Java-классов побайтово совпадают с 0.1.19; изменён только PaletteCatalog:
[сравнение](2026-11-04-ayu-light/ayu-light-binary-comparison.json).
В частности, исправление Xtext viewer colors из 0.1.19 сохранено побайтово.
Прежние 14 файлов палитр и ZIP 0.1.17 / 0.1.18 / 0.1.19 не изменились:
[SHA-256](2026-11-04-ayu-light/previous-palettes-and-zips.json).
Ресурсы и plugin.xml текущих исходников совпали с shipping JAR.

| Windows x64 EDT | Axiom JVM | Candidate | CandidateRestart | CandidateSurfaces |
|---|---|---:|---:|---:|
| 2026.1.2 | 17.0.18 | 84 | 207 | 222 |
| 2026.1.3 | 17.0.18 | 84 | 207 | 222 |
| 2026.2.2 | 25.0.2 | 84 | 207 | 222 |

Всего **1539 runtime PASS, 0 FAIL**. Проверяется новая палитра.
Полная матрица 14 прежних палитр из 0.1.19 повторно не запускалась,
поскольку общий механизм не менялся.

Candidate проверяет четвёртую светлую тему, label, preview/Cancel/Apply,
33 named colors, реальные BSL background/foreground/keyword/comment/string/number
и selection foreground. Переходы Ayu Dark → GitHub Light → Winter Light →
Night Owl Light → Ayu Light не меняют текст и modification stamp.
Проверены личный keyword новой схемы, независимость personal keys всех
14 прежних схем, сохранение общего шрифта.

CandidateRestart проверяет native Light и личный цвет после JVM restart,
сброс роли к стилю и удаление personal key, правильную группу/профиль,
сохранение введённого вручную explicit background после повторного apply/CSS,
точный restore значений **и отсутствия записей**, сохранение прежних personal keys.

CandidateSurfaces проверяет background/foreground/selection и RGB
keyword/number(native text)/string/comment/parameter/identifier в QL, QL DCS
и двух реальных embedded контролах конструктора. Для UI используется
ограниченное ожидание всех четырёх цветов; текст запросов остаётся неизменным.
Проверены три native BSL merge panes после переоткрытия в новой палитре,
точный restore всех затронутых предпочтений.

Для README снято полное окно EDT 2026.1.3 после restart, 884×581:
Navigator / BSL / Properties, стандартные цвета без personal overrides и правки PNG.
Визуально просмотрены галерея, страница тем, embedded QL/DCS и BSL merge.

## Очистка и границы

Отдельные Cleanup / Reconcile / p2 Uninstall / Audit — PASS во всех копиях.
Сохранены 1257 / 1267 / 1234 базовых ID/версий. Runtime bundle каждой версии
побайтово совпал с shipping ZIP (shipping-bundle.txt в папках свидетельств).
Рабочие EDT/workspace не менялись. Временные junction подключали только прежние
изолированные копии и Maven cache; после проверки удалены, цели сохранены.

Native SWT после смены Light/Dark основы может требовать restart. Открытое
BSL-сравнение требует переоткрытия для новой подсветки — прежнее ограничение.
Полный metadata query wizard, Mac 0.1.20, все tooltip/completion/DPI-сценарии
не проверены. Известные native Properties CSS / Right QL / wmic сообщения
не объявлены исправленными. Unbordered и светлая Dracula не добавлялись.

## Поставка и воспроизведение

Bundle: `0.1.20.202611040911`.
ZIP SHA-256: `e920b4a03c4fc7331e96dea1ecda3182aa2003907340d40696c216c834f7be4b`.
Локальный `dist/edt-themes-0.1.20.zip` содержит все четыре светлые темы.
Публичные Releases и сайт обновлений остаются на 0.1.16; commit/push/release
не выполнялись. Пользователь проверит палитры локально и отдельно разрешит push.

В отдельной копии EDT: p2 Install → build-workbench-probe → Candidate →
CandidateRestart → CandidateSurfaces; во всех candidate-фазах `-ProfileId ayu-light`.
Для README: CandidateGalleryPrepare → CandidateGallery.
Завершение: Cleanup → Reconcile → p2 Uninstall → Audit.
EDT 2026.1 — Axiom JDK 17; EDT 2026.2.2 — Axiom JDK 25.
