# Ayu Dark (Bordered) — 0.1.12

Дата: 18 октября 2026. По просьбе пользователя продолжена очередь после
Night Owl; добавлена только схема `ayu-dark`. Следующая — Shades of Purple.
Superpowers и субагенты не используются.
18.10.2026 пользователь подтвердил работу 0.1.12 («работает»), без сведений
об ОС/сценариях; оставшаяся ручная матрица ниже сохраняется.

## Источник и адаптация

Ayu **1.4.1**, commit `2c6feb7215bf7ca6c8c9a51066f255025df3a5b9`:
[ayu-dark.json](https://github.com/ayu-theme/vscode-ayu/blob/2c6feb7215bf7ca6c8c9a51066f255025df3a5b9/ayu-dark.json),
[package.json](https://github.com/ayu-theme/vscode-ayu/blob/2c6feb7215bf7ca6c8c9a51066f255025df3a5b9/package.json),
[MIT notice](https://github.com/ayu-theme/vscode-ayu/blob/2c6feb7215bf7ca6c8c9a51066f255025df3a5b9/LICENSE).
Исследование 14.10.2026; используются закреплённые данные без обновления upstream.
В package.json путь ayu-dark.json имеет label **Ayu Dark**, id **Ayu Dark Bordered**;
Unbordered — отдельный файл. Полный notice Copyright (c) 2016 Ike Kurghinyan
включён в JAR и побайтно совпал с источником.

Из [17 ролей анализа, раздел 5.8](../research/2026-10-14-theme-catalog.md)
15 сохранены точно. Panel адаптирована из #0D1017 в **#1B1F29** — исходный
editorGroup.border, чтобы полосы рабочих окон отличались от editor #10141C.
Полупрозрачный hover #47526640 пересчитан поверх этой панели в **#262C38**.
Остальные цвета: text #BFBDB6, keyword/directive #FF8F40, string #AAD94C,
number #D2A6FF, accent/warning #E6B450, error #D95757. Selection #193155
сведён из #3388FF40 поверх editor, line #161A24 сохранён непрозрачным.
Directive использует Storage, общая accent — золотую ссылку вместо Library coral.
Disabled взят из непрозрачного comment, без отбрасывания alpha у исходного
disabledForeground. Нативные начертания BSL сохранены; исполняемый код,
иконки, шрифты и CSS исходной темы не включены.

Сохранены приглушённые comment/disabled **#5A6673**, contrast/editor **3.15:1**.
Полная доступность всех токенов не заявляется; эти роли можно изменить лично.
Расчётные text/editor **9.81:1**, selection **6.93:1**.

## Сборка и сохранность

**71 unit tests**, 0 failures/errors/skipped; Verify — BUILD SUCCESS.
Новые проверки охватывают native targets Ayu, panel/hover и reset с сохранением
личных цветов семи прежних схем. Из product Java изменена только запись
PaletteCatalog; общий UI, применение, snapshot/restore и шрифт сохранены.
Поставка: собственные bundle/feature, **31 classes major 61**, восемь палитр,
семь MIT notices, без test-only fragment.

Target: EDT **2026.1.3+25**, Eclipse **4.30.0**, Xtext UI **2.33.0**,
BSL UI **23.0.1.v202609041212**, theming UI **2.1.0.v202609041212**,
Java **17.0.18**, Maven **3.9.11**, Windows x64. Runtime использует только
`.work/edt-test` и `.work/workspace-probe`; рабочая EDT не меняется.
Bundle/feature **0.1.12.202610181345**, ZIP **101 063 bytes**. SHA-256:
`9c2a06e518fd2afa04f0d4b00f8421799dda5842365da9746b53564c6afc024f`.
**31 прежний файл** палитр/поставок сохранил SHA-256.
[Verify](0.1.12/ayu-verify.log), [source audit](0.1.12/ayu-source-audit.log).

## Проверка в EDT

**Ayu: 221 PASS / 0 FAIL**, exit 0 (включая возврат состояния предыдущей
попытки). Проверены все native targets, реальные фон и BSL glyph keyword,
number/string, preview, отдельный ID, девятая строка popup (штатное оформление
и восемь схем), Apply/OK/Cancel, reset роли/палитры, внешний manual comment,
BSL=false, независимый шрифт и личные цвета всех семи прежних схем.
Проверены пиксели **двух вертикальных и одной горизонтальной полосы**:
**#1B1F29**, отличный от editor #10141C.

Просмотрены [рабочие окна](0.1.12/ayu-dark-workbench.png) и
[popup](0.1.12/ayu-dark-profile-list.png): полосы видны, полное название
читается, keyword оранжевый, string зелёный, number фиолетовый.
[BSL](0.1.12/ayu-dark-bsl.png), [роли](0.1.12/ayu-dark-roles.png),
[preview](0.1.12/ayu-dark-interface-preview.png), [журнал](0.1.12/runtime-ayu.txt).

[Первый запуск](0.1.12/initial-ayu-probe.txt) остановился на проверке пикселей
выбранной строки через GC(Display): прочитан цвет, не совпадающий с палитрой.
В тесте захват переведён на **GC самого Table**, а popup сохраняется до assertion.
Повторно измерены фон #193155 и contrast **6.93:1**. Product-код списка не менялся;
это уточнение способа захвата, без ослабления порога контраста.

**AyuRestart: 135 PASS / 0 FAIL**, exit 0, отдельный процесс JVM.
Подтверждены saved ID, выбор на странице, personal keyword Ayu, личные цвета
семи прежних схем и цвет трёх полос разделения. Restore проверен для каждого
original key и сохраняет personal palette. В конце тестовые personal keys
всех восьми схем, active profile и journal очищены.
[Restart](0.1.12/runtime-ayurestart.txt),
[рабочие окна](0.1.12/ayu-dark-restart-workbench.png),
[личные роли](0.1.12/ayu-dark-personal-roles.png),
[личный BSL](0.1.12/ayu-dark-personal-bsl.png).
Итого **356 assertions** в двух успешных целевых запусках. Полные циклы прежних
схем не повторялись; отдельный Cleanup не требовался.

В логах есть известная CSS-диагностика `TabbedPropertyTitle.setColor` с null
color: 208 сообщений в первом успешном процессе и 36 во втором. Она также
наблюдалась в [0.1.11](night-owl-validation.md); целевые assertions проходят.
[Сводка и первый stack](0.1.12/runtime-diagnostics.log) сохранены. Полная
визуальная корректность Properties с рабочим metadata-проектом остаётся
в ручной матрице. Снимок первого Apply содержит отдельные светлые native
контролы; после restart они тёмные, как и в предыдущем сценарии Night Owl.

P2 **Install/Reconcile/Uninstall/Audit — exit 0**. Собственные feature,
bundle и test fragment удалены; active profile и font journal не остались.
**1267 базовых ID/версий** совпали с рабочей EDT.
[Install](0.1.12/ayu-p2-install.log), [Reconcile](0.1.12/runtime-reconcile.log),
[Uninstall](0.1.12/ayu-p2-uninstall.log), [Audit](0.1.12/ayu-p2-audit.log),
[bundles](0.1.12/ayu-bundles-audit.log).

## Оставшаяся ручная матрица

| Сценарий | Статус |
|---|---|
| Completion/hover с рабочим metadata-проектом | Не проверено |
| Содержимое Properties с рабочим metadata-проектом | Не проверено |
| Реальные заголовки error/warning, все нативные контролы | Не проверено |
| Styled selection всех токенов BSL, DPI и масштабирование | Не проверено |
| Содержимое форм/MXL/схем/СКД | Исключено; карта targets сохранена |
| Новая палитра на macOS x64 | Не проверено |

Установка: [инструкция](../user-guide.md).
