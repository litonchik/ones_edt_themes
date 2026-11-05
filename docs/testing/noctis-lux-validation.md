# Noctis Lux — локальный кандидат 0.1.21

Дата: **04.11.2026**. Добавлена одна палитра `noctis-lux`, группа LIGHT.
В группе «Светлые» теперь пять схем: GitHub Light Default,
Winter is Coming (Light), Night Owl Light, Ayu Light (Bordered), Noctis Lux.
Прежние 15 палитр и общий механизм 0.1.20 сохранены.
[Первичный источник, адаптация 17 ролей и контраст](../research/2026-11-04-noctis-lux.md).
MIT Noctis побайтово совпала с уже включённым notice; атрибуция расширена на Light.

## Проверки

**101 unit tests**, 0 failures/errors; Maven 3.9.11 / Tycho 4.0.13 / Java 17.
ZIP verify: только собственные bundle/feature, 38 classes major 61, 16 палитр,
лицензии и p2 metadata. Подготовка локального p2-сайта — PASS, без публикации.
37 из 38 Java-классов побайтово совпадают с 0.1.20; изменён только PaletteCatalog:
[сравнение](2026-11-04-noctis-lux/noctis-lux-binary-comparison.json).
В частности, исправление Xtext viewer colors из 0.1.19 сохранено побайтово.
Прежние 15 файлов палитр и ZIP 0.1.17 / 0.1.18 / 0.1.19 / 0.1.20 не изменились:
[SHA-256](2026-11-04-noctis-lux/previous-palettes-and-zips.json).
Ресурсы и plugin.xml текущих исходников совпали с shipping JAR.

| Windows x64 EDT | Axiom JVM | Candidate | CandidateRestart | CandidateSurfaces |
|---|---|---:|---:|---:|
| 2026.1.2 | 17.0.18 | 87 | 208 | 222 |
| 2026.1.3 | 17.0.18 | 87 | 208 | 222 |
| 2026.2.2 | 25.0.2 | 87 | 208 | 222 |

Всего **1551 runtime PASS, 0 FAIL**. Проверяется новая палитра.
Полная матрица 15 прежних палитр из 0.1.20 повторно не запускалась,
поскольку общий механизм не менялся.

Candidate проверяет пятую светлую тему, label, preview/Cancel/Apply,
33 named colors, реальные BSL background/foreground/keyword/comment/string/number
и selection foreground. Переходы Noctis Minimus → GitHub Light → Winter Light →
Night Owl Light → Ayu Light → Noctis Lux не меняют текст и modification stamp.
Проверены личный keyword новой схемы, независимость personal keys всех
15 прежних схем, сохранение общего шрифта.

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
Отдельно снято однострочное выделение обычного идентификатора: используется
непрозрачный upstream list.activeSelectionBackground #B6E1E7. Изначально
рассчитанный editor selection #D8EEEC почти совпадал с line #DAEEEE (1.005:1),
поэтому перед финальной поставкой заменён на более различимый исходный цвет
(1.170:1 относительно line, 5.956:1 для selection text). Добавлена unit-проверка
различимости, проверены реальные RGB и неизменность текста при выделении.
Вся таблица выше относится к финальному shipping bundle после этой адаптации.

## Очистка и границы

Отдельные Cleanup / Reconcile / p2 Uninstall / Audit — PASS во всех копиях.
Сохранены 1257 / 1267 / 1234 базовых ID/версий. Runtime bundle каждой версии
побайтово совпал с shipping ZIP (shipping-bundle.txt в папках свидетельств).
Рабочие EDT/workspace не менялись. Временные junction подключали только прежние
изолированные копии и Maven cache; после проверки удалены, цели сохранены.

Native SWT после смены Light/Dark основы может требовать restart. Открытое
BSL-сравнение требует переоткрытия для новой подсветки — прежнее ограничение.
Полный metadata query wizard, Mac 0.1.21, все tooltip/completion/DPI-сценарии
не проверены. Известные native Properties CSS / Right QL / wmic сообщения
не объявлены исправленными. Hibernus, Lilac и светлая Dracula не добавлялись.

## Поставка и воспроизведение

Bundle: `0.1.21.202611041025`.
ZIP SHA-256: `a034fddcbda425eea433646881b27ad4424dcf974f71963d8d4b94b34a220bfc`.
Локальный `dist/edt-themes-0.1.21.zip` содержит все пять светлых тем.
Публичные Releases и сайт обновлений остаются на 0.1.16; commit/push/release
не выполнялись. Пользователь проверит палитры локально и отдельно разрешит push.

В отдельной копии EDT: p2 Install → build-workbench-probe → Candidate →
CandidateRestart → CandidateSurfaces; во всех candidate-фазах `-ProfileId noctis-lux`.
Для README: CandidateGalleryPrepare → CandidateGallery.
Завершение: Cleanup → Reconcile → p2 Uninstall → Audit.
EDT 2026.1 — Axiom JDK 17; EDT 2026.2.2 — Axiom JDK 25.
