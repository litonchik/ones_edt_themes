# Winter is Coming (Light): адаптация 17 ролей

Дата: **04.11.2026**. Следующая после GitHub Light Default палитра из каталога.
Добавляется только одна новая схема, ID `winter-is-coming-light`, группа LIGHT.
Светлый вариант Dracula по-прежнему исключён. Общая логика 0.1.17 не меняется.

Источник — Winter is Coming **1.5.0**, commit
`260547834cb6ac37dd5b8bb5842cc1c8d3164946`, MIT, JohnPapa.net LLC.
Прочитаны официальные [manifest](https://github.com/johnpapa/vscode-winteriscoming/blob/260547834cb6ac37dd5b8bb5842cc1c8d3164946/package.json),
[Light JSON](https://github.com/johnpapa/vscode-winteriscoming/blob/260547834cb6ac37dd5b8bb5842cc1c8d3164946/themes/WinterIsComing-light-color-theme.json)
и [лицензия](https://github.com/johnpapa/vscode-winteriscoming/blob/260547834cb6ac37dd5b8bb5842cc1c8d3164946/LICENSE.md).
SHA-256 прочитанного JSON: `07cf9830fa9af9c9a0bce8f682d2ef13f8c624d3aa726d653d8baf412dd73839`.
Существующая лицензия Winter сохранена, атрибуция расширена на Light.
No Italics отдельно не добавляется: плагин сохраняет native BSL font styles.

| Роль | RGB | Сопоставление |
|---|---|---|
| editor.background | #FFFFFF | editor.background |
| text | #236EBF | editor.foreground; характерный синий текст |
| panel | #F3F3F3 | sideBar.background |
| border | #2F86D2 | editorWidget.border / focusBorder |
| selection.background | #CEE1F0 | editor.selectionBackground |
| selection.text | #1857A4 | UI foreground вместо editor.foreground для контраста |
| line | #EFF2EF | editor.lineHighlightBackground |
| comment | #357B42 | comment |
| keyword | #08738F | исходный #0991B6 затемнён для читаемости |
| string | #A44185 | string / string.quoted |
| number | #174781 | constant.numeric |
| directive | #08738F | общая роль keyword для BSL/QL, как у Dark Blue |
| accent | #236EBF | textLink.foreground, общая UI/link/builtin роль |
| error | #C33735 | адаптация исходного #EF5350 для текста UI |
| warning | #8A6500 | тёмный янтарный на основе terminal.ansiYellow #C5A332 |
| disabled | #236EBF | editor.foreground вместо слишком светлого editorLineNumber #2F86D2 |
| hover | #EFF2EF | непрозрачный line highlight для читаемости общей роли |

Прозрачный upstream hover #36B2E7A4 слишком насыщен для общей UI-роли с синим
текстом; используется исходный непрозрачный цвет текущей строки. Это адаптация,
а не отсечение alpha. EditorGroup border также не обрезается: берётся отдельный
исходный непрозрачный editorWidget.border.

Контраст WCAG relative luminance (без заявления о полной доступности):
основной текст — 5.18:1 на белом / 4.59:1 на текущей строке;
комментарий — 5.17 / 4.58; ключевые слова после адаптации — 5.44 / 4.82
(исходный #0991B6 давал 3.66 / 3.25); выделение — 5.33:1;
warning на panel — 4.80:1. Автотест проверяет минимум 4.5:1 для основных
текстовых ролей на editor/line/hover и для выбранного текста/error/warning.

Результат запуска отделён от сопоставления цветов:
[runtime-проверка 0.1.18](../testing/winter-light-validation.md).
