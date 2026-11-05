# Noctis Lux: адаптация 17 ролей

Дата: **04.11.2026**. Добавляется только одна схема: `noctis-lux`, группа LIGHT,
label **Noctis Lux**. Это первый светлый вариант семейства Noctis по manifest,
отдельный профиль; светлого «Minimus» в источнике нет. Hibernus, Lilac и светлая
Dracula в этот этап не входят. Общий механизм 0.1.20 не меняется.

Источник — Noctis **10.43.3**, commit `4a82370b2c064e36726c23e94f27ed3782bd6ecd`.
Прочитаны официальные [manifest](https://github.com/liviuschera/noctis/blob/4a82370b2c064e36726c23e94f27ed3782bd6ecd/package.json),
[Lux JSON](https://github.com/liviuschera/noctis/blob/4a82370b2c064e36726c23e94f27ed3782bd6ecd/themes/lux.json)
и [MIT](https://github.com/liviuschera/noctis/blob/4a82370b2c064e36726c23e94f27ed3782bd6ecd/LICENSE.md), Liviu Schera, 2018.
Файл лицензии определён через GitHub contents API; MIT побайтово совпадает
с уже включённым `noctis-LICENSE.txt`. SHA-256 JSON: `adfc405f421f039cf908b7e8402ee300e303698ba7475bd3206803bec0f95534`.
Прежняя Noctis Minimus и остальные 14 схем сохраняются побайтово.

| Роль | RGB | Основание |
|---|---|---|
| editor.background | #FEF8EC | editor.background, кремовый фон |
| text | #005661 | editor.foreground, тёмно-бирюзовый |
| panel | #F9F1E1 | sideBar.background |
| border | #F0E7D1 | editorGroup.border, непрозрачный |
| selection.background | #B6E1E7 | list.activeSelectionBackground, различимое выделение |
| selection.text | #005661 | editor.foreground |
| line | #DAEEEE | editor.lineHighlightBackground #D1EBEFCC поверх editor |
| comment | #5B6B6B | COMMENT #8CA6A6, затемнён |
| keyword | #B33D67 | KEYWORD #FF5792, затемнён |
| string | #007846 | STRING #00B368, затемнён |
| number | #5842FF | NUMBER #5842FF без изменения |
| directive | #006CB0 | MISC #0094F0, затемнён; адаптация BSL, как у Minimus |
| accent | #007383 | textLink.foreground #00C6E0, затемнён |
| error | #CC3300 | errorForeground #FF4000, затемнён |
| warning | #966121 | editorWarning.foreground #E69533, затемнён |
| disabled | #5B6B6B | адаптированный COMMENT вместо светлого line numbers |
| hover | #F2EDDE | editorHoverWidget.background, непрозрачный |

Alpha-композиция: sRGB-каналы, 8-bit alpha с округлением до ближайшего целого;
прозрачность не отсекается. Подложка line — editor.background.
Исходный editor.selectionBackground #ADE2EB77 после сведения поверх editor даёт
#D8EEEC, почти равный line #DAEEEE (1.005:1). Для общей роли выделения выбран
исходный непрозрачный list.activeSelectionBackground #B6E1E7: 1.170:1 относительно
текущей строки. Это проектная адаптация для различимости однострочного выделения,
а не заявление о нормативном контрасте интерфейса. Установленный Eclipse
CursorLinePainter сохраняет фон текущей строки при однострочном выделении;
при многострочном деактивирует его (org.eclipse.jface.text 3.24.200 source).
Hover использует отдельный непрозрачный фон виджета, а не highlight #0099AD3F.
Для общей text-роли используется editor.foreground #005661; более узкий TEXT
scope #004D57 не подменяет основной цвет. Directive=MISC — проектная адаптация
к BSL по прежней схеме Minimus, а не утверждение об upstream BSL-scopes.

Текстовые роли затемнены масштабированием трёх sRGB-каналов общим коэффициентом
до контраста не ниже 4.6:1 на editor/line/hover; error/warning — на panel.
Соотношение каналов сохраняется приблизительно после округления, точное
perceptual hue не заявляется. Основной текст и NUMBER оставлены исходными.
Native BSL font styles сохраняются; ITALIC/BOLD и отдельные языковые scopes
исходной темы не переносятся.

Контраст WCAG relative luminance, без заявления о полной доступности:

| Роль | Источник | Источник на line | Итог | Итог на line (error/warning: panel) |
|---|---|---:|---|---:|
| text | #005661 | 6.97 | #005661 | 6.97 |
| comment | #8CA6A6 | 2.15 | #5B6B6B | 4.64 |
| keyword | #FF5792 | 2.48 | #B33D67 | 4.62 |
| string | #00B368 | 2.28 | #007846 | 4.62 |
| number | #5842FF | 4.80 | #5842FF | 4.80 |
| directive | #0094F0 | 2.69 | #006CB0 | 4.63 |
| accent | #00C6E0 | 1.72 | #007383 | 4.62 |
| error | #FF4000 | 2.91 | #CC3300 | 4.63 |
| warning | #E69533 | 2.00 | #966121 | 4.64 |

Selection text #005661 на #B6E1E7 — **5.96:1**.
Автотест проверяет минимум 4.5:1 для основных ролей на editor/line/hover,
для выбранного текста на selection и error/warning на panel.
Результаты реального запуска — [в отчёте 0.1.21](../testing/noctis-lux-validation.md).
