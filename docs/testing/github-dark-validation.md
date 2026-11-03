# GitHub Dark Default — 0.1.8

Дата: 14 октября 2026. Добавляется одна новая схема `github-dark-default`.
Следующая по очереди — Winter is Coming (Dark Blue). Пользователь подтвердил
работу предыдущей Dracula 0.1.7; ОС и отдельные сценарии не сообщены.
Superpowers не используются согласно AGENTS.md.
18.10.2026 пользователь подтвердил работу 0.1.8 («проверил, работает»).
ОС и отдельные сценарии не сообщены; ручная матрица ниже остаётся открытой.

## Источник и адаптация

GitHub Theme **6.3.5**, commit `cd78e5e4e7bcf132a6f428ae0f32264bb1b729cf`:
[theme.js](https://github.com/primer/github-vscode-theme/blob/cd78e5e4e7bcf132a6f428ae0f32264bb1b729cf/src/theme.js),
[colors.js](https://github.com/primer/github-vscode-theme/blob/cd78e5e4e7bcf132a6f428ae0f32264bb1b729cf/src/colors.js),
зависимость [Primer 7.10.0](https://unpkg.com/@primer/primitives@7.10.0/dist/json/colors/dark.json).
Исследование 14.10.2026; перенесены фиксированные данные без исполнения upstream
генератора и установки npm-пакетов. В JAR включены отдельные полные MIT notices
GitHub Theme (Copyright 2020 Primer) и Primer primitives (Copyright 2018 GitHub Inc.).

17 ролей совпадают с [анализом, раздел 5.3](../research/2026-10-14-theme-catalog.md).
Учтены source overrides: text #E6EDF3, disabled #7D8590, accent #2F81F7.
Editor #0D1117, panel #161B22, keyword/directive #FF7B72,
string #A5D6FF, number #79C0FF, comment #8B949E.
Selection сведён в #142744, line — #171B22, hover — #1F242C.
Расчётные contrast text/editor 16.02:1, selection 12.66:1, comment/editor 6.15:1.
Акцент UI и встроенные функции BSL по-прежнему объединены; отдельные semantic
scopes, иконки и CSS исходной темы не переносились.

## Проверки

**64 unit tests, 0 failures/errors/skipped. Verify — BUILD SUCCESS.**
Проверены реальные native target maps для новых цветов и независимый reset
личной GitHub-палитры от Dracula/One Dark Pro. Остальные unit-сценарии сохранены.
P2: собственные bundle/feature, **31 classes major 61**, пять палитр и четыре
полных MIT notices. Product Java изменён только записью реестра PaletteCatalog;
UI, settings map, snapshots, rollback и шрифт не менялись.

Target: EDT **2026.1.3+25**, Eclipse **4.30.0**, Xtext UI **2.33.0**,
BSL UI **23.0.1.v202609041212**, theming UI **2.1.0.v202609041212**,
Java **17.0.18**, Maven **3.9.11**, Windows x64. Рабочая EDT не изменяется;
runtime используется только из `.work/edt-test` с `.work/workspace-probe`.

Bundle/feature **0.1.8.202610141519**; ZIP **97 043 bytes**. SHA-256:
`7a384bc94d5a1346454428dc3e14c3ff1985b362453d1d386601c4dc9eb6c4cb`.
**GitHub: 208 PASS / GitHubRestart: 122 PASS**, 0 FAIL, оба exit 0.
Фактические settings и BSL glyph keyword/number/string, preview, отдельный ID
и шестая строка списка (штатное оформление + 5 схем), читаемость popup,
Apply/OK/Cancel, reset роли/палитры, защита ручного comment, BSL=false,
сохранность font preference и личных цветов четырёх прежних схем.
Второй процесс подтвердил saved ID, personal keyword и выбор GitHub на странице;
restore проверен для каждого original key. Тестовые personal keys/journal очищены.

Просмотрены [popup](0.1.8/github-dark-default-profile-list.png) и
[реальный BSL](0.1.8/github-dark-default-bsl.png): текст/цвета читаются,
keyword коралловый, number/string голубые, комментарии серые.
Журналы: [GitHub](0.1.8/runtime-github.txt),
[Restart](0.1.8/runtime-githubrestart.txt), [Verify](0.1.8/github-verify.log).

Дополнительно выполнена регрессия Dracula: **207 PASS**, 0 FAIL, exit 0.
После вопроса пользователя полный перезапуск старой схемы признан избыточным:
общий product-код применения не менялся, независимость её personal keys уже
проверена GitHub-сценарием. DraculaRestart не запускался. Для возврата среды
добавлена test-only фаза Cleanup, без UI/glyph-проверок старой палитры.
Cleanup завершилась exit 0: **104 PASS / 0 FAIL**, восстановлены все original
values, очищены тестовые personal keys пяти схем и активный профиль.
Журнал: [Cleanup](0.1.8/runtime-cleanup.txt).
P2 Install/Reconcile/Uninstall завершились exit 0; собственные тестовые bundles
удалены. **1267 базовых ID/версий** совпали с рабочей EDT. Прежние четыре
палитры и ZIP/hash-файлы сохранены (20 SHA-256); два новых license notices
побайтно совпали с первичными источниками.
P2 Audit завершился exit 0: feature/bundle/fragment отсутствуют,
активный профиль и font journal не остались. [Audit](0.1.8/github-p2-audit.log).

## Оставшаяся ручная матрица

| Сценарий | Статус |
|---|---|
| Project Explorer/Properties, completion/hover с рабочим проектом | Не проверено |
| Реальные заголовки error/warning, все нативные контролы | Не проверено |
| Styled selection всех токенов BSL, DPI и масштабирование | Не проверено |
| Содержимое форм/MXL/схем/СКД | Исключено; карта targets сохранена |
| Новая палитра на macOS x64 | Не проверено |

Установка: [инструкция](../user-guide.md).
