# Night Owl — 0.1.11

Дата: 18 октября 2026. Добавлена только схема `night-owl`. Пользователь
подтвердил исправление разделителей Winter 0.1.10 («теперь отлично»), без
сведений об ОС/сценариях. Следующая схема — Ayu Dark (Bordered).
Superpowers и субагенты не используются.

## Источник и адаптация

Night Owl **2.1.1**, commit `cc291eba7976b20d7c66bde6883c27b902196b07`:
[тёмная палитра](https://github.com/sdras/night-owl-vscode-theme/blob/cc291eba7976b20d7c66bde6883c27b902196b07/themes/Night%20Owl-color-theme.json),
[MIT notice](https://github.com/sdras/night-owl-vscode-theme/blob/cc291eba7976b20d7c66bde6883c27b902196b07/LICENSE.md).
Исследование 14.10.2026; реализованы закреплённые данные без обновления upstream.
Полный notice Copyright (c) 2018 Sarah Drasner включён в JAR и побайтно совпал
с сохранённым источником. Статья указывала Light No Italics; для Dark-архитектуры
плагина выбран тёмный вариант того же семейства, как предусмотрено анализом.

Из [17 ролей анализа, раздел 5.7](../research/2026-10-14-theme-catalog.md)
16 сохранены точно. Только panel адаптирована из #011627 в **#0E293F**,
исходный list.inactiveSelectionBackground, чтобы разделители рабочих окон
отличались от редактора. Это учитывает подтверждённое исправление Winter.
Editor #011627, text #D6DEEB, keyword #C792EA, string #ECC48D,
number #F78C6C, directive #FFCB8B, accent #82AAFF.
Line #072435 и hover #2D2D5E сведены из alpha поверх editor; расчёты проверены.
Directive использует C# Region как BSL-адаптацию, общая accent — Library blue,
а не поздний support.function green. Нативные начертания BSL сохранены;
иконки, шрифты, CSS и исполняемый код исходной темы не включены.

Сохранён исходный приглушённый comment **#637777**, contrast/editor **3.87:1**.
Он темнее основного текста; полная доступность всех токенов не заявляется.
Расчётные text/editor **13.54:1**, selection **11.65:1**, disabled/editor
**4.29:1**. Цвет комментариев доступен для личной правки через редактор ролей.

## Сборка и сохранность

**69 unit tests**, 0 failures/errors/skipped; Verify — BUILD SUCCESS.
Новые проверки охватывают native targets Night Owl, отличающийся panel
и reset с сохранением личных цветов шести прежних схем.
Из product Java изменена только запись PaletteCatalog; общий UI, применение,
snapshot/restore и шрифт сохранены. Поставка: собственные bundle/feature,
**31 classes major 61**, семь палитр и шесть MIT notices, без test fragment.

Target: EDT **2026.1.3+25**, Eclipse **4.30.0**, Xtext UI **2.33.0**,
BSL UI **23.0.1.v202609041212**, theming UI **2.1.0.v202609041212**,
Java **17.0.18**, Maven **3.9.11**, Windows x64. Runtime использует только
`.work/edt-test` и `.work/workspace-probe`; рабочая EDT не меняется.
Bundle/feature **0.1.11.202610181327**, ZIP **99 804 bytes**. SHA-256:
`de8b5c1ae6b85b20c4d6a7e62a3d7dcd437bbc2a4c95260f4892c192a2e982c6`.
**28 прежних файлов** палитр/поставок сохранили SHA-256.
[Verify](0.1.11/night-owl-verify.log), [source audit](0.1.11/night-owl-source-audit.log).

## Проверка в EDT

**NightOwl: 219 PASS / 0 FAIL**, exit 0. Проверены все native targets,
фон и реальные BSL glyph keyword/number/string, preview, отдельный ID,
восьмая строка popup (штатное оформление + 7 схем), контраст выбранной строки,
Apply/OK/Cancel, reset роли/палитры, защита внешнего comment, BSL=false,
независимость font preference и личных цветов всех шести прежних схем.

Проверены фактические пиксели **двух вертикальных и одной горизонтальной
полосы**: #0E293F, отличный от фона редактора #011627.
Просмотрены [рабочие окна](0.1.11/night-owl-workbench.png) и
[popup](0.1.11/night-owl-profile-list.png): разделители видны, название читается,
keyword фиолетовый, number коралловый, string золотистый. Комментарии сохранены
приглушёнными, как в исходнике; это явно принятое ограничение контраста.
[BSL](0.1.11/night-owl-bsl.png), [роли](0.1.11/night-owl-roles.png),
[preview](0.1.11/night-owl-interface-preview.png), [журнал](0.1.11/runtime-nightowl.txt).

**NightOwlRestart: 133 PASS / 0 FAIL**, exit 0, отдельный процесс JVM.
Подтверждены saved ID, выбор на странице, personal keyword Night Owl,
личные цвета шести прежних схем и #0E293F всех трёх полос. Restore проверен
для каждого original key и сохраняет personal palette; в конце тестовые
personal keys всех семи схем, active profile и journal очищены.
[Restart](0.1.11/runtime-nightowlrestart.txt),
[рабочие окна после restart](0.1.11/night-owl-restart-workbench.png),
[личные роли](0.1.11/night-owl-personal-roles.png),
[личный BSL](0.1.11/night-owl-personal-bsl.png).
Итого **352 runtime assertions** в двух целевых запусках; полные циклы прежних
схем не повторялись, дополнительный Cleanup не требовался.

Границы проверки UI: после первого Apply в тестовом процессе некоторые native
контролы оставались светлыми; на снимке следующего процесса они тёмные.
В логах CSS Properties повторяется `TabbedPropertyTitle.setColor` с null color:
184 сообщения в первом процессе и 36 во втором. Такая же диагностика была
в [0.1.10](0.1.10/runtime-winterseparators.log) (108 сообщений); она относится
к общему CSS Properties, не является провалом перечисленных assertions.
Сохранены [сводка и первый stack](0.1.11/runtime-diagnostics.log).
Полная визуальная корректность Properties с рабочим metadata-проектом остаётся
в ручной матрице; успех тестов цветов не объявляется проверкой всего UI.

P2 **Install/Reconcile/Uninstall/Audit — exit 0**. Собственные feature,
bundle и test fragment удалены; active profile и font journal не остались.
**1267 базовых ID/версий** совпали с рабочей EDT.
[Install](0.1.11/night-owl-p2-install.log), [Reconcile](0.1.11/runtime-reconcile.log),
[Uninstall](0.1.11/night-owl-p2-uninstall.log), [Audit](0.1.11/night-owl-p2-audit.log),
[bundles](0.1.11/night-owl-bundles-audit.log).

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
