# Разделители рабочих окон Winter — 0.1.10

Дата: 18 октября 2026. Исправление по двум скриншотам пользователя:
вертикальная полоса между окнами в Winter сливается с фоном. Новые схемы
не добавлены; следующей в очереди остаётся Night Owl.
Пользователь подтвердил исправление 18.10.2026 («теперь отлично»); ОС
и отдельные сценарии не сообщены.

## Причина и изменение

В 0.1.9 обе роли `panel` и `editor.background` имели значение #011627.
В установленной EDT 2026.1.3+25 правило `.MPartSashContainer` из
`com._1c.g5.v8.dt.theming.ui_2.1.0.v202609041212.jar/css/dark/edt-dark_partstyle.css`
использует `com._1c.g5.v8.dt.theming.ui.INACTIVE_PART_BACKGROUND_COLOR`.
В карте плагина это роль `panel`, а не `border`. Поэтому яркая роль border
сама по себе не выделяла область между окнами. CSS прочитан из локального
целевого bundle 18.10.2026; vendor bundles не менялись.

Только `panel` Winter изменён на **#0E293F**. Оттенок взят из
`list.inactiveSelectionBackground` закреплённого
[исходника Winter 1.5.0](https://github.com/johnpapa/vscode-winteriscoming/blob/260547834cb6ac37dd5b8bb5842cc1c8d3164946/themes/WinterIsComing-dark-blue-color-theme.json).
Это адаптация к EDT; таблица первоначального анализа сохранена как история.
Цвет распространяется также на заголовки, выбранные вкладки и другие targets
роли panel. Фон редактора #011627 и остальные 16 ролей Winter сохранены.
Пять прежних палитр не менялись. Новые preference keys, CSS и product Java
не добавлялись; личные варианты остаются независимыми.

## Сборка и сохранность

**67 unit tests**, 0 failures/errors/skipped; Verify — BUILD SUCCESS.
Новый тест проверяет отдельный panel и фактический target разделителя.
Bundle/feature **0.1.10.202610181304**, 31 classes major 61, шесть палитр,
пять MIT notices. ZIP **98 550 bytes**, SHA-256:
`693fffb149aae4072c7f02e11951e6ea74cee326902bc3d3c0c6de601874ada4`.
**25 прежних файлов** палитр/поставок сохранили SHA-256; в Winter намеренно
изменена только роль panel. [Verify](0.1.10/separators-verify.log),
[source audit](0.1.10/separators-source-audit.log).

Target: EDT **2026.1.3+25**, Eclipse **4.30.0**, Xtext UI **2.33.0**,
BSL UI **23.0.1.v202609041212**, theming UI **2.1.0.v202609041212**,
Java **17.0.18**, Maven **3.9.11**, Windows x64. Проверки используют только
`.work/edt-test` и `.work/workspace-probe`; рабочая EDT не меняется.

## Визуальная проверка в EDT

**WinterSeparators: 76 PASS / 0 FAIL**, exit 0 (включая восстановление
состояния после подготовки стенда). Для сравнения исходная 0.1.9 воспроизведена
тестовым значением panel #011627; затем эмулирован её стандартный активный
профиль без личного panel key, и выполнен обычный Apply новых defaults.
Старый ZIP сохранён. Проверены фактические пиксели **двух вертикальных и одной
горизонтальной полосы**: до #011627, после **#0E293F**. Все non-panel targets,
фон BSL, glyph keyword/number/string и настройка шрифта сохранены.

Просмотрены [до](0.1.10/winter-separators-before.png) и
[после](0.1.10/winter-separators-after.png): полосы различимы между навигатором,
редактором и свойствами. [Журнал](0.1.10/runtime-winterseparators.txt).
Проверка использует публичную модель Eclipse MPartSashContainer и SWT GC;
renderer объединяет вложенные sash-контейнеры, поэтому измеряются видимые
промежутки между фактическими дочерними controls. В test-only perspective
использован действующий Project Explorer вместо удалённого ResourceNavigator;
отдельная новая perspective даёт воспроизводимую раскладку с Properties.
В рабочую установку эти изменения стенда не входят.

**WinterSeparatorsRestart: 122 PASS / 0 FAIL**, exit 0, отдельный процесс.
Проверены сохранённый профиль, новый цвет всех трёх полос, личный keyword Winter
и независимый comment GitHub. Restore вернул каждый original key и сохранил
личные цвета; затем тестовые personal keys всех шести палитр, active profile
и journal очищены. Дополнительный Cleanup не требовался.
[Restart](0.1.10/runtime-winterseparatorsrestart.txt),
[снимок](0.1.10/winter-separators-restart.png).
Итого **198 runtime assertions** в двух успешных целевых запусках;
полные циклы прежних схем не повторялись.

P2 **Install/Reconcile/Uninstall/Audit — exit 0**. Собственные feature,
bundle и fragment удалены; active profile и font journal не остались.
**1267 базовых ID/версий** совпали с рабочей EDT.
Журналы: [Install](0.1.10/separators-p2-install.log),
[Reconcile](0.1.10/runtime-reconcile.log), [Uninstall](0.1.10/separators-p2-uninstall.log),
[Audit](0.1.10/separators-p2-audit.log), [bundles](0.1.10/separators-bundles-audit.log).

## Обновление и ограничения

После установки 0.1.10 откройте страницу тем и нажмите **Применить**:
уже выбранная Winter получит новый стандартный цвет панелей.
Если роль **Фон панелей** была изменена лично, верните только её кнопкой
**Вернуть цвет стиля → Применить**. Остальные личные роли сохраняются.

macOS, другие DPI и все вложенные разделители специальных редакторов
не проверены; прежняя [ручная матрица Winter](winter-is-coming-validation.md)
сохраняется. Исправление не меняет карты исключений форм/MXL/СКД.
