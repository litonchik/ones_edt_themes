# Shades of Purple — 0.1.13

Дата: 18 октября 2026. Пользователь подтвердил работу Ayu 0.1.12 («работает»),
без сведений об ОС/сценариях, и попросил продолжить. Добавлена только базовая
схема `shades-of-purple`; следующая — Noctis Minimus.
Superpowers и субагенты не используются.
18.10.2026 пользователь подтвердил работу 0.1.13 («работает»), без сведений
об ОС/сценариях; оставшаяся ручная матрица ниже сохраняется.

## Источник и адаптация

Shades of Purple **7.3.6**, commit `e8eb49f33e5db05ceba6677367b33ddb27ad821c`:
[палитра](https://github.com/ahmadawais/shades-of-purple-vscode/blob/e8eb49f33e5db05ceba6677367b33ddb27ad821c/themes/shades-of-purple-color-theme.json),
[package.json](https://github.com/ahmadawais/shades-of-purple-vscode/blob/e8eb49f33e5db05ceba6677367b33ddb27ad821c/package.json),
[MIT notice](https://github.com/ahmadawais/shades-of-purple-vscode/blob/e8eb49f33e5db05ceba6677367b33ddb27ad821c/LICENSE.md).
Исследование 14.10.2026; используются закреплённые данные без обновления upstream.
Выбран основной вариант, не отдельный Super Dark. Полный MIT notice
Copyright (c) 2015-∞ Ahmad Awais включён в JAR и побайтно совпал с источником.

Все [17 ролей анализа, раздел 5.9](../research/2026-10-14-theme-catalog.md)
сохранены точно. Editor **#2D2B55**, text **#FFFFFF**, panel **#222244** из
sideBar.background, border #25254B. Одна роль panel одновременно окрашивает
заголовки, вкладки, popup и рабочие разделители; фон отличается от editor.
Keyword #FF9D00, string #A5FF90, number #FF628C, directive/warning #FAD000,
comment/accent #B362FF, disabled #A599E9, line #1F1F41.
Selection #7448B0 сведён из #B362FF88 поверх editor; hover #202041 из
#1E1E3F80 и error #E43938 из #EC3A37F5 — поверх panel. Все три расчёта проверены.
Number использует Constant, directive — Storage. Общая accent сохраняет
фиолетовую ссылку вместо отдельного оранжевого support.function BSL.
Нативные начертания BSL сохранены; CSS, шрифты, иконки и исполняемый код
исходной темы не включены.

Сохранены исходные comment/accent **#B362FF**, contrast/editor **3.85:1**.
Полная доступность всех токенов не заявляется; эти роли доступны для личной
правки. Расчётные text/editor **13.22:1**, selection **6.37:1**,
disabled/editor **5.25:1**.

## Сборка и сохранность

**73 unit tests**, 0 failures/errors/skipped; Verify — BUILD SUCCESS.
Новые проверки охватывают native targets, вычисленные цвета и reset с
сохранением личных вариантов восьми прежних схем. Из product Java изменена
только запись PaletteCatalog; общий UI, применение, snapshot/restore и шрифт
сохранены. Поставка: собственные bundle/feature, **31 classes major 61**,
девять палитр, восемь MIT notices, без test-only fragment.

Target: EDT **2026.1.3+25**, Eclipse **4.30.0**, Xtext UI **2.33.0**,
BSL UI **23.0.1.v202609041212**, theming UI **2.1.0.v202609041212**,
Java **17.0.18**, Maven **3.9.11**, Windows x64. Runtime использует только
`.work/edt-test` и `.work/workspace-probe`; рабочая EDT не меняется.
Bundle/feature **0.1.13.202610181757**, ZIP **102 402 bytes**. SHA-256:
`29e546ddbe240d90bf05e49a4bff42d88a6a8645ffef6931faa8b2a4d1929e98`.
**34 прежних файла** палитр/поставок сохранили SHA-256.
[Verify](0.1.13/shades-verify.log), [source audit](0.1.13/shades-source-audit.log).

## Проверка в EDT

**Shades: 221 PASS / 0 FAIL**, exit 0. Проверены все native targets,
реальные фон и BSL glyph keyword/number/string, preview, отдельный ID,
десятая строка popup (штатное оформление и девять схем), Apply/OK/Cancel,
reset роли/палитры, внешний manual comment, BSL=false, независимость шрифта
и личных вариантов всех восьми прежних схем. Выбранная строка захвачена
непосредственно из Table: фон #7448B0, contrast **6.37:1**.
Пиксели **двух вертикальных и одной горизонтальной полосы** проверены:
**#222244**, отличный от editor #2D2B55.

Просмотрены [рабочие окна](0.1.13/shades-of-purple-workbench.png) и
[popup](0.1.13/shades-of-purple-profile-list.png): полосы видны, полное
название читается, keyword оранжевый, string зелёный, number розовый,
комментарии фиолетовые.
[BSL](0.1.13/shades-of-purple-bsl.png), [роли](0.1.13/shades-of-purple-roles.png),
[preview](0.1.13/shades-of-purple-interface-preview.png),
[журнал](0.1.13/runtime-shades.txt).

**ShadesRestart: 137 PASS / 0 FAIL**, exit 0, отдельный процесс JVM.
Проверены saved ID, выбор на странице, personal keyword Shades, независимые
личные цвета восьми прежних схем и цвет всех трёх полос разделения.
Restore вернул каждый original key, сохранив personal palette; затем
тестовые personal keys всех девяти схем, active profile и journal очищены.
[Restart](0.1.13/runtime-shadesrestart.txt),
[рабочие окна](0.1.13/shades-of-purple-restart-workbench.png),
[личные роли](0.1.13/shades-of-purple-personal-roles.png),
[личный BSL](0.1.13/shades-of-purple-personal-bsl.png).
Итого **358 runtime assertions** в двух успешных целевых запусках.
Полные циклы прежних схем не повторялись; отдельный Cleanup не требовался.

В логах сохраняется известная CSS-диагностика Properties:
`TabbedPropertyTitle.setColor` с null color, 184/36 сообщений, как в
[0.1.11](night-owl-validation.md). [Сводка и первый stack](0.1.13/runtime-diagnostics.log)
сохранены отдельно от целевых assertions. Полная визуальная корректность
Properties с рабочим metadata-проектом остаётся в ручной матрице.
Первый Apply оставляет некоторые native controls светлыми; после restart
снимок показывает Dark, как в предыдущих проверках.

P2 **Install/Reconcile/Uninstall/Audit — exit 0**. Собственные feature,
bundle и test fragment удалены; active profile и font journal не остались.
**1267 базовых ID/версий** совпали с рабочей EDT.
[Install](0.1.13/shades-p2-install.log), [Reconcile](0.1.13/runtime-reconcile.log),
[Uninstall](0.1.13/shades-p2-uninstall.log), [Audit](0.1.13/shades-p2-audit.log),
[bundles](0.1.13/shades-bundles-audit.log).

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
