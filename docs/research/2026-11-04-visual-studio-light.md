# Visual Studio Light (C/C++): адаптация 17 ролей

Дата: **04.11.2026**. Добавляется только `visual-studio-light-cpp`,
label **Visual Studio Light (C/C++)**, группа LIGHT. Это основной вариант
Light из C/C++ Themes **2.1.0**, файл `cpptools_light_vs_new.json`, не 2017 Light.
Прежние 16 схем и общий механизм 0.1.23 сохраняются.

## Первичные источники

Microsoft `vscode-cpptools`, commit `3c14e3155a42976f765828b5ca2bbd9154a9aa54`:
[manifest](https://github.com/microsoft/vscode-cpptools/blob/3c14e3155a42976f765828b5ca2bbd9154a9aa54/Themes/package.json),
[палитра](https://github.com/microsoft/vscode-cpptools/blob/3c14e3155a42976f765828b5ca2bbd9154a9aa54/Themes/themes/cpptools_light_vs_new.json),
[MIT](https://github.com/microsoft/vscode-cpptools/blob/3c14e3155a42976f765828b5ca2bbd9154a9aa54/Themes/LICENSE.txt).
Файл MIT побайтово совпал с уже поставляемым notice Microsoft для Dark;
отдельный дубликат лицензии не добавляется, атрибуция расширяется на Light.

Отсутствующие UI-значения взяты из закреплённых Light-defaults Microsoft VS Code,
commit `f725241620f540a7f750649713e710561fbabac8`:
[editorColors.ts](https://github.com/microsoft/vscode/blob/f725241620f540a7f750649713e710561fbabac8/src/vs/platform/theme/common/colors/editorColors.ts),
[theme.ts](https://github.com/microsoft/vscode/blob/f725241620f540a7f750649713e710561fbabac8/src/vs/workbench/common/theme.ts).
Исходники прочитаны 04.11.2026; исполняемый код, CSS, иконки и шрифты не включаются.
[SHA-256 источников, привязки и расчёты](visual-studio-light-source-mapping.json).

## Роли

| Роль | RGB | Основание |
|---|---|---|
| editor.background | #FFFFFF | C/C++ editor.background |
| text | #000000 | C/C++ editor.foreground |
| panel | #F3F3F3 | VS Code Light sideBar.background; also C/C++ suggest widget |
| border | #E7E7E7 | VS Code Light editorGroup.border |
| selection.background | #ADD6FF | VS Code Light editor.selectionBackground |
| selection.text | #000000 | editor.foreground; native selection foreground |
| line | #F3F3F3 | adapted filled current line from light UI surface; upstream uses border |
| comment | #007A00 | comment #008000 darkened |
| keyword | #0000FF | generic keyword/storage; separate C++ control flow not modeled |
| string | #A31515 | string / semantic stringLiteral |
| number | #08784E | constant.numeric / semantic numberLiteral #098658 darkened |
| directive | #8A1BFF | entity.name.function.preprocessor (macro name) |
| accent | #006CB4 | activityBarBadge.background #007ACC darkened |
| error | #C42F2F | invalid #CD3131 darkened |
| warning | #896102 | VS Code editorWarning.foreground #BF8803 darkened |
| disabled | #227189 | editorLineNumber.foreground #2B91AF darkened |
| hover | #E8E8E8 | list.hoverBackground |

В BSL одна общая категория keyword: выбран базовый синий, как в тёмной паре.
Специальные C++ control-flow #8F08C4, типы, имена функций и кавычки отдельно
не переносятся. Directive использует цвет имени макроса; обычные токены
препроцессора C++ #808080 не подменяют эту роль. Стили шрифта остаются штатными.

Фон текущей строки — адаптация #F3F3F3, поскольку исходный Light использует
тонкую рамку, а общий редактор 17 ролей EDT задаёт заливку. Цвета comment,
number, accent, error, warning и disabled затемнены равномерным масштабированием
RGB, чтобы сохранить оттенок и контраст **не ниже 4.5:1** на белом, текущей
строке и hover. Это RGB-расчёт, не заявление о полной доступности EDT.
Синий оттенок номеров строк сохранён через общую роль disabled.

Проверка реальных цветов, личных ключей, startup и restore вынесена в
[отчёт 0.1.24](../testing/visual-studio-light-validation.md).
