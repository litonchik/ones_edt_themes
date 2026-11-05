# Winter is Coming (Light) — локальный кандидат 0.1.18

Дата: **04.11.2026**. Добавлена одна палитра `winter-is-coming-light`, группа LIGHT.
В группе «Светлые» теперь две схемы: GitHub Light Default и Winter is Coming (Light).
Тёмные 11 схем, прежняя светлая тема и общий механизм 0.1.17 сохранены.

[Первичный источник, адаптация 17 ролей и контраст](../research/2026-11-04-winter-light.md).
Существующая MIT-лицензия Winter сохранена; атрибуция расширена на Light.

## Проверки

**89 unit tests**, 0 failures/errors; Maven 3.9.11 / Tycho 4.0.13 / Java 17.
ZIP verify: только собственные bundle/feature, 38 classes major 61, 13 палитр,
лицензии и p2 metadata. Подготовка локального p2-сайта из ZIP — PASS.
Кроме PaletteCatalog.class, все 37 общих Java-классов побайтово совпадают с 0.1.17:
[сравнение](2026-11-04-winter-light/winter-light-binary-comparison.json).
Старые 12 файлов палитр и ZIP 0.1.17 побайтово сохранены:
[SHA-256](2026-11-04-winter-light/previous-palettes-and-zip.json).

| Windows x64 EDT | Axiom JVM | Candidate | CandidateRestart | CandidateSurfaces |
|---|---|---:|---:|---:|
| 2026.1.2 | 17.0.18 | 76 | 201 | 210 |
| 2026.1.3 | 17.0.18 | 76 | 201 | 210 |
| 2026.2.2 | 25.0.2 | 76 | 201 | 210 |

Всего **1461 runtime PASS, 0 FAIL**. Проверяется новая палитра;
полная матрица общих изменений 0.1.17 повторно не запускается.

Candidate проверяет видимость второй светлой темы, локальный preview/Cancel,
Apply, 33 named colors, BSL keyword/comment/string/number, выбранный текст,
переходы между новой схемой, Dark Blue и GitHub Light без изменения документа,
личный keyword и независимость personal keys всех 12 прежних палитр.
Шрифт не меняется. CandidateRestart проверяет native Light, личный вариант
после JVM restart, сброс роли к стилю, открытие правильной группы/профиля,
точный restore значений и отсутствия записей, сохранение прежних personal keys.

CandidateSurfaces проверяет RGB keyword/number(native text)/string/comment/
parameter/identifier и неизменность текста в QL, QL DCS и двух реальных embedded
контролах конструктора. Проверены три native BSL merge panes после переоткрытия
в новой палитре и восстановление всех затронутых предпочтений.

Для галереи снято полное окно EDT 2026.1.3 после restart, 884×581:
Navigator / BSL / Properties, без personal colors и последующей обработки PNG.

## Очистка и границы

Отдельные Cleanup / Reconcile / p2 Uninstall / Audit — PASS во всех копиях.
Сохранены 1257 / 1267 / 1234 базовых ID/версий. Runtime bundle каждой версии
совпал с shipping ZIP (см. shipping-bundle.txt в папках свидетельств).
Рабочие EDT/workspace не менялись. Временные junction подключали только
прежние изолированные копии и Maven cache; после проверки удалены, цели сохранены.

Native SWT после смены светлой/тёмной основы может требовать restart.
Уже открытое BSL-сравнение требует переоткрытия для новой подсветки — известное
ограничение 0.1.17 сохраняется. Полный metadata query wizard, Mac 0.1.18,
все tooltip/completion/DPI-сценарии не проверены. Native Properties CSS-диагностика
не объявлена исправленной. Светлый вариант Dracula не добавлялся.

## Поставка

Bundle: `0.1.18.202611040624`.
ZIP SHA-256: `e714154ff07de87e20c64b5d46218e0e1a394061b34e67635ae0c6b335e5a29e`.
Локальный файл `dist/edt-themes-0.1.18.zip` содержит обе новые светлые палитры.
Публичные Releases и сайт обновлений остаются на 0.1.16; commit/push/release
для этой работы не выполнялись, пользователь проверит несколько тем локально.

## Воспроизведение

В отдельной копии EDT после p2 Install и build-workbench-probe выполните
Candidate → CandidateRestart → CandidateSurfaces с `-ProfileId winter-is-coming-light`.
Для README: CandidateGalleryPrepare → CandidateGallery.
Завершение: Cleanup → Reconcile → p2 Uninstall → Audit.
Для EDT 2026.2.2 укажите Axiom JDK 25; для 2026.1 — JDK 17.
