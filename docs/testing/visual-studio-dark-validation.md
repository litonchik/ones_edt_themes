# Visual Studio Dark (C/C++) — 0.1.15

Дата: 18 октября 2026. Пользователь подтвердил работу Noctis 0.1.14
(«работает»), без сведений об ОС/сценариях, и попросил продолжить.
Добавлена только `visual-studio-dark-cpp`. Остался один кандидат — Monokai Pro
(CE); выбор открытого источника CE требует подтверждения.
Superpowers и субагенты не используются.
Пользователь подтвердил работу 0.1.15 («работает»), без сведений об ОС/сценариях.
Затем согласовано завершение очереди на 11 палитрах; Monokai Pro (CE) отложена.
Этот отчёт описывает исходную сборку палитры; последующая упаковка MIT для GitHub
проверяется отдельно в `repository-preparation.md`.

## Источник и адаптация

Microsoft C/C++ Themes **2.1.0**, commit
`eb981644369c99d39054578a769af572c24bd1eb`:
[Dark](https://github.com/microsoft/vscode-cpptools/blob/eb981644369c99d39054578a769af572c24bd1eb/Themes/themes/cpptools_dark_vs_new.json),
[package.json](https://github.com/microsoft/vscode-cpptools/blob/eb981644369c99d39054578a769af572c24bd1eb/Themes/package.json),
[MIT notice](https://github.com/microsoft/vscode-cpptools/blob/eb981644369c99d39054578a769af572c24bd1eb/Themes/LICENSE.txt).
Исследование 14.10.2026; используются закреплённые данные без обновления upstream.
Проверено соответствие label **Dark (Visual Studio - C/C++)** файлу
`cpptools_dark_vs_new.json`; отдельный вариант 2017 Dark не используется.
Полный MIT notice Copyright (c) Microsoft Corporation из Themes/LICENSE.txt
включён в JAR и побайтно совпал с источником.

Все [17 ролей анализа, раздел 5.1](../research/2026-10-14-theme-catalog.md)
сохранены точно. Общие категории C/C++ перенесены на BSL; C++ semantic scopes,
CSS, иконки, шрифты и исполняемый код upstream не включены.

| Роль | RGB | Источник / адаптация |
|---|---|---|
| editor.background | #1E1E1E | editor.background |
| text | #DADADA | editor.foreground |
| panel | #303031 | menu.background, адаптация рабочих панелей |
| border | #404040 | editorIndentGuide.background, адаптация |
| selection.background | #3A3D41 | editor.inactiveSelectionBackground |
| selection.text | #DADADA | editor.foreground, адаптация |
| line | #3A3D41 | inactiveSelectionBackground, адаптация |
| comment | #57A64A | comment |
| keyword | #569CD6 | keyword |
| string | #CE9178 | string |
| number | #B5CEA8 | constant.numeric |
| directive | #BEB7FF | entity.name.function.preprocessor, имя макроса |
| accent | #007ACC | activityBarBadge.background, адаптация |
| error | #F44747 | invalid, адаптация |
| warning | #CE9178 | тёплый string, предложенный fallback |
| disabled | #A6A6A6 | input.placeholderForeground |
| hover | #383B3D | list.dropBackground |

Все цвета непрозрачные. Warning не объявляется исходным цветом предупреждений
VS Code; directive использует имя макроса, а не общий meta.preprocessor.
Нативные начертания BSL сохранены. Полная копия интерфейса Visual Studio
и C++ семантики не заявляется.

Расчётные contrast: text/editor **11.9255:1**, text/panel **9.4312:1**,
selection **7.8088:1**, comment/editor **5.5242:1**, disabled/panel **5.4159:1**.
Accent/editor **3.6960:1**: исходный синий сохранён; полная доступность всех
ролей не заявляется, цвет можно изменить лично.

## Сборка и сохранность

**77 unit tests**, 0 failures/errors/skipped; Verify — BUILD SUCCESS.
Новые проверки охватывают native targets и reset с сохранением личных вариантов
десяти прежних схем. Из product Java изменена только запись PaletteCatalog;
общий UI, применение, snapshot/restore и шрифт сохранены.
Поставка: собственные bundle/feature, **31 classes major 61**,
одиннадцать палитр, десять MIT notices, без test-only fragment.

Target: EDT **2026.1.3+25**, Eclipse **4.30.0**, Xtext UI **2.33.0**,
BSL UI **23.0.1.v202609041212**, theming UI **2.1.0.v202609041212**,
Java **17.0.18**, Maven **3.9.11**, Windows x64. Runtime использует только
`.work/edt-test` и `.work/workspace-probe`; рабочая EDT не меняется.
Bundle/feature **0.1.15.202610181859**, ZIP **105 225 bytes**. SHA-256:
`3f42f3690a3db7e1c960cb654d1a9f54d22603d9dd0e05c4af73e7243595e288`.
**40 прежних файлов** палитр/поставок сохранили SHA-256.
[Verify](0.1.15/visual-studio-verify.log),
[source audit](0.1.15/visual-studio-source-audit.log).

## Проверка в EDT

**VisualStudio: 223 PASS / 0 FAIL**, exit 0. Проверены native targets,
реальные фон и BSL glyph keyword/number/string, preview, отдельный ID,
двенадцатая строка popup (штатное оформление и одиннадцать схем),
Apply/OK/Cancel, reset роли/палитры, внешний manual comment, BSL=false,
независимость шрифта и personal keys всех десяти прежних схем.
Выбранная строка Table: фон #3A3D41, contrast **7.81:1**.
Пиксели **двух вертикальных и одной горизонтальной полосы** проверены:
**#303031**, отличный от editor #1E1E1E.

Просмотрены [рабочие окна](0.1.15/visual-studio-dark-cpp-workbench.png) и
[popup](0.1.15/visual-studio-dark-cpp-profile-list.png): полосы видны, полное
название читается, keyword голубой, string тёплый, number светло-зелёный.
[BSL](0.1.15/visual-studio-dark-cpp-bsl.png),
[роли](0.1.15/visual-studio-dark-cpp-roles.png),
[preview](0.1.15/visual-studio-dark-cpp-interface-preview.png),
[журнал](0.1.15/runtime-visualstudio.txt).

**VisualStudioRestart: 141 PASS / 0 FAIL**, exit 0, отдельный процесс JVM.
Проверены saved ID, выбор на странице, personal keyword Visual Studio,
личные цвета десяти прежних схем и цвет трёх полос разделения. Restore
вернул каждый original key, сохранив personal palette; затем тестовые
personal keys всех одиннадцати схем, active profile и journal очищены.
[Restart](0.1.15/runtime-visualstudiorestart.txt),
[рабочие окна](0.1.15/visual-studio-dark-cpp-restart-workbench.png),
[личные роли](0.1.15/visual-studio-dark-cpp-personal-roles.png),
[личный BSL](0.1.15/visual-studio-dark-cpp-personal-bsl.png).
Итого **364 runtime assertions** в двух успешных целевых запусках.
Полные циклы прежних схем не повторялись; отдельный Cleanup не требовался.

В логах сохраняется известная CSS-диагностика Properties:
`TabbedPropertyTitle.setColor` с null color, 184/36 сообщений, как в
[0.1.14](noctis-minimus-validation.md).
[Сводка и первый stack](0.1.15/runtime-diagnostics.log) сохранены отдельно.
Полная визуальная корректность Properties с рабочим metadata-проектом
остаётся в ручной матрице. Первый Apply оставляет некоторые native controls
светлыми; просмотренный restart показывает их тёмными, как в прежних проверках.

P2 **Install/Reconcile/Uninstall/Audit — exit 0**. Собственные feature,
bundle и test fragment удалены; active profile и font journal не остались.
**1267 базовых ID/версий** совпали с рабочей EDT.
[Install](0.1.15/visual-studio-p2-install.log),
[Reconcile](0.1.15/runtime-reconcile.log),
[Uninstall](0.1.15/visual-studio-p2-uninstall.log),
[Audit](0.1.15/visual-studio-p2-audit.log),
[bundles](0.1.15/visual-studio-bundles-audit.log).

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
