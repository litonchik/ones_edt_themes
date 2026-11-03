# Dracula — 0.1.7

Дата: 14 октября 2026. За подход добавляется одна новая схема — `dracula`.
Darcula сохраняет отдельный ID, ресурс и личные цвета. Далее по очереди —
GitHub Dark Default. Пользователь подтвердил работу предыдущей 0.1.6 («работает»),
без сведений об ОС/конкретных сценариях.
Пользователь 14.10.2026 также подтвердил работу 0.1.7 («работает»).
ОС и конкретные сценарии не сообщены; полная ручная матрица остаётся отдельно.

## Источник и адаптация

[Dracula 2.25.1, исходный YAML](https://github.com/dracula/visual-studio-code/blob/a08a206f2c8420ba3c05f0e8d01d43b2f933fdf8/src/dracula.yml),
commit `a08a206f2c8420ba3c05f0e8d01d43b2f933fdf8`, исследован 14.10.2026.
Базовый вариант, не Soft. Все 17 RGB соответствуют
[каталогу, раздел 5.4](../research/2026-10-14-theme-catalog.md).
Полный MIT notice Dracula Theme и attribution включены в JAR.

Фон #282A36, текст #F8F8F2, keyword #FF79C6, string #F1FA8C,
number/directive #BD93F9. Alpha текущей строки сведена в #353747,
hover поверх панели — в #3B3E4F. Warning #FFB86C перенесён из warning border,
accent #8BE9FD совмещает UI и BSL Builtin function.

Исходный comment/disabled #6272A4 сохранён. Расчётный contrast comment/редактор
3.03:1, disabled/panel 2.51:1; их можно изменить в редакторе палитры.
Обычный выбранный текст имеет contrast 8.59:1. Это числовой анализ, не заявление
полной доступности SWT или проверки styled selection всех токенов.

## Проверки сборки

До добавления схемы два новых теста получили ожидаемый отказ неизвестному ID.
После добавления: **62 unit tests, 0 failures/errors/skipped**.
Полный Verify — **BUILD SUCCESS**. P2 содержит собственные bundle/feature,
**31 classes major 61**, четыре палитры и оба MIT notices.

Цель: EDT 2026.1.3+25 Windows x64, Eclipse 4.30.0, Xtext UI 2.33.0,
BSL UI 23.0.1.v202609041212, theming UI 2.1.0.v202609041212,
Java 17.0.18 (Axiom JDK), Maven 3.9.11. Guards/API/карта settings не менялись.
Из product Java менялся только реестр PaletteCatalog; UI, snapshots,
selective explicit apply и независимый шрифт используют прежнюю реализацию.

ZIP: **95 003 bytes**, bundle/feature **0.1.7.202610141444**.
SHA-256: `c428d96fa46f8698d2d9f3aa05a8beff7752a1f74a68918d81653112a529f0ac`.
Применение проверяется только в `.work/edt-test`/`.work/workspace-probe`;
установленная рабочая EDT не изменяется.

**Dracula: 207 PASS / 0 FAIL, exit 0. DraculaRestart: 120 PASS / 0 FAIL, exit 0.**
Проверены реальные settings и BSL glyph keyword/number/string, preview,
выбор отдельного ID Dracula (пятая строка: штатное оформление + 4 схемы),
читаемость popup, Apply/OK/Cancel, личные drafts, reset роли/палитры,
защита внешнего comment, BSL=false и независимый font preference.
Личные цвета Darcula/Monokai/One Dark Pro сохранены при работе с Dracula.
После второго запуска проверены сохранённый профиль, личный BSL keyword,
отображение выбранной Dracula на странице, restore каждого original key.
Затем тестовые personal keys всех четырёх схем и журнал очищены.

Журналы: [Dracula](0.1.7/runtime-dracula.txt),
[Restart](0.1.7/runtime-dracularestart.txt), [Verify](0.1.7/dracula-verify.log).
Просмотрены снимки [popup](0.1.7/dracula-profile-list.png) и
[реального BSL](0.1.7/dracula-bsl.png): название читается,
keyword розовый, number фиолетовый, string жёлтый; комментарии приглушены.
Результаты относятся к изолированной Windows EDT, не всей ручной матрице.

Регрессия общего probe: **OneDark 206 PASS / OneDarkRestart 119 PASS**, 0 FAIL,
оба exit 0. Включает старую схему, фактические BSL glyph и перезапуск/restore
в каталоге из четырёх схем. Тестовый journal и personal keys очищены.

P2 Install/Reconcile/Uninstall завершились exit 0. Тестовая установка удалена,
базовые bundles сохранены; working EDT не изменялась.
17 прежних файлов палитр/поставок совпали с исходными SHA-256.
Полный MIT notice Dracula совпал с первичным источником побайтно.
P2 Audit — exit 0: собственные feature/bundle/fragment отсутствуют,
активный профиль/font journal не остались; **1267 базовых ID/версий** совпали
с рабочей EDT. [Журнал Audit](0.1.7/dracula-p2-audit.log).

## Остаток ручной матрицы

| Сценарий | Статус |
|---|---|
| Project Explorer/Properties с реальным пользовательским проектом | Не проверено |
| Completion/hover, реальные заголовки error/warning | Не проверено |
| Все styled selection токены BSL, DPI и масштабирование | Не проверено |
| Содержимое форм/MXL/схем/СКД | Исключено; карта targets сохранена |
| Новая Dracula на macOS x64 | Не проверено |

Установка/обновление: [инструкция](../user-guide.md).
Superpowers отключены пользователем для проекта 14.10.2026 и не используются
с момента запроса; необходимые технические проверки выполняются напрямую.
