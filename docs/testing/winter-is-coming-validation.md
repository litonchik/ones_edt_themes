# Winter is Coming (Dark Blue) — 0.1.9

Дата: 18 октября 2026. Добавлена одна новая схема `winter-is-coming-dark-blue`.
Позднее пользователь обнаружил слившиеся разделители окон: исправлено в
[0.1.10](winter-separators-validation.md). Ниже сохранены факты проверки 0.1.9.
Пользователь подтвердил работу предыдущей 0.1.8 («проверил, работает»), без
сведений об ОС/сценариях. Следующая схема — Night Owl. Superpowers не используются.

## Источник и адаптация

Winter is Coming **1.5.0**, commit `260547834cb6ac37dd5b8bb5842cc1c8d3164946`:
[палитра Dark Blue](https://github.com/johnpapa/vscode-winteriscoming/blob/260547834cb6ac37dd5b8bb5842cc1c8d3164946/themes/WinterIsComing-dark-blue-color-theme.json),
[MIT notice](https://github.com/johnpapa/vscode-winteriscoming/blob/260547834cb6ac37dd5b8bb5842cc1c8d3164946/LICENSE.md).
Исследование 14.10.2026; реализация использует закреплённые данные, без обновления
upstream-версии. Полный notice Copyright (c) 2015-2017 JohnPapa.net, LLC включён
в JAR и побайтно совпадает с сохранённым первичным источником.

Все 17 ролей совпали с [анализом, раздел 5.5](../research/2026-10-14-theme-catalog.md).
Фон editor/panel #011627, text #A7DBF7, keyword/directive #00BFF9,
string #BCF0C0, number #8DEC95, comment #999999. Полупрозрачный
line #0C499444 сведён поверх editor в #042444; вычисление проверено отдельно.
Selection #103362 использует обычный editor foreground. Accent #219FD5 взят
из UI-ссылки, disabled — из номера строки, hover #0C4994 — из editor hover.
Наклонное начертание BSL не меняется: варианты Italics/No Italics отдельно
не добавлялись. Иконки, шрифты, CSS и исполняемый код upstream не включены.
Расчётный contrast text/editor **12.33:1**, selection **8.44:1**,
comment/editor **6.44:1**; исходные оттенки сохранены.

## Сборка и поставка

**66 unit tests, 0 failures/errors/skipped. Verify — BUILD SUCCESS.**
Новые тесты проверяют native targets Winter и независимость reset от личных
вариантов всех пяти прежних схем. Product Java изменён только записью в
PaletteCatalog; общий код применения, snapshots, UI и шрифта сохранён.
В p2 ZIP входят собственные bundle/feature, **31 classes major 61**,
шесть палитр и пять полных MIT notices; test-only fragment в поставку не входит.

Target: EDT **2026.1.3+25**, Eclipse **4.30.0**, Xtext UI **2.33.0**,
BSL UI **23.0.1.v202609041212**, theming UI **2.1.0.v202609041212**,
Java **17.0.18**, Maven **3.9.11**, Windows x64. Runtime использует только
`.work/edt-test` и `.work/workspace-probe`; рабочая EDT не меняется.

Bundle/feature **0.1.9.202610181244**; ZIP **98 401 bytes**. SHA-256:
`d21bec61382b56d725eef550df0c1b79d92f989b7096caa8151ede405940778c`.
Сохранность пяти прежних палитр и поставок подтверждена по **23 SHA-256**.
Журналы: [Verify](0.1.9/winter-verify.log), [source audit](0.1.9/winter-source-audit.log).

## Проверка в EDT

**Winter: 209 PASS / 0 FAIL**, exit 0. Проверены все фактические settings,
реальные BSL glyph keyword/number/string и фон, preview, отдельный ID,
седьмая строка popup (штатное оформление + 6 схем), контраст выбранной строки,
Apply/OK/Cancel, сброс роли и палитры, защита внешнего manual comment,
BSL=false и независимость font preference. Reset Winter сохраняет personal
keys Darcula, Monokai, One Dark Pro, Dracula и GitHub Dark Default.

Просмотрены [popup](0.1.9/winter-is-coming-dark-blue-profile-list.png) и
[реальный BSL](0.1.9/winter-is-coming-dark-blue-bsl.png): полное название темы
видно, светлый текст читается; keyword голубой, string/number зелёные,
comments серые. [Роли](0.1.9/winter-is-coming-dark-blue-roles.png) и
[preview](0.1.9/winter-is-coming-dark-blue-interface-preview.png) сохранены.
Журнал: [Winter](0.1.9/runtime-winter.txt).

**WinterRestart: 124 PASS / 0 FAIL**, exit 0, отдельный процесс JVM.
Подтверждены saved ID, personal keyword #ABCDEF, выбор темы на странице
и сохранность personal keys всех пяти прежних схем. Restore вернул каждый
original key, сохранив личный вариант. В конце очищены тестовые personal keys
всех шести схем и journal. Дополнительная фаза Cleanup не потребовалась.
Журнал: [Restart](0.1.9/runtime-winterrestart.txt),
[личные роли](0.1.9/winter-is-coming-dark-blue-personal-roles.png),
[личный BSL](0.1.9/winter-is-coming-dark-blue-personal-bsl.png).
Полные runtime-циклы прежних схем не запускались: общий product-код не менялся,
их personal keys проверены в сценарии Winter. Итого **333 runtime assertions**.

P2 **Install/Reconcile/Uninstall/Audit — exit 0**. Собственные feature,
bundle и test fragment удалены из тестовой EDT; активный профиль и font journal
не остались. **1267 базовых ID/версий** совпали с рабочей EDT.
Журналы: [Install](0.1.9/winter-p2-install.log),
[Reconcile](0.1.9/runtime-reconcile.log), [Uninstall](0.1.9/winter-p2-uninstall.log),
[Audit](0.1.9/winter-p2-audit.log), [bundles](0.1.9/winter-bundles-audit.log).

## Оставшаяся ручная матрица

| Сценарий | Статус |
|---|---|
| Project Explorer/Properties, completion/hover с рабочим проектом | Не проверено |
| Реальные заголовки error/warning, все нативные контролы | Не проверено |
| Styled selection всех токенов BSL, DPI и масштабирование | Не проверено |
| Содержимое форм/MXL/схем/СКД | Исключено; карта targets сохранена |
| Новая палитра на macOS x64 | Не проверено |

Установка: [инструкция](../user-guide.md).
