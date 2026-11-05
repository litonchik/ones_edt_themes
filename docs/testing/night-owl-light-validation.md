# Night Owl Light — локальный кандидат 0.1.19

Дата: **04.11.2026**. Добавлена одна палитра `night-owl-light`, группа LIGHT.
В группе «Светлые» теперь GitHub Light Default, Winter is Coming (Light), Night Owl Light.
Прежние 13 палитр сохранены. Исправлен сброс цветов viewers штатной Light CSS.
[Первичный источник, адаптация 17 ролей и контраст](../research/2026-11-04-night-owl-light.md).
Существующий текст MIT Night Owl сохранён; атрибуция расширена на Light.

## Проверки

**93 unit tests**, 0 failures/errors; Maven 3.9.11 / Tycho 4.0.13 / Java 17.
ZIP verify: собственные bundle/feature, 38 classes major 61, 14 палитр,
лицензии и p2 metadata. Локальная подготовка p2-сайта из проверенного ZIP — PASS,
публикация не выполнялась. Изменены реестр и XtextPresentationRefresh
(включая его inner classes); остальные 34 из 38 классов
побайтово совпадают с 0.1.18: [сравнение](2026-11-04-night-owl-light/night-owl-light-binary-comparison.json).
Прежние 13 файлов палитр и ZIP 0.1.17 / 0.1.18 побайтово сохранены:
[SHA-256](2026-11-04-night-owl-light/previous-palettes-and-zips.json).

| Windows x64 EDT | Axiom JVM | Candidate | CandidateRestart | CandidateAllSurfaces |
|---|---|---:|---:|---:|
| 2026.1.2 | 17.0.18 | 81 | 206 | 808 |
| 2026.1.3 | 17.0.18 | 81 | 206 | 808 |
| 2026.2.2 | 25.0.2 | 81 | 206 | 808 |

В итоговой матрице **3285 runtime PASS, 0 FAIL**. Из-за изменения общего
обновления цветов проверены **все 14 палитр** в каждой из трёх EDT.

## Обнаруженный дефект и исправление

До исправления [surface-проверка падала](2026-11-04-night-owl-light/before-fix-surface-report.txt):
в четырёх открытых QL viewers были записаны правильные preferences, но после
штатного Light CSS фон оказывался #FFFFFF, foreground #000000 вместо
#FBFBFB / #403F53. Белый фон GitHub/Winter Light скрывал этот дефект.
Проверка также исключила surrendered/manual keys и ожидала завершения UI.
Surface probe теперь использует один ThemeStartup coordinator, как страница плагина.

XtextPresentationRefresh восстанавливает background/foreground/selection из
эффективной language preference chain после CSS, только при SystemDefault=false.
Используются публичные APIs; новых preference targets и записей нет.
Точечная проверка CandidateSurfaces после исправления — 222 PASS на EDT 2026.1.3,
затем выполнена итоговая матрица ниже с тем же shipping bundle.

Candidate проверяет третью светлую тему и её label, preview/Cancel/Apply,
33 named colors, реальные BSL keyword/comment/string/number и selection foreground.
Переходы Night Owl → GitHub Light → Winter Light → Night Owl Light не меняют
текст и modification stamp. Проверены личный keyword новой схемы и независимость
personal keys всех 13 прежних схем. Общий шрифт не меняется.
CandidateRestart проверяет native Light, личный цвет после JVM restart,
сброс роли к стилю и удаление personal key, открытие правильной группы/профиля,
сохранение введённого вручную explicit background после повторного apply/CSS,
точный restore значений **и отсутствия записей**, сохранение personal keys прежних тем.

CandidateAllSurfaces проверяет все 14 палитр: background/foreground/selection,
RGB keyword/number(native text)/string/comment/parameter/identifier и
неизменность текста в QL, QL DCS и двух реальных embedded контролах конструктора.
Перед этой фазой сбрасываются искусственные personal markers предыдущих фаз,
их сохранность уже проверена отдельно. Первый расширенный запуск
[обнаружил маркер #123456 вместо стандартного комментария Darcula](2026-11-04-night-owl-light/test-fixture-marker-report.txt);
исправлена только подготовка тестовой среды, итоговый shipping ZIP не менялся.
Первый прогон EDT 2026.2.2 также [остановился на selection background](2026-11-04-night-owl-light/26.2-selection-wait-report.txt)
в embedded Shades of Purple. Ожидание только editor.background заменено
ограниченным ожиданием всех четырёх SWT colors; добавлены expected/actual
значения выделения. Итоговый прогон 26.2.2 с тем же shipping ZIP — 808 PASS.
Проверены три native BSL merge panes после переоткрытия
в новой палитре и точное восстановление затронутых предпочтений.

Для README снято полное окно EDT 2026.1.3 после restart, 884×581:
Navigator / BSL / Properties, стандартные цвета без personal overrides и правки PNG.
Визуально просмотрены галерея, страница тем, embedded QL/DCS и BSL merge.

## Очистка и границы

Отдельные Cleanup / Reconcile / p2 Uninstall / Audit — PASS во всех копиях.
Сохранены 1257 / 1267 / 1234 базовых ID/версий; shipping bundle совпал с runtime
в каждой версии (shipping-bundle.txt в папках свидетельств).
Рабочие EDT/workspace не менялись. Временные junction подключали только прежние
изолированные копии и Maven cache; после проверки удалены, цели сохранены.

Native SWT после смены Light/Dark основы может требовать restart. Уже открытое
BSL-сравнение требует переоткрытия для новой подсветки — прежнее ограничение.
Полный metadata query wizard, Mac 0.1.19, все tooltip/completion/DPI-сценарии
не проверены. Известные native Properties CSS, Right QL и wmic сообщения
не объявлены исправленными. Светлый вариант Dracula не добавлялся.

## Поставка и воспроизведение

Bundle: `0.1.19.202611040723`.
ZIP SHA-256: `2f9ab2ca4227b290318f3081af742a839ad1451389f40b8f7b655b55a0053018`.
Локальный `dist/edt-themes-0.1.19.zip` содержит все три светлые палитры.
Публичные Releases и сайт обновлений остаются на 0.1.16; commit/push/release
не выполнялись. Пользователь проверит несколько светлых тем вместе.

В отдельной копии EDT: p2 Install → build-workbench-probe → Candidate →
CandidateRestart → CandidateAllSurfaces, во всех candidate-фазах
`-ProfileId night-owl-light`. Для галереи — CandidateGalleryPrepare → CandidateGallery.
Завершение: Cleanup → Reconcile → p2 Uninstall → Audit.
EDT 2026.1 — Axiom JDK 17; EDT 2026.2.2 — Axiom JDK 25.
