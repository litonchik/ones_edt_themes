# Noctis Minimus — 0.1.14

Дата: 18 октября 2026. Пользователь подтвердил работу Shades 0.1.13 («работает»),
без сведений об ОС/сценариях, и попросил продолжить. Добавлена только схема
`noctis-minimus`; следующая — Visual Studio Dark (C/C++).
Superpowers и субагенты не используются.
18.10.2026 пользователь подтвердил работу 0.1.14 («работает»), без сведений
об ОС/сценариях; оставшаяся ручная матрица ниже сохраняется.

## Источник и адаптация

Noctis **10.43.3**, commit `4a82370b2c064e36726c23e94f27ed3782bd6ecd`:
[Minimus](https://github.com/liviuschera/noctis/blob/4a82370b2c064e36726c23e94f27ed3782bd6ecd/themes/minimus.json),
[package.json](https://github.com/liviuschera/noctis/blob/4a82370b2c064e36726c23e94f27ed3782bd6ecd/package.json),
[MIT notice](https://github.com/liviuschera/noctis/blob/4a82370b2c064e36726c23e94f27ed3782bd6ecd/LICENSE.md).
Исследование 14.10.2026; используются закреплённые данные без обновления upstream.
Выбран именно Minimus из исходной статьи и package.json; базовый Noctis
с другим фоном/текстом не смешивается с ним. Полный notice Copyright (c) 2018
Liviu Schera включён в JAR и побайтно совпал с источником.

Из [17 ролей анализа, раздел 5.10](../research/2026-10-14-theme-catalog.md)
15 сохранены точно. Две адаптации приняты при реализации:

- **Panel #101213** из editorGroup.border вместо sidebar #17232B. Контраст
  с editor #1B2932 вырос с 1.075:1 до 1.261:1 для различимых рабочих полос.
- **Number #8E87C5** вместо исходного #7068B1: сохранён фиолетовый оттенок,
  яркость повышена. Контраст с editor вырос с **3.0528:1 до 4.5551:1**;
  порог 4.5:1 проверен отдельно. Это решение для отмеченного в анализе риска
  нечитаемых чисел; reset роли возвращает адаптированный цвет EDT.

Остальные цвета: editor #1B2932, text #C5CDD3, keyword #C88DA2,
string #72C09F, directive/accent #5998C0, border #496D83, error #B96346,
warning #C09159, disabled #6496B4, hover #00558A. Selection #2A404D сведён
из #496D8355, line #1D3443 — из #1D3544EE поверх editor; оба расчёта проверены.
Directive использует MISC, общая accent сохраняет UI-ссылку вместо отдельного
support.function. Disabled взят из descriptionForeground. Нативные начертания
BSL сохранены; CSS, иконки, шрифты и исполняемый код upstream не включены.

Сохранён приглушённый comment **#5E7887**, contrast/editor **3.20:1**.
Полная доступность всех токенов не заявляется; эту роль можно изменить лично.
Расчётные text/editor **9.25:1**, selection **9.94:1**, disabled/editor **4.66:1**.

## Сборка и сохранность

**75 unit tests**, 0 failures/errors/skipped; Verify — BUILD SUCCESS.
Новые проверки охватывают native targets, адаптированные цвета и reset с
сохранением личных вариантов девяти прежних схем. Из product Java изменена
только запись PaletteCatalog; общий UI, применение, snapshot/restore и шрифт
сохранены. Поставка: собственные bundle/feature, **31 classes major 61**,
десять палитр, девять MIT notices, без test-only fragment.

Target: EDT **2026.1.3+25**, Eclipse **4.30.0**, Xtext UI **2.33.0**,
BSL UI **23.0.1.v202609041212**, theming UI **2.1.0.v202609041212**,
Java **17.0.18**, Maven **3.9.11**, Windows x64. Runtime использует только
`.work/edt-test` и `.work/workspace-probe`; рабочая EDT не меняется.
Bundle/feature **0.1.14.202610181841**, ZIP **103 717 bytes**. SHA-256:
`58018a0d0e1a208e1c32b4acce9a283d7a196b0f3e303c23f4612e762d80da72`.
**37 прежних файлов** палитр/поставок сохранили SHA-256.
[Verify](0.1.14/noctis-verify.log), [source audit](0.1.14/noctis-source-audit.log).

## Проверка в EDT

**Noctis: 222 PASS / 0 FAIL**, exit 0. Проверены все native targets,
реальные фон и BSL glyph keyword/number/string, включая number #8E87C5,
preview, отдельный ID, одиннадцатая строка popup (штатное оформление и десять
схем), Apply/OK/Cancel, reset роли/палитры, внешний manual comment,
BSL=false, независимость шрифта и личных вариантов всех девяти прежних схем.
Выбранная строка Table: фон #2A404D, contrast **9.94:1**.
Пиксели **двух вертикальных и одной горизонтальной полосы** проверены:
**#101213**, отличный от editor #1B2932.

Просмотрены [рабочие окна](0.1.14/noctis-minimus-workbench.png) и
[popup](0.1.14/noctis-minimus-profile-list.png): полосы видны, полное название
читается, keyword розовый, string мятный, осветлённый number фиолетовый.
[BSL](0.1.14/noctis-minimus-bsl.png), [роли](0.1.14/noctis-minimus-roles.png),
[preview](0.1.14/noctis-minimus-interface-preview.png),
[журнал](0.1.14/runtime-noctis.txt).

**NoctisRestart: 139 PASS / 0 FAIL**, exit 0, отдельный процесс JVM.
Проверены saved ID, выбор на странице, personal keyword Noctis, личные цвета
девяти прежних схем и цвет трёх полос разделения. Restore вернул каждый
original key, сохранив personal palette; затем тестовые personal keys всех
десяти схем, active profile и journal очищены.
[Restart](0.1.14/runtime-noctisrestart.txt),
[рабочие окна](0.1.14/noctis-minimus-restart-workbench.png),
[личные роли](0.1.14/noctis-minimus-personal-roles.png),
[личный BSL](0.1.14/noctis-minimus-personal-bsl.png).
Итого **361 runtime assertion** в двух успешных целевых запусках.
Полные циклы прежних схем не повторялись; отдельный Cleanup не требовался.

В логах сохраняется известная CSS-диагностика Properties:
`TabbedPropertyTitle.setColor` с null color, 184/36 сообщений, как в
[0.1.13](shades-of-purple-validation.md).
[Сводка и первый stack](0.1.14/runtime-diagnostics.log) сохранены отдельно.
Полная визуальная корректность Properties с рабочим metadata-проектом
остаётся в ручной матрице. Первый Apply оставляет некоторые native controls
светлыми; после restart снимок показывает Dark, как в предыдущих проверках.

P2 **Install/Reconcile/Uninstall/Audit — exit 0**. Собственные feature,
bundle и test fragment удалены; active profile и font journal не остались.
**1267 базовых ID/версий** совпали с рабочей EDT.
[Install](0.1.14/noctis-p2-install.log), [Reconcile](0.1.14/runtime-reconcile.log),
[Uninstall](0.1.14/noctis-p2-uninstall.log), [Audit](0.1.14/noctis-p2-audit.log),
[bundles](0.1.14/noctis-bundles-audit.log).

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
