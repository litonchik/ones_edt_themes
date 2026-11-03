# Проверка EDT 2026 и семантических цветов вызовов

Дата: 30.10.2026. Запрос пользователя: EDT 2026.1.2, 2026.1.3,
2026.2.2, текстовые поверхности конструктора запроса и сравнения/объединения.
2025.2.6 исключена из дальнейшей поддержки по указанию пользователя.
Проверки выполняются на Windows x64, в копиях `.work/edt-*`, с отдельными
workspace/home/appdata. Установленные EDT и пользовательские workspace не меняются.

## Исходная поставка 0.1.15

| EDT | Результат текущего ZIP до изменений |
|---|---|
| 2025.2.6+4 | p2 отклоняет зависимость BSL UI `[23.0.1,23.1.0)`; установлен BSL UI 22.0.0. Дальнейшая адаптация отменена пользователем. |
| 2026.1.2+2 | p2 устанавливает bundle/feature без изменения базовых bundles; `EdtCompatibility` блокирует применение из-за другого qualifier BSL UI. |
| 2026.1.3+25 | Исходная целевая платформа; воспроизведён баг семантических цветов. |
| 2026.2.2+21 | p2 отклоняет BSL UI 24.0.0; прежние guard дополнительно рассчитаны на Eclipse 4.30/Xtext 2.33/Java 17. |

Первичные источники: локальные `bundles.info`, `1cedt.ini`, `plugin.xml`, CSS
и bytecode установленных поставок, прочитанные 30.10.2026. Их копии и логи —
[артефакты исследования](2026-10-30-compatibility/).

## Проверенные версии компонентов

| EDT | Eclipse | Xtext UI | BSL UI | Theming UI | JVM |
|---|---|---|---|---|---|
| 2026.1.2+2 | 4.30.0.v20231201-0110 | 2.33.0.v20231121-0955 | 23.0.1.v202607090722 | 2.1.0.v202607090722 | Axiom 17.0.18 |
| 2026.1.3+25 | 4.30.0.v20231201-0110 | 2.33.0.v20231121-0955 | 23.0.1.v202609041212 | 2.1.0.v202609041212 | Axiom 17.0.18 |
| 2026.2.2+21 | 4.38.0.v20251120-1800 | 2.41.0.v20251124-0739 | 24.0.0.v202610211527 | 2.1.1.v202610211527 | Axiom 25.0.2 |

Dark CSS BSL, QL, QL DCS и theming UI побайтово одинаковы во всех трёх
поставках. [Воспроизводимый inventory](../../scripts/inspect-edt-compatibility.ps1)
и [сравнение](2026-10-30-compatibility/platform-inspection.txt).
Проверка совместимости использует конкретные сочетания компонентов;
неизвестные сборки и EDT 2025 не разрешены. Это не обещание поддержки любых 2026.x.
macOS в этой проверке не запускалась; прежнее подтверждение 0.1.5 не расширяется.

## Воспроизведение бага

[Исходный runtime-отчёт](2026-10-30-compatibility/semantic-before.txt):
в настоящем BSL-редакторе штатный Dark даёт вызову `Сообщить` RGB 255,140,110.
После Apply Darcula цвет остаётся прежним, после изменения текста становится
88,157,246. Проверка падает на различии до/после изменения.

Причина: native `BslHighlightingReconciler` и
`BslSemanticHighlightingCalculator` пропускают неизменённый modification stamp;
сохранённые semantic positions продолжают содержать старые `TextAttribute`.
`invalidateTextPresentation()` обновляет лексическую часть, но старые
семантические атрибуты накладываются поверх неё.

Исправление 0.1.16 использует публичный Xtext service provider и свежий
семантический калькулятор, чтобы пересчитать диапазоны неизменённого документа.
Временный presentation listener накладывает актуальные атрибуты после native
presenter. Перед следующей правкой он удаляется: дальнейшую подсветку снова
ведёт штатный reconciler. Listener также удаляется при следующем применении
и закрытии контрола. Текст, dirty state, modification stamp и undo не используются
для принудительного обновления; vendor JAR и private API не меняются.

BSL использует IDE semantic API Xtext, а QL/DCS — legacy UI semantic API.
Выбор сделан явно по проверенному имени языка. Для QL/DCS дополнительно
подключён `QueryHighlightingHelper` через опубликованный
`com._1c.g5.v8.dt.ql.ui.qlUiModuleExtension`: при изменении token styles
он вызывает публичный `HighlightingReconciler.refresh()` также у embedded
viewer. QL DCS использует ту же точку расширения (проверено по созданию
injector в установленном `QlDcsUiPlugin`). Содержимое `CommonExpression`
сохраняет штатную семантическую классификацию EDT; свои категории токенов
плагин не назначает.

Отдельно обнаружено штатное ограничение QL: реальный lexer выдаёт `NUMBER`,
тогда как установленный `QlTokenToAttributeMapper` проверяет `RULE_INT`.
В проверенном запросе (также в `ПЕРВЫЕ 123`) число поэтому использует
`Ql_Others`/роль `text`. Это подтверждено grammar node и фактическим style range,
а не только наличием preference key `Ql_Numbers`. Вендорный mapper не заменён.
Строка после `ПОДОБНО` использует штатную строковую категорию. Runtime проверяет
именно наблюдаемые native категории: background, keyword, NUMBER/text и string.

## Проверка 0.1.16

Сборка Maven 3.9.11 / Tycho 4.0.13 под Axiom Java 17.0.18 — **exit 0**.
**79 unit tests**, 0 failures/errors/skipped. p2 ZIP содержит только собственные
bundle/feature, 36 Java classes major 61; test fragment и vendor bundles не включены.
[Лог сборки](2026-10-30-compatibility/build-0.1.16.log).

Итоговый bundle — `0.1.16.202610292155`. Во всех трёх копиях SHA-256 bundle
совпал с bundle внутри итогового ZIP:
[побайтовая проверка поставки](2026-10-30-compatibility/shipping-bundle-verification.txt).

| EDT / Windows x64 | Semantic | Surfaces | Ошибки |
|---|---|---|---|
| 2026.1.2+2 / Java 17 | [56 PASS](2026-10-30-compatibility/2026.1.2/semantic.txt) | [312 PASS](2026-10-30-compatibility/2026.1.2/surfaces.txt) | 0 |
| 2026.1.3+25 / Java 17 | [56 PASS](2026-10-30-compatibility/2026.1.3/semantic.txt) | [312 PASS](2026-10-30-compatibility/2026.1.3/surfaces.txt) | 0 |
| 2026.2.2+21 / Java 25 | [56 PASS](2026-10-30-compatibility/2026.2.2/semantic.txt) | [312 PASS](2026-10-30-compatibility/2026.2.2/surfaces.txt) | 0 |

Всего **1104 runtime PASS**, все итоговые процессы Semantic/Surfaces — exit 0.
Проверено следующее:

- В настоящем BSL-редакторе все 11 палитр обновляют встроенные, локальные и
  вложенные вызовы сразу; последующая правка не меняет уже применённый RGB.
  Apply не меняет текст, dirty state и document modification stamp.
- Explicit restore возвращает штатный цвет до и после правки.
  Переход через native theme engine освобождает профиль; цвет после правки
  совпадает с новым открытым редактором. Два конфликтующих theme keys
  сохраняются намеренно, чтобы не отменить выбор native темы; финальная
  тестовая очистка удаляет остаток журнала.
- Страница плагина реально создаёт выбор профиля, шрифта и редактор палитры
  во всех трёх EDT; страница открывается и закрывается без применения.
- Обычный Text Editor получает фон и текст палитры. Два самостоятельных
  редактора QL/QL DCS и два настоящих embedded `QlEditor` из конструктора
  проверены во всех 11 палитрах: фон, keyword, native NUMBER/text, string.
- Через зарегистрированную EDT factory создан штатный BSL three-side merge
  viewer; три панели проверены по фону и keyword Visual Studio Dark (C/C++).
  Различия и текст просмотрены на снимках; содержимое synthetic input не
  сохранялось в пользовательские проекты.
- После Surfaces проверены **115 исходных значений/состояний наличия** настроек
  в каждой workspace, включая новые QL/DCS keys. Все совпали с baseline.

Просмотрены шесть финальных снимков:
[QL/DCS 2026.1.2](2026-10-30-compatibility/2026.1.2/query-constructor.png),
[merge 2026.1.2](2026-10-30-compatibility/2026.1.2/edt-merge.png),
[QL/DCS 2026.1.3](2026-10-30-compatibility/2026.1.3/query-constructor.png),
[merge 2026.1.3](2026-10-30-compatibility/2026.1.3/edt-merge.png),
[QL/DCS 2026.2.2](2026-10-30-compatibility/2026.2.2/query-constructor.png),
[merge 2026.2.2](2026-10-30-compatibility/2026.2.2/edt-merge.png).
Красные annotations QL относятся к synthetic ссылке `Справочник.Тест` без
metadata-конфигурации; это не ошибочное применение цветов темы.

## Границы проверки

Проверены реальные текстовые контролы конструктора, **не полный мастер с
рабочей metadata-конфигурацией**. Выбор таблиц/полей, все страницы мастера,
объединение реальных metadata-объектов, completion/hover, все варианты
selection/DPI и macOS этого выпуска остаются ручной матрицей. Для merge
визуально проверена Visual Studio Dark, для QL/DCS — RGB всех 11 палитр.
Формы/MXL/графические схемы по-прежнему исключены из изменения содержимого.

В тестовых логах сохраняется известная до 0.1.16 CSS-диагностика
`TabbedPropertyTitle.setColor` с null color. В изолированном окружении также
есть сообщения отсутствующего `wmic`/настройки путей платформы 1С.
Эти сообщения не объявляются исправленными и не заменяют результаты assertions.

## Очистка и поставка

Финальные **Cleanup / Reconcile / p2 Uninstall / Audit — exit 0** во всех трёх
копиях. Active profile и активные color/font journals очищены; собственные
bundle/fragment больше не зарегистрированы. Базовые ID/версии совпали и с
установленными EDT, и с inventory до начала работы:

- [2026.1.2: аудит](2026-10-30-compatibility/2026.1.2/bundles-audit.txt),
  [удаление](2026-10-30-compatibility/2026.1.2/final-uninstall.log).
- [2026.1.3: аудит](2026-10-30-compatibility/2026.1.3/bundles-audit.txt),
  [удаление](2026-10-30-compatibility/2026.1.3/final-uninstall.log).
- [2026.2.2: аудит](2026-10-30-compatibility/2026.2.2/bundles-audit.txt),
  [удаление](2026-10-30-compatibility/2026.2.2/final-uninstall.log).

Рабочие установки EDT и пользовательские workspace не менялись.

Архив: `dist/edt-themes-0.1.16.zip`.
SHA-256: `36531d6f4bd329594153a1ded8f321e583a49e4975d033f3085ca26f89db489f`.

## Воспроизведение

Воспроизведение в подготовленной тестовой копии:

```powershell
./scripts/p2-test.ps1 -Action Install -EdtCopy .work/edt-2026.1.2
./scripts/build-workbench-probe.ps1 -EdtCopy .work/edt-2026.1.2
./scripts/run-workbench-probe.ps1 -Phase Semantic -EdtCopy .work/edt-2026.1.2 -WorkRoot .work/compat-2026.1.2
./scripts/run-workbench-probe.ps1 -Phase Surfaces -EdtCopy .work/edt-2026.1.2 -WorkRoot .work/compat-2026.1.2
```

Для 2026.2.2 во всех командах указывается `-JavaHome 'C:/Program Files/Axiom/AxiomJDK-Pro-25-Full'`.
При прерывании — отдельная фаза `Cleanup`, затем `Reconcile`, p2 Uninstall/Audit.
